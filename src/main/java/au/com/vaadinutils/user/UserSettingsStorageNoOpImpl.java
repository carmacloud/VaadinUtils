package au.com.vaadinutils.user;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Replaced in Vaadin 14 migration.
 */
public class UserSettingsStorageNoOpImpl implements UserSettingsStorage {
    Map<String, String> map = new ConcurrentHashMap<>();

    @Override
    public void store(final String key, final String value) {
        map.put(key, value);
    }

    @Override
    public String get(final String key) {
        return map.get(key);
    }

    @Override
    public void erase(final String partialKey) {
        // TODO Auto-generated method stub
    }
}
