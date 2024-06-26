package au.com.vaadinutils.listener;

/**
 * Replaced in Vaadin 14 migration.
 */
public class ListenerManagerFactory {
    public static <K> ListenerManager<K> createListenerManager(final String name, final long maxSize) {
        return new GenericListenerManager<>(name, maxSize);
    }

    public static <K> ListenerManager<K> createThreadSafeListenerManager(final String name, final long maxSize) {
        return new GenericListenerManagerThreadSafe<>(name, maxSize);
    }
}
