package pro.belbix.ethparser.web3;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.math.BigInteger;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.log4j.Log4j2;
import org.web3j.protocol.core.methods.response.EthBlock.Block;
import org.web3j.protocol.core.methods.response.Transaction;
import pro.belbix.ethparser.model.Web3Model;

@Log4j2
public class Web3TransactionFlowable implements Runnable {

  public static final int WAIT_BETWEEN_BLOCKS = 5 * 1000;
  private final AtomicBoolean run = new AtomicBoolean(true);
  private final Web3Functions web3Functions;
  private final List<BlockingQueue<Web3Model<Transaction>>> transactionConsumers;
  private final String network;
  private final int blockStep;
  private Integer from;
  private BigInteger lastBlock;
  private int lastParsedBlock = Integer.MAX_VALUE;

  public Web3TransactionFlowable(
      Integer from,
      Web3Functions web3Functions,
      List<BlockingQueue<Web3Model<Transaction>>> transactionConsumers,
      String network,
      int blockStep
  ) {
    this.web3Functions = web3Functions;
    this.from = from;
    this.transactionConsumers = transactionConsumers;
    this.network = network;
    this.blockStep = blockStep;
  }

  public void stop() {
    run.set(false);
  }

  @SuppressWarnings("BusyWait")
  @Override
  public void run() {
    log.info("Start Transaction Flowable");
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
          if (diff > blockStep) {
            to = from + blockStep;
          }
        }
        AtomicInteger counter = new AtomicInteger(0);
        web3Functions.findBlocksByBlockBatch(from, to, network)
            .sorted(Comparator.comparing(Block::getNumber))
            .forEach(block ->
                transactionConsumers.forEach(queue ->
                    block.getTransactions().forEach(t -> {
                      counter.incrementAndGet();
                      writeInQueue(queue, (Transaction) t.get(), transactionConsumers.size());
                    })
                )
            );
        lastParsedBlock = to;
        log.info("Parse {} transactions from {} to {} on block: {} - {}",
            network, from, to, currentBlock, counter.get());
        from = to + 1;
      } catch (Exception e) {
        log.error("Error in transaction flow", e);
      }
    }
  }

  private <T> void writeInQueue(BlockingQueue<Web3Model<T>> queue, T o, int queues) {
    try {
      Web3Model<T> model = new Web3Model<>(o, network);
      while (!queue.offer(model, 15, SECONDS)) {
        log.warn("The queue is full for transactions, size {}. All queues this type {}",
            queue.size(), queues);
      }
    } catch (Exception e) {
      log.error("Error write in queue", e);
    }
  }

  public int getLastParsedBlock() {
    return lastParsedBlock;
  }
}
