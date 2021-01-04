package pro.belbix.ethparser.web3.erc20.db;

import static pro.belbix.ethparser.web3.ContractConstants.ZERO_ADDRESS;
import static pro.belbix.ethparser.web3.erc20.TransferType.PS_EXIT;
import static pro.belbix.ethparser.web3.erc20.TransferType.PS_STAKE;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.dto.TransferDTO;
import pro.belbix.ethparser.repositories.TransferRepository;
import pro.belbix.ethparser.web3.PriceProvider;

@Service
@Log4j2
public class TransferDBService {

    private final TransferRepository transferRepository;
    private final PriceProvider priceProvider;

    public TransferDBService(TransferRepository transferRepository,
                             PriceProvider priceProvider) {
        this.transferRepository = transferRepository;
        this.priceProvider = priceProvider;
    }

    public boolean saveDto(TransferDTO dto) {
        if (transferRepository.existsById(dto.getId())) {
            log.warn("Duplicate transfer info " + dto);
            return false;
        }
        transferRepository.saveAndFlush(dto);
        checkBalance(dto);
        fillProfit(dto);
        transferRepository.save(dto);
        return true;
    }

    public boolean checkBalance(TransferDTO dto) {
        if (ZERO_ADDRESS.equals(dto.getOwner())) {
            return true;
        }
        Double balance = transferRepository.getBalanceForOwner(dto.getOwner(), dto.getBlockDate());
        if (balance == null) {
            balance = 0.0;
        }
        if (Math.abs(balance - dto.getBalance()) > 1) {
            log.error("WRONG BALANCE! dbBalance: " + balance + " != " + dto.getBalance());
            return false;
        }
        return true;
    }

    public void fillProfit(TransferDTO dto) {
        //TODO later
        if (PS_STAKE.name().equals(dto.getType()) || PS_EXIT.name().equals(dto.getType())) {

        }

    }

}
