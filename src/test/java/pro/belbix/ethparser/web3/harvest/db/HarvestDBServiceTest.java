package pro.belbix.ethparser.web3.harvest.db;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import pro.belbix.ethparser.dto.v0.HarvestDTO;

public class HarvestDBServiceTest {

    private static final Double PROFIT = 41.2912731552351;
    private static final String WITH = "Withdraw";
    private static final String DEP = "Deposit";
    private static final String STAKE = "Staked";

    @Test
    public void testCalculateProfit() {

        double profit;

        //testing illegal arguments
        List<HarvestDTO> transfers = new ArrayList<>();
        transfers.add(createDto(transfers.size(), STAKE, 100.0, 100.0, 0.0, 0.0, 0.0, PROFIT));
        transfers.add(createDto(transfers.size(), DEP, 100.0, null, 0.0, 0.0, 0.0, PROFIT));
        transfers.add(createDto(transfers.size(), DEP, 100.0, 0.0, 0.0, 0.0, 0.0, PROFIT));

        profit = HarvestDBService.calculateProfit(transfers);
        assertEquals("CalculateProfit - Illegal", "0,000000", String.format(Locale.FRANCE, "%.6f", profit));

        //testing share price
        transfers = new ArrayList<>();
        transfers.add(createDto(transfers.size(), WITH, 200.0, 100.0, null, 0.0, 0.0, PROFIT));
        transfers.add(createDto(transfers.size(), WITH, 100.0, 100.0, 0.0, 0.0, 0.0, PROFIT));
        transfers.add(createDto(transfers.size(), WITH, 0.0, 50.0, 2.0, 0.0, 0.0, PROFIT));

        profit = HarvestDBService.calculateProfit(transfers);
        assertEquals("CalculateProfit - Share Price", "300,000000", String.format(Locale.FRANCE, "%.6f", profit));

    }

    @Test
    public void testCalculateProfitUsd(){

        double profit;

        //testing illegal arguments
        List<HarvestDTO> transfers = new ArrayList<>();
        transfers.add(createDto(transfers.size(), STAKE, 100.0, 100.0, 0.0, 0.0, 100.0, PROFIT));
        transfers.add(createDto(transfers.size(), STAKE, 100.0, 100.0, 0.0, null, 100.0, PROFIT));
        transfers.add(createDto(transfers.size(), STAKE, 100.0, 100.0, 0.0, 100.0, 0.0, PROFIT));
        transfers.add(createDto(transfers.size(), STAKE, 100.0, 100.0, 0.0, 100.0, null, PROFIT));
        transfers.add(createDto(transfers.size(), STAKE, 100.0, 100.0, 0.0, 100.0, 100.0, 0.0));
        transfers.add(createDto(transfers.size(), STAKE, 100.0, 100.0, 0.0, 100.0, 100.0, null));
        transfers.add(createDto(transfers.size(), STAKE, 100.0, 100.0, 0.0, 100.0, 100.0, Double.NaN));
        transfers.add(createDto(transfers.size(), STAKE, 100.0, 100.0, 0.0, 100.0, 100.0, Double.NEGATIVE_INFINITY));


        for (HarvestDTO transfer : transfers){

            profit = HarvestDBService.calculateProfitUsd(transfer);
            assertEquals("CalculateProfitUsd - Illegal", "0,000000" , String.format(Locale.FRANCE, "%.6f", profit));

        }

        //testing valid arguments
        transfers.get(0).setLastUsdTvl(110.0);
        profit = HarvestDBService.calculateProfitUsd(transfers.get(0));
        assertEquals("CalculateProfitUsd", "45,4204004707586" , String.format(Locale.FRANCE, "%.13f", profit));

    }

    private HarvestDTO createDto(int id, String methodName, Double ownerBalance, Double amount, Double sharePrice, Double lastUsdTvl, Double lastTvl, Double profit) {

        HarvestDTO dto = new HarvestDTO();
        dto.setId(id + "");
        dto.setMethodName(methodName);
        dto.setOwnerBalance(ownerBalance);
        dto.setAmount(amount);
        dto.setSharePrice(sharePrice);
        dto.setLastUsdTvl(lastUsdTvl);
        dto.setLastTvl(lastTvl);
        dto.setProfit(profit);
        return dto;

    }
}
