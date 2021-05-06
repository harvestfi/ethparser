package pro.belbix.ethparser.web3;

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

@Log4j2
public abstract class Web3Parser<T extends DtoI, K> {

  public static final int INPUT_QUEUE_SIZE = 10;
  public static final int OUTPUT_OUTPUT_SIZE = 10;

  protected final BlockingQueue<Web3Model<K>> input = new ArrayBlockingQueue<>(INPUT_QUEUE_SIZE);
  protected final BlockingQueue<T> output = new ArrayBlockingQueue<>(OUTPUT_OUTPUT_SIZE);
  protected static final AtomicBoolean run = new AtomicBoolean(true);
  protected Instant lastTx = Instant.now();

  private final ParserInfo parserInfo;
  protected final AppProperties appProperties;

  protected Web3Parser(ParserInfo parserInfo,
      AppProperties appProperties) {
    this.parserInfo = parserInfo;
    this.appProperties = appProperties;
  }


  public void startParse() {
    parserInfo.addParser(this);
    subscribeToInput();
    new Thread(() -> {
      while (run.get()) {
        Web3Model<K> web3Model = null;
        try {
          web3Model = input.poll(5, TimeUnit.SECONDS);
          if (web3Model == null || !isActiveForNetwork(web3Model.getNetwork())) {
            continue;
          }
          T dto = parse(web3Model.getValue(), web3Model.getNetwork());
          if (dto != null && run.get()) {
            lastTx = Instant.now();
            if (save(dto) && run.get()) {
              sendToWs(dto);
            }
          }
        } catch (Exception e) {
          log.error("Error in loop {} with {}",
              this.getClass().getSimpleName(), web3Model, e);
          if (appProperties.isStopOnParseError()) {
            System.exit(-1);
          }
        }
      }
    }).start();
  }

  protected abstract void subscribeToInput();

  public abstract T parse(K ethObject, String network);

  protected abstract boolean save(T dto);

  private void sendToWs(T dto) {
    try {
      while (!output.offer(dto, 5, TimeUnit.SECONDS) && run.get()) {
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
