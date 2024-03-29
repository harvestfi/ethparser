package pro.belbix.ethparser.web3;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.PreDestroy;
import lombok.extern.log4j.Log4j2;
import pro.belbix.ethparser.dto.DtoI;
import pro.belbix.ethparser.model.Web3Model;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.web3.contracts.db.ErrorDbService;

@Log4j2
public abstract class Web3Parser<T extends DtoI, K> {

  public static final int INPUT_QUEUE_SIZE = 10;
  public static final int OUTPUT_OUTPUT_SIZE = 10;

  protected final BlockingQueue<Web3Model<K>> input = new ArrayBlockingQueue<>(INPUT_QUEUE_SIZE);
  protected final BlockingQueue<T> output = new ArrayBlockingQueue<>(OUTPUT_OUTPUT_SIZE);
  protected static final AtomicBoolean run = new AtomicBoolean(true);
  protected Instant lastTx = Instant.now();
  private int emptyMessageCount = 0;

  private final ParserInfo parserInfo;
  protected final AppProperties appProperties;

  private final ErrorDbService errorDbService;

  protected Web3Parser(ParserInfo parserInfo,
      AppProperties appProperties,
      ErrorDbService errorDbService) {
    this.parserInfo = parserInfo;
    this.appProperties = appProperties;
    this.errorDbService = errorDbService;
  }


  public void startParse() {
    parserInfo.addParser(this);
    subscribeToInput();
    new Thread(() -> {
      while (run.get()) {
        Web3Model<K> web3Model = null;
        try {
          web3Model = input.poll(5, TimeUnit.SECONDS);
          if (web3Model == null) {
            incrementAndPrintEmptyCount();
          }
          if (web3Model == null || !isActiveForNetwork(web3Model.getNetwork())) {
            continue;
          }
          Instant startParse = Instant.now();
          T dto = parse(web3Model.getValue(), web3Model.getNetwork());
          if (dto != null && run.get()) {
            log.trace("Web3Object for {} parsed by {}",
                getClass().getSimpleName(), Duration.between(startParse, Instant.now()).toMillis());
            lastTx = Instant.now();
            if (save(dto) && run.get()) {
              log.trace("Web3Object for {} persisted by {}",
                  getClass().getSimpleName(), Duration.between(lastTx, Instant.now()).toMillis());
              sendToWs(dto);
            }
          }
        } catch (Exception e) {
          // don't show errors after shutdown
          if (run.get()) {
            log.error("Error in loop {} with {}",
                this.getClass().getSimpleName(), web3Model, e);
            errorDbService.saveErrorWeb3ModelToDb(web3Model, getClass().getSimpleName());
          } else {
            log.debug("After shutdown - Error in loop {} with {}",
                this.getClass().getSimpleName(), web3Model, e);
          }
          if (appProperties.isStopOnParseError()) {
            System.exit(-1);
          }
        }
      }
    }).start();
  }

  private void incrementAndPrintEmptyCount() {
    emptyMessageCount++;
    log.trace("Handled {} empty messages fro {}",
        emptyMessageCount, this.getClass().getSimpleName());
  }


  protected abstract void subscribeToInput();

  public abstract T parse(K ethObject, String network);

  protected abstract boolean save(T dto);

  protected void sendToWs(T dto) {
    try {
      while (run.get()) {
        boolean recorded = output.offer(dto, 5, TimeUnit.SECONDS);
        if (recorded) {
          log.trace("Put dto to WS {}", dto);
          break;
        }
        log.warn("Output queue is full for {}", this.getClass().getSimpleName());
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  protected abstract boolean isActiveForNetwork(String network);

  public BlockingQueue<T> getOutput() {
    return output;
  }

  @PreDestroy
  public void stop() {
    run.set(false);
  }

  public Instant getLastTx() {
    return lastTx;
  }
}
