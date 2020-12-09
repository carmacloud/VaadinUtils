package au.com.vaadinutils.user;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @deprecated Replaced in Vaadin 14 migration.
 */
public class UserSettingsStorageNoOpImpl implements UserSettingsStorage {
    Map<String, String> map = new ConcurrentHashMap<>();

    @Override
    public void store(String key, String value) {
        map.put(key, value);
    }

    @Override
    public String get(String key) {
        return map.get(key);
    }

    @Override
    public void erase(String partialKey) {
        // TODO Auto-generated method stub
    }
}
