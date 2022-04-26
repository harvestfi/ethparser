package pro.belbix.ethparser.utils.recalculation;

import static pro.belbix.ethparser.web3.contracts.ContractConstantsV7.FARM_TOKEN;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.dto.v0.TransferDTO;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.repositories.v0.TransferRepository;
import pro.belbix.ethparser.repositories.v0.UniswapRepository;
import pro.belbix.ethparser.web3.erc20.db.TransferDBService;
import pro.belbix.ethparser.web3.erc20.parser.TransferParser;
import pro.belbix.ethparser.web3.prices.PriceProvider;

@Service
@Log4j2
public class TransfersRecalculate {

  private final TransferDBService transferDBService;
  private final TransferRepository transferRepository;
  private final TransferParser transferParser;
  private final UniswapRepository uniswapRepository;
  private final AppProperties appProperties;
  private final PriceProvider priceProvider;

  @Value("${transfer-recalculate.fromBlockDate:0}")
  private long fromBlockDate = 0;

  @Value("${transfer-recalculate.onlyType:false}")
  private boolean onlyType = false;

  @Value("${transfer-recalculate.balances:true}")
  private boolean balances = true;

  @Value("${transfer-recalculate.methods:false}")
  private boolean methods = false;

  @Value("${transfer-recalculate.prices:false}")
  private boolean prices = false;

  @Value("${transfer-recalculate.profit:false}")
  private boolean profit = false;

  public TransfersRecalculate(TransferDBService transferDBService,
      TransferRepository transferRepository,
      TransferParser transferParser,
      UniswapRepository uniswapRepository,
      AppProperties appProperties, PriceProvider priceProvider) {
    this.transferDBService = transferDBService;
    this.transferRepository = transferRepository;
    this.transferParser = transferParser;
    this.uniswapRepository = uniswapRepository;
    this.appProperties = appProperties;
    this.priceProvider = priceProvider;
  }

  public void start() {
    if (balances) {
      recalculateBalances();
    }
    if (methods) {
      reparseEmptyMethods();
    }
    if (prices) {
      reparseEmptyPrices();
    }
    if (profit) {
      reparseEmptyProfits();
    }
  }

  private void recalculateBalances() {
    List<TransferDTO> dtos = transferRepository
        .fetchAllFromBlockDate(fromBlockDate, appProperties.getUtilNetwork());
    List<TransferDTO> result = new ArrayList<>();
    for (TransferDTO dto : dtos) {
      try {
        if (onlyType) {
          transferParser.fillTransferType(dto);
        } else {
          transferParser.fillMethodName(dto);
          transferParser.fillTransferType(dto);
          transferDBService.fillBalances(dto);
          transferDBService.fillProfit(dto);
        }

        if (onlyType) {
          result.add(dto);
          if (result.size() % 10000 == 0) {
            transferRepository.saveAll(result);
            log.info("Last " + dto.print());
            result.clear();
          }
        } else {
          transferRepository.save(dto);
          log.info("Save " + dto.print());
        }
      } catch (Exception e) {
        log.error("Error with " + dto.toString());
        throw e;
      }
    }
    if (!result.isEmpty()) {
      transferRepository.saveAll(result);
    }
  }

  private void reparseEmptyMethods() {
    List<TransferDTO> dtos = transferRepository.fetchAllWithoutMethods(appProperties.getUtilNetwork());
    log.info("Events for reparsing " + dtos.size());
    for (TransferDTO dto : dtos) {
      try {
        transferParser.fillMethodName(dto);
        transferRepository.save(dto);
        log.info("Save " + dto.print());

      } catch (Exception e) {
        log.error("Error with " + dto.toString());
        throw e;
      }
    }
  }

  private void reparseEmptyPrices() {
    List<TransferDTO> dtos = transferRepository.fetchAllWithoutPrice(appProperties.getUtilNetwork());
    log.info("Events for reparsing " + dtos.size());
    List<TransferDTO> result = new ArrayList<>();
    int count = 0;
    for (TransferDTO dto : dtos) {
      try {
        dto.setPrice(getFarmPrice(dto.getBlock(), dto.getNetwork()));
        result.add(dto);
        count++;
        if (count % 1000 == 0) {
          transferRepository.saveAll(result);
          log.info("Price saved " + count);
        }

      } catch (Exception e) {
        log.error("Error with " + dto.toString());
        throw e;
      }
    }
    transferRepository.saveAll(result);
  }

  private void reparseEmptyProfits() {
    List<TransferDTO> dtos = transferRepository.fetchAllWithoutProfits(appProperties.getUtilNetwork());
    log.info("Events for reparsing " + dtos.size());
    for (TransferDTO dto : dtos) {
      try {
        transferDBService.fillProfit(dto);
        transferRepository.save(dto);
        log.info("Save " + dto.print());

      } catch (Exception e) {
        log.error("Error with " + dto.toString());
        throw e;
      }
    }
  }

  private double getFarmPrice(long block, String network) {
    return priceProvider.getPriceForCoin(FARM_TOKEN, block, network);
  }
}
