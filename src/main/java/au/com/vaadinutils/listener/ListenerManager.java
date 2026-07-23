package au.com.vaadinutils.listener;

/**
 * Replaced in Vaadin 14 migration.
 */
public interface ListenerManager<K> {
    void addListener(K listener);

    void removeListener(K listener);

    void notifyListeners(ListenerCallback<K> callback);

    boolean hasListeners();

    void destroy();
}
