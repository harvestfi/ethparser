package pro.belbix.ethparser.utils;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

public class Caller {

  public static <T> Optional<T> silentCall(Callable<T> callable) {
    try {
      T result = callable.call();
      if (result == null) {
        return Optional.empty();
      }
      return Optional.ofNullable(result);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return Optional.empty();
  }

  public static <T> boolean isFilledList(List<T> list) {
    return !list.isEmpty() && list.get(0) != null;
  }

}
