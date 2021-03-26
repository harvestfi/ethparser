package pro.belbix.ethparser.web3.layers.blocks.downloader;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.EthBlock;
import pro.belbix.ethparser.entity.a_layer.EthBlockEntity;
import pro.belbix.ethparser.web3.Web3Service;
import pro.belbix.ethparser.web3.layers.blocks.db.EthBlockDbService;
import pro.belbix.ethparser.web3.layers.blocks.parser.EthBlockParser;

@Service
@Log4j2
public class EthBlockDownloader {

  private final Web3Service web3Service;
  private final EthBlockDbService ethBlockDbService;
  private final EthBlockParser ethBlockParser;

  @Value("${block-download.from:}")
  private Integer from;
  @Value("${block-download.to:}")
  private Integer to;

  AtomicInteger count = new AtomicInteger(0);
  AtomicBoolean run = new AtomicBoolean(true);

  public EthBlockDownloader(Web3Service web3Service,
      EthBlockDbService ethBlockDbService,
      EthBlockParser ethBlockParser) {
    this.web3Service = web3Service;
    this.ethBlockDbService = ethBlockDbService;
    this.ethBlockParser = ethBlockParser;
  }

    public void start() {
        if (from == null) {
            log.error("From parameter is required");
            return;
        }
        AtomicLong blockNumber = new AtomicLong(from.longValue());
        while (run.get()) {
            parseBlockAndSave(blockNumber.get());
            if (to != null && to >= blockNumber.get()) {
                break;
            }
            blockNumber.incrementAndGet();
        }
    }

    private void parseBlockAndSave(long block) {
      Instant timer = Instant.now();

      EthBlock ethBlock = web3Service.findBlockByNumber(block, true);
      log.debug("Fetched via web3 {} {}", block,
          Duration.between(timer, Instant.now()).toMillis());
      timer = Instant.now();
      EthBlockEntity ethBlockEntity = ethBlockParser.parse(ethBlock);
      if (ethBlockEntity == null) {
        return;
      }
      log.debug("Parsed {} {}", block,
          Duration.between(timer, Instant.now()).toMillis());

      final long blockNum = ethBlockEntity.getNumber();
      final Instant taskTimer = Instant.now();
      try {
        var persistedBlock = ethBlockDbService.save(ethBlockEntity);
        long dur = Duration.between(taskTimer, Instant.now()).toMillis();
        if (persistedBlock != null) {
          log.info("Handled {}. Saved block {} for {}",
              count.get(), blockNum, dur);
        } else {
          log.info("Handled {}. Block have not saved {} for {}",
              count.get(), blockNum, dur);
        }

        count.incrementAndGet();
      } catch (Exception e) {
        log.error("Error save {}", blockNum, e);
        run.set(false);
      }
    }

}
