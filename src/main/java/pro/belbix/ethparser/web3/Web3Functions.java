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
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
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
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.Response.Error;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthBlock.Block;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import pro.belbix.ethparser.properties.AppProperties;

@Service
@Log4j2
public class Web3Functions {

  private final static SimpleDecoder SIMPLE_DECODER = new SimpleDecoder();
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
    getWeb3Service(network).waitInit();
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
        }, "fetchTransactionReceipt " + hash + " " + network);
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
    }, "fetchTransactionReceiptBatch " + hashes + " " + network);

    if (batchResponse == null) {
      return Stream.of();
    }

    return batchResponse.getResponses().stream()
        .map(r -> ((EthGetTransactionReceipt) r).getTransactionReceipt());
  }

  public Transaction findTransaction(String hash, String network) {
    return getWeb3Service(network).callWithRetry(
        () -> getWeb3(network).ethGetTransactionByHash(hash).send().getTransaction().orElse(null),
        "findTransaction " + hash + " " + network);
  }

  public Stream<Block> findBlocksByBlockBatch(int start, int end, String network) {
    BatchResponse batchResponse = getWeb3Service(network).callWithRetry(() -> {
          BatchRequest batchRequest = getWeb3(network).newBatch();
          for (int block = start; block <= end; block++) {
            batchRequest.add(getWeb3(network).ethGetBlockByNumber(
                DefaultBlockParameter.valueOf(BigInteger.valueOf(block)), true));
          }
          return batchRequest.send();
        },
        "findBlocksByBlockBatch " + start + " " + end + " " + network);
    if (batchResponse == null) {
      return Stream.of();
    }
    return batchResponse.getResponses().stream()
        .map(r -> ((EthBlock) r).getBlock());
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
    }, "findBlockByHash " + blockHash + " " + network);
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
    }, "findBlockByNumber " + number + " " + network);
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
    }, "fetchAverageGasPrice " + network);
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
    Request<?, EthLog> request = prepareEthLogRequest(addresses, start, end, network, topics, new String[0]);
    if (request == null) {
      return List.of();
    }
    EthLog result = getWeb3Service(network).callWithRetry(() -> {
      EthLog ethLog = request.send();
      if (ethLog == null) {
        log.error("get logs null result");
        return null;
      }
      if (ethLog.getError() != null) {
        log.error("Can't get eth log. " + ethLog.getError().getMessage());
        return null;
      }
      return ethLog;
    }, "fetchContractLogs " + addresses + " " + start + " " + end + " " + network);
    if (result == null) {
      return List.of();
    }
    return result.getLogs();
  }

  public List<LogResult> fetchContractLogsBatch(
      List<String> addresses,
      Integer start,
      Integer end,
      String network,
      String[] optionalTopics,
      String[] mandatoryTopics
  ) {
    if (start == null || end == null) {
      throw new IllegalStateException("Null ranges doesn't supported");
    }
    if ((end - start) / appProperties.getHandleLoopStep() > appProperties.getHandleLoopStep()) {
      throw new IllegalStateException("Too big range! " + start + " " + end);
    }
    int step = appProperties.getHandleLoopStep();

    BatchResponse batchResponse = getWeb3Service(network).callWithRetry(() -> {
      BatchRequest batchRequest = getWeb3(network).newBatch();

      int from = start;
      int to = Math.min(end, start + step);
      while (true) {
        var req = prepareEthLogRequest(
            addresses,
            from,
            to,
            network,
            optionalTopics,
            mandatoryTopics
        );
        if (req != null) {
          batchRequest.add(req);
        }
        if (to >= end) {
          break;
        }
        from = to;
        to += step;
        to = Math.min(to, end);
      }

      return batchRequest.send();
    }, "fetchEthLogsBatch " + start + " " + end + " " + network);

    if (batchResponse == null) {
      return List.of();
    }

    //noinspection unchecked
    return batchResponse.getResponses().stream()
        .map(r -> ((EthLog) r).getLogs())
        .flatMap(Collection::stream)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  @SuppressWarnings("rawtypes")
  public Optional<Log> findLastLogEvent(
      String address,
      int _from,
      int _to,
      String network,
      Predicate<? super List<Type>> predicate,
      String... topics) {
    int step = appProperties.getHandleLoopStep();
    int from = Math.max(_from, _to - step);
    int to = _to;
    while (true) {
      List<LogResult> results =
          fetchContractLogsBatch(List.of(address), from, to, network, topics, new String[0]);
      Optional<Log> lastLog = SIMPLE_DECODER.findLogByPredicate(results, predicate);
      if (lastLog.isPresent()) {
        return lastLog;
      }
      if (from <= _from) {
        break;
      }
      to = from;
      from = Math.max(from - step, _from);
    }
    return Optional.empty();
  }

  @SuppressWarnings("rawtypes")
  public List<LogResult> findLastLogBatchByEventMandatoryTopics(
      String address,
      int _from,
      int _to,
      String network,
      String... mandatoryTopics) {
    int step = appProperties.getHandleLoopStep();
    int from = Math.max(_from, _to - step);
    int to = _to;
    int batchNumber = 1;
    while (true) {
      if (batchNumber > 1000) {
        log.info("Processed 1000 batches, no logs found");
        return List.of();
      }
      List<LogResult> results =
          fetchContractLogsBatch(List.of(address), from, to, network, new String[0],
              mandatoryTopics);
      if (results.size() > 0) {
        log.info("Processed: " + batchNumber + " batches");
        return results;
      }
      if (from <= _from) {
        break;
      }
      to = from;
      from = Math.max(from - step, _from);
      batchNumber += 1;
    }
    log.info("Processed: " + batchNumber + " batches");
    return List.of();
  }

  public Request<?, EthLog> prepareEthLogRequest(
      List<String> addresses,
      Integer start,
      Integer end,
      String network,
      String[] optionalTopics,
      String[] mandatoryTopics
  ) {
    DefaultBlockParameter fromBlock;
    DefaultBlockParameter toBlock;
    if (start == null) {
      fromBlock = EARLIEST;
    } else {
      if (start < 0) {
        return null;
      }
      fromBlock = new DefaultBlockParameterNumber(new BigInteger(start + ""));
    }
    if (end == null) {
      toBlock = LATEST;
    } else {
      if (end < 0) {
        return null;
      }
      toBlock = new DefaultBlockParameterNumber(new BigInteger(end + ""));
    }
    EthFilter filter = new EthFilter(fromBlock,
        toBlock, addresses);
    for (String topic : mandatoryTopics) {
      filter.addSingleTopic(topic);
    }
    filter.addOptionalTopics(optionalTopics);
    return getWeb3(network).ethGetLogs(filter);
  }

  public BigInteger fetchBalance(String hash, Long block, String network) {
    DefaultBlockParameter blockP;
    if (block != null) {
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
    }, "fetchBalance " + hash + " " + block + " " + network);
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
    }, "fetchCurrentBlock " + network);
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
        if (!"execution reverted".equals(ethCall.getError().getMessage())) {
          log.warn("{} callFunction callback is error: {}",
              function.getName(), ethCall.getError().getMessage());
        }
        if ("execution aborted (timeout = 5s)".equals(ethCall.getError().getMessage())) {
          return null;
        }
        throw new IllegalStateException(
            "Not retryable response: " + ethCall.getError().getMessage());
      }
      return ethCall;
    }, "callFunction " + function.getName() + " " + contractAddress
        + " " + block.getValue() + " " + network);
    if (result == null) {
      return null;
    }
    try {
      return FunctionReturnDecoder.decode(result.getValue(), function.getOutputParameters());
    } catch (Exception e) {
      log.warn("Error decode response {} for {}", function.getName(), contractAddress);
    }
    return List.of();
  }

  Flowable<Transaction> transactionFlowable(String startBlock, String network) {
    Flowable<Transaction> flowable;
    if (Strings.isBlank(startBlock)) {
      flowable = getWeb3Service(network)
          .callWithRetry(() -> getWeb3(network).transactionFlowable(),
              "transactionFlowable " + startBlock + " " + network);
    } else {
      log.info("Start flow from block " + startBlock);
      flowable = getWeb3Service(network)
          .callWithRetry(() -> getWeb3(network).replayPastAndFutureTransactionsFlowable(
              DefaultBlockParameter.valueOf(new BigInteger(startBlock))),
              "transactionFlowable " + startBlock + " " + network);
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
            .callWithRetry(() -> getWeb3(network).replayPastTransactionsFlowable(start, end),
                "transactionsFlowable " + start + " " + end);
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
          BigInteger.valueOf(lastBlockSupplier.get()
              .orElse(0L)));
//              .orElse((long) ContractUtils.getStartBlock(network))));

    } else {
      startBlockP = DefaultBlockParameter.valueOf(new BigInteger(startBlock));
    }
    Flowable<EthBlock> flowable =
        getWeb3Service(network).callWithRetry(() ->
                getWeb3(network).replayPastAndFutureBlocksFlowable(startBlockP, true),
            "blockFlowable " + startBlock + " " + network);
    if (flowable == null) {
      return Flowable.empty();
    }
    return flowable;
  }
}
