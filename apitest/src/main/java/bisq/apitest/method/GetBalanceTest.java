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

package bisq.apitest.method;

import bisq.proto.grpc.GetBalanceRequest;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;



import bisq.apitest.OrderedRunner;
import bisq.apitest.annotation.Order;
import bisq.apitest.linux.BitcoinCli;

@Slf4j
@RunWith(OrderedRunner.class)
public class GetBalanceTest extends MethodTest {

    @BeforeClass
    public static void setUp() {
        try {
            setUpScaffold("bitcoind,seednode,arbdaemon,alicedaemon");

            String newAddress = new BitcoinCli(config, "getnewaddress").run().getOutput();
            String generateToAddressCmd = format("generatetoaddress %d \"%s\"", 1, newAddress);

            BitcoinCli generateToAddress = new BitcoinCli(config, generateToAddressCmd).run();
            log.info("{}\n{}", generateToAddress.getCommandWithOptions(), generateToAddress.getOutputValueAsStringArray());
            MILLISECONDS.sleep(1500); // give bisq app time to parse block

        } catch (IOException | InterruptedException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    @Order(1)
    public void testGetBalance() {
        var balance = grpcStubs.walletsService.getBalance(GetBalanceRequest.newBuilder().build()).getBalance();
        assertEquals(1000000000, balance);
    }

    @AfterClass
    public static void tearDown() {
        tearDownScaffold();
    }
}