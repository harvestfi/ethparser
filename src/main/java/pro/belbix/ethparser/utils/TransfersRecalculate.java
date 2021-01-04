package pro.belbix.ethparser.utils;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.dto.TransferDTO;
import pro.belbix.ethparser.repositories.TransferRepository;
import pro.belbix.ethparser.web3.erc20.TransferType;
import pro.belbix.ethparser.web3.erc20.db.TransferDBService;
import pro.belbix.ethparser.web3.erc20.parser.TransferParser;

@Service
@Log4j2
public class TransfersRecalculate {

    private final TransferDBService transferDBService;
    private final TransferRepository transferRepository;
    private final TransferParser transferParser;

    @Value("${transfer-recalculate.onlyType:false}")
    private boolean onlyType = false;

    @Value("${transfer-recalculate.balances:true}")
    private boolean balances = true;

    public TransfersRecalculate(TransferDBService transferDBService,
                                TransferRepository transferRepository,
                                TransferParser transferParser) {
        this.transferDBService = transferDBService;
        this.transferRepository = transferRepository;
        this.transferParser = transferParser;
    }

    public void start() {
        if (balances) {
            recalculateBalances();
        } else {
            reparseEmptyMethods();
        }
    }

    private void recalculateBalances() {
        List<TransferDTO> dtos = transferRepository.getAllByOrderByBlockDate();
        List<TransferDTO> result = new ArrayList<>();
        for (TransferDTO dto : dtos) {
            try {
                if (onlyType) {
                    TransferParser.fillTransferType(dto);
                } else {
                    transferParser.fillMethodName(dto);
                    TransferParser.fillTransferType(dto);
                    if (TransferType.valueOf(dto.getType()).isUser()) {
                        transferParser.fillBalance(dto);
                        boolean balanceOk = transferDBService.checkBalance(dto);
                        if (!balanceOk) {
                            throw new IllegalStateException("Balance is not ok");
                        }
                    }
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
}
