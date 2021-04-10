package pro.belbix.ethparser.web3;

import java.util.concurrent.Callable;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class Web3Utils {

  private Web3Utils() {
  }

  public final static int RETRY_COUNT = 5000;

  public static <T> T callWithRetry(Callable<T> callable) {
    int count = 0;
    while (true) {
      T result = null;
      Exception lastError = null;
      try {
        result = callable.call();
      } catch (IllegalStateException e) {
        if (e.getMessage().startsWith("Not retryable response")) {
          return null;
        }
      } catch (Exception e) { //by default all errors, but can be filtered by type
        log.warn("Retryable error", e);
        lastError = e;
      }

      if (result != null) {
        return result;
      }
      count++;
      if (count > RETRY_COUNT) {
        if (lastError != null) {
          lastError.printStackTrace();
        }
        return null;
      }
      log.warn("Fail call web3, retry " + count);
      try {
        //noinspection BusyWait
        Thread.sleep(1000);
      } catch (InterruptedException ignore) {
      }
    }
  }

}
