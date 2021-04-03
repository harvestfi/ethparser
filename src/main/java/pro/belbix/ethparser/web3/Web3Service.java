package pro.belbix.ethparser.web3;

import static pro.belbix.ethparser.service.AbiProviderService.BSC_NETWORK;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;

import lombok.extern.log4j.Log4j2;
import okhttp3.OkHttpClient;
import org.apache.logging.log4j.util.Strings;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import pro.belbix.ethparser.properties.AppProperties;

@Log4j2
abstract class Web3Service {

  private final String network;
  final AppProperties appProperties;

  private Web3j web3;
  private boolean init = false;
  private boolean initStarted = false;

  public Web3Service(String network, AppProperties appProperties) {
    this.network = network;
    this.appProperties = appProperties;
  }

  void init() {
    if (appProperties.isOnlyApi()) {
      return;
    }
    log.info("{} web3 service connecting ...", network);
    String web3Url;
    if (ETH_NETWORK.equals(network)) {
      web3Url = appProperties.getWeb3Url();
    } else if (BSC_NETWORK.equals(network)) {
      web3Url = appProperties.getWeb3BscUrl();
    } else {
      throw new IllegalStateException("Unknown network " + network);
    }
    if (Strings.isBlank(web3Url)) {
      throw new IllegalStateException("Web3 url not defined");
    }

    web3 = Web3j.build(
        new HttpService(web3Url, new OkHttpClient.Builder().build(), false)
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
}
