package pro.belbix.ethparser.utils.download;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.Transaction;
import pro.belbix.ethparser.dto.v0.DeployerDTO;
import pro.belbix.ethparser.model.Web3Model;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.utils.LoopHandler;
import pro.belbix.ethparser.web3.Web3Functions;
import pro.belbix.ethparser.web3.contracts.ContractUtils;
import pro.belbix.ethparser.web3.deployer.db.DeployerDbService;
import pro.belbix.ethparser.web3.deployer.parser.DeployerEventToContractTransformer;
import pro.belbix.ethparser.web3.deployer.parser.DeployerTransactionsParser;

@Service
@Log4j2
public class DeployerTransactionsDownloader {

  private final Web3Functions web3Functions;
  private final DeployerDbService deployerDbService;
  private final DeployerTransactionsParser parser;
  private final AppProperties appProperties;
  private final DeployerEventToContractTransformer deployerEventToContractTransformer;
  private final BlockingQueue<Web3Model<Transaction>> transactionQueue = new ArrayBlockingQueue<>(
      100);

  @Value("${deployer-download.from:}")
  private Integer from;

  @Value("${deployer-download.to:}")
  private Integer to;

  public DeployerTransactionsDownloader(
      Web3Functions web3Functions,
      DeployerDbService deployerDbService,
      DeployerTransactionsParser parser,
      AppProperties appProperties,
      DeployerEventToContractTransformer deployerEventToContractTransformer) {
    this.web3Functions = web3Functions;
    this.deployerDbService = deployerDbService;
    this.parser = parser;
    this.appProperties = appProperties;
    this.deployerEventToContractTransformer = deployerEventToContractTransformer;
  }

  public void start() {
    if (from == null) {
      from = ContractUtils.getStartBlock(appProperties.getUtilNetwork());
    }
    if (to == null) {
      to = Integer.MAX_VALUE;
    }

    log.info("DeployerTransactionsDownloader start");
    new LoopHandler(300, this::parse).start(from, to);
  }

  private void parse(Integer start, Integer end) {
    web3Functions.findBlocksByBlockBatch(start, end, appProperties.getUtilNetwork())
        .forEach(block -> block.getTransactions().forEach(transactionResult -> {
              DeployerDTO dto = parser.parseDeployerTransaction(
                  (Transaction) transactionResult.get(), appProperties.getUtilNetwork());
              if (dto != null) {
                try {
                  deployerEventToContractTransformer.handleAndSave(dto);
                  deployerDbService.save(dto);
                } catch (Exception e) {
                  log.error("Can't save " + dto, e);
                }
              }
            }
        ));
  }

  public void setFrom(Integer from) {
    this.from = from;
  }

  public void setTo(Integer to) {
    this.to = to;
  }
}
