package au.com.vaadinutils.listener;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Replaced in Vaadin 14 migration.
 */
class GenericListenerManagerThreadSafe<K> extends GenericListenerManager<K> {
    public GenericListenerManagerThreadSafe(final String name, final long maxSize) {
        super(name, maxSize);
    }

    @Override
    protected Map<K, Date> createSet() {
        return new ConcurrentHashMap<>();
    }

}
