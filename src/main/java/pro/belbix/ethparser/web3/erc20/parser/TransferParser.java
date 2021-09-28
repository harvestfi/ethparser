package pro.belbix.ethparser.web3.erc20.parser;

import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.BALANCE_OF;

import java.math.BigInteger;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.Transaction;
import pro.belbix.ethparser.dto.v0.TransferDTO;
import pro.belbix.ethparser.entity.contracts.ContractEntity;
import pro.belbix.ethparser.model.tx.TokenTx;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.properties.NetworkProperties;
import pro.belbix.ethparser.repositories.ErrorsRepository;
import pro.belbix.ethparser.web3.EthBlockService;
import pro.belbix.ethparser.web3.ParserInfo;
import pro.belbix.ethparser.web3.Web3Functions;
import pro.belbix.ethparser.web3.Web3Parser;
import pro.belbix.ethparser.web3.Web3Subscriber;
import pro.belbix.ethparser.web3.abi.FunctionsUtils;
import pro.belbix.ethparser.web3.contracts.ContractUtils;
import pro.belbix.ethparser.web3.contracts.db.ContractDbService;
import pro.belbix.ethparser.web3.contracts.db.ErrorDbService;
import pro.belbix.ethparser.web3.erc20.TransferType;
import pro.belbix.ethparser.web3.erc20.db.TransferDBService;
import pro.belbix.ethparser.web3.erc20.decoder.ERC20Decoder;
import pro.belbix.ethparser.web3.prices.PriceProvider;

@Service
@Log4j2
public class TransferParser extends Web3Parser<TransferDTO, Log> {

  private final ERC20Decoder erc20Decoder = new ERC20Decoder();
  private final Web3Functions web3Functions;
  private final Web3Subscriber web3Subscriber;
  private final EthBlockService ethBlockService;
  private final TransferDBService transferDBService;
  private final PriceProvider priceProvider;
  private final FunctionsUtils functionsUtils;
  private final NetworkProperties networkProperties;
  private final ContractDbService contractDbService;

  public TransferParser(Web3Functions web3Functions,
      Web3Subscriber web3Subscriber, EthBlockService ethBlockService,
      ParserInfo parserInfo,
      TransferDBService transferDBService,
      PriceProvider priceProvider,
      FunctionsUtils functionsUtils, AppProperties appProperties,
      NetworkProperties networkProperties,
      ContractDbService contractDbService,
      ErrorDbService errorDbService) {
    super(parserInfo, appProperties, errorDbService);
    this.web3Functions = web3Functions;
    this.web3Subscriber = web3Subscriber;
    this.ethBlockService = ethBlockService;
    this.transferDBService = transferDBService;
    this.priceProvider = priceProvider;
    this.functionsUtils = functionsUtils;
    this.networkProperties = networkProperties;
    this.contractDbService = contractDbService;
  }

  @Override
  protected void subscribeToInput() {
    web3Subscriber.subscribeOnLogs(input, this.getClass().getSimpleName());
  }

  @Override
  protected boolean save(TransferDTO dto) {
    return transferDBService.saveDto(dto);
  }

  @Override
  protected boolean isActiveForNetwork(String network) {
    return networkProperties.get(network).isParseTransfers();
  }

  public TransferDTO parse(Log ethLog, String network) {
    if (ethLog == null || !ContractUtils.isFarmAddress(ethLog.getAddress())) {
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
    dto.setValue(functionsUtils.parseAmount(tx.getValue(), tx.getTokenAddress(), ETH_NETWORK));

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
        log.warn("Cant decode method for " + hash);
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
    dto.setPrice(
        priceProvider.getPriceForCoin(dto.getTokenAddress(), dto.getBlock(), dto.getNetwork()));
  }

  private double getBalance(String holder, String tokenAddress, long block) {
    BigInteger balanceI = functionsUtils.callIntByNameWithAddressArg(
        BALANCE_OF, holder, tokenAddress, block, ETH_NETWORK)
        .orElseThrow(() -> new IllegalStateException("Error get balance for " + tokenAddress));
    return functionsUtils.parseAmount(balanceI, tokenAddress, ETH_NETWORK);
  }
}
