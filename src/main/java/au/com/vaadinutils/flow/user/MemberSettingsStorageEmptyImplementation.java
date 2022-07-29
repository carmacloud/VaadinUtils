package au.com.vaadinutils.flow.user;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MemberSettingsStorageEmptyImplementation implements MemberSettingsStorage {

    private Map<String, String> map = new ConcurrentHashMap<>();

    @Override
    public void store(String key, String value) {
        map.put(key, value);
    }

    @Override
    public String get(String key) {
        return map.get(key);
    }

    @Override
    public Map<String, String> get(List<String> keys) {
        final Map<String, String> settings = new HashMap<>();
        keys.forEach(k -> settings.put(k, map.get(k)));

        return settings;
    }

    @Override
    public void preLoad() {
    }

    @Override
    public void storeInCache(String key, String value) {
        map.put(key, value);
    }
}
