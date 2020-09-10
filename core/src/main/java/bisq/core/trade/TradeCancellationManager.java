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

package bisq.core.trade;

import bisq.core.btc.wallet.Restrictions;
import bisq.core.offer.Offer;
import bisq.core.trade.protocol.ProcessModel;

import bisq.common.handlers.ErrorMessageHandler;
import bisq.common.handlers.ResultHandler;

import org.bitcoinj.core.Coin;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import lombok.extern.slf4j.Slf4j;

import static com.google.common.base.Preconditions.checkNotNull;

@Slf4j
@Singleton
public final class TradeCancellationManager {

    ///////////////////////////////////////////////////////////////////////////////////////////
    // Constructor
    ///////////////////////////////////////////////////////////////////////////////////////////

    @Inject
    public TradeCancellationManager() {
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // API
    ///////////////////////////////////////////////////////////////////////////////////////////

    public void requestCancelTrade(Trade trade,
                                   ResultHandler resultHandler,
                                   ErrorMessageHandler errorMessageHandler) {
        ProcessModel processModel = trade.getProcessModel();
        Offer offer = checkNotNull(trade.getOffer());
        Coin secDepositOfRequester = getSecurityDepositForRequester();
        Coin totalSecDeposit = offer.getSellerSecurityDeposit().add(offer.getBuyerSecurityDeposit());
        Coin secDepositForForPeer = totalSecDeposit.subtract(secDepositOfRequester);
        Coin tradeAmount = checkNotNull(trade.getTradeAmount(), "tradeAmount must not be null");
        if (trade instanceof BuyerTrade) {
            processModel.setBuyerPayoutAmountFromCanceledTrade(secDepositOfRequester.value);
            processModel.setSellerPayoutAmountFromCanceledTrade(tradeAmount.add(secDepositForForPeer).value);
        } else {
            processModel.setBuyerPayoutAmountFromCanceledTrade(secDepositForForPeer.value);
            processModel.setSellerPayoutAmountFromCanceledTrade(tradeAmount.add(secDepositOfRequester).value);
        }
        trade.getTradeProtocol().onRequestCancelTrade(resultHandler, errorMessageHandler);
    }

    public Coin getSecurityDepositForRequester() {
        return Restrictions.getMinRefundAtMediatedDispute();
    }

    private Coin getTotalSecDepositForAcceptingTrader(Offer offer) {
        Coin totalSecDeposit = offer.getSellerSecurityDeposit().add(offer.getBuyerSecurityDeposit());
        return totalSecDeposit.subtract(getSecurityDepositForRequester());
    }

    public Coin getDefaultSecDepositOfAcceptingTrader(Trade trade) {
        Offer offer = checkNotNull(trade.getOffer());
        return trade instanceof BuyerTrade ?
                offer.getBuyerSecurityDeposit() :
                offer.getSellerSecurityDeposit();
    }

    public Coin getLostSecDepositOfRequestingTrader(Trade trade) {
        Offer offer = checkNotNull(trade.getOffer());
        return getTotalSecDepositForAcceptingTrader(offer).subtract(getDefaultSecDepositOfAcceptingTrader(trade));
    }

    public void acceptRequest(Trade trade,
                              ResultHandler resultHandler,
                              ErrorMessageHandler errorMessageHandler) {
        ProcessModel processModel = trade.getProcessModel();
        Offer offer = checkNotNull(trade.getOffer());
        Coin secDepositOfRequester = getSecurityDepositForRequester();
        Coin totalSecDeposit = offer.getSellerSecurityDeposit().add(offer.getBuyerSecurityDeposit());
        Coin secDepositOfAcceptingTrader = totalSecDeposit.subtract(secDepositOfRequester);
        Coin tradeAmount = checkNotNull(trade.getTradeAmount(), "tradeAmount must not be null");
        if (trade instanceof BuyerTrade) {
            processModel.setBuyerPayoutAmountFromCanceledTrade(secDepositOfAcceptingTrader.value);
            processModel.setSellerPayoutAmountFromCanceledTrade(tradeAmount.add(secDepositOfRequester).value);
        } else {
            processModel.setBuyerPayoutAmountFromCanceledTrade(secDepositOfRequester.value);
            processModel.setSellerPayoutAmountFromCanceledTrade(tradeAmount.add(secDepositOfAcceptingTrader).value);
        }
        trade.getTradeProtocol().onAcceptCancelTradeRequest(resultHandler, errorMessageHandler);
    }

    public void rejectRequest(Trade trade,
                              ResultHandler resultHandler,
                              ErrorMessageHandler errorMessageHandler) {
        trade.getTradeProtocol().onRejectCancelTradeRequest(resultHandler, errorMessageHandler);
    }
}
