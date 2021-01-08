package pro.belbix.ethparser.web3.harvest;

import static pro.belbix.ethparser.web3.MethodDecoder.parseAmount;

import java.math.BigInteger;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.dto.HarvestDTO;
import pro.belbix.ethparser.web3.Functions;
import pro.belbix.ethparser.web3.PriceProvider;
import pro.belbix.ethparser.web3.harvest.contracts.StakeContracts;
import pro.belbix.ethparser.web3.harvest.contracts.Vaults;
import pro.belbix.ethparser.web3.uniswap.contracts.LpContracts;

@Service
@Log4j2
public class HarvestOwnerBalanceCalculator {

    private final Functions functions;
    private final PriceProvider priceProvider;

    public HarvestOwnerBalanceCalculator(Functions functions, PriceProvider priceProvider) {
        this.functions = functions;
        this.priceProvider = priceProvider;
    }

    public boolean fillBalance(HarvestDTO dto) {
        try {
            if (Vaults.vaultNameToHash.containsKey(dto.getVault())) {
                if (Vaults.isPsName(dto.getVault())) {
                    return balanceForPs(dto);
                }
                return balanceForVault(dto);
            } else {
                return balanceForNonVaultLp(dto);
            }
        } catch (Exception e) {
            log.error("Can't fill balance for " + dto.print(), e);
        }
        return false;
    }

    private boolean balanceForPs(HarvestDTO dto) {
        String psHash = Vaults.vaultNameToHash.get(dto.getVault());
        BigInteger balanceI = functions.callBalanceOf(dto.getOwner(), psHash, dto.getBlock().longValue());
        if (balanceI == null) {
            log.warn("Can reach ps balance for " + dto.print());
            return false;
        }
        double balance = parseAmount(balanceI, psHash);
        dto.setOwnerBalance(balance);

        double price = priceProvider.getPriceForCoin(dto.getVault(), dto.getBlock().longValue());
        dto.setOwnerBalanceUsd(balance * price);
        return true;
    }

    private boolean balanceForVault(HarvestDTO dto) {
        long block = dto.getBlock().longValue();
        String vaultHash = Vaults.vaultNameToHash.get(dto.getVault());
        BigInteger balanceI;
        if (dto.isMigrated()) {
            //migration process broken UnderlyingBalance for vault
            //but we have shortcut - after migration we can check balanceOf
            String stHash = StakeContracts.vaultHashToStakeHash.get(vaultHash);
            if (stHash == null) {
                throw new IllegalStateException("Not found st for " + dto.getVault());
            }
            balanceI = functions.callBalanceOf(dto.getOwner(), stHash, block);
        } else {
            balanceI = functions.callUnderlyingBalance(dto.getOwner(), vaultHash, block);
        }
        if (balanceI == null) {
            log.warn("Can reach vault balance for " + dto.print());
            //maybe strategy disabled? try balanceOf
            balanceI = functions.callBalanceOf(dto.getOwner(), vaultHash, block);
            if (balanceI == null) {
                return false;
            }
        }

        double balance = parseAmount(balanceI, vaultHash);
        dto.setOwnerBalance(balance);

        //fill USD value
        if (Vaults.isLp(dto.getVault())) {
            String lpHash = LpContracts.harvestStrategyToLp.get(vaultHash);
            if (lpHash == null) {
                throw new IllegalStateException("Not found lp hash for " + vaultHash);
            }
            double amountUsd = priceProvider
                .getLpPositionAmountInUsd(lpHash, balance, block);
            dto.setOwnerBalanceUsd(amountUsd);
        } else {
            double price = priceProvider.getPriceForCoin(dto.getVault(), block);
            dto.setOwnerBalanceUsd(balance * price);
        }
        return true;
    }

    private boolean balanceForNonVaultLp(HarvestDTO dto) {
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
        double balance = parseAmount(balanceI, lpHash);
        dto.setOwnerBalance(balance);

        //fill USD value
        double amountUsd = priceProvider.getLpPositionAmountInUsd(lpHash, balance, dto.getBlock().longValue());
        dto.setOwnerBalanceUsd(amountUsd);
        return true;
    }

}
