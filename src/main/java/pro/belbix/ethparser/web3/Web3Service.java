package pro.belbix.ethparser.web3;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.Response.Error;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthBlock.Block;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import pro.belbix.ethparser.properties.Web3Properties;

@Service
public class Web3Service {

    public static final DefaultBlockParameter BLOCK_NUMBER_30_AUGUST_2020 = DefaultBlockParameter
        .valueOf(new BigInteger("10765094"));
    public static final DefaultBlockParameter BLOCK_NUMBER_27_OCT_2020 = DefaultBlockParameter
        .valueOf(new BigInteger("11137613"));
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
    private void init() {
        log.info("Connecting to Ethereum ...");
        String url;
        if (Strings.isBlank(web3Properties.getWeb3Url())) {
            url = System.getProperty("ethjava.web3.url");
        } else {
            url = web3Properties.getWeb3Url();
        }
        if (url == null) {
            throw new IllegalStateException("Web3 url not defined");
        }

        web3 = Web3j.build(new HttpService(url));
        log.info("Successfully connected to Ethereum");
        init = true;
    }

    public void subscribeTransactionFlowable() {
        checkInit();
        Flowable<Transaction> flowable;
        if (web3Properties.getStartBlock() == null || web3Properties.getStartBlock().isEmpty()) {
            flowable = web3.transactionFlowable();
        } else {
            log.info("Start flow from block " + web3Properties.getStartBlock());
            flowable = web3.replayPastAndFutureTransactionsFlowable(
                DefaultBlockParameter.valueOf(new BigInteger(web3Properties.getStartBlock())));
        }
        Disposable subscription = flowable
            .subscribe(tx -> consumers.forEach(queue -> {
                try {
                    queue.put(tx);
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

    public Block findBlock(String blockHash) {
        EthBlock ethBlock;
        try {
            ethBlock = web3.ethGetBlockByHash(blockHash, false).send();
        } catch (IOException e) {
            log.error("Error get block " + blockHash, e);
            return null;
        }
        if (ethBlock != null && ethBlock.getError() != null) {
            log.error("Error fetching block " + ethBlock.getError().getMessage());
            return null;
        }

        if (ethBlock != null) {
            return ethBlock.getBlock();
        }
        return null;
    }

    public double fetchAverageGasPrice() {
        EthGasPrice gasPrice;
        try {
            gasPrice = web3.ethGasPrice().send();
        } catch (IOException e) {
            log.error("Error fetch gas", e);
            return 0.0;
        }
        if (gasPrice != null && gasPrice.getError() != null) {
            log.error("Error gas fetching " + gasPrice.getError().getMessage());
            return 0.0;
        }
        if (gasPrice != null) {
            return gasPrice.getGasPrice().doubleValue() / 1000_000_000;
        }
        return 0.0;
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
