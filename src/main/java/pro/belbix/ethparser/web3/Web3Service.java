package pro.belbix.ethparser.web3;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.web3j.protocol.core.DefaultBlockParameterName.EARLIEST;
import static org.web3j.protocol.core.DefaultBlockParameterName.LATEST;
import static pro.belbix.ethparser.service.AbiProviderService.BSC_NETWORK;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;
import static pro.belbix.ethparser.web3.Web3Utils.callWithRetry;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.ZERO_ADDRESS;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import lombok.extern.log4j.Log4j2;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.BatchRequest;
import org.web3j.protocol.core.BatchResponse;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.Response.Error;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import pro.belbix.ethparser.properties.AppProperties;

@Service
@Log4j2
public class Web3Service {
  public static final int LOG_LAST_PARSED_COUNT = 1_000;
  public static final DefaultBlockParameter BLOCK_NUMBER_30_AUGUST_2020 = DefaultBlockParameter
      .valueOf(new BigInteger("10765094"));
  private final AppProperties appProperties;

  private Web3j web3;
  private boolean init = false;

  public Web3Service(AppProperties appProperties) {
    this.appProperties = appProperties;
  }

  @PostConstruct
  private void init() {
    if (appProperties.isOnlyApi()) {
      return;
    }
    log.info("Connecting to Ethereum ...");
    String web3Url;
    String web3User;
    String web3Password;
    if (ETH_NETWORK.equals(appProperties.getNetwork())) {
      web3Url = appProperties.getWeb3Url();
      web3User = appProperties.getWeb3User();
      web3Password = appProperties.getWeb3Password();
    } else if (BSC_NETWORK.equals(appProperties.getNetwork())) {
      web3Url = appProperties.getWeb3BscUrl();
      web3User = appProperties.getWeb3BscUser();
      web3Password = appProperties.getWeb3BscPassword();
    } else {
      throw new IllegalStateException("Unknown network " + appProperties.getNetwork());
    }
    if (web3Url == null) {
      throw new IllegalStateException("Web3 url not defined");
    }
    OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
    clientBuilder.callTimeout(600, SECONDS)
        .writeTimeout(600, SECONDS)
        .connectTimeout(600, SECONDS)
        .authenticator((route, response) -> response.request().newBuilder()
            .header("Authorization",
                Credentials.basic(web3User, web3Password))
            .build());

    HttpService service = new HttpService(
        appProperties.getWeb3Url(),
        clientBuilder.build(),
        false
    );
    web3 = Web3j.build(service);

    log.info("Successfully connected to Ethereum");
    init = true;
  }

  public void waitInit() {
    while (!init) {
      log.info("Wait initialization...");
      try {
        //noinspection BusyWait
        Thread.sleep(1000);
      } catch (InterruptedException ignored) {
      }
    }
  }

  public TransactionReceipt fetchTransactionReceipt(String hash) {
    waitInit();

    EthGetTransactionReceipt result =
        callWithRetry(() -> {
          EthGetTransactionReceipt ethGetTransactionReceipt
              = web3.ethGetTransactionReceipt(hash).send();
          if (ethGetTransactionReceipt == null) {
            log.error("Null receipt for hash: " + hash);
            return null;
          }
          Error error = ethGetTransactionReceipt.getError();
          if (error != null) {
            log.error("Got " + error.getCode() + " " + error.getMessage()
                + " " + error.getData());
            return null;
          }

          //alchemy.io can't return it immediately and return empty response
          if (ethGetTransactionReceipt.getTransactionReceipt().isEmpty()) {
            log.warn("Receipt is empty, retry with sleep");
            Thread.sleep(5000);
            return null;
          }
          return ethGetTransactionReceipt;
        });
    if (result == null) {
      return null;
    }
    return result.getTransactionReceipt()
        .orElseThrow(() -> new IllegalStateException("Receipt is null for " + hash));
  }

  public Stream<Optional<TransactionReceipt>> fetchTransactionReceiptBatch(
      Collection<String> hashes) {
    waitInit();
    BatchResponse batchResponse = callWithRetry(() -> {
      BatchRequest batchRequest = web3.newBatch();
      hashes.forEach(h ->
          batchRequest.add(web3.ethGetTransactionReceipt(h))
      );
      return batchRequest.send();
    });

    if (batchResponse == null) {
      return Stream.of();
    }

    return batchResponse.getResponses().stream()
        .map(r -> ((EthGetTransactionReceipt) r).getTransactionReceipt());
  }

  public Transaction findTransaction(String hash) {
    waitInit();
    return callWithRetry(
        () -> web3.ethGetTransactionByHash(hash).send().getTransaction().orElse(null));
  }

  public EthBlock findBlockByHash(String blockHash, boolean returnFullTransactionObjects) {
    waitInit();
    return callWithRetry(() -> {
      EthBlock ethBlock = web3.ethGetBlockByHash(blockHash, returnFullTransactionObjects).send();
      if (ethBlock == null) {
        log.error("Error fetching block with hash " + blockHash);
        return null;
      }
      if (ethBlock.getError() != null) {
        log.error("Error fetching block " + ethBlock.getError().getMessage());
        return null;
      }
      return ethBlock;
    });
  }

  public EthBlock findBlockByNumber(long number, boolean returnFullTransactionObjects) {
    waitInit();
    return callWithRetry(() -> {
      EthBlock ethBlock = web3.ethGetBlockByNumber(
          DefaultBlockParameter.valueOf(BigInteger.valueOf(number)),
          returnFullTransactionObjects).send();
      if (ethBlock == null) {
        log.error("Error fetching block with number " + number);
        return null;
      }
      if (ethBlock.getError() != null) {
        log.error("Error fetching block " + ethBlock.getError().getMessage());
        return null;
      }
      return ethBlock;
    });
  }

  public double fetchAverageGasPrice() {
    waitInit();
    EthGasPrice result = callWithRetry(() -> {
      EthGasPrice gasPrice = web3.ethGasPrice().send();
      if (gasPrice == null) {
        log.error("Null gas fetching result");
        return null;
      }
      if (gasPrice.getError() != null) {
        log.error("Error gas fetching " + gasPrice.getError().getMessage());
        return null;
      }
      return gasPrice;
    });
    if (result == null) {
      return 0.0;
    }
    return result.getGasPrice().doubleValue() / 1000_000_000;
  }

  @SuppressWarnings("rawtypes")
  public List<LogResult> fetchContractLogs(
      List<String> addresses,
      Integer start,
      Integer end,
      String... topics) {
    waitInit();
    DefaultBlockParameter fromBlock;
    DefaultBlockParameter toBlock;
    if (start == null) {
      fromBlock = EARLIEST;
    } else {
      fromBlock = new DefaultBlockParameterNumber(new BigInteger(start + ""));
    }
    if (end == null) {
      toBlock = LATEST;
    } else {
      toBlock = new DefaultBlockParameterNumber(new BigInteger(end + ""));
    }
    EthFilter filter = new EthFilter(fromBlock,
        toBlock, addresses);
    filter.addOptionalTopics(topics);
    EthLog result = callWithRetry(() -> {
      EthLog ethLog = web3.ethGetLogs(filter).send();
      if (ethLog == null) {
        log.error("get logs null result");
        return null;
      }
      if (ethLog.getError() != null) {
        log.error("Can't get eth log. " + ethLog.getError().getMessage());
        return null;
      }
      return ethLog;
    });
    if (result == null) {
      return List.of();
    }
    return result.getLogs();
  }

  public double fetchBalance(String hash) {
    waitInit();
    EthGetBalance result = callWithRetry(() -> {
      EthGetBalance ethGetBalance = web3.ethGetBalance(hash, LATEST).send();
      if (ethGetBalance == null) {
        log.error("Get balance response is null");
        return null;
      }
      if (ethGetBalance.getError() != null) {
        log.error("Get balance error callback " + ethGetBalance.getError().getMessage());
        return null;
      }
      return ethGetBalance;
    });
    if (result == null) {
      return 0.0;
    }
    return result.getBalance().doubleValue();
  }

  public BigInteger fetchCurrentBlock() {
    EthBlockNumber result = callWithRetry(() -> {
      EthBlockNumber ethBlockNumber = web3.ethBlockNumber().send();
      if (ethBlockNumber == null) {
        log.error("Null callback last block");
        return null;
      }
      if (ethBlockNumber.getError() != null) {
        log.error("Error from last block: " + ethBlockNumber.getError());
        return null;
      }
      return ethBlockNumber;
    });
    if (result == null) {
      return BigInteger.ZERO;
    }
    return result.getBlockNumber();
  }

  @SuppressWarnings("rawtypes")
  public List<Type> callFunction(Function function, String contractAddress,
      DefaultBlockParameter block) {
    org.web3j.protocol.core.methods.request.Transaction transaction =
        org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(
            ZERO_ADDRESS, contractAddress, FunctionEncoder.encode(function));

    EthCall result = callWithRetry(() -> {
      EthCall ethCall = web3.ethCall(transaction, block).send();
      if (ethCall == null) {
        log.warn("callFunction is null {}", function.getName());
        return null;
      }
      if (ethCall.getError() != null) {
        log.warn("{} callFunction callback is error: {}",
            function.getName(), ethCall.getError().getMessage());
        if ("execution aborted (timeout = 5s)".equals(ethCall.getError().getMessage())) {
          return null;
        }
        throw new IllegalStateException(
            "Not retryable response: " + ethCall.getError().getMessage());
      }
      return ethCall;
    });
    if (result == null) {
      return null;
    }

    return FunctionReturnDecoder.decode(result.getValue(), function.getOutputParameters());
  }

  public Web3j getWeb3() {
    return web3;
  }

  @PreDestroy
  private void close() {
    log.info("Close web3");
    if (web3 != null) {
      web3.shutdown();
    }
  }
}
