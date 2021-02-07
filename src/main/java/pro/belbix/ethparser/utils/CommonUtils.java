package pro.belbix.ethparser.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CommonUtils {

    public static long parseLong(String from, long def) {
        if (from != null) {
            return Long.parseLong(from);
        }
        return def;
    }

    public static <K, V> Map<K, V> createUniqueMap(Object... objects) {
        if (objects.length % 2 != 0) {
            throw new IllegalStateException("Wrong objects length");
        }
        if (objects.length == 0) {
            return Collections.emptyMap();
        }
        K key = null;
        Map<K, V> result = new HashMap<>();
        for (int i = 0; i < objects.length; i++) {
            Object o = objects[i];
            if (i % 2 != 0) {
                if (result.containsKey(key)) {
                    throw new IllegalStateException("Not unique key " + key);
                }
                //noinspection unchecked
                V value = (V) o;
                if (result.containsValue(value)) {
                    throw new IllegalStateException("Not unique value " + o);
                }
                result.put(key, value);
            } else {
                //noinspection unchecked
                key = (K) o;
            }
        }
        return Collections.unmodifiableMap(result);
    }

}
