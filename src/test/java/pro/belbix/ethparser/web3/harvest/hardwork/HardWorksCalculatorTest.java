package pro.belbix.ethparser.web3.harvest.hardwork;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.dto.v0.HardWorkDTO;
import pro.belbix.ethparser.dto.v0.HarvestDTO;
import pro.belbix.ethparser.repositories.v0.HardWorkRepository;
import pro.belbix.ethparser.repositories.v0.HarvestRepository;
import pro.belbix.ethparser.web3.EthBlockService;
import pro.belbix.ethparser.web3.harvest.HardWorkCalculator;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
public class HardWorksCalculatorTest {

  @MockBean
  private HarvestRepository harvestRepository;
  @MockBean
  private HardWorkRepository hardworkRepository;
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

  @BeforeEach
    public void setup() {
      when(ethBlockService.getLastBlock(ETH_NETWORK)).thenReturn(fakeBlock2);
        when(ethBlockService.getTimestampSecForBlock(fakeBlock2, ETH_NETWORK)).thenReturn(fakeEndBlockDate2);
    }

    private HarvestDTO mockHarvest(String ethAddr, String vault, double balance, long blockDate) {
        HarvestDTO harvest = new HarvestDTO();
        harvest.setOwner(ethAddr);
        harvest.setVaultAddress(vault);
        harvest.setOwnerBalance(balance);
        harvest.setBlockDate(blockDate);
        return harvest;
    }

    private HardWorkDTO mockHardWork(String vault, long block, long blockDate) {
        HardWorkDTO hardwork = new HardWorkDTO();
        hardwork.setVaultAddress(vault);
        hardwork.setBlock(block);
        hardwork.setBlockDate(blockDate);
        hardwork.setEthPrice(fakeEthPrice);
        return hardwork;
    }

    @Test
    public void shouldCalcHardWorksFeeByPeriodsAndVaults() {
        when(harvestRepository.fetchAllByOwner(fakeEthAddr, 0, fakeEndBlockDate2, ETH_NETWORK))
            .thenReturn(List.of(harvest1, harvest2, harvest3, harvest4, harvest5, harvest6));

        List<HardWorkDTO> hardworks = List.of(
            mockHardWork(fakeVault1, fakeBlock1, harvest1.getBlockDate()),
            mockHardWork(fakeVault1, fakeBlock1, harvest2.getBlockDate()),
            mockHardWork(fakeVault2, fakeBlock2, harvest6.getBlockDate())
        );

        when(hardworkRepository.findAllByVaultOrderByBlockDate(fakeVault1,ETH_NETWORK, harvest1.getBlockDate(), harvest6.getBlockDate()))
            .thenReturn(List.copyOf(hardworks));

        double feeInUsd = hardworkCalculator.calculateTotalHardWorksFeeByOwner(fakeEthAddr, ETH_NETWORK);
        double expected = hardworks.size() * 0.1 * fakeEthPrice;
        assertEquals(expected, feeInUsd, 1e-4);
    }

    @Test
    public void shouldIgnoreHardWorksFeeWhenNotMatchingHarvests() {
        when(harvestRepository.fetchAllByOwner(fakeEthAddr, 0, fakeEndBlockDate2, ETH_NETWORK))
            .thenReturn(List.of());

        Double feeInUsd = hardworkCalculator.calculateTotalHardWorksFeeByOwner(fakeEthAddr, ETH_NETWORK);
        Double expected = 0d;
        assertEquals(expected, feeInUsd);
    }

    @Test
    public void shouldIgnoreHardWorksFeeNotMatchingPeriods() {
        when(harvestRepository.fetchAllByOwner(fakeEthAddr, 0, fakeEndBlockDate2, ETH_NETWORK))
            .thenReturn(List.of(harvest1, harvest2, harvest5, harvest6));

        List<HardWorkDTO> hardworks = List.of(
            mockHardWork(fakeVault1, fakeBlock1, 3L),
            mockHardWork(fakeVault1, fakeBlock1, harvest2.getBlockDate()),
            mockHardWork(fakeVault1, fakeBlock1, harvest5.getBlockDate())
        );

        when(hardworkRepository.findAllByVaultOrderByBlockDate(fakeVault1,ETH_NETWORK, harvest1.getBlockDate(), harvest6.getBlockDate()))
            .thenReturn(List.copyOf(hardworks));

        Double feeInUsd = hardworkCalculator.calculateTotalHardWorksFeeByOwner(fakeEthAddr, ETH_NETWORK);
        Double expected = 0.4d;
        assertEquals(expected, feeInUsd);
    }

    @Test
    public void shouldIgnoreHardWorksFeeWhenZeroHarvestBalance() {
        when(harvestRepository.fetchAllByOwner(fakeEthAddr, 0, fakeEndBlockDate2, ETH_NETWORK))
            .thenReturn(List.of(harvest2, harvest6));

        verify(hardworkRepository, times(0)).findAllByVaultOrderByBlockDate(anyString(),anyString(), anyLong(), anyLong());

        Double feeInUsd = hardworkCalculator.calculateTotalHardWorksFeeByOwner(fakeEthAddr, ETH_NETWORK);
        Double expected = 0d;
        assertEquals(expected, feeInUsd);
    }

    @Test
    public void shouldCalcHardWorksFeeWhenNoHarvestOut() {
        when(harvestRepository.fetchAllByOwner(fakeEthAddr, 0, fakeEndBlockDate2, ETH_NETWORK))
            .thenReturn(List.of(harvest1, harvest1));

        HardWorkDTO hardwork = mockHardWork(fakeVault1, fakeBlock1, harvest1.getBlockDate());

        when(hardworkRepository.findAllByVaultOrderByBlockDate(fakeVault1,ETH_NETWORK, harvest1.getBlockDate(), fakeEndBlockDate2))
            .thenReturn(List.of(hardwork));

        Double feeInUsd = hardworkCalculator.calculateTotalHardWorksFeeByOwner(fakeEthAddr, ETH_NETWORK);
        Double expected = 0.2d;
        assertEquals(expected, feeInUsd);
    }

}
