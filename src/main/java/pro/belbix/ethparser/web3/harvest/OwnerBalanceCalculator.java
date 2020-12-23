package pro.belbix.ethparser.web3.harvest;

import static pro.belbix.ethparser.model.HarvestTx.parseAmount;

import java.math.BigInteger;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.dto.HarvestDTO;
import pro.belbix.ethparser.web3.Functions;
import pro.belbix.ethparser.web3.PriceProvider;
import pro.belbix.ethparser.web3.harvest.contracts.Vaults;
import pro.belbix.ethparser.web3.uniswap.contracts.LpContracts;

@Service
@Log4j2
public class OwnerBalanceCalculator {

    private final Functions functions;
    private final PriceProvider priceProvider;

    public OwnerBalanceCalculator(Functions functions, PriceProvider priceProvider) {
        this.functions = functions;
        this.priceProvider = priceProvider;
    }

    public boolean fillBalance(HarvestDTO dto) {
        try {
            if(Vaults.isPs(dto.getVault())) {
                //ignore PS
                return false;
            }
            String vaultHash = Vaults.vaultNameToHash.get(dto.getVault());
            double balance;
            if (vaultHash == null) {
                //get balance from LP
                String lpHash = LpContracts.lpNameToHash.get(dto.getVault());
                if (lpHash == null) {
                    log.error("Not found vault/lp hash for " + dto.getVault());
                    return false;
                }
                BigInteger balanceI = functions.callBalanceOf(dto.getOwner(), lpHash, dto.getBlock().longValue());
                if (balanceI == null) {
                    log.warn("Can reach lp balance for " + dto.print());
                    return false;
                }
                balance = parseAmount(balanceI, lpHash);
                dto.setOwnerBalance(balance);

                //fill USD value
                double amountUsd = priceProvider.getLpPositionAmountInUsd(lpHash, balance, dto.getBlock().longValue());
                dto.setOwnerBalanceUsd(amountUsd);
            } else {
                //get balance from vault
                BigInteger balanceI = functions
                    .callUnderlyingBalance(dto.getOwner(), vaultHash, dto.getBlock().longValue());
                if (balanceI == null) {
                    log.warn("Can reach vault balance for " + dto.print());
                    return false;
                }

                balance = parseAmount(balanceI, vaultHash);
                dto.setOwnerBalance(balance);

                //fill USD value
                if (Vaults.isLp(dto.getVault())) {
                    String lpHash = LpContracts.harvestStrategyToLp.get(vaultHash);
                    if (lpHash == null) {
                        throw new IllegalStateException("Not found lp hash for " + vaultHash);
                    }
                    double amountUsd = priceProvider.getLpPositionAmountInUsd(lpHash, balance, dto.getBlock().longValue());
                    dto.setOwnerBalanceUsd(amountUsd);
                } else {
                    double price = priceProvider.getPriceForCoin(dto.getVault(), dto.getBlock().longValue());
                    dto.setOwnerBalanceUsd(balance * price);
                }
            }
            return true;
        } catch (Exception e) {
            log.error("Can't fill balance for " + dto.print(), e);
        }
        return false;
    }

}
