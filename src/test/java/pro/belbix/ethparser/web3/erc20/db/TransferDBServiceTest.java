package pro.belbix.ethparser.web3.erc20.db;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertAll;
import static pro.belbix.ethparser.web3.erc20.TransferType.BAL_TX;
import static pro.belbix.ethparser.web3.erc20.TransferType.COMMON;
import static pro.belbix.ethparser.web3.erc20.TransferType.LP_SELL;
import static pro.belbix.ethparser.web3.erc20.TransferType.PS_EXIT;
import static pro.belbix.ethparser.web3.erc20.TransferType.PS_STAKE;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import pro.belbix.ethparser.dto.v0.TransferDTO;

public class TransferDBServiceTest {

    private static final String ADDR = "owner";
    private static final String OTHER = "recipient";

    @Test
    public void testCalculateSellProfit3() {
        List<TransferDTO> transfers = new ArrayList<>();
        transfers.add(createDto(transfers.size(), COMMON.name(), 100, 100, OTHER, ADDR));
        transfers.add(createDto(transfers.size(), LP_SELL.name(), 50, 90, ADDR, OTHER));
        transfers.add(createDto(transfers.size(), COMMON.name(), 50, 90, ADDR, OTHER));
        transfers.add(createDto(transfers.size(), COMMON.name(), 10, 200, OTHER, ADDR));
        transfers.add(createDto(transfers.size(), LP_SELL.name(), 10, 201, ADDR, OTHER));
        TransferDBService.calculateSellProfits(transfers, ADDR);

        assertDto(transfers.get(0), "", "", true);
        assertDto(transfers.get(1), "-5,6", "-500,0", false);
        assertDto(transfers.get(2), "", "", true);
        assertDto(transfers.get(3), "", "", true);
        assertDto(transfers.get(4), "0,0", "10,0", false);
    }

    @Test
    public void testCalculateSellProfit2() {
        List<TransferDTO> transfers = new ArrayList<>();
        transfers.add(createDto(transfers.size(), COMMON.name(), 100, 100, OTHER, ADDR));
        transfers.add(createDto(transfers.size(), LP_SELL.name(), 50, 90, ADDR, OTHER));
        transfers.add(createDto(transfers.size(), COMMON.name(), 20, 90, ADDR, OTHER));
        transfers.add(createDto(transfers.size(), LP_SELL.name(), 30, 120, ADDR, OTHER));
        TransferDBService.calculateSellProfits(transfers, ADDR);

        assertDto(transfers.get(0), "0,0", "0,0", true);
        assertDto(transfers.get(1), "-5,6", "-500,0", false);
        assertDto(transfers.get(2), "", "", true);
        assertDto(transfers.get(3), "3,3", "400,0", false);
    }

    @Test
    public void testCalculateSellProfit() {
        List<TransferDTO> transfers = new ArrayList<>();
        transfers.add(createDto(transfers.size(), COMMON.name(), 100, 100, OTHER, ADDR));
        transfers.add(createDto(transfers.size(), LP_SELL.name(), 50, 90, ADDR, OTHER));
        transfers.add(createDto(transfers.size(), LP_SELL.name(), 50, 120, ADDR, OTHER));
        TransferDBService.calculateSellProfits(transfers, ADDR);

        assertDto(transfers.get(0), "0,0", "0,0", true);
        assertDto(transfers.get(1), "-5,6", "-500,0", false);
        assertDto(transfers.get(2), "8,3", "1000,0", false);
    }

    @Test
    public void testCalculateSellProfitBal() {
        List<TransferDTO> transfers = new ArrayList<>();
        transfers.add(createDto(transfers.size(), BAL_TX.name(), 100, 100, OTHER, ADDR));
        transfers.add(createDto(transfers.size(), LP_SELL.name(), 100, 150, ADDR, OTHER));

        TransferDBService.calculateSellProfits(transfers, ADDR);

        assertDto(transfers.get(0), "0,0", "0,0", true);
        assertDto(transfers.get(1), "33,3", "5000,0", false);
    }

    @Test
    public void testCalculateSellProfitPS() {
        List<TransferDTO> transfers = new ArrayList<>();
        transfers.add(createDto(transfers.size(), BAL_TX.name(), 100, 100, OTHER, ADDR));
        transfers.add(createDto(transfers.size(), PS_STAKE.name(), 100, 120, OTHER, ADDR));
        transfers.add(createDto(transfers.size(), PS_EXIT.name(), 110, 90, OTHER, ADDR));
        transfers.add(createDto(transfers.size(), LP_SELL.name(), 110, 150, ADDR, OTHER));

        TransferDBService.calculateSellProfits(transfers, ADDR);

        assertDto(transfers.get(0), "", "", true);
        assertDto(transfers.get(1), "", "", true);
        assertDto(transfers.get(2), "", "", true);
        assertDto(transfers.get(3), "33,3", "5000,0", false);
    }

    @Test
    public void testCalculatePsProfit() {
        double profit = 0;
        List<TransferDTO> transfers = new ArrayList<>();
        transfers.add(createPsStake(transfers.size(), 100, 100));
        transfers.add(createPsExit(transfers.size(), 75, 25));

        profit = TransferDBService.calculatePsProfit(transfers);
        assertEquals("profit", "0,000000", String.format("%.6f", profit));

        transfers.add(createPsExit(transfers.size(), 26, 0));
        profit = TransferDBService.calculatePsProfit(transfers);
        assertEquals("profit", "1,000000", String.format("%.6f", profit));

        transfers.add(createPsStake(transfers.size(), 100, 100));
        transfers.add(createPsExit(transfers.size(), 20, 80));
        profit = TransferDBService.calculatePsProfit(transfers);
        assertEquals("profit", "0,000000", String.format("%.6f", profit));

        transfers.add(createPsExit(transfers.size(), 100, 0));
        profit = TransferDBService.calculatePsProfit(transfers);
        assertEquals("profit", "20,000000", String.format("%.6f", profit));

        transfers.add(createPsStake(transfers.size(), 100, 100));
        transfers.add(createPsStake(transfers.size(), 100, 200));
        transfers.add(createPsExit(transfers.size(), 210, 0));
        profit = TransferDBService.calculatePsProfit(transfers);
        assertEquals("profit", "10,000000", String.format("%.6f", profit));
    }

    private void assertDto(TransferDTO dto, String profit, String profitUsd, boolean nullProfit) {
        if (nullProfit && dto.getProfit() == null) {
            return;
        }
        assertAll(
            () -> assertEquals(dto.getId() + " profit", profit, String.format("%.1f", dto.getProfit())),
            () -> assertEquals(dto.getId() + " profitUsd", profitUsd, String.format("%.1f", dto.getProfitUsd()))
        );
    }

    private TransferDTO createPsStake(int id, double value, double balance) {
        TransferDTO dto = new TransferDTO();
        dto.setId(id + "");
        dto.setType(PS_STAKE.name());
        dto.setValue(value);
        dto.setBalanceRecipient(balance);
        dto.setBalanceRecipient(balance);
        dto.setOwner(ADDR);
        dto.setRecipient(OTHER);
        return dto;
    }

    private TransferDTO createPsExit(int id, double value, double balance) {
        TransferDTO dto = new TransferDTO();
        dto.setId(id + "");
        dto.setType(PS_EXIT.name());
        dto.setValue(value);
        dto.setBalanceRecipient(balance);
        dto.setBalanceRecipient(balance);
        dto.setOwner(OTHER);
        dto.setRecipient(ADDR);
        return dto;
    }

    private TransferDTO createDto(int id, String type, double value, double price, String owner, String recipient) {
        TransferDTO dto = new TransferDTO();
        dto.setId(id + "");
        dto.setType(type);
        dto.setValue(value);
        dto.setPrice(price);
        dto.setOwner(owner);
        dto.setRecipient(recipient);
        return dto;
    }
}
