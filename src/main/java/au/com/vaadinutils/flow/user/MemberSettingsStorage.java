package au.com.vaadinutils.flow.user;

import java.util.List;
import java.util.Map;

public interface MemberSettingsStorage {

    void store(String key, String value);
    String get(String key);
    Map<String, String> get(List<String> keys);
    void preLoad();
    void storeInCache(String key, String value);
}
