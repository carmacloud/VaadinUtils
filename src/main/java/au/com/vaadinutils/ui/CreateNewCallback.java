package au.com.vaadinutils.ui;

/**
 * Retain, used in new twin Column Select.
 */
public interface CreateNewCallback<T> {
    void createNew(RefreshCallback refreshCallback);
}
