package pro.belbix.ethparser.web3.erc20.db;

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
        fillBalance(dto);
        transferRepository.save(dto);
        return true;
    }

    public void fillBalance(TransferDTO dto) {
        Double balance = transferRepository.getBalanceForOwner(dto.getOwner(), dto.getBlockDate());
        if (balance == null) {
            balance = 0.0;
        }
        balance += dto.getValue();
        dto.setBalance(balance);
        double price = priceProvider.getPriceForCoin(dto.getName(), dto.getBlock());
        dto.setPrice(price);
        dto.setBalanceUsd(balance * price);
    }

}
