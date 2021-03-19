package pro.belbix.ethparser.web3.layers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.entity.a_layer.EthBlockEntity;
import pro.belbix.ethparser.web3.layers.blocks.parser.EthBlockParser;

@Service
@Log4j2
public class SubscriptionRouter {

    private final AtomicBoolean run = new AtomicBoolean(true);
    private final List<BlockingQueue<EthBlockEntity>> blockConsumers = new ArrayList<>();

    private final EthBlockParser ethBlockParser;

    public SubscriptionRouter(EthBlockParser ethBlockParser) {
        this.ethBlockParser = ethBlockParser;
    }

    @PostConstruct
    private void init() {
        distributeQueue(ethBlockParser.getOutput(), blockConsumers);
    }

    public void subscribeOnBlocks(BlockingQueue<EthBlockEntity> consumer) {
        blockConsumers.add(consumer);
    }

    private <T> void distributeQueue(BlockingQueue<T> producer, List<BlockingQueue<T>> consumers) {
        new Thread(() -> {
            int errorCount = 0;
            while (run.get()) {
                try {
                    T obj = producer.poll(1, TimeUnit.SECONDS);
                    if (obj != null) {
                        consumers.forEach(c -> {
                            try {
                                c.put(obj);
                            } catch (InterruptedException ignored) {
                            }
                        });
                    }
                    errorCount = 0;
                } catch (Exception e) {
                    log.error("Error distribute", e);
                    errorCount++;
                    if (errorCount > 10) {
                        break;
                    }
                }
            }
        }).start();
    }

}
