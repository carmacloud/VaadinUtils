package au.com.vaadinutils.user;

/**
 * @deprecated Replaced in Vaadin 14 migration.
 */
public class UserSettingsStorageFactory {
    static UserSettingsStorage storage = new UserSettingsStorageNoOpImpl();

    static public UserSettingsStorage getUserSettingsStorage() {
        return storage;
    }

    static public void setStorageEngine(UserSettingsStorage storageEngine) {
        storage = storageEngine;
    }
}
