package pro.belbix.ethparser.web3.uniswap;

import static pro.belbix.ethparser.model.HarvestTx.parseAmount;

import java.math.BigInteger;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.dto.UniswapDTO;
import pro.belbix.ethparser.repositories.UniswapRepository;
import pro.belbix.ethparser.web3.Functions;
import pro.belbix.ethparser.web3.PriceProvider;
import pro.belbix.ethparser.web3.uniswap.contracts.LpContracts;

@Service
@Log4j2
public class UniOwnerBalanceCalculator {

    private final Functions functions;
    private final PriceProvider priceProvider;
    private final UniswapRepository uniswapRepository;

    public UniOwnerBalanceCalculator(Functions functions, PriceProvider priceProvider,
                                     UniswapRepository uniswapRepository) {
        this.functions = functions;
        this.priceProvider = priceProvider;
        this.uniswapRepository = uniswapRepository;
    }

    public boolean fillBalance(UniswapDTO dto) {
        try {
            if (dto.isLiquidity()) {
                return balanceForLp(dto);
            } else {
                return balanceForFarm(dto);
            }

        } catch (Exception e) {
            log.error("Can't fill balance for " + dto.print(), e);
        }
        return false;
    }

    private boolean balanceForFarm(UniswapDTO dto) {
        List<UniswapDTO> txs = uniswapRepository.fetchAllByOwner(dto.getOwner(), 0, dto.getBlockDate());
        double balance = 0;
        for (UniswapDTO oldTx : txs) {
            if (oldTx.getId().equals(dto.getId())) {
                continue;
            }
            if (oldTx.isBuy()) {
                balance += oldTx.getAmount();
            }
            if (oldTx.isSell()) {
                balance -= oldTx.getAmount();
            }
        }
        if (dto.isBuy()) {
            balance += dto.getAmount();
        }
        if (dto.isSell()) {
            balance -= dto.getAmount();
        }
        dto.setOwnerBalance(balance);
        dto.setOwnerBalanceUsd(balance * dto.getLastPrice());
        return true;
    }

    private boolean balanceForLp(UniswapDTO dto) {
        String lpHash;
        if (dto.getLp() == null) {
            lpHash = LpContracts.findLpForCoins(dto.getCoin(), dto.getOtherCoin());
        } else {
            lpHash = LpContracts.lpNameToHash.get(dto.getLp());
        }
        if (lpHash == null) {
            log.error("Not found vault/lp hash for " + dto.getLp());
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
