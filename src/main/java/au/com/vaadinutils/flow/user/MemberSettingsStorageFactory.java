package au.com.vaadinutils.flow.user;

public class MemberSettingsStorageFactory {
    
    private static MemberSettingsStorage  storage  = new MemberSettingsStorageEmptyImplementation();
    
    static public MemberSettingsStorage getUserSettingsStorage() {
        return storage;
    }

    static public void setStorageEngine(MemberSettingsStorage storageEngine) {
        storage = storageEngine;
    }
}
