package pro.belbix.ethparser.web3.erc20.db;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.dto.TransferDTO;
import pro.belbix.ethparser.repositories.TransferRepository;

@Service
@Log4j2
public class TransferDBService {

    private final TransferRepository transferRepository;

    public TransferDBService(TransferRepository transferRepository) {
        this.transferRepository = transferRepository;
    }

    public boolean saveDto(TransferDTO dto) {
        if (transferRepository.existsById(dto.getId())) {
            log.warn("Duplicate transfer info " + dto);
            return false;
        }
        transferRepository.save(dto);
        return true;
    }

}
