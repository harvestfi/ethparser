package pro.belbix.ethparser.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CommonUtils {

    public static long parseLong(String from, long defaultValue) {
        if (from != null) {
            return Long.parseLong(from);
        }
        return defaultValue;
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

    public static <T> List<T> reduceListElements(List<T> list, int divider) {
        return IntStream.range(0, list.size())
            .filter(i -> i % divider == 0)
            .mapToObj(list::get)
            .collect(Collectors.toList());
    }

}
