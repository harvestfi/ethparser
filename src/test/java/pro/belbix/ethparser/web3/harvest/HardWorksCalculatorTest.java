package pro.belbix.ethparser.web3.harvest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.dto.HardWorkDTO;
import pro.belbix.ethparser.dto.HarvestDTO;
import pro.belbix.ethparser.repositories.HardWorkRepository;
import pro.belbix.ethparser.repositories.HarvestRepository;
import pro.belbix.ethparser.web3.EthBlockService;
import pro.belbix.ethparser.web3.prices.PriceProvider;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
public class HardWorksCalculatorTest {

    @MockBean
    private HarvestRepository harvestRepository;
    @MockBean
    private HardWorkRepository hardworkRepository;
    @MockBean
    private PriceProvider priceProvider;
    @MockBean
    private EthBlockService ethBlockService;

    @Autowired
    private HardWorkCalculator hardworkCalculator;

    final String fakeEthAddr = "0x8f566f82c13ffb1bc72169ddb7beb1b19a5726ff";

    final String fakeVault1 = "v1";
    final String fakeVault2 = "v2";
    final long fakeBlock1 = 1L;
    final long fakeBlock2 = 5L;

    final long fakeStartBlockDate1 = 1L;
    final long fakeEndBlockDate1 = 2L;
    final long fakeStartBlockDate2 = 5L;
    final long fakeEndBlockDate2 = 6L;
    final double fakeEthPrice = 2;

    final HarvestDTO harvest1 = mockHarvest(fakeEthAddr, fakeVault1, 1, fakeStartBlockDate1);
    final HarvestDTO harvest2 = mockHarvest(fakeEthAddr, fakeVault1, 0, fakeEndBlockDate1);
    final HarvestDTO harvest3 = mockHarvest(fakeEthAddr, fakeVault2, 1, fakeStartBlockDate1);
    final HarvestDTO harvest4 = mockHarvest(fakeEthAddr, fakeVault2, 0, fakeEndBlockDate1);
    final HarvestDTO harvest5 = mockHarvest(fakeEthAddr, fakeVault1, 1, fakeStartBlockDate2);
    final HarvestDTO harvest6 = mockHarvest(fakeEthAddr, fakeVault1, 0, fakeEndBlockDate2);

    @Before
    public void setup() {
        when(ethBlockService.getLastBlock()).thenReturn(fakeBlock2);
        when(ethBlockService.getTimestampSecForBlock(null, fakeBlock2)).thenReturn(fakeEndBlockDate2);
        when(priceProvider.getPriceForCoin("ETH", fakeBlock1))
            .thenReturn(fakeEthPrice);
        when(priceProvider.getPriceForCoin("ETH", fakeBlock2))
            .thenReturn(fakeEthPrice);
    }

    private HarvestDTO mockHarvest(String ethAddr, String vault, double balance, long blockDate) {
        HarvestDTO harvest = new HarvestDTO();
        harvest.setOwner(ethAddr);
        harvest.setVault(vault);
        harvest.setOwnerBalance(balance);
        harvest.setBlockDate(blockDate);
        return harvest;
    }

    private HardWorkDTO mockHardWork(String vault, long block, HarvestDTO harvestIn, HarvestDTO harvestOut) {
        HardWorkDTO hardwork = new HardWorkDTO();
        hardwork.setVault(vault);
        hardwork.setBlock(block);
        hardwork.setBlockDate(harvestOut.getBlockDate());

        when(hardworkRepository.findAllByVaultOrderByBlockDate(vault, harvestIn.getBlockDate(), harvestOut.getBlockDate()))
            .thenReturn(List.of(hardwork));

        return hardwork;
    }

    @Test
    public void shouldCalcHardWorksFeeByPeriodsAndVaults() {
        when(harvestRepository.fetchAllByOwner(fakeEthAddr, 0, fakeEndBlockDate2))
            .thenReturn(List.of(harvest1, harvest2, harvest3, harvest4, harvest5, harvest6));

        List<HardWorkDTO> hardworks = List.of(
            mockHardWork(fakeVault1, fakeBlock1, harvest1, harvest2),
            mockHardWork(fakeVault2, fakeBlock2, harvest3, harvest4),
            mockHardWork(fakeVault1, fakeBlock2, harvest5, harvest6)
        );

        double feeInUsd = hardworkCalculator.calculateTotalHardWorksFeeByOwner(fakeEthAddr);
        double expected = hardworks.size() * 0.1 * fakeEthPrice;
        assertEquals(expected, feeInUsd, 1e-4);
    }

    @Test
    public void shouldCalcHardWorksFeeWithHarvestsInRandomOrder() {
        when(harvestRepository.fetchAllByOwner(fakeEthAddr, 0, fakeEndBlockDate2))
            .thenReturn(List.of(harvest2, harvest4, harvest6, harvest1, harvest3, harvest5));

        List<HardWorkDTO> hardworks = List.of(
            mockHardWork(fakeVault1, fakeBlock1, harvest1, harvest2),
            mockHardWork(fakeVault2, fakeBlock2, harvest3, harvest4),
            mockHardWork(fakeVault1, fakeBlock2, harvest5, harvest6)
        );

        double feeInUsd = hardworkCalculator.calculateTotalHardWorksFeeByOwner(fakeEthAddr);
        double expected = hardworks.size() * 0.1 * fakeEthPrice;
        assertEquals(expected, feeInUsd, 1e-4);
    }

    @Test
    public void shouldIgnoreHardWorksFeeWhenNotMatchingHarvests() {
        when(harvestRepository.fetchAllByOwner(fakeEthAddr, 0, fakeEndBlockDate2))
            .thenReturn(List.of());

        Double feeInUsd = hardworkCalculator.calculateTotalHardWorksFeeByOwner(fakeEthAddr);
        Double expected = 0d;
        assertEquals(expected, feeInUsd);
    }

    @Test
    public void shouldIgnoreHardWorksFeeWhenNotMatchingPeriods() {
        when(harvestRepository.fetchAllByOwner(fakeEthAddr, 0, fakeEndBlockDate2))
            .thenReturn(List.of(harvest1, harvest2));

        Double feeInUsd = hardworkCalculator.calculateTotalHardWorksFeeByOwner(fakeEthAddr);
        Double expected = 0d;
        assertEquals(expected, feeInUsd);
    }

    @Test
    public void shouldIgnoreHardWorksFeeWhenNotMatchingVaults() {
        when(harvestRepository.fetchAllByOwner(fakeEthAddr, 0, fakeEndBlockDate2))
            .thenReturn(List.of(harvest1, harvest2));

        mockHardWork(fakeVault2, fakeBlock1, harvest3, harvest4);

        Double feeInUsd = hardworkCalculator.calculateTotalHardWorksFeeByOwner(fakeEthAddr);
        Double expected = 0d;
        assertEquals(expected, feeInUsd);
    }

    @Test
    public void shouldIgnoreHardWorksFeeWhenZeroHarvestBalance() {
        when(harvestRepository.fetchAllByOwner(fakeEthAddr, 0, fakeEndBlockDate2))
            .thenReturn(List.of(harvest2, harvest6));

        mockHardWork(fakeVault1, fakeBlock1, harvest1, harvest2);

        Double feeInUsd = hardworkCalculator.calculateTotalHardWorksFeeByOwner(fakeEthAddr);
        Double expected = 0d;
        assertEquals(expected, feeInUsd);
    }

    @Test
    public void shouldCalcHardWorksFeeWhenNoHarvestOut() {
        when(harvestRepository.fetchAllByOwner(fakeEthAddr, 0, fakeEndBlockDate2))
            .thenReturn(List.of(harvest1, harvest1));

        HardWorkDTO hardwork = new HardWorkDTO();
        hardwork.setVault(fakeVault1);
        hardwork.setBlock(fakeBlock1);

        when(hardworkRepository.findAllByVaultOrderByBlockDate(fakeVault1, harvest1.getBlockDate(), fakeEndBlockDate2))
            .thenReturn(List.of(hardwork));

        Double feeInUsd = hardworkCalculator.calculateTotalHardWorksFeeByOwner(fakeEthAddr);
        Double expected = 0.2d;
        assertEquals(expected, feeInUsd);
    }

}
