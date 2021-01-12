package pro.belbix.ethparser.web3.erc20.db;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertAll;
import static pro.belbix.ethparser.web3.erc20.TransferType.COMMON;
import static pro.belbix.ethparser.web3.erc20.TransferType.LP_ADD;
import static pro.belbix.ethparser.web3.erc20.TransferType.LP_BUY;
import static pro.belbix.ethparser.web3.erc20.TransferType.LP_RECEIVE;
import static pro.belbix.ethparser.web3.erc20.TransferType.LP_REM;
import static pro.belbix.ethparser.web3.erc20.TransferType.LP_SELL;
import static pro.belbix.ethparser.web3.erc20.TransferType.LP_SEND;
import static pro.belbix.ethparser.web3.erc20.TransferType.PS_EXIT;
import static pro.belbix.ethparser.web3.erc20.TransferType.PS_INTERNAL;
import static pro.belbix.ethparser.web3.erc20.TransferType.PS_STAKE;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import pro.belbix.ethparser.dto.TransferDTO;

public class TransferDBServiceTest {

    private static final String ADDR = "owner";
    private static final String OTHER = "recipient";

    @Test
    public void testCalculateProfit() {
        List<TransferDTO> transfers = new ArrayList<>();
        transfers.add(createDto(transfers.size(), COMMON.name(), 100, 1000, OTHER, ADDR));
        transfers.add(createDto(transfers.size(), LP_SELL.name(), 50, 500, ADDR, OTHER));
        transfers.add(createDto(transfers.size(), COMMON.name(), 25, 200, ADDR, OTHER));
        transfers.add(createDto(transfers.size(), LP_SELL.name(), 20, 5, ADDR, OTHER));
        transfers.add(createDto(transfers.size(), COMMON.name(), 100, 1500, OTHER, ADDR));
        transfers.add(createDto(transfers.size(), LP_SELL.name(), 10, 100, ADDR, OTHER));
        transfers.add(createDto(transfers.size(), PS_STAKE.name(), 100, 100, ADDR, OTHER));
        transfers.add(createDto(transfers.size(), PS_EXIT.name(), 100, 100, OTHER, OTHER));
        transfers.add(createDto(transfers.size(), PS_INTERNAL.name(), 100, 100, ADDR, OTHER));
        transfers.add(createDto(transfers.size(), LP_SEND.name(), 10, 90, ADDR, OTHER));
        transfers.add(createDto(transfers.size(), LP_RECEIVE.name(), 15, 70, OTHER, OTHER));
        transfers.add(createDto(transfers.size(), LP_ADD.name(), 20, 150, ADDR, OTHER));
        transfers.add(createDto(transfers.size(), LP_REM.name(), 10, 200, OTHER, ADDR)); //12
        transfers.add(createDto(transfers.size(), LP_BUY.name(), 10, 100, OTHER, ADDR));
        transfers.add(createDto(transfers.size(), LP_SELL.name(), 7, 200, ADDR, OTHER));
        transfers.add(createDto(transfers.size(), LP_SELL.name(), 7, 5, ADDR, OTHER));
        TransferDBService.calculateSellProfits(transfers, ADDR);

        assertDto(transfers.get(0), "0,0", "0,0");
        assertDto(transfers.get(1), "0,0", "-25000,0");
        assertDto(transfers.get(2), "0,0", "0,0");
        assertDto(transfers.get(3), "0,0", "-35900,0");
        assertDto(transfers.get(4), "0,0", "0,0");
        assertDto(transfers.get(5), "0,0", "-14142,9");
        assertDto(transfers.get(6), "0,0", "0,0");
        assertDto(transfers.get(7), "0,0", "0,0");
        assertDto(transfers.get(8), "0,0", "0,0");
        assertDto(transfers.get(9), "0,0", "0,0");
        assertDto(transfers.get(10), "0,0", "0,0");
        assertDto(transfers.get(11), "0,0", "0,0");
        assertDto(transfers.get(12), "0,0", "0,0");
        assertDto(transfers.get(13), "0,0", "0,0");
        assertDto(transfers.get(14), "0,0", "-8257,1");
        assertDto(transfers.get(15), "0,0", "-9622,1");

    }

    private void assertDto(TransferDTO dto, String profit, String profitUsd) {
        if (dto.getProfit() == null) {
            return;
        }
        assertAll(
            () -> assertEquals(dto.getId() + " profit", profit, String.format("%.1f", dto.getProfit())),
            () -> assertEquals(dto.getId() + " profitUsd", profitUsd, String.format("%.1f", dto.getProfitUsd()))
        );
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
