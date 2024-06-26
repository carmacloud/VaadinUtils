package au.com.vaadinutils.dao;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Not used?
 */
public class JpaEntityHelper {

    final static AtomicInteger seeds = new AtomicInteger();

    public static String getGuid(final Object clazz) {
        return getGuid();
    }

    public static String getGuid() {
        final long stamp = System.currentTimeMillis();
        final long id = seeds.incrementAndGet();

        final int current = seeds.get();
        if (current > 100000) {
            seeds.compareAndSet(current, 0);
        }
        return (stamp + "-" + id);

    }

}
