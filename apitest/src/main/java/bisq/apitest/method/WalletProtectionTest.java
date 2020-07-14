package bisq.apitest.method;

import io.grpc.StatusRuntimeException;

import lombok.extern.slf4j.Slf4j;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import static bisq.apitest.config.BisqAppConfig.alicedaemon;



import bisq.apitest.OrderedRunner;
import bisq.apitest.annotation.Order;

@Slf4j
@RunWith(OrderedRunner.class)
public class WalletProtectionTest extends MethodTest {

    @BeforeClass
    public static void setUp() {
        setUpScaffold(alicedaemon.name());
    }

    @Test
    @Order(1)
    public void testSetWalletPassword() {
        var request = createSetWalletPasswordRequest("first-password");
        grpcStubs.walletsService.setWalletPassword(request);
    }

    @Test
    @Order(2)
    public void testGetBalanceOnEncryptedWalletShouldThrowException() {
        exceptionRule.expect(StatusRuntimeException.class);
        exceptionRule.expectMessage("UNKNOWN: wallet is locked");
        getBalance();
    }

    @Test
    @Order(3)
    public void testUnlockWalletFor4Seconds() {
        var request = createUnlockWalletRequest("first-password", 4);
        grpcStubs.walletsService.unlockWallet(request);
        getBalance(); // should not throw 'wallet locked' exception

        sleep(4500); // let unlock timeout expire
        exceptionRule.expect(StatusRuntimeException.class);
        exceptionRule.expectMessage("UNKNOWN: wallet is locked");
        getBalance();
    }

    @Test
    @Order(4)
    public void testGetBalanceAfterUnlockTimeExpiryShouldThrowException() {
        var request = createUnlockWalletRequest("first-password", 3);
        grpcStubs.walletsService.unlockWallet(request);
        sleep(4000); // let unlock timeout expire
        exceptionRule.expect(StatusRuntimeException.class);
        exceptionRule.expectMessage("UNKNOWN: wallet is locked");
        getBalance();
    }

    @Test
    @Order(5)
    public void testLockWalletBeforeUnlockTimeoutExpiry() {
        unlockWallet("first-password", 60);
        var request = createLockWalletRequest();
        grpcStubs.walletsService.lockWallet(request);

        exceptionRule.expect(StatusRuntimeException.class);
        exceptionRule.expectMessage("UNKNOWN: wallet is locked");
        getBalance();
    }

    @Test
    @Order(6)
    public void testLockWalletWhenWalletAlreadyLockedShouldThrowException() {
        exceptionRule.expect(StatusRuntimeException.class);
        exceptionRule.expectMessage("UNKNOWN: wallet is already locked");
        var request = createLockWalletRequest();
        grpcStubs.walletsService.lockWallet(request);
    }

    @Test
    @Order(7)
    public void testUnlockWalletTimeoutOverride() {
        unlockWallet("first-password", 2);
        sleep(500); // override unlock timeout after 0.5s
        unlockWallet("first-password", 6);
        sleep(5000);
        getBalance();   // getbalance 5s after resetting unlock timeout to 6s
    }

    @Test
    @Order(8)
    public void testSetNewWalletPassword() {
        var request = createSetWalletPasswordRequest("first-password", "second-password");
        grpcStubs.walletsService.setWalletPassword(request);

        unlockWallet("second-password", 2);
        getBalance();
        sleep(2500); // allow time for wallet save
    }

    @Test
    @Order(9)
    public void testSetNewWalletPasswordWithIncorrectNewPasswordShouldThrowException() {
        exceptionRule.expect(StatusRuntimeException.class);
        exceptionRule.expectMessage("UNKNOWN: incorrect old password");
        var request = createSetWalletPasswordRequest("bad old password", "irrelevant");
        grpcStubs.walletsService.setWalletPassword(request);
    }

    @Test
    @Order(10)
    public void testRemoveNewWalletPassword() {
        var request = createRemoveWalletPasswordRequest("second-password");
        grpcStubs.walletsService.removeWalletPassword(request);
        getBalance(); // should not throw 'wallet locked' exception
    }

    @AfterClass
    public static void tearDown() {
        tearDownScaffold();
    }
}