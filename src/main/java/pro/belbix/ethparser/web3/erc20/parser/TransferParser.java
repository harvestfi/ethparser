package pro.belbix.ethparser.web3.erc20.parser;

import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.BALANCE_OF;

import java.math.BigInteger;
import java.time.Instant;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.PreDestroy;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.Transaction;
import pro.belbix.ethparser.dto.DtoI;
import pro.belbix.ethparser.dto.v0.TransferDTO;
import pro.belbix.ethparser.entity.contracts.ContractEntity;
import pro.belbix.ethparser.model.Web3Model;
import pro.belbix.ethparser.model.tx.TokenTx;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.properties.NetworkProperties;
import pro.belbix.ethparser.web3.EthBlockService;
import pro.belbix.ethparser.web3.ParserInfo;
import pro.belbix.ethparser.web3.Web3Functions;
import pro.belbix.ethparser.web3.Web3Parser;
import pro.belbix.ethparser.web3.Web3Subscriber;
import pro.belbix.ethparser.web3.abi.FunctionsUtils;
import pro.belbix.ethparser.web3.contracts.ContractConstants;
import pro.belbix.ethparser.web3.contracts.db.ContractDbService;
import pro.belbix.ethparser.web3.erc20.TransferType;
import pro.belbix.ethparser.web3.erc20.db.TransferDBService;
import pro.belbix.ethparser.web3.erc20.decoder.ERC20Decoder;
import pro.belbix.ethparser.web3.prices.PriceProvider;

@Service
@Log4j2
public class TransferParser implements Web3Parser {
  private static final AtomicBoolean run = new AtomicBoolean(true);
  private final BlockingQueue<Web3Model<Log>> logs = new ArrayBlockingQueue<>(100);
  private final BlockingQueue<DtoI> output = new ArrayBlockingQueue<>(100);
  private final ERC20Decoder erc20Decoder = new ERC20Decoder();
  private final Web3Functions web3Functions;
  private final Web3Subscriber web3Subscriber;
  private final EthBlockService ethBlockService;
  private final ParserInfo parserInfo;
  private final TransferDBService transferDBService;
  private final PriceProvider priceProvider;
  private final FunctionsUtils functionsUtils;
  private final AppProperties appProperties;
  private final NetworkProperties networkProperties;
  private final ContractDbService contractDbService;
  private Instant lastTx = Instant.now();

  public TransferParser(Web3Functions web3Functions,
      Web3Subscriber web3Subscriber, EthBlockService ethBlockService,
      ParserInfo parserInfo,
      TransferDBService transferDBService,
      PriceProvider priceProvider,
      FunctionsUtils functionsUtils, AppProperties appProperties,
      NetworkProperties networkProperties,
      ContractDbService contractDbService) {
    this.web3Functions = web3Functions;
    this.web3Subscriber = web3Subscriber;
    this.ethBlockService = ethBlockService;
    this.parserInfo = parserInfo;
    this.transferDBService = transferDBService;
    this.priceProvider = priceProvider;
    this.functionsUtils = functionsUtils;
    this.appProperties = appProperties;
    this.networkProperties = networkProperties;
    this.contractDbService = contractDbService;
  }

  @Override
  public void startParse() {
    log.info("Start parse Token info logs");
    parserInfo.addParser(this);
    web3Subscriber.subscribeOnLogs(logs, this.getClass().getSimpleName());
    new Thread(() -> {
      while (run.get()) {
        Web3Model<Log> ethLog = null;
        try {
          ethLog = logs.poll(1, TimeUnit.SECONDS);
          if (ethLog == null
              || !networkProperties.get(ethLog.getNetwork())
              .isParseTransfers()) {
            continue;
          }
          TransferDTO dto = parseLog(ethLog.getValue());
          if (dto != null  && run.get()) {
            lastTx = Instant.now();
            boolean saved = transferDBService.saveDto(dto);
            if (saved) {
              output.put(dto);
            }
          }
        } catch (Exception e) {
          log.error("Error parse token info from " + ethLog, e);
          if (appProperties.isStopOnParseError()) {
            System.exit(-1);
          }
        }
      }
    }).start();
  }

  public TransferDTO parseLog(Log ethLog) {
    if (ethLog == null || !ContractConstants.FARM_TOKEN.equals(ethLog.getAddress())) {
      return null;
    }

    TokenTx tx = erc20Decoder.decode(ethLog);

    if (tx == null
        || !"Transfer".equals(tx.getMethodName())) {
      return null;
    }

    long blockTime = ethBlockService.getTimestampSecForBlock(tx.getBlock(), ETH_NETWORK);

    TransferDTO dto = new TransferDTO();
    dto.setNetwork(ETH_NETWORK);
    dto.setId(tx.getHash() + "_" + tx.getLogId());
    dto.setBlock(tx.getBlock());
    dto.setBlockDate(blockTime);
    dto.setName(contractDbService
        .getNameByAddress(tx.getTokenAddress(), ETH_NETWORK)
        .orElseThrow());
    dto.setTokenAddress(tx.getTokenAddress());
    dto.setOwner(tx.getOwner());
    dto.setRecipient(tx.getRecipient());
    dto.setValue(
        contractDbService.parseAmount(tx.getValue(), tx.getTokenAddress(), ETH_NETWORK));

    fillMethodName(dto);
    fillTransferType(dto);
    fillBalance(dto);
    fillPrice(dto);

    log.info(dto.print());
    return dto;
  }

  public void fillMethodName(TransferDTO dto) {
    String methodName = dto.getMethodName();
    if (methodName == null) {
      String hash = dto.getId().split("_")[0];
      Transaction ethTx = web3Functions.findTransaction(hash, ETH_NETWORK);
      methodName = erc20Decoder.decodeMethodName(ethTx.getInput());
      if (methodName == null) {
        log.warn("Can't decode method for " + hash);
        dto.setMethodName(ethTx.getInput().substring(0, 10));
        return;
      }
    } else {
      if (methodName.startsWith("0x")) {
        String name = erc20Decoder.decodeMethodName(methodName);
        if (name != null) {
          methodName = name;
        } else {
          log.warn("Still can't parse method " + methodName + " for " + dto.getId());
        }
      }
    }

    dto.setMethodName(methodName);
  }

  public void fillTransferType(TransferDTO dto) {
    int ownerContractType = contractDbService
        .getContractByAddress(dto.getOwner(), dto.getNetwork())
        .map(ContractEntity::getType)
        .orElse(-1);
    int recipientContractType = contractDbService
        .getContractByAddress(dto.getRecipient(), dto.getNetwork())
        .map(ContractEntity::getType)
        .orElse(-1);
    TransferType type = TransferType.getType(dto, ownerContractType, recipientContractType);
    dto.setType(type.name());
  }

  public void fillBalance(TransferDTO dto) {
    dto.setBalanceOwner(getBalance(dto.getOwner(), dto.getTokenAddress(), dto.getBlock()));
    dto.setBalanceRecipient(getBalance(dto.getRecipient(), dto.getTokenAddress(), dto.getBlock()));
  }

  public void fillPrice(TransferDTO dto) {
    dto.setPrice(priceProvider.getPriceForCoin(dto.getName(), dto.getBlock(), ETH_NETWORK));
  }

  private double getBalance(String holder, String tokenAddress, long block) {
    BigInteger balanceI = functionsUtils.callIntByNameWithAddressArg(
        BALANCE_OF, holder, tokenAddress, block, ETH_NETWORK)
        .orElseThrow(() -> new IllegalStateException("Error get balance for " + tokenAddress));
    return contractDbService.parseAmount(balanceI, tokenAddress, ETH_NETWORK);
  }

  @Override
  public BlockingQueue<DtoI> getOutput() {
    return output;
  }

  @Override
  public Instant getLastTx() {
    return lastTx;
  }

  @PreDestroy
  public void stop() {
    run.set(false);
  }
}
