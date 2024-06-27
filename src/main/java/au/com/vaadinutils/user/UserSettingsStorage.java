package au.com.vaadinutils.user;

/**
 * Replaced in Vaadin 14 migration.
 */
public interface UserSettingsStorage {
    void store(String key, String value);

    String get(String string);

    void erase(String partialKey);
}
