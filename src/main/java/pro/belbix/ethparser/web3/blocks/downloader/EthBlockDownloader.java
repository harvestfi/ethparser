package pro.belbix.ethparser.web3.blocks.downloader;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.EthBlock;
import pro.belbix.ethparser.entity.a_layer.EthBlockEntity;
import pro.belbix.ethparser.web3.Web3Service;
import pro.belbix.ethparser.web3.blocks.db.EthBlockDbService;
import pro.belbix.ethparser.web3.blocks.parser.EthBlockParser;

@Service
@Log4j2
public class EthBlockDownloader {

    private static final int MAX_TASKS = 10;

    private final Web3Service web3Service;
    private final EthBlockDbService ethBlockDbService;
    private final EthBlockParser ethBlockParser;

    @Value("${block-download.from:}")
    private Integer from;
    @Value("${block-download.to:}")
    private Integer to;

    ThreadPoolExecutor executor = new ThreadPoolExecutor(
        0,
        MAX_TASKS,
        1L, TimeUnit.SECONDS,
        new SynchronousQueue<>()
    );
    AtomicInteger count = new AtomicInteger(0);

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
        while (true) {
            parseAndSave(blockNumber.get());
            if (to != null && to >= blockNumber.get()) {
                break;
            }
            blockNumber.incrementAndGet();
        }
    }

    private void parseAndSave(long block) {
        Instant timer = Instant.now();

        EthBlock ethBlock =
            web3Service.findBlockByNumber(block, true);
        log.debug("Fetched via web3 {} {}", block,
            Duration.between(timer, Instant.now()).toMillis());
        timer = Instant.now();
        EthBlockEntity ethBlockEntity = ethBlockParser.parse(ethBlock);
        log.debug("Parsed {} {}", block,
            Duration.between(timer, Instant.now()).toMillis());

        save(ethBlockEntity);
    }

    private void save(EthBlockEntity ethBlockEntity) {
        waitFreeExecutors(executor);
        executor.submit(() -> {
            Thread.currentThread().setName(ethBlockEntity.getNumber() + " worker");
            try {
                Instant taskTimer = Instant.now();
                if (ethBlockDbService.save(ethBlockEntity) != null) {
                    log.info("{} Saved block {} {}", count.get(), ethBlockEntity.getNumber(),
                        Duration.between(taskTimer, Instant.now()).toMillis());
                } else {
                    log.info("{} Block have not saved {} {}", count.get(), ethBlockEntity.getNumber(),
                        Duration.between(taskTimer, Instant.now()).toMillis());
                }
            } catch (Exception e) {
                log.error("Error save {}", ethBlockEntity.getNumber(), e);
                parseAndSave(ethBlockEntity.getNumber());
            }
            count.incrementAndGet();
        });
    }

    private void waitFreeExecutors(ThreadPoolExecutor executor) {
        log.debug("Active executors {}", executor.getActiveCount());
        while (executor.getActiveCount() == MAX_TASKS) {
            log.info("Task queue is full, wait a second");
            try {
                //noinspection BusyWait
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
        }
    }


}
