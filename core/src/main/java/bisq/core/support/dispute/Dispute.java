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

package bisq.core.support.dispute;

import bisq.core.proto.CoreProtoResolver;
import bisq.core.support.SupportType;
import bisq.core.support.messages.ChatMessage;
import bisq.core.trade.Contract;

import bisq.network.p2p.NodeAddress;

import bisq.common.crypto.PubKeyRing;
import bisq.common.proto.ProtoUtil;
import bisq.common.proto.network.NetworkPayload;
import bisq.common.storage.Storage;
import bisq.common.util.Utilities;

import com.google.protobuf.ByteString;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;

@Slf4j
@EqualsAndHashCode
@Getter
public final class Dispute implements NetworkPayload {
    private final String tradeId;
    private final String id;
    private final int traderId;
    private final boolean disputeOpenerIsBuyer;
    private final boolean disputeOpenerIsMaker;
    // PubKeyRing of trader who opened the dispute
    private final PubKeyRing traderPubKeyRing;
    private final long tradeDate;
    private final Contract contract;
    @Nullable
    private final byte[] contractHash;
    @Nullable
    private final byte[] depositTxSerialized;
    @Nullable
    private final byte[] payoutTxSerialized;
    @Nullable
    private final String depositTxId;
    @Nullable
    private final String payoutTxId;
    private final String contractAsJson;
    @Nullable
    private final String makerContractSignature;
    @Nullable
    private final String takerContractSignature;
    private final PubKeyRing conflictResolverPubKeyRing;
    private final boolean isSupportTicket;
    private final ObservableList<ChatMessage> chatMessages = FXCollections.observableArrayList();
    private BooleanProperty isClosedProperty = new SimpleBooleanProperty();
    // disputeResultProperty.get is Nullable!
    private ObjectProperty<DisputeResult> disputeResultProperty = new SimpleObjectProperty<>();
    @Nullable
    private String disputePayoutTxId;

    private long openingDate;

    // Added in v1.1.6. Is false if received from old clients.
    private boolean isMediationDispute;

    transient private Storage<? extends DisputeList> storage;


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Constructor
    ///////////////////////////////////////////////////////////////////////////////////////////

    public Dispute(Storage<? extends DisputeList> storage,
                   String tradeId,
                   int traderId,
                   boolean disputeOpenerIsBuyer,
                   boolean disputeOpenerIsMaker,
                   PubKeyRing traderPubKeyRing,
                   long tradeDate,
                   Contract contract,
                   @Nullable byte[] contractHash,
                   @Nullable byte[] depositTxSerialized,
                   @Nullable byte[] payoutTxSerialized,
                   @Nullable String depositTxId,
                   @Nullable String payoutTxId,
                   String contractAsJson,
                   @Nullable String makerContractSignature,
                   @Nullable String takerContractSignature,
                   PubKeyRing conflictResolverPubKeyRing,
                   boolean isSupportTicket,
                   boolean isMediationDispute) {
        this(tradeId,
                traderId,
                disputeOpenerIsBuyer,
                disputeOpenerIsMaker,
                traderPubKeyRing,
                tradeDate,
                contract,
                contractHash,
                depositTxSerialized,
                payoutTxSerialized,
                depositTxId,
                payoutTxId,
                contractAsJson,
                makerContractSignature,
                takerContractSignature,
                conflictResolverPubKeyRing,
                isSupportTicket,
                isMediationDispute);
        this.storage = storage;
        openingDate = new Date().getTime();
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // PROTO BUFFER
    ///////////////////////////////////////////////////////////////////////////////////////////

    public Dispute(String tradeId,
                   int traderId,
                   boolean disputeOpenerIsBuyer,
                   boolean disputeOpenerIsMaker,
                   PubKeyRing traderPubKeyRing,
                   long tradeDate,
                   Contract contract,
                   @Nullable byte[] contractHash,
                   @Nullable byte[] depositTxSerialized,
                   @Nullable byte[] payoutTxSerialized,
                   @Nullable String depositTxId,
                   @Nullable String payoutTxId,
                   String contractAsJson,
                   @Nullable String makerContractSignature,
                   @Nullable String takerContractSignature,
                   PubKeyRing conflictResolverPubKeyRing,
                   boolean isSupportTicket,
                   boolean isMediationDispute) {
        this.tradeId = tradeId;
        this.traderId = traderId;
        this.disputeOpenerIsBuyer = disputeOpenerIsBuyer;
        this.disputeOpenerIsMaker = disputeOpenerIsMaker;
        this.traderPubKeyRing = traderPubKeyRing;
        this.tradeDate = tradeDate;
        this.contract = contract;
        this.contractHash = contractHash;
        this.depositTxSerialized = depositTxSerialized;
        this.payoutTxSerialized = payoutTxSerialized;
        this.depositTxId = depositTxId;
        this.payoutTxId = payoutTxId;
        this.contractAsJson = contractAsJson;
        this.makerContractSignature = makerContractSignature;
        this.takerContractSignature = takerContractSignature;
        this.conflictResolverPubKeyRing = conflictResolverPubKeyRing;
        this.isSupportTicket = isSupportTicket;
        this.isMediationDispute = isMediationDispute;

        if (isMediationDispute) {
            id = tradeId + "_" + traderId + "_mediation";
        } else {
            id = tradeId + "_" + traderId; // We do not add anything to not break persisted data
        }
    }

    @Override
    public protobuf.Dispute toProtoMessage() {
        protobuf.Dispute.Builder builder = protobuf.Dispute.newBuilder()
                .setTradeId(tradeId)
                .setTraderId(traderId)
                .setDisputeOpenerIsBuyer(disputeOpenerIsBuyer)
                .setDisputeOpenerIsMaker(disputeOpenerIsMaker)
                .setTraderPubKeyRing(traderPubKeyRing.toProtoMessage())
                .setTradeDate(tradeDate)
                .setContract(contract.toProtoMessage())
                .setContractAsJson(contractAsJson)
                .setArbitratorPubKeyRing(conflictResolverPubKeyRing.toProtoMessage()) // We renamed to conflictResolverPubKeyRing but need to keep protobuf as it was to be backward compatible
                .setIsSupportTicket(isSupportTicket)
                .addAllDisputeCommunicationMessages(chatMessages.stream()
                        .map(msg -> msg.toProtoNetworkEnvelope().getDisputeCommunicationMessage())
                        .collect(Collectors.toList()))
                .setIsClosed(isClosedProperty.get())
                .setOpeningDate(openingDate)
                .setId(id)
                .setIsMediationDispute(isMediationDispute);

        Optional.ofNullable(contractHash).ifPresent(e -> builder.setContractHash(ByteString.copyFrom(e)));
        Optional.ofNullable(depositTxSerialized).ifPresent(e -> builder.setDepositTxSerialized(ByteString.copyFrom(e)));
        Optional.ofNullable(payoutTxSerialized).ifPresent(e -> builder.setPayoutTxSerialized(ByteString.copyFrom(e)));
        Optional.ofNullable(depositTxId).ifPresent(builder::setDepositTxId);
        Optional.ofNullable(payoutTxId).ifPresent(builder::setPayoutTxId);
        Optional.ofNullable(disputePayoutTxId).ifPresent(builder::setDisputePayoutTxId);
        Optional.ofNullable(makerContractSignature).ifPresent(builder::setMakerContractSignature);
        Optional.ofNullable(takerContractSignature).ifPresent(builder::setTakerContractSignature);
        Optional.ofNullable(disputeResultProperty.get()).ifPresent(result -> builder.setDisputeResult(disputeResultProperty.get().toProtoMessage()));
        return builder.build();
    }

    public static Dispute fromProto(protobuf.Dispute proto, CoreProtoResolver coreProtoResolver) {
        final Dispute dispute = new Dispute(proto.getTradeId(),
                proto.getTraderId(),
                proto.getDisputeOpenerIsBuyer(),
                proto.getDisputeOpenerIsMaker(),
                PubKeyRing.fromProto(proto.getTraderPubKeyRing()),
                proto.getTradeDate(),
                Contract.fromProto(proto.getContract(), coreProtoResolver),
                ProtoUtil.byteArrayOrNullFromProto(proto.getContractHash()),
                ProtoUtil.byteArrayOrNullFromProto(proto.getDepositTxSerialized()),
                ProtoUtil.byteArrayOrNullFromProto(proto.getPayoutTxSerialized()),
                ProtoUtil.stringOrNullFromProto(proto.getDepositTxId()),
                ProtoUtil.stringOrNullFromProto(proto.getPayoutTxId()),
                proto.getContractAsJson(),
                ProtoUtil.stringOrNullFromProto(proto.getMakerContractSignature()),
                ProtoUtil.stringOrNullFromProto(proto.getTakerContractSignature()),
                PubKeyRing.fromProto(proto.getArbitratorPubKeyRing()), // We renamed to conflictResolverPubKeyRing but need to keep protobuf as it was to be backward compatible
                proto.getIsSupportTicket(),
                proto.getIsMediationDispute());

        dispute.chatMessages.addAll(proto.getDisputeCommunicationMessagesList().stream()
                .map(ChatMessage::fromPayloadProto)
                .collect(Collectors.toList()));

        dispute.openingDate = proto.getOpeningDate();
        dispute.isClosedProperty.set(proto.getIsClosed());
        if (proto.hasDisputeResult())
            dispute.disputeResultProperty.set(DisputeResult.fromProto(proto.getDisputeResult()));
        dispute.disputePayoutTxId = ProtoUtil.stringOrNullFromProto(proto.getDisputePayoutTxId());
        return dispute;
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // API
    ///////////////////////////////////////////////////////////////////////////////////////////

    public void addChatMessage(ChatMessage chatMessage) {
        if (!chatMessages.contains(chatMessage)) {
            chatMessages.add(chatMessage);
            storage.queueUpForSave();
        } else {
            log.error("disputeDirectMessage already exists");
        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Setters
    ///////////////////////////////////////////////////////////////////////////////////////////

    // In case we get the object via the network storage is not set as its transient, so we need to set it.
    public void setStorage(Storage<? extends DisputeList> storage) {
        this.storage = storage;
    }

    public void setIsClosed(boolean isClosed) {
        boolean changed = this.isClosedProperty.get() != isClosed;
        this.isClosedProperty.set(isClosed);
        if (changed)
            storage.queueUpForSave();
    }

    public void setDisputeResult(DisputeResult disputeResult) {
        boolean changed = disputeResultProperty.get() == null || !disputeResultProperty.get().equals(disputeResult);
        disputeResultProperty.set(disputeResult);
        if (changed)
            storage.queueUpForSave();
    }

    public void setDisputePayoutTxId(String disputePayoutTxId) {
        boolean changed = this.disputePayoutTxId == null || !this.disputePayoutTxId.equals(disputePayoutTxId);
        this.disputePayoutTxId = disputePayoutTxId;
        if (changed)
            storage.queueUpForSave();
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Getters
    ///////////////////////////////////////////////////////////////////////////////////////////

    public String getShortTradeId() {
        return Utilities.getShortId(tradeId);
    }

    public ReadOnlyBooleanProperty isClosedProperty() {
        return isClosedProperty;
    }

    public ReadOnlyObjectProperty<DisputeResult> disputeResultProperty() {
        return disputeResultProperty;
    }

    public Date getTradeDate() {
        return new Date(tradeDate);
    }

    public Date getOpeningDate() {
        return new Date(openingDate);
    }

    public boolean isClosed() {
        return isClosedProperty.get();
    }

    public NodeAddress getConflictResolverNodeAddress() {
        return isMediationDispute ? contract.getMediatorNodeAddress() : contract.getArbitratorNodeAddress();
    }

    @Override
    public String toString() {
        return "Dispute{" +
                "tradeId='" + tradeId + '\'' +
                ", id='" + id + '\'' +
                ", traderId=" + traderId +
                ", disputeOpenerIsBuyer=" + disputeOpenerIsBuyer +
                ", disputeOpenerIsMaker=" + disputeOpenerIsMaker +
                ", openingDate=" + openingDate +
                ", traderPubKeyRing=" + traderPubKeyRing +
                ", tradeDate=" + tradeDate +
                ", contract=" + contract +
                ", contractHash=" + Utilities.bytesAsHexString(contractHash) +
                ", depositTxSerialized=" + Utilities.bytesAsHexString(depositTxSerialized) +
                ", payoutTxSerialized not displayed for privacy reasons..." +
                ", depositTxId='" + depositTxId + '\'' +
                ", payoutTxId='" + payoutTxId + '\'' +
                ", contractAsJson='" + contractAsJson + '\'' +
                ", makerContractSignature='" + makerContractSignature + '\'' +
                ", takerContractSignature='" + takerContractSignature + '\'' +
                ", conflictResolverPubKeyRing=" + conflictResolverPubKeyRing +
                ", isSupportTicket=" + isSupportTicket +
                ", chatMessages=" + chatMessages +
                ", isClosed=" + isClosedProperty.get() +
                ", disputeResult=" + disputeResultProperty.get() +
                ", disputePayoutTxId='" + disputePayoutTxId + '\'' +
                ", isClosedProperty=" + isClosedProperty +
                ", disputeResultProperty=" + disputeResultProperty +
                '}';
    }

    public SupportType getSupportType() {
        return isMediationDispute ? SupportType.MEDIATION : SupportType.ARBITRATION;
    }
}
