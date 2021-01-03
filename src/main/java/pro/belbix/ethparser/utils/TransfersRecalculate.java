package pro.belbix.ethparser.utils;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.dto.TransferDTO;
import pro.belbix.ethparser.repositories.TransferRepository;
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

    public TransfersRecalculate(TransferDBService transferDBService,
                                TransferRepository transferRepository,
                                TransferParser transferParser) {
        this.transferDBService = transferDBService;
        this.transferRepository = transferRepository;
        this.transferParser = transferParser;
    }

    public void start() {
        List<TransferDTO> dtos = transferRepository.getAllByOrderByBlockDate();
        List<TransferDTO> result = new ArrayList<>();
        for (TransferDTO dto : dtos) {
            try {
                if (!onlyType) {
                    transferDBService.fillBalance(dto);
                }
                TransferParser.fillTransferType(dto);
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
    }

    public void reparseEmptyMethods() {
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
