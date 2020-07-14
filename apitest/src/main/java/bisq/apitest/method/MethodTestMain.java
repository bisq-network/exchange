package bisq.apitest.method;

import lombok.extern.slf4j.Slf4j;

import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import static java.lang.String.format;

/**
 * Driver for running API method tests.
 *
 * This may not seem necessary, but test cases are contained in the apitest sub
 * project's main sources, not its test sources.  An IDE will not automatically configure
 * JUnit test launchers, and a gradle build will not automatically run the test cases.
 *
 * However, it is easy to manually configure an IDE launcher to run all, some or one
 * JUnit test, and new gradle tasks should be provided to run all, some, or one test.
 */
@Slf4j
public class MethodTestMain {

    public static void main(String[] args) {
        JUnitCore jUnitCore = new JUnitCore();
        jUnitCore.addListener(new RunListener() {
            public void testStarted(Description description) {
                log.info("{}", description);
            }

            public void testIgnored(Description description) {
                log.info("Ignored {}", description);
            }

            public void testFailure(Failure failure) {
                log.error("Failed {}", failure.getTrace());
            }
        });
        Result result = jUnitCore.run(GetVersionTest.class, GetBalanceTest.class, WalletProtectionTest.class);
        ResultUtil.printResult(result);
    }

    private static class ResultUtil {
        public static void printResult(Result result) {
            log.info("Total tests: {},  Failed: {},  Ignored: {}",
                    result.getRunCount(),
                    result.getFailureCount(),
                    result.getIgnoreCount());

            if (result.wasSuccessful()) {
                log.info("All tests passed");
            } else if (result.getFailureCount() > 0) {
                log.error("{} test(s) failed", result.getFailureCount());
                result.getFailures().iterator().forEachRemaining(f -> log.error(format("%s.%s()%n\t%s",
                        f.getDescription().getTestClass().getName(),
                        f.getDescription().getMethodName(),
                        f.getTrace())));
            }
        }
    }
}