package pro.belbix.ethparser.web3;

import static pro.belbix.ethparser.service.AbiProviderService.BSC_NETWORK;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Callable;
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

  public final static int RETRY_COUNT = 5000;
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

    web3 = Web3j.build(
        new HttpService(
            web3Url,
            new OkHttpClient.Builder()
                .readTimeout(Duration.of(30, ChronoUnit.SECONDS))
                .callTimeout(Duration.of(30, ChronoUnit.SECONDS))
                .writeTimeout(Duration.of(30, ChronoUnit.SECONDS))
                .connectTimeout(Duration.of(30, ChronoUnit.SECONDS))
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
    if (web3 != null) {
      web3.shutdown();
    }
    init = false;
    initStarted = false;
  }

  public <T> T callWithRetry(Callable<T> callable, String logMessage) {
    int count = 0;
    while (true) {
      waitInit();
      T result = null;
      Exception lastError = null;
      try {
        result = callable.call();
      } catch (IllegalStateException e) {
        if (e.getMessage().startsWith("Not retryable response")) {
          return null;
        }
      }
//      catch (ClientConnectionException e) { //by default all errors, but can be filtered by type
//        log.error("Connection exception, reconnect...", e);
//        close();
//        waitInit();
//        lastError = e;
//      }
      catch (Exception e) { //by default all errors, but can be filtered by type
        log.warn(logMessage+ " Retryable error", e);
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
