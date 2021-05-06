package pro.belbix.ethparser.web3.uniswap.parser;

import static pro.belbix.ethparser.model.tx.UniswapTx.SWAP;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.PARSABLE_UNI_PAIRS;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.web3j.abi.datatypes.Address;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple2;
import pro.belbix.ethparser.dto.v0.UniswapDTO;
import pro.belbix.ethparser.model.tx.UniswapTx;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.properties.NetworkProperties;
import pro.belbix.ethparser.web3.EthBlockService;
import pro.belbix.ethparser.web3.ParserInfo;
import pro.belbix.ethparser.web3.Web3Functions;
import pro.belbix.ethparser.web3.Web3Parser;
import pro.belbix.ethparser.web3.Web3Subscriber;
import pro.belbix.ethparser.web3.contracts.db.ContractDbService;
import pro.belbix.ethparser.web3.harvest.parser.UniToHarvestConverter;
import pro.belbix.ethparser.web3.prices.PriceProvider;
import pro.belbix.ethparser.web3.uniswap.UniOwnerBalanceCalculator;
import pro.belbix.ethparser.web3.uniswap.db.UniswapDbService;
import pro.belbix.ethparser.web3.uniswap.decoder.UniswapLpLogDecoder;

@Service
@Log4j2
public class UniswapLpLogParser extends Web3Parser<UniswapDTO, Log> {

  private final UniswapLpLogDecoder uniswapLpLogDecoder = new UniswapLpLogDecoder();
  private final Web3Functions web3Functions;
  private final Web3Subscriber web3Subscriber;
  private final UniswapDbService uniswapDbService;
  private final EthBlockService ethBlockService;
  private final PriceProvider priceProvider;
  private final UniToHarvestConverter uniToHarvestConverter;
  private final UniOwnerBalanceCalculator uniOwnerBalanceCalculator;
  private final NetworkProperties networkProperties;
  private final ContractDbService contractDbService;

  public UniswapLpLogParser(Web3Functions web3Functions,
      Web3Subscriber web3Subscriber, UniswapDbService uniswapDbService,
      EthBlockService ethBlockService,
      PriceProvider priceProvider,
      UniToHarvestConverter uniToHarvestConverter,
      ParserInfo parserInfo,
      UniOwnerBalanceCalculator uniOwnerBalanceCalculator,
      AppProperties appProperties,
      NetworkProperties networkProperties,
      ContractDbService contractDbService) {
    super(parserInfo, appProperties);
    this.web3Functions = web3Functions;
    this.web3Subscriber = web3Subscriber;
    this.uniswapDbService = uniswapDbService;
    this.ethBlockService = ethBlockService;
    this.priceProvider = priceProvider;
    this.uniToHarvestConverter = uniToHarvestConverter;
    this.uniOwnerBalanceCalculator = uniOwnerBalanceCalculator;
    this.networkProperties = networkProperties;
    this.contractDbService = contractDbService;
  }

  @Override
  protected void subscribeToInput() {
    web3Subscriber.subscribeOnLogs(input, this.getClass().getSimpleName());
  }

  @Override
  protected boolean save(UniswapDTO dto) {
    enrichDto(dto);
    uniOwnerBalanceCalculator.fillBalance(dto);
    uniToHarvestConverter.addDtoToQueue(dto);
    return uniswapDbService.saveUniswapDto(dto);
  }

  @Override
  protected boolean isActiveForNetwork(String network) {
    return networkProperties.get(network).isParseUniswapLog();
  }

  public UniswapDTO parse(Log ethLog, String network) {
    if (!isValidLog(ethLog)) {
      return null;
    }
    UniswapTx tx = new UniswapTx();
    tx.setFirstTokenIsKey(firstTokenIsKey(ethLog.getAddress()));
    tx.setCoin(mapLpAddress(ethLog.getAddress(), true));
    tx.setOtherCoin(mapLpAddress(ethLog.getAddress(), false));
    tx.setCoinAddress(contractDbService.findLpByAddress(ethLog.getAddress(), ETH_NETWORK)
        .map(lp -> lp.getKeyToken().getContract().getAddress())
        .orElseThrow(
            () -> new IllegalStateException("Not found key token for " + ethLog.getAddress())));
    uniswapLpLogDecoder.decode(tx, ethLog);
    if (tx.getHash() == null) {
      return null;
    }

    UniswapDTO dto = createDto(tx);

    //enrich owner
    TransactionReceipt receipt = web3Functions.fetchTransactionReceipt(dto.getHash(), ETH_NETWORK);
    dto.setOwner(receipt.getFrom());

    //enrich date
    dto.setBlockDate(
        ethBlockService
            .getTimestampSecForBlock(ethLog.getBlockNumber().longValue(), ETH_NETWORK));

    if (dto.getLastPrice() == null) {
      Double otherCoinPrice = priceProvider
          .getPriceForCoin(dto.getOtherCoin(), dto.getBlock().longValue(), ETH_NETWORK);
      if (otherCoinPrice != null) {
        dto.setPrice((dto.getOtherAmount() * otherCoinPrice) / dto.getAmount());
      } else {
        throw new IllegalStateException("Price not found " + dto.print());
      }
    }

    log.info(dto.print());

    return dto;
  }

  private boolean isValidLog(Log log) {
    if (log == null || log.getTopics() == null || log.getTopics().isEmpty()) {
      return false;
    }
    return PARSABLE_UNI_PAIRS.get(ETH_NETWORK).contains(log.getAddress());
  }

  private void enrichDto(UniswapDTO dto) {
    dto.setLastGas(web3Functions.fetchAverageGasPrice(ETH_NETWORK));
  }

  private UniswapDTO createDto(UniswapTx tx) {
    String coinName = contractDbService
        .getNameByAddress(tx.getCoinAddress(), ETH_NETWORK)
        .orElseThrow();
    String lpName = contractDbService
        .getNameByAddress(tx.getLpAddress(), ETH_NETWORK)
        .orElseThrow();

    UniswapDTO uniswapDTO = new UniswapDTO();
    uniswapDTO.setId(tx.getHash() + "_" + tx.getLogId());
    uniswapDTO.setHash(tx.getHash());
    uniswapDTO.setOwner(tx.getOwner());
    uniswapDTO.setBlock(tx.getBlock());
    uniswapDTO.setCoin(coinName);
    uniswapDTO.setCoinAddress(tx.getCoinAddress());
    uniswapDTO.setConfirmed(tx.isSuccess());
    uniswapDTO.setLp(lpName);
    uniswapDTO.setLpAddress(tx.getLpAddress());
    uniswapDTO.setMethodName(tx.getMethodName());

    if (tx.getCoinAddress().equalsIgnoreCase(tx.getCoinIn().getValue())) {
      assertBuy(false, tx.getBuy());
      uniswapDTO.setAmount(contractDbService
          .parseAmount(tx.getAmountIn(), tx.getCoinIn().getValue(), ETH_NETWORK));
      uniswapDTO.setOtherCoin(addrToStr(tx.getCoinOut()));
      uniswapDTO.setOtherCoinAddress(tx.getCoinOut().getValue());
      uniswapDTO.setOtherAmount(
          contractDbService
              .parseAmount(tx.getAmountOut(), tx.getCoinOut().getValue(), ETH_NETWORK));
      if (tx.getType().equals(SWAP)) {
        uniswapDTO.setType("SELL");
      } else {
        uniswapDTO.setType(tx.getType());
      }
    } else if (tx.getCoinAddress().equalsIgnoreCase(tx.getCoinOut().getValue())) {
      assertBuy(true, tx.getBuy());
      uniswapDTO.setAmount(contractDbService
          .parseAmount(tx.getAmountOut(), tx.getCoinOut().getValue(), ETH_NETWORK));
      uniswapDTO.setOtherCoin(addrToStr(tx.getCoinIn()));
      uniswapDTO.setOtherCoinAddress(tx.getCoinIn().getValue());
      uniswapDTO.setOtherAmount(contractDbService
              .parseAmount(tx.getAmountIn(), tx.getCoinIn().getValue(), ETH_NETWORK));
      if (tx.getType().equals(SWAP)) {
        uniswapDTO.setType("BUY");
      } else {
        uniswapDTO.setType(tx.getType());
      }
    } else {
      throw new IllegalStateException("Contract can't identified " + toString());
    }

    if (uniswapDTO.getOtherCoin().equals("USDC")) {
      double price = (uniswapDTO.getOtherAmount() / uniswapDTO.getAmount());
      uniswapDTO.setPrice(price);
    }
    return uniswapDTO;
  }

  public void assertBuy(boolean expected, Boolean actual) {
    if (actual == null) {
      throw new IllegalStateException("Buy now is null");
    }
    if (actual != expected) {
      throw new IllegalStateException("Unexpected setup");
    }
  }

  private String addrToStr(Address adr) {
    return contractDbService.getNameByAddress(adr.getValue(), ETH_NETWORK)
        .orElseThrow(() -> new IllegalStateException("Not found name for " + adr.getValue()));
  }

  public boolean firstTokenIsKey(String lpAddress) {
    Tuple2<String, String> tokens = contractDbService
        .tokenAddressesByUniPairAddress(lpAddress, ETH_NETWORK);
    String keyCoin = contractDbService.findLpByAddress(lpAddress, ETH_NETWORK)
        .map(lp -> lp.getKeyToken().getContract().getAddress())
        .orElseThrow(() -> new IllegalStateException("Key coin not found for " + lpAddress));
    if (tokens.component1().equalsIgnoreCase(keyCoin)) {
      return true;
    } else if (tokens.component2().equalsIgnoreCase(keyCoin)) {
      return false;
    } else {
      throw new IllegalStateException("Not found key name in lp " + lpAddress);
    }
  }

  private String mapLpAddress(String address, boolean isKeyCoin) {
    String keyCoinAdr = contractDbService.findLpByAddress(address, ETH_NETWORK)
        .map(lp -> lp.getKeyToken().getContract().getAddress())
        .orElseThrow(() -> new IllegalStateException("Key coin not found for " + address));
    Tuple2<String, String> tokensAdr = contractDbService
        .tokenAddressesByUniPairAddress(address, ETH_NETWORK);

    int i;
    if (tokensAdr.component1().equalsIgnoreCase(keyCoinAdr)) {
      i = 1;
    } else if (tokensAdr.component2().equalsIgnoreCase(keyCoinAdr)) {
      i = 2;
    } else {
      throw new IllegalStateException("Key coin not found in " + tokensAdr);
    }
    if (isKeyCoin) {
      return getStringFromPair(tokensAdr, i, false);
    } else {
      return getStringFromPair(tokensAdr, i, true);
    }
  }

  private static String getStringFromPair(Tuple2<String, String> pair, int i, boolean inverse) {
    if (i == 1) {
      if (inverse) {
        return pair.component2();
      } else {
        return pair.component1();
      }
    } else if (i == 2) {
      if (inverse) {
        return pair.component1();
      } else {
        return pair.component2();
      }
    } else {
      throw new IllegalStateException("Wrong index for pair " + i);
    }
  }
}
