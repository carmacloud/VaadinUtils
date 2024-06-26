package au.com.vaadinutils.user;

/**
 * Replaced in Vaadin 14 migration.
 */
public class UserSettingsStorageFactory {
    static UserSettingsStorage storage = new UserSettingsStorageNoOpImpl();

    static public UserSettingsStorage getUserSettingsStorage() {
        return storage;
    }

    static public void setStorageEngine(final UserSettingsStorage storageEngine) {
        storage = storageEngine;
    }
}
