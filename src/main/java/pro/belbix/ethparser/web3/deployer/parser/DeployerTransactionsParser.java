package pro.belbix.ethparser.web3.deployer.parser;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import pro.belbix.ethparser.dto.DeployerDTO;
import pro.belbix.ethparser.dto.DtoI;
import pro.belbix.ethparser.model.DeployerTx;
import pro.belbix.ethparser.web3.EthBlockService;
import pro.belbix.ethparser.web3.ParserInfo;
import pro.belbix.ethparser.web3.Web3Parser;
import pro.belbix.ethparser.web3.Web3Service;
import pro.belbix.ethparser.web3.deployer.db.DeployerDbService;
import pro.belbix.ethparser.web3.deployer.decoder.DeployerDecoder;

import javax.annotation.PreDestroy;
import java.time.Instant;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static pro.belbix.ethparser.web3.Web3Service.LOG_LAST_PARSED_COUNT;

@Service
@Log4j2
public class DeployerTransactionsParser implements Web3Parser
{
    private static final String DEPLOYER_ADDRESS = "0xf00dD244228F51547f0563e60bCa65a30FBF5f7f";
    private static final AtomicBoolean run = new AtomicBoolean(true);
    private final Web3Service web3Service;
    private final DeployerDecoder deployerDecoder;
    private final BlockingQueue<Transaction> transactions = new ArrayBlockingQueue<>(100);
    private final BlockingQueue<DtoI> output = new ArrayBlockingQueue<>(100);
    private final DeployerDbService deployerDbService;
    private final EthBlockService ethBlockService;
    private final ParserInfo parserInfo;
    private long parsedTxCount = 0;
    private Instant lastTx = Instant.now();

    public DeployerTransactionsParser(Web3Service web3Service, DeployerDecoder deployerDecoder,
                                      DeployerDbService deployerDbService, EthBlockService ethBlockService,
                                      ParserInfo parserInfo)
    {
        this.web3Service = web3Service;
        this.deployerDecoder = deployerDecoder;
        this.deployerDbService = deployerDbService;
        this.ethBlockService = ethBlockService;
        this.parserInfo = parserInfo;
    }

    public void startParse()
    {
        log.info("Start parse Deployer transactions");
        parserInfo.addParser(this);
        web3Service.subscribeOnTransactions(transactions);
        new Thread(() ->
        {
            while (run.get())
            {
                Transaction transaction = null;
                try
                {
                    transaction = transactions.poll(1, TimeUnit.SECONDS);
                }
                catch (InterruptedException ignored)
                {
                }
                DeployerDTO dto = parseDeployerTransaction(transaction);
                if (dto != null)
                {
                    lastTx = Instant.now();
                    try
                    {
                        boolean success = deployerDbService.save(dto);
                        if (success)
                        {
                            output.put(dto);
                        }
                    }
                    catch (Exception e)
                    {
                        log.error("Can't save " + dto.toString(), e);
                    }
                }
            }
        }).start();
    }

    public DeployerDTO parseDeployerTransaction(Transaction tx)
    {
        DeployerDTO deployerDTO = null;
        if (tx != null)
        {
            incrementAndPrintCount(tx);
            DeployerTx deployerTx = decodeTransaction(tx);
            if (deployerTx != null)
            {
                deployerDTO = deployerTx.toDto();
                deployerDTO.setBlockDate(ethBlockService.getTimestampSecForBlock(tx.getBlockHash(),
                        tx.getBlockNumber().longValue()));
                print(deployerDTO);
            }
        }
        return deployerDTO;
    }

    private void incrementAndPrintCount(Transaction tx)
    {
        parsedTxCount++;
        if (parsedTxCount % LOG_LAST_PARSED_COUNT == 0)
        {
            log.info("Deployer parsed " + parsedTxCount + ", last block: " + tx.getBlockNumber());
        }
    }

    private DeployerTx decodeTransaction(Transaction tx)
    {
        DeployerTx deployerTx = null;
        try
        {
            if (isValidTransaction(tx))
            {
                if (tx.getTo() == null)
                {
                    // contract creation
                    deployerTx = (DeployerTx) deployerDecoder.mapTypesToModel(null, "CONTRACT_CREATION", tx);
                }
                else if ("0x".equalsIgnoreCase(tx.getInput()))
                {
                    // no data, probably sending eth
                    deployerTx = (DeployerTx) deployerDecoder.mapTypesToModel(null, "NO_INPUT_DATA", tx);
                }
                else
                {
                    // everything else
                    try
                    {
                        deployerTx = (DeployerTx) deployerDecoder.decodeInputData(tx);
                    }
                    catch (IllegalStateException ise)
                    {
                        // unknown tx
                        deployerTx = (DeployerTx) deployerDecoder.mapTypesToModel(null, tx.getInput().substring(0, 10), tx);
                        log.warn("Unknown tx methodId: " + deployerTx.getMethodName() + " hash: " + deployerTx.getHash());
                    }
                }
                TransactionReceipt transactionReceipt = web3Service.fetchTransactionReceipt(tx.getHash());
                deployerTx.setGasUsed(transactionReceipt.getGasUsed());
                deployerTx.setSuccess("0x1".equalsIgnoreCase(transactionReceipt.getStatus()));
            }
        }
        catch (Exception e)
        {
            log.error("Error tx " + tx.getHash(), e);
        }
        return deployerTx;
    }

    private boolean isValidTransaction(Transaction tx)
    {
        // If deployer address ever changes -- supply a list and check here
        return DEPLOYER_ADDRESS.equalsIgnoreCase(tx.getFrom());
    }

    private void print(DeployerDTO deployerDTO)
    {
        if (deployerDTO.isConfirmed())
        {
            log.info(deployerDTO.print() + " " + deployerDTO);
        }
        else
        {
            log.debug(deployerDTO.print() + " " + deployerDTO);
        }
    }

    public BlockingQueue<DtoI> getOutput()
    {
        return output;
    }

    @PreDestroy
    public void stop()
    {
        run.set(false);
    }

    @Override
    public Instant getLastTx()
    {
        return lastTx;
    }
}
