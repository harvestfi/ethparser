package pro.belbix.ethparser.utils.recalculation;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.dto.v0.TransferDTO;
import pro.belbix.ethparser.dto.v0.UniswapDTO;
import pro.belbix.ethparser.repositories.v0.TransferRepository;
import pro.belbix.ethparser.repositories.v0.UniswapRepository;
import pro.belbix.ethparser.web3.erc20.db.TransferDBService;
import pro.belbix.ethparser.web3.erc20.parser.TransferParser;

@Service
@Log4j2
public class TransfersRecalculate {

  private final TransferDBService transferDBService;
  private final TransferRepository transferRepository;
  private final TransferParser transferParser;
  private final UniswapRepository uniswapRepository;

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
      UniswapRepository uniswapRepository) {
    this.transferDBService = transferDBService;
    this.transferRepository = transferRepository;
    this.transferParser = transferParser;
    this.uniswapRepository = uniswapRepository;
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
    List<TransferDTO> dtos = transferRepository.fetchAllFromBlockDate(fromBlockDate);
    List<TransferDTO> result = new ArrayList<>();
    for (TransferDTO dto : dtos) {
      try {
        if (onlyType) {
          TransferParser.fillTransferType(dto);
        } else {
          transferParser.fillMethodName(dto);
          TransferParser.fillTransferType(dto);
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
    List<TransferDTO> dtos = transferRepository.fetchAllWithoutMethods();
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
    List<TransferDTO> dtos = transferRepository.fetchAllWithoutPrice();
    log.info("Events for reparsing " + dtos.size());
    List<TransferDTO> result = new ArrayList<>();
    int count = 0;
    for (TransferDTO dto : dtos) {
      try {
        dto.setPrice(getFarmPrice(dto.getBlockDate()));
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
    List<TransferDTO> dtos = transferRepository.fetchAllWithoutProfits();
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

  private double getFarmPrice(long blockDate) {
    UniswapDTO uniswapDTO = uniswapRepository
        .findFirstByBlockDateBeforeAndCoinOrderByBlockDesc(blockDate, "FARM");
    if (uniswapDTO != null) {
      return uniswapDTO.getLastPrice();
    } else {
      log.warn("FARM price not found at block date" + blockDate);
      return 0.0;
    }
  }
}
