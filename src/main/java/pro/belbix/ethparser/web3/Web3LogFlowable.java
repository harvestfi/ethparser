package pro.belbix.ethparser.web3;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import lombok.extern.log4j.Log4j2;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import org.web3j.protocol.core.methods.response.Log;
import pro.belbix.ethparser.model.Web3Model;

@Log4j2
public class Web3LogFlowable implements Runnable {

  public static final int BLOCKS_STEP = 100;
  public static final int WAIT_BETWEEN_BLOCKS = 5 * 1000;
  private final AtomicBoolean run = new AtomicBoolean(true);
  private final Web3Functions web3Functions;
  private final Map<String, BlockingQueue<Web3Model<Log>>> logConsumers;
  private final String network;
  private Integer from;
  private BigInteger lastBlock;
  private final Supplier<List<String>> addressesSupplier;
  private final Supplier<Long> blockLimitations;

  public Web3LogFlowable(
      Supplier<List<String>> addressesSupplier,
      Integer from,
      Web3Functions web3Functions,
      Map<String, BlockingQueue<Web3Model<Log>>> logConsumers,
      String network,
      Supplier<Long> blockLimitations
  ) {
    this.addressesSupplier = addressesSupplier;
    this.web3Functions = web3Functions;
    this.from = from;
    this.logConsumers = logConsumers;
    this.network = network;
    this.blockLimitations = blockLimitations;
  }

  public void stop() {
    run.set(false);
  }

  @SuppressWarnings("BusyWait")
  @Override
  public void run() {
    log.info("Start LogFlowable");
    BigInteger currentBlock;
    while (run.get()) {
      try {
        currentBlock = web3Functions.fetchCurrentBlock(network);
        if (lastBlock != null && lastBlock.intValue() >= currentBlock.intValue()) {
          Thread.sleep(WAIT_BETWEEN_BLOCKS);
          continue;
        }
        lastBlock = currentBlock;
        int to = currentBlock.intValue();
        if (from == null) {
          from = to;
        } else {
          int diff = to - from;
          if (diff > BLOCKS_STEP) {
            to = from + BLOCKS_STEP;
          }
        }

        while (true) {
          long blockLimit = blockLimitations.get();
          if (to > blockLimit) {
            log.info("Log flow wait limit... {} - {} = {}", to, blockLimit, to - blockLimit);
            Thread.sleep(5000);
          } else {
            break;
          }
        }

        //noinspection rawtypes
        List<EthLog.LogResult> logResults = web3Functions
            .fetchContractLogs(addressesSupplier.get(), from, to, network);
        log.info("Parse {} log from {} to {} on block: {} - {}", network, from, to,
            currentBlock, logResults.size());
        //noinspection rawtypes
        for (LogResult logResult : logResults) {
          Log ethLog = (Log) logResult.get();
          if (ethLog == null) {
            continue;
          }
          for (Entry<String, BlockingQueue<Web3Model<Log>>> queue : logConsumers.entrySet()) {
            writeInQueue(queue.getValue(), queue.getKey(), ethLog, logConsumers.size());
          }
        }
        from = to + 1;
      } catch (Exception e) {
        log.error("Error in log flow", e);
      }
    }
  }

  private <T> void writeInQueue(
      BlockingQueue<Web3Model<T>> queue, String name, T o, int queues) {
    try {
      Web3Model<T> model = new Web3Model<>(o, network);
      while (!queue.offer(model, 15, SECONDS)) {
        log.warn("The queue is full for {}, size {}. All queues this type {}",
            name, queue.size(), queues);
      }
    } catch (Exception e) {
      log.error("Error write in queue", e);
    }
  }
}
