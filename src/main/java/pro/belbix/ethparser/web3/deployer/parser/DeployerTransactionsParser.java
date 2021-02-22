package pro.belbix.ethparser.web3.deployer.parser;

import static pro.belbix.ethparser.web3.Web3Service.LOG_LAST_PARSED_COUNT;

import java.time.Instant;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.PreDestroy;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.Transaction;
import pro.belbix.ethparser.dto.DeployerDTO;
import pro.belbix.ethparser.dto.DtoI;
import pro.belbix.ethparser.model.DeployerTx;
import pro.belbix.ethparser.web3.EthBlockService;
import pro.belbix.ethparser.web3.ParserInfo;
import pro.belbix.ethparser.web3.Web3Parser;
import pro.belbix.ethparser.web3.Web3Service;
import pro.belbix.ethparser.web3.deployer.db.DeployerDbService;
import pro.belbix.ethparser.web3.deployer.decoder.DeployerDecoder;

@Service
@Log4j2
public class DeployerTransactionsParser implements Web3Parser {
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

  public DeployerTransactionsParser(
      Web3Service web3Service,
      DeployerDecoder deployerDecoder,
      DeployerDbService deployerDbService,
      EthBlockService ethBlockService,
      ParserInfo parserInfo) {
    this.web3Service = web3Service;
    this.deployerDecoder = deployerDecoder;
    this.deployerDbService = deployerDbService;
    this.ethBlockService = ethBlockService;
    this.parserInfo = parserInfo;
  }

  public void startParse() {
    log.info("Start parse Deployer transactions");
    parserInfo.addParser(this);
    web3Service.subscribeOnTransactions(transactions);
    new Thread(
            () -> {
              while (run.get()) {
                Transaction transaction = null;
                try {
                  transaction = transactions.poll(1, TimeUnit.SECONDS);
                } catch (InterruptedException ignored) {
                }
                DeployerDTO dto = parseDeployerTransaction(transaction);
                if (dto != null) {
                  lastTx = Instant.now();
                  try {
                    boolean success = deployerDbService.save(dto);
                    if (success) {
                      output.put(dto);
                    }
                  } catch (Exception e) {
                    log.error("Can't save " + dto.toString(), e);
                  }
                }
              }
            })
        .start();
  }

  public DeployerDTO parseDeployerTransaction(Transaction tx) {
    DeployerDTO deployerDTO = null;
    if (tx != null) {
      incrementAndPrintCount(tx);
      DeployerTx deployerTx = deployerDecoder.decodeTransaction(tx);
      if (deployerTx != null) {
        deployerDTO = deployerTx.toDto();
        deployerDTO.setBlockDate(
            ethBlockService.getTimestampSecForBlock(
                tx.getBlockHash(), tx.getBlockNumber().longValue()));
        print(deployerDTO);
      }
    }
    return deployerDTO;
  }

  private void incrementAndPrintCount(Transaction tx) {
    parsedTxCount++;
    if (parsedTxCount % LOG_LAST_PARSED_COUNT == 0) {
      log.info("Deployer parsed " + parsedTxCount + ", last block: " + tx.getBlockNumber());
    }
  }

  private void print(DeployerDTO deployerDTO) {
    if (deployerDTO.getConfirmed() == 1) {
      log.info(deployerDTO.print() + " " + deployerDTO);
    } else {
      log.debug(deployerDTO.print() + " " + deployerDTO);
    }
  }

  public BlockingQueue<DtoI> getOutput() {
    return output;
  }

  @PreDestroy
  public void stop() {
    run.set(false);
  }

  @Override
  public Instant getLastTx() {
    return lastTx;
  }
}
