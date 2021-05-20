package pro.belbix.ethparser.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.web3j.tuples.generated.Tuple2;

@Service
@Log4j2
public class DtoCache {

  private static final int TTL = 5 * 60_000; // 5 min

  private final Map<String, Tuple2<Long, List<?>>> cache = new HashMap<>();

  public <T> List<T> load(String name, Supplier<List<T>> supplier) {
    clean();
    Tuple2<Long, List<?>> cached = cache.get(name);
    List<?> list;
    if (cached == null || isAlive(cached.component1())) {
      list = supplier.get();
      cache.put(name, new Tuple2<>(System.currentTimeMillis(), list));
    } else {
      log.info("Used cache {}", name);
      list = cached.component2();
    }
    //noinspection unchecked
    return (List<T>) list;
  }

  private void clean() {
    new HashSet<>(cache.keySet()).forEach(key -> {
      if (isAlive(cache.get(key).component1())) {
        cache.remove(key);
      }
    });
  }

  private boolean isAlive(long time) {
    return System.currentTimeMillis() - time > TTL;
  }

}
