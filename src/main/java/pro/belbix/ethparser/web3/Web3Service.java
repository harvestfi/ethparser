package pro.belbix.ethparser.web3;

import io.reactivex.disposables.Disposable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.Response.Error;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import pro.belbix.ethparser.properties.Web3Properties;

@Service
public class Web3Service {

    private static final Logger log = LoggerFactory.getLogger(Web3Service.class);
    private Web3j web3;
    private final Set<Disposable> subscriptions = new HashSet<>();
    private final Web3Properties web3Properties;
    private boolean init = false;
    private final List<BlockingQueue<Transaction>> consumers = new ArrayList<>();

    public Web3Service(Web3Properties web3Properties) {
        this.web3Properties = web3Properties;
    }

    @PostConstruct
    public void init() {
        log.info("Connecting to Ethereum ...");
        web3 = Web3j.build(new HttpService("https://mainnet.infura.io/v3/" + web3Properties.getApiKey()));
        log.info("Successfully connected to Ethereum");
        init = true;
    }

    public void subscribeTransactionFlowable() {
        checkInit();
        Disposable subscription = web3.transactionFlowable()
            .subscribe(tx -> consumers.forEach(b -> {
                try {
                    b.put(tx);
                } catch (InterruptedException ignored) {
                }
            }));
        subscriptions.add(subscription);

        log.info("Subscribe to Transaction Flowable");
    }

    public TransactionReceipt fetchTransactionReceipt(String hash) {
        checkInit();
        try {
            EthGetTransactionReceipt ethGetTransactionReceipt = web3.ethGetTransactionReceipt(hash).send();
            Error error = ethGetTransactionReceipt.getError();
            if (error != null) {
                log.error("Got " + error.getCode() + " " + error.getMessage() + " " + error.getData());
            } else {
                return ethGetTransactionReceipt.getTransactionReceipt().orElse(null);
            }
        } catch (IOException e) {
            log.error("", e);
        }

        return null;
    }

    public Transaction findTransaction(String hash) throws IOException {
        return web3.ethGetTransactionByHash(hash).send().getTransaction().orElse(null);
    }

    public void subscribeOn(BlockingQueue<Transaction> queue) {
        consumers.add(queue);
    }

    @PreDestroy
    private void close() {
        log.info("Close web3");
        subscriptions.forEach(Disposable::dispose);
        web3.shutdown();
    }

    private void checkInit() {
        while (!init) {
            log.info("Wait initialization...");
            try {
                //noinspection BusyWait
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
        }
    }

}
