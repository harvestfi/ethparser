package pro.belbix.ethparser.web3.deployer.parser;

import static pro.belbix.ethparser.web3.deployer.decoder.DeployerActivityEnum.CONTRACT_CREATION;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import pro.belbix.ethparser.codegen.FunctionWrapper;
import pro.belbix.ethparser.codegen.GeneratedContract;
import pro.belbix.ethparser.codegen.SimpleContractGenerator;
import pro.belbix.ethparser.dto.v0.DeployerDTO;
import pro.belbix.ethparser.model.tx.DeployerTx;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.properties.NetworkProperties;
import pro.belbix.ethparser.repositories.ErrorsRepository;
import pro.belbix.ethparser.web3.EthBlockService;
import pro.belbix.ethparser.web3.ParserInfo;
import pro.belbix.ethparser.web3.Web3Functions;
import pro.belbix.ethparser.web3.Web3Parser;
import pro.belbix.ethparser.web3.Web3Subscriber;
import pro.belbix.ethparser.web3.deployer.db.DeployerDbService;
import pro.belbix.ethparser.web3.deployer.decoder.DeployerDecoder;
import pro.belbix.ethparser.web3.deployer.transform.DeployerEventToContractTransformer;

@Service
@Log4j2
public class DeployerTransactionsParser extends Web3Parser<DeployerDTO, Transaction> {

  public static final int LOG_LAST_PARSED_COUNT = 100_000;
  private final DeployerDecoder deployerDecoder = new DeployerDecoder();
  private final DeployerDbService deployerDbService;
  private final EthBlockService ethBlockService;
  private final SimpleContractGenerator simpleContractGenerator;
  private final Web3Functions web3Functions;
  private final DeployerEventToContractTransformer deployerEventToContractTransformer;
  private final NetworkProperties networkProperties;
  private final Web3Subscriber web3Subscriber;
  private long parsedTxCount = 0;

  public DeployerTransactionsParser(
      DeployerDbService deployerDbService,
      EthBlockService ethBlockService,
      AppProperties appProperties, ParserInfo parserInfo,
      SimpleContractGenerator simpleContractGenerator,
      Web3Functions web3Functions,
      DeployerEventToContractTransformer deployerEventToContractTransformer,
      NetworkProperties networkProperties,
      Web3Subscriber web3Subscriber,
      ErrorsRepository errorsRepository) {
    super(parserInfo, appProperties, errorsRepository);
    this.deployerDbService = deployerDbService;
    this.ethBlockService = ethBlockService;
    this.simpleContractGenerator = simpleContractGenerator;
    this.web3Functions = web3Functions;
    this.deployerEventToContractTransformer = deployerEventToContractTransformer;
    this.networkProperties = networkProperties;
    this.web3Subscriber = web3Subscriber;
  }

  @Override
  protected void subscribeToInput() {
    web3Subscriber.subscribeOnTransactions(input);
  }

  @Override
  protected boolean save(DeployerDTO dto) {
    try {
      deployerEventToContractTransformer.handleAndSave(dto);
      return deployerDbService.save(dto);
    } catch (Exception e) {
      log.error("Can't save " + dto.toString(), e);
    }
    return false;
  }

  @Override
  protected boolean isActiveForNetwork(String network) {
    return networkProperties.get(network)
        .isParseDeployerTransactions();
  }

  @Override
  public DeployerDTO parse(Transaction tx, String network) {
    if (tx == null) {
      return null;
    }

    incrementAndPrintCount(tx);
    DeployerTx deployerTx = deployerDecoder.decodeTransaction(tx, network);
    if (deployerTx == null) {
      return null;
    }
    TransactionReceipt transactionReceipt =
        web3Functions.fetchTransactionReceipt(tx.getHash(), network);
    deployerTx.setGasUsed(transactionReceipt.getGasUsed());
    deployerTx.setSuccess("0x1".equalsIgnoreCase(transactionReceipt.getStatus()));

    DeployerDTO dto = deployerTx.toDto(network);
    dto.setBlockDate(
        ethBlockService.getTimestampSecForBlock(tx.getBlockNumber().longValue(), network));

    if (CONTRACT_CREATION.name().equals(dto.getType())) {
      dto.setToAddress(transactionReceipt.getContractAddress());
    }
    GeneratedContract generatedContract = simpleContractGenerator.getContract(
        dto.getToAddress(), null, null, network);
    if (generatedContract != null) {
      dto.setName(generatedContract.getName());
      if (dto.getMethodName().startsWith("0x")) {
        FunctionWrapper functionWrapper = generatedContract.getFunction(dto.getMethodName());
        if (functionWrapper != null) {
          dto.setMethodName(functionWrapper.getFunction().getName());
        }
      }
    }

    print(dto);

    return dto;
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

}
