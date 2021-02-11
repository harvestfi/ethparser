package pro.belbix.ethparser.web3.deployer.downloader;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.Transaction;
import pro.belbix.ethparser.dto.DeployerDTO;
import pro.belbix.ethparser.web3.Web3Service;
import pro.belbix.ethparser.web3.deployer.db.DeployerDbService;
import pro.belbix.ethparser.web3.deployer.parser.DeployerTransactionsParser;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

@Service
@Log4j2
public class DeployerTransactionsDownloader
{
    private final Web3Service web3Service;
    private final DeployerDbService deployerDbService;
    private final DeployerTransactionsParser parser;
    private final BlockingQueue<Transaction> transactions = new ArrayBlockingQueue<>(100);

    @Value("${deployer-download.from:}")
    private Integer from;
    @Value("${deployer-download.to:}")
    private Integer to;

    public DeployerTransactionsDownloader(Web3Service web3Service, DeployerDbService deployerDbService,
                                          DeployerTransactionsParser parser)
    {
        this.web3Service = web3Service;
        this.deployerDbService = deployerDbService;
        this.parser = parser;
    }

    public void start()
    {
        if (from == null || to == null || from > to)
        {
            log.error("There is an issue with the block range specified: " + from + " - " + to);
            return;
        }
        log.info("DeployerTransactionsDownloader start");
        parse(from, to);
    }

    private void parse(Integer start, Integer end)
    {
        web3Service.subscribeTransactionFlowableRange(start, end);
        web3Service.subscribeOnTransactions(transactions);
        while (true)
        {
            Transaction transaction = null;
            try
            {
                transaction = transactions.poll(1, TimeUnit.SECONDS);
            }
            catch (InterruptedException ignored)
            {
            }
            DeployerDTO dto = parser.parseDeployerTransaction(transaction);
            if (dto != null)
            {
                try
                {
                    deployerDbService.save(dto);
                }
                catch (Exception e)
                {
                    log.error("Can't save " + dto.toString(), e);
                }
            }
            if (web3Service.isLogFlowableRangeComplete())
            {
                web3Service.shutdown();
                return;
            }
        }
    }

    public void setFrom(Integer from)
    {
        this.from = from;
    }

    public void setTo(Integer to)
    {
        this.to = to;
    }
}
