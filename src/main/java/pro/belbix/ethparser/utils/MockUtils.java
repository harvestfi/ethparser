package pro.belbix.ethparser.utils;

import static pro.belbix.ethparser.model.UniswapTx.ADD_LIQ;
import static pro.belbix.ethparser.model.UniswapTx.REMOVE_LIQ;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import pro.belbix.ethparser.dto.v0.HardWorkDTO;
import pro.belbix.ethparser.dto.v0.HarvestDTO;
import pro.belbix.ethparser.dto.v0.ImportantEventsDTO;
import pro.belbix.ethparser.dto.v0.UniswapDTO;
import pro.belbix.ethparser.web3.contracts.ContractUtils;
import pro.belbix.ethparser.web3.harvest.decoder.HarvestVaultLogDecoder;

public class MockUtils {
    private static final List<String> harvestMethods =
        new ArrayList<>(new HarvestVaultLogDecoder().getMethodNamesByMethodId().values());

    public static UniswapDTO createUniswapDTO(long seed) {
        double currentCount = seed * new Random().nextDouble();
        UniswapDTO uniswapDTO = new UniswapDTO();
        uniswapDTO.setId("0x" + (seed * 1000000));
        uniswapDTO.setAmount(currentCount);
        uniswapDTO.setOtherAmount(currentCount);
        uniswapDTO.setCoin("FARM");
        uniswapDTO.setOtherCoin("ETH");
        uniswapDTO.setHash("0x" + seed);
        uniswapDTO.setType(new Random().nextBoolean() ?
            new Random().nextBoolean() ? "BUY" : "SELL" :
            new Random().nextBoolean() ? ADD_LIQ : REMOVE_LIQ);
        uniswapDTO.setPrice(currentCount);
        uniswapDTO.setConfirmed(new Random().nextBoolean());
        uniswapDTO.setLastGas(currentCount / 6);
        uniswapDTO.setBlockDate(Instant.now().plus(seed, ChronoUnit.MINUTES).getEpochSecond());
        return uniswapDTO;
    }

    public static HarvestDTO createHarvestDTO(long seed) {
        double currentCount = seed * new Random().nextDouble();
        HarvestDTO harvestDTO = new HarvestDTO();
        harvestDTO.setAmount(currentCount * 10000);
        harvestDTO.setUsdAmount((long) currentCount * 100);
        harvestDTO.setVault(new ArrayList<>(ContractUtils.getAllVaultNames())
            .get(new Random().nextInt(ContractUtils.getAllVaultNames().size() - 1)));
        harvestDTO.setId("0x" + (seed * 1000000));
        harvestDTO.setHash("0x" + seed);
        harvestDTO.setMethodName(harvestMethods.get(new Random().nextInt(harvestMethods.size() - 1)));
        harvestDTO.setLastUsdTvl(currentCount * 1000000);
        harvestDTO.setConfirmed(1);
        harvestDTO.setLastGas(currentCount / 6);
        harvestDTO.setBlockDate(Instant.now().plus(seed, ChronoUnit.MINUTES).getEpochSecond());
        harvestDTO.setLastAllUsdTvl(seed * 5.1);
        return harvestDTO;
    }

    public static HardWorkDTO createHardWorkDTO(long seed) {
        HardWorkDTO hardWorkDTO = new HardWorkDTO();
        hardWorkDTO.setId("0x" + (seed * 1000000));
        hardWorkDTO.setVault(new ArrayList<>(ContractUtils.getAllVaultNames())
            .get(new Random().nextInt(ContractUtils.getAllVaultNames().size() - 1)));
        hardWorkDTO.setBlockDate(Instant.now().plus(seed, ChronoUnit.MINUTES).getEpochSecond());
        hardWorkDTO.setShareChange(seed / 1000.0);
        hardWorkDTO.setFullRewardUsd(seed / 69.0);
        hardWorkDTO.setFullRewardUsdTotal(seed);
        hardWorkDTO.setTvl(seed * 60);
        hardWorkDTO.setPerc((double) seed / 633.0);
        hardWorkDTO.setPsApr((double) seed / 63.0);
        return hardWorkDTO;
    }

    public static ImportantEventsDTO createImportantEventsDTO(long seed) {
        ImportantEventsDTO dto = new ImportantEventsDTO();
        dto.setId(seed + "id");
        dto.setHash(seed + "hash");
        dto.setBlock(seed);
        dto.setBlockDate(Instant.now().getEpochSecond());
        dto.setEvent("StrategyChanged");
        dto.setOldStrategy("oldsStr");
        dto.setNewStrategy("newStr");
        dto.setVault("vault");
        dto.setMintAmount(seed * 0.3);
        dto.setInfo("{}");
        return dto;
    }

}
