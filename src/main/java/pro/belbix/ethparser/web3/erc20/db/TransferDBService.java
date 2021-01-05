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
        checkBalances(dto);
        fillProfit(dto);
        transferRepository.save(dto);
        return true;
    }

    public boolean checkBalances(TransferDTO dto) {
        return checkBalance(dto.getOwner(), dto.getBalanceOwner(), dto.getBlockDate())
            && checkBalance(dto.getRecipient(), dto.getBalanceRecipient(), dto.getBlockDate());
    }

    private boolean checkBalance(String holder, double expectedBalance, long blockDate) {
        if (ZERO_ADDRESS.equals(holder)) {
            return true;
        }
        Double balance = transferRepository.getBalanceForOwner(holder, blockDate);
        if (balance == null) {
            balance = 0.0;
        }
        if (Math.abs(balance - expectedBalance) > 1) {
            log.error("WRONG BALANCE for " + holder + " dbBalance: " + balance + " != " + expectedBalance);
            return false;
        }
        return true;
    }

    // used only for recalculation
    public void fillBalances(TransferDTO dto) {
        Double balanceOwner = transferRepository.getBalanceForOwner(dto.getOwner(), dto.getBlockDate());
        if (balanceOwner == null) {
            balanceOwner = 0.0;
        }
        dto.setBalanceOwner(balanceOwner);

        Double balanceRecipient = transferRepository.getBalanceForOwner(dto.getRecipient(), dto.getBlockDate());
        if (balanceRecipient == null) {
            balanceRecipient = 0.0;
        }
        dto.setBalanceRecipient(balanceRecipient);
    }

    public void fillProfit(TransferDTO dto) {
        //TODO later
        if (PS_STAKE.name().equals(dto.getType()) || PS_EXIT.name().equals(dto.getType())) {

        }

    }

}
