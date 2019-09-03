/*
 * This file is part of Bisq.
 *
 * Bisq is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bisq is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bisq. If not, see <http://www.gnu.org/licenses/>.
 */

package bisq.core.support;

import bisq.core.btc.setup.WalletsSetup;
import bisq.core.support.messages.ChatMessage;
import bisq.core.support.messages.SupportMessage;

import bisq.network.p2p.AckMessage;
import bisq.network.p2p.AckMessageSourceType;
import bisq.network.p2p.DecryptedMessageWithPubKey;
import bisq.network.p2p.NodeAddress;
import bisq.network.p2p.P2PService;
import bisq.network.p2p.SendMailboxMessageListener;

import bisq.common.Timer;
import bisq.common.UserThread;
import bisq.common.crypto.PubKeyRing;
import bisq.common.proto.network.NetworkEnvelope;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;

@Slf4j
public abstract class SupportManager {
    @Getter
    protected final P2PService p2PService;
    protected final WalletsSetup walletsSetup;
    @Setter
    @Getter
    protected SupportSession supportSession;
    protected final Map<String, Timer> delayMsgMap = new HashMap<>();

    protected final CopyOnWriteArraySet<DecryptedMessageWithPubKey> decryptedMailboxMessageWithPubKeys = new CopyOnWriteArraySet<>();
    protected final CopyOnWriteArraySet<DecryptedMessageWithPubKey> decryptedDirectMessageWithPubKeys = new CopyOnWriteArraySet<>();
    protected boolean allServicesInitialized;


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Constructor
    ///////////////////////////////////////////////////////////////////////////////////////////

    public SupportManager(P2PService p2PService, WalletsSetup walletsSetup) {
        this.p2PService = p2PService;
        this.walletsSetup = walletsSetup;

        // We get first the message handler called then the onBootstrapped
        p2PService.addDecryptedDirectMessageListener((decryptedMessageWithPubKey, senderAddress) -> {
            decryptedDirectMessageWithPubKeys.add(decryptedMessageWithPubKey);
            tryApplyMessages();
        });
        p2PService.addDecryptedMailboxListener((decryptedMessageWithPubKey, senderAddress) -> {
            decryptedMailboxMessageWithPubKeys.add(decryptedMessageWithPubKey);
            tryApplyMessages();
        });
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Abstract methods
    ///////////////////////////////////////////////////////////////////////////////////////////

    protected abstract void dispatchMessage(SupportMessage networkEnvelope);

    public abstract void persist();

    public abstract NodeAddress getPeerNodeAddress(ChatMessage message, SupportSession supportSession);

    public abstract SupportType getSupportType();


    ///////////////////////////////////////////////////////////////////////////////////////////
    // API
    ///////////////////////////////////////////////////////////////////////////////////////////

    public void onAllServicesInitialized() {
        allServicesInitialized = true;
    }

    protected boolean isReady() {
        return allServicesInitialized &&
                p2PService.isBootstrapped() &&
                walletsSetup.isDownloadComplete() &&
                walletsSetup.hasSufficientPeersForBroadcast();
    }

    public void tryApplyMessages() {
        if (isReady())
            applyMessages();
    }

    protected void applyMessages() {
        decryptedDirectMessageWithPubKeys.forEach(decryptedMessageWithPubKey -> {
            NetworkEnvelope networkEnvelope = decryptedMessageWithPubKey.getNetworkEnvelope();
            if (networkEnvelope instanceof SupportMessage) {
                dispatchMessage((SupportMessage) networkEnvelope);
            } else if (networkEnvelope instanceof AckMessage) {
                processAckMessage((AckMessage) networkEnvelope, null);
            }
        });
        decryptedDirectMessageWithPubKeys.clear();

        decryptedMailboxMessageWithPubKeys.forEach(decryptedMessageWithPubKey -> {
            NetworkEnvelope networkEnvelope = decryptedMessageWithPubKey.getNetworkEnvelope();
            log.debug("decryptedMessageWithPubKey.message " + networkEnvelope);
            if (networkEnvelope instanceof SupportMessage) {
                dispatchMessage((SupportMessage) networkEnvelope);
                p2PService.removeEntryFromMailbox(decryptedMessageWithPubKey);
            } else if (networkEnvelope instanceof AckMessage) {
                processAckMessage((AckMessage) networkEnvelope, decryptedMessageWithPubKey);
            }
        });
        decryptedMailboxMessageWithPubKeys.clear();
    }

    protected boolean canProcessMessage(SupportMessage message) {
        return message.getSupportType() == getSupportType();
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Message handler
    ///////////////////////////////////////////////////////////////////////////////////////////

    protected void onChatMessage(ChatMessage chatMessage) {
        final String tradeId = chatMessage.getTradeId();
        final String uid = chatMessage.getUid();
        boolean channelOpen = supportSession.channelOpen(chatMessage);
        if (!channelOpen) {
            log.debug("We got a chatMessage but we don't have a matching chat. TradeId = " + tradeId);
            if (!delayMsgMap.containsKey(uid)) {
                Timer timer = UserThread.runAfter(() -> onChatMessage(chatMessage), 1);
                delayMsgMap.put(uid, timer);
            } else {
                String msg = "We got a chatMessage after we already repeated to apply the message after a delay. That should never happen. TradeId = " + tradeId;
                log.warn(msg);
            }
            return;
        }

        cleanupRetryMap(uid);
        PubKeyRing receiverPubKeyRing = supportSession.getPeerPubKeyRing(chatMessage);

        supportSession.storeChatMessage(chatMessage);

        // We never get a errorMessage in that method (only if we cannot resolve the receiverPubKeyRing but then we
        // cannot send it anyway)
        if (receiverPubKeyRing != null)
            sendAckMessage(chatMessage, receiverPubKeyRing, true, null);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Ack
    ///////////////////////////////////////////////////////////////////////////////////////////

    private void processAckMessage(AckMessage ackMessage,
                                   @Nullable DecryptedMessageWithPubKey decryptedMessageWithPubKey) {
        if (ackMessage.getSourceType() == AckMessageSourceType.DISPUTE_MESSAGE) {
            if (ackMessage.isSuccess()) {
                log.info("Received AckMessage for {} with tradeId {} and uid {}",
                        ackMessage.getSourceMsgClassName(), ackMessage.getSourceId(), ackMessage.getSourceUid());
            } else {
                log.warn("Received AckMessage with error state for {} with tradeId {} and errorMessage={}",
                        ackMessage.getSourceMsgClassName(), ackMessage.getSourceId(), ackMessage.getErrorMessage());
            }

            supportSession.getChatMessages()
                    .forEach(msg -> {
                        if (ackMessage.isSuccess())
                            msg.setAcknowledged(true);
                        else
                            msg.setAckError(ackMessage.getErrorMessage());
                    });
            persist();

            if (decryptedMessageWithPubKey != null)
                p2PService.removeEntryFromMailbox(decryptedMessageWithPubKey);
        }
    }

    protected void sendAckMessage(SupportMessage supportMessage, PubKeyRing peersPubKeyRing,
                                  boolean result, @Nullable String errorMessage) {
        String tradeId = supportMessage.getTradeId();
        String uid = supportMessage.getUid();
        AckMessage ackMessage = new AckMessage(p2PService.getNetworkNode().getNodeAddress(),
                AckMessageSourceType.DISPUTE_MESSAGE,
                supportMessage.getClass().getSimpleName(),
                uid,
                tradeId,
                result,
                errorMessage);
        final NodeAddress peersNodeAddress = supportMessage.getSenderNodeAddress();
        log.info("Send AckMessage for {} to peer {}. tradeId={}, uid={}",
                ackMessage.getSourceMsgClassName(), peersNodeAddress, tradeId, uid);
        p2PService.sendEncryptedMailboxMessage(
                peersNodeAddress,
                peersPubKeyRing,
                ackMessage,
                new SendMailboxMessageListener() {
                    @Override
                    public void onArrived() {
                        log.info("AckMessage for {} arrived at peer {}. tradeId={}, uid={}",
                                ackMessage.getSourceMsgClassName(), peersNodeAddress, tradeId, uid);
                    }

                    @Override
                    public void onStoredInMailbox() {
                        log.info("AckMessage for {} stored in mailbox for peer {}. tradeId={}, uid={}",
                                ackMessage.getSourceMsgClassName(), peersNodeAddress, tradeId, uid);
                    }

                    @Override
                    public void onFault(String errorMessage) {
                        log.error("AckMessage for {} failed. Peer {}. tradeId={}, uid={}, errorMessage={}",
                                ackMessage.getSourceMsgClassName(), peersNodeAddress, tradeId, uid, errorMessage);
                    }
                }
        );
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Util
    ///////////////////////////////////////////////////////////////////////////////////////////

    protected void cleanupRetryMap(String uid) {
        if (delayMsgMap.containsKey(uid)) {
            Timer timer = delayMsgMap.remove(uid);
            if (timer != null)
                timer.stop();
        }
    }
}
