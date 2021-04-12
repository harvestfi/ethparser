package pro.belbix.ethparser.utils;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class Caller {

  public static <T> Optional<T> silentCall(Callable<T> callable) {
    try {
      return Optional.ofNullable(callable.call());
    } catch (Exception e) {
      log.error("Error call", e);
    }
    return Optional.empty();
  }

  public static <T> boolean isNotEmptyList(List<T> list) {
    return !list.isEmpty() && list.get(0) != null;
  }

}
