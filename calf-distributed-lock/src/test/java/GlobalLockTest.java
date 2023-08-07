import com.github.shawven.calf.lock.support.lock.GlobalLock;
import com.github.shawven.calf.lock.support.lock.GlobalTryLock;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.google.common.util.concurrent.Uninterruptibles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author xw
 * @date 2023/5/23
 */
public class GlobalLockTest {

    private Service service = new Service();

    public int num = 0;

    private static Interner<String> lock = Interners.newWeakInterner();

    public void curLock(String uid) {
        synchronized (lock.intern(uid)) {
            num ++;
        }
    }

    @Test
    public void testCurLock() {
        List<Runnable> runnables = new ArrayList<>();
        for (int i = 0; i < 100000; i++) {
            int finalI = i;
            runnables.add(() -> {
                curLock(new String("1"));
            });
        }
        allOf(runnables.toArray(new Runnable[0])).join();
        System.out.println(num);
    }

    @Test
    public void testLock() {
        allOf(
                () -> service.lock("lock"),
                () -> service.tryLock("tryLock")
        ).join();
    }

    @Test
    public void testLockWithKey() {
        allOf(
                () -> service.lockKey("lockKey", 1),
                () -> service.lockKey("lockKey", 2),
                () -> service.lockKey("notKey", "trueLockKey"),
                () -> service.lockKey("notKey", "trueLockKey"),
                () -> service.tryLockKey("tryLockKey", 1),
                () -> service.tryLockKey("tryLockKey", 2)
        ).join();
    }

    @Test
    public void testLockWithExclusive() {
        allOf(
                () -> service.tryLockWithExclusive("tryLockWithExclusive"),
                () -> service.tryLockWithExclusive("tryLockWithExclusive")
        ).join();
    }

    /**
     * CompletableFuture allOf
     *
     * @param tasks
     * @return
     */
    public static CompletableFuture<Void> allOf(Runnable... tasks) {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (Runnable task : tasks) {
            futures.add(CompletableFuture.runAsync(task, executor));
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    @Component
    public static class Service {

        public final Logger logger = LoggerFactory.getLogger(GlobalLockTest.class);

        public final AtomicInteger num = new AtomicInteger();

        @GlobalLock
        public void lock(String name) {
            logger.info("{} locked", name);
            num.incrementAndGet();
            Uninterruptibles.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);
            logger.info("{} unlocked", name);
        }

        @GlobalTryLock
        public void tryLock(String name) {
            logger.info("{} locked", name);
            num.incrementAndGet();
            Uninterruptibles.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);
            logger.info("{} unlocked", name);
        }

        @GlobalLock(key = {"#p0", "#p1"})
        public void lockKey(String name, int i) {
            logger.info("{}{} locked", name, i);
            num.incrementAndGet();
            Uninterruptibles.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);
            logger.info("{}{} unlocked", name, i);
        }


        @GlobalLock(key = "#value")
        public void lockKey(String name, String value) {
            logger.info("{} locked", value);
            num.incrementAndGet();
            Uninterruptibles.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);
            logger.info("{} unlocked", value);
        }

        @GlobalTryLock(key = "#p0 +'_' + #p1")
        public void tryLockKey(String name, int i) {
            logger.info("{}{} locked", name, i);
            num.incrementAndGet();
            Uninterruptibles.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);
            logger.info("{}{} unlocked", name, i);
        }

        @GlobalTryLock(timeToExclusive = 10)
        public void tryLockWithExclusive(String name) {
            logger.info("{} locked", name);
            num.incrementAndGet();
            Uninterruptibles.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);
            logger.info("{} unlocked", name);
        }

        public AtomicInteger getNum() {
            return num;
        }
    }
}
