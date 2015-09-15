package io.xude.filter;

import io.xude.Service;
import io.xude.Services;
import io.xude.testing.DelayFilter;
import io.xude.util.EmptySubscriber;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

import static io.xude.util.Publishers.just;
import static org.junit.Assert.assertTrue;

public class TimeoutFilterTest {

    @Test(timeout = 5_000L)
    public void testTimeoutCase() throws InterruptedException {
        testTimeoutFilter(100, 50, true);
    }

    @Test(timeout = 5_000L)
    public void testNormalCase() throws InterruptedException {
        testTimeoutFilter(50, 100, false);
    }

    private void testTimeoutFilter(long delayMs, long timeoutMs, boolean mustFail) throws InterruptedException {
        System.out.println("TimeoutFilterTest delay: " + delayMs
            + ", timeout: " + timeoutMs + ", expecting failure: " + mustFail);

        Service<Integer, String> timeoutService =
            new TimeoutFilter<Integer, String>(timeoutMs)
                .andThen(new DelayFilter<>(delayMs))
                .andThen(Services.fromFunction(Object::toString));

        CountDownLatch latch = new CountDownLatch(1);
        timeoutService.apply(just(1)).subscribe(new EmptySubscriber<String>() {
            @Override
            public void onNext(String s) {
                if (mustFail) {
                    assertTrue("Shouldn't receive onNext event", false);
                }
                latch.countDown();
            }

            @Override
            public void onError(Throwable t) {
                if (!mustFail) {
                    assertTrue("Shouldn't receive onError event", false);
                }
                latch.countDown();
            }
        });

        latch.await();
    }
}
