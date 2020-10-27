package pro.belbix.ethparser.web3;

import static org.web3j.protocol.core.DefaultBlockParameterName.EARLIEST;
import static org.web3j.protocol.core.DefaultBlockParameterName.LATEST;

import io.reactivex.disposables.Disposable;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.http.HttpService;
import pro.belbix.ethparser.properties.Web3Properties;

@Service
public class Web3Service {

    public static final DefaultBlockParameter BLOCK_NUMBER = DefaultBlockParameter.valueOf(new BigInteger("11044276"));

    private static final Logger log = LoggerFactory.getLogger(Web3Service.class);
    private Web3j web3;
    private final Set<Disposable> subscriptions = new HashSet<>();
    private final BlockingQueue<Transaction> transactions = new ArrayBlockingQueue<>(10_000);
    private final Web3Properties web3Properties;
    private boolean init = false;

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
            .subscribe(transactions::put);
        subscriptions.add(subscription);

        log.info("Subscribe to Transaction Flowable");
    }

    public void fetchEvents(String hash) {
        checkInit();
        EthFilter filter = new EthFilter(EARLIEST, LATEST, hash);
        List<LogResult> logResults;
        try {
            logResults = web3.ethGetLogs(filter).send().getLogs();
        } catch (IOException e) {
            log.error("", e);
        }

    }

    public BlockingQueue<Transaction> getTransactions() {
        return transactions;
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
                Thread.sleep(1000);
            } catch (InterruptedException e) {}
        }
    }

}
