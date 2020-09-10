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

package bisq.core.trade.protocol.tasks.cancel;

import bisq.core.trade.BuyerTrade;
import bisq.core.trade.Trade;
import bisq.core.trade.protocol.tasks.SetupPayoutTxListener;

import bisq.common.UserThread;
import bisq.common.taskrunner.TaskRunner;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SetupCanceledTradePayoutTxListener extends SetupPayoutTxListener {
    @SuppressWarnings({"unused"})
    public SetupCanceledTradePayoutTxListener(TaskRunner taskHandler, Trade trade) {
        super(taskHandler, trade);
    }

    @Override
    protected void run() {
        try {
            runInterceptHook();

            super.run();

        } catch (Throwable t) {
            failed(t);
        }
    }

    @Override
    protected void setState() {
        trade.setBuyersCancelTradeState(BuyerTrade.BuyersCancelTradeState.PAYOUT_TX_SEEN_IN_NETWORK);
        if (trade.getPayoutTx() != null) {
            // We need to delay that call as we might get executed at startup after mailbox messages are
            // applied where we iterate over our pending trades. The closeCanceledTrade method would remove
            // that trade from the list causing a ConcurrentModificationException.
            // To avoid that we delay for one render frame.
            UserThread.execute(() -> processModel.getTradeManager().closeCanceledTrade(trade));
        }
    }
}
