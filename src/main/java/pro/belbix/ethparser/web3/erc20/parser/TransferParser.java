package pro.belbix.ethparser.web3.erc20.parser;

import static pro.belbix.ethparser.web3.abi.FunctionsNames.BALANCE_OF;
import static pro.belbix.ethparser.web3.MethodDecoder.parseAmount;

import java.math.BigInteger;
import java.time.Instant;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.Transaction;
import pro.belbix.ethparser.dto.DtoI;
import pro.belbix.ethparser.dto.v0.TransferDTO;
import pro.belbix.ethparser.model.TokenTx;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.web3.EthBlockService;
import pro.belbix.ethparser.web3.abi.FunctionsUtils;
import pro.belbix.ethparser.web3.MethodDecoder;
import pro.belbix.ethparser.web3.ParserInfo;
import pro.belbix.ethparser.web3.Web3Parser;
import pro.belbix.ethparser.web3.Web3Service;
import pro.belbix.ethparser.web3.contracts.ContractConstants;
import pro.belbix.ethparser.web3.contracts.ContractType;
import pro.belbix.ethparser.web3.contracts.ContractUtils;
import pro.belbix.ethparser.web3.erc20.TransferType;
import pro.belbix.ethparser.web3.erc20.db.TransferDBService;
import pro.belbix.ethparser.web3.erc20.decoder.ERC20Decoder;
import pro.belbix.ethparser.web3.prices.PriceProvider;

@Service
@Log4j2
public class TransferParser implements Web3Parser {

  private static final AtomicBoolean run = new AtomicBoolean(true);
  private final BlockingQueue<Log> logs = new ArrayBlockingQueue<>(100);
  private final BlockingQueue<DtoI> output = new ArrayBlockingQueue<>(100);
  private final ERC20Decoder erc20Decoder = new ERC20Decoder();
  private final Web3Service web3Service;
  private final EthBlockService ethBlockService;
  private final ParserInfo parserInfo;
  private final TransferDBService transferDBService;
  private final PriceProvider priceProvider;
  private final FunctionsUtils functionsUtils;
  private final AppProperties appProperties;
  private Instant lastTx = Instant.now();

  public TransferParser(Web3Service web3Service,
      EthBlockService ethBlockService,
      ParserInfo parserInfo,
      TransferDBService transferDBService,
      PriceProvider priceProvider,
      FunctionsUtils functionsUtils, AppProperties appProperties) {
    this.web3Service = web3Service;
    this.ethBlockService = ethBlockService;
    this.parserInfo = parserInfo;
    this.transferDBService = transferDBService;
    this.priceProvider = priceProvider;
    this.functionsUtils = functionsUtils;
    this.appProperties = appProperties;
  }

  @Override
  public void startParse() {
    log.info("Start parse Token info logs");
    parserInfo.addParser(this);
    web3Service.subscribeOnLogs(logs);
    new Thread(() -> {
      while (run.get()) {
        Log ethLog = null;
        try {
          ethLog = logs.poll(1, TimeUnit.SECONDS);
          TransferDTO dto = parseLog(ethLog);
          if (dto != null) {
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

    long blockTime = ethBlockService.getTimestampSecForBlock(tx.getBlock());

    TransferDTO dto = new TransferDTO();
    dto.setId(tx.getHash() + "_" + tx.getLogId());
    dto.setBlock(tx.getBlock());
    dto.setBlockDate(blockTime);
    dto.setName(ContractUtils.getNameByAddress(tx.getTokenAddress()).orElseThrow());
    dto.setOwner(tx.getOwner());
    dto.setRecipient(tx.getRecipient());
    dto.setValue(parseAmount(tx.getValue(), tx.getTokenAddress()));

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
      Transaction ethTx = web3Service.findTransaction(hash);
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

  public static void fillTransferType(TransferDTO dto) {
    TransferType type = TransferType.getType(dto);
    dto.setType(type.name());
  }

  public void fillBalance(TransferDTO dto) {
    String tokenAddress = ContractUtils.getAddressByName(dto.getName(), ContractType.TOKEN)
        .orElseThrow(() -> new IllegalStateException("Not found adr for " + dto.getName()));
    dto.setBalanceOwner(getBalance(dto.getOwner(), tokenAddress, dto.getBlock()));
    dto.setBalanceRecipient(getBalance(dto.getRecipient(), tokenAddress, dto.getBlock()));
  }

  public void fillPrice(TransferDTO dto) {
    dto.setPrice(priceProvider.getPriceForCoin(dto.getName(), dto.getBlock()));
  }

  private double getBalance(String holder, String tokenAddress, long block) {
    BigInteger balanceI = functionsUtils.callIntByName(
        BALANCE_OF, holder, tokenAddress, block)
        .orElseThrow(() -> new IllegalStateException("Error get balance for " + tokenAddress));
    return MethodDecoder.parseAmount(balanceI, tokenAddress);
  }

  @Override
  public BlockingQueue<DtoI> getOutput() {
    return output;
  }

  @Override
  public Instant getLastTx() {
    return lastTx;
  }
}
