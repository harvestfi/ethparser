package pro.belbix.ethparser.web3;

import static org.web3j.protocol.core.DefaultBlockParameterName.EARLIEST;
import static org.web3j.protocol.core.DefaultBlockParameterName.LATEST;
import static pro.belbix.ethparser.service.AbiProviderService.BSC_NETWORK;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.ZERO_ADDRESS;

import io.reactivex.Flowable;
import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.util.Strings;
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
import pro.belbix.ethparser.properties.AppProperties;

@Service
@Log4j2
public class Web3Functions {

  private final AppProperties appProperties;
  private final Web3EthService web3EthService;
  private final Web3BscService web3BscService;

  public Web3Functions(AppProperties appProperties,
      Web3EthService web3EthService, Web3BscService web3BscService) {
    this.appProperties = appProperties;
    this.web3EthService = web3EthService;
    this.web3BscService = web3BscService;
  }

  private Web3Service getWeb3Service(String network) {
    if (ETH_NETWORK.equals(network)) {
      return web3EthService;
    } else if (BSC_NETWORK.equals(network)) {
      return web3BscService;
    } else {
      throw new IllegalStateException("Unknown network " + network);
    }
  }

  private Web3j getWeb3(String network) {
    return getWeb3Service(network).getWeb3();
  }

  public TransactionReceipt fetchTransactionReceipt(String hash, String network) {

    EthGetTransactionReceipt result =
        getWeb3Service(network).callWithRetry(() -> {
          EthGetTransactionReceipt ethGetTransactionReceipt
              = getWeb3(network).ethGetTransactionReceipt(hash).send();
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
      Collection<String> hashes, String network) {
    BatchResponse batchResponse = getWeb3Service(network).callWithRetry(() -> {
      BatchRequest batchRequest = getWeb3(network).newBatch();
      hashes.forEach(h ->
          batchRequest.add(getWeb3(network).ethGetTransactionReceipt(h))
      );
      return batchRequest.send();
    });

    if (batchResponse == null) {
      return Stream.of();
    }

    return batchResponse.getResponses().stream()
        .map(r -> ((EthGetTransactionReceipt) r).getTransactionReceipt());
  }

  public Transaction findTransaction(String hash, String network) {
    return getWeb3Service(network).callWithRetry(
        () -> getWeb3(network).ethGetTransactionByHash(hash).send().getTransaction().orElse(null));
  }

  public EthBlock findBlockByHash(
      String blockHash,
      boolean returnFullTransactionObjects,
      String network) {
    return getWeb3Service(network).callWithRetry(() -> {
      EthBlock ethBlock = getWeb3(network)
          .ethGetBlockByHash(blockHash, returnFullTransactionObjects)
          .send();
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

  public EthBlock findBlockByNumber(
      long number,
      boolean returnFullTransactionObjects,
      String network) {
    return getWeb3Service(network).callWithRetry(() -> {
      EthBlock ethBlock = getWeb3(network).ethGetBlockByNumber(
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

  public double fetchAverageGasPrice(String network) {
    EthGasPrice result = getWeb3Service(network).callWithRetry(() -> {
      EthGasPrice gasPrice = getWeb3(network).ethGasPrice().send();
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
      String network,
      String... topics) {
    DefaultBlockParameter fromBlock;
    DefaultBlockParameter toBlock;
    if (start == null) {
      fromBlock = EARLIEST;
    } else {
      if(start < 0) {
        return List.of();
      }
      fromBlock = new DefaultBlockParameterNumber(new BigInteger(start + ""));
    }
    if (end == null) {
      toBlock = LATEST;
    } else {
      if(end<0) {
        return List.of();
      }
      toBlock = new DefaultBlockParameterNumber(new BigInteger(end + ""));
    }
    EthFilter filter = new EthFilter(fromBlock,
        toBlock, addresses);
    filter.addOptionalTopics(topics);
    EthLog result = getWeb3Service(network).callWithRetry(() -> {
      EthLog ethLog = getWeb3(network).ethGetLogs(filter).send();
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

  public BigInteger fetchBalance(String hash, Long block, String network) {
    DefaultBlockParameter blockP;
    if(block != null) {
      blockP = new DefaultBlockParameterNumber(BigInteger.valueOf(block));
    } else {
      blockP = LATEST;
    }
    EthGetBalance result = getWeb3Service(network).callWithRetry(() -> {
      EthGetBalance ethGetBalance = getWeb3(network).ethGetBalance(hash, blockP).send();
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
      return BigInteger.ZERO;
    }
    return result.getBalance();
  }

  public BigInteger fetchCurrentBlock(String network) {
    EthBlockNumber result = getWeb3Service(network).callWithRetry(() -> {
      EthBlockNumber ethBlockNumber = getWeb3(network).ethBlockNumber().send();
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
      DefaultBlockParameter block, String network) {
    org.web3j.protocol.core.methods.request.Transaction transaction =
        org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(
            ZERO_ADDRESS, contractAddress, FunctionEncoder.encode(function));

    EthCall result = getWeb3Service(network).callWithRetry(() -> {
      EthCall ethCall = getWeb3(network).ethCall(transaction, block).send();
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

  Flowable<Transaction> transactionFlowable(String startBlock, String network) {
    Flowable<Transaction> flowable;
    if (Strings.isBlank(startBlock)) {
      flowable = getWeb3Service(network)
          .callWithRetry(() -> getWeb3(network).transactionFlowable());
    } else {
      log.info("Start flow from block " + startBlock);
      flowable = getWeb3Service(network)
          .callWithRetry(() -> getWeb3(network).replayPastAndFutureTransactionsFlowable(
              DefaultBlockParameter.valueOf(new BigInteger(startBlock))));
    }
    if (flowable == null) {
      return Flowable.empty();
    }
    return flowable;
  }

  Flowable<Transaction> transactionsFlowable(
      DefaultBlockParameter start,
      DefaultBlockParameter end,
      String network
  ) {
    Flowable<Transaction> flowable =
        getWeb3Service(network)
            .callWithRetry(() -> getWeb3(network).replayPastTransactionsFlowable(start, end));
    if (flowable == null) {
      return Flowable.empty();
    }
    return flowable;
  }

  Flowable<EthBlock> blockFlowable(
      String startBlock,
      Supplier<Optional<Long>> lastBlockSupplier,
      String network) {
    DefaultBlockParameter startBlockP;
    if (Strings.isBlank(startBlock)) {
      startBlockP = DefaultBlockParameter.valueOf(
          BigInteger.valueOf(lastBlockSupplier.get().orElse(0L)));

    } else {
      startBlockP = DefaultBlockParameter.valueOf(
          new BigInteger(appProperties.getParseBlocksFrom()));
    }
    Flowable<EthBlock> flowable =
        getWeb3Service(network).callWithRetry(() ->
            getWeb3(network).replayPastAndFutureBlocksFlowable(startBlockP, true));
    if (flowable == null) {
      return Flowable.empty();
    }
    return flowable;
  }
}
