package pro.belbix.ethparser.web3;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.log4j.Log4j2;
import okhttp3.OkHttpClient;
import org.apache.logging.log4j.util.Strings;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.exceptions.ClientConnectionException;
import org.web3j.protocol.http.HttpService;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.properties.NetworkProperties;

@Log4j2
abstract class Web3Service {

  private static final AtomicBoolean run = new AtomicBoolean(true);
  public final static int RETRY_COUNT = 100;
  private final String network;
  final AppProperties appProperties;
  final NetworkProperties networkProperties;

  private Web3j web3;
  private boolean init = false;
  private transient boolean initStarted = false;

  public Web3Service(String network,
      AppProperties appProperties,
      NetworkProperties networkProperties) {
    this.network = network;
    this.appProperties = appProperties;
    this.networkProperties = networkProperties;
  }

  void init() {
    if (appProperties.isOnlyApi()) {
      return;
    }
    log.info("{} web3 service connecting ...", network);
    String web3Url = networkProperties.get(network).getWeb3Url();
    if (Strings.isBlank(web3Url)) {
      throw new IllegalStateException("Web3 url not defined");
    }
    int timeout = networkProperties.get(network).getWeb3Timeout();
    web3 = Web3j.build(
        new HttpService(
            web3Url,
            new OkHttpClient.Builder()
                .readTimeout(Duration.of(timeout, ChronoUnit.SECONDS))
                .callTimeout(Duration.of(timeout, ChronoUnit.SECONDS))
                .writeTimeout(Duration.of(timeout, ChronoUnit.SECONDS))
                .connectTimeout(Duration.of(timeout, ChronoUnit.SECONDS))
                .build(),
            false)
    );
    log.info("{} web3 service successfully connected", network);
    init = true;
  }

  Web3j getWeb3() {
    return web3;
  }

  void waitInit() {
    if (!initStarted) {
      synchronized (this) {
        if (!initStarted) {
          initStarted = true;
          init();
        }
      }
    }
    while (!init) {
      log.info("Wait {} web3 service initialization...", network);
      try {
        //noinspection BusyWait
        Thread.sleep(1000);
      } catch (InterruptedException ignored) {
      }
    }
  }

  void close() {
    log.info("Close {} web3 service", network);
    run.set(false);
    if (web3 != null) {
      web3.shutdown();
    }
    init = false;
    initStarted = false;
  }

  public <T> T callWithRetry(Callable<T> callable, String logMessage) {
    int count = 0;
    while (run.get()) {
      waitInit();
      T result = null;
      Exception lastError = null;
      try {
        result = callable.call();
      } catch (IllegalStateException e) {
        if (e.getMessage().startsWith("Not retryable response")) {
          return null;
        }
      } catch (ClientConnectionException e) {
        if (e.getMessage().contains("Invalid method parameter(s)")) {
          return null;
        }
        log.warn(logMessage + " Retryable client error", e);
        lastError = e;
      } catch (Exception e) { //by default all errors, but can be filtered by type
        log.warn(logMessage + " Retryable error", e);
        lastError = e;
      }

      if (result != null) {
        return result;
      }
      count++;
      if (count > RETRY_COUNT) {
        if (lastError != null) {
          lastError.printStackTrace();
          log.error("Retry limit {}, last error:", logMessage, lastError);
        }
        return null;
      }
      log.warn("Fail call web3 {}, retry {}, error: {}", logMessage, count,
          lastError != null ? lastError.getMessage() : "Unknown error");
      try {
        //noinspection BusyWait
        Thread.sleep(1000);
      } catch (InterruptedException ignore) {
      }
    }
    return null;
  }
}
