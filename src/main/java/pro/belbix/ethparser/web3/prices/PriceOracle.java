package pro.belbix.ethparser.web3.prices;

import static java.util.Objects.requireNonNullElse;
import static pro.belbix.ethparser.utils.Caller.silentCall;
import static pro.belbix.ethparser.web3.FunctionsNames.TOTAL_SUPPLY;
import static pro.belbix.ethparser.web3.FunctionsNames.UNDERLYING;
import static pro.belbix.ethparser.web3.FunctionsNames.GET_PRICE;
import static pro.belbix.ethparser.web3.MethodDecoder.parseAmount;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.ZERO_ADDRESS;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.ORACLE;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.D18;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import lombok.extern.log4j.Log4j2;

import org.apache.commons.math3.analysis.FunctionUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.web3j.tuples.generated.Tuple2;
import pro.belbix.ethparser.dto.v0.PriceDTO;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.repositories.v0.PriceRepository;
import pro.belbix.ethparser.utils.Caller;
import pro.belbix.ethparser.web3.FunctionsUtils;
import pro.belbix.ethparser.web3.contracts.ContractConstants;
import pro.belbix.ethparser.web3.contracts.ContractType;
import pro.belbix.ethparser.web3.contracts.ContractUtils;
import pro.belbix.ethparser.web3.prices.db.PriceDBService;
import pro.belbix.ethparser.web3.EthBlockService;

@Service
@Log4j2
public class PriceOracle {

    private final Map<String, TreeMap<Long, Double>> lastPrices = new HashMap<>();
    private long updateBlockDifference = 0;
    private final Pageable limitOne = PageRequest.of(0, 1);

    private final FunctionsUtils functionsUtils;
    private final PriceRepository priceRepository;
    private final AppProperties appProperties;
    private final PriceDBService priceDBService;
    private final EthBlockService ethBlockService;

    public PriceOracle(FunctionsUtils functionsUtils, PriceRepository priceRepository,
                         AppProperties appProperties, PriceDBService priceDBService,
                         EthBlockService ethBlockService) {
        this.functionsUtils = functionsUtils;
        this.priceRepository = priceRepository;
        this.appProperties = appProperties;
        this.priceDBService = priceDBService;
        this.ethBlockService = ethBlockService;
    }

    // you can use Vault name instead of coinName if it is not a LP
    public Double getPriceForCoin(String coinName, long block) {
            if (ZERO_ADDRESS.equalsIgnoreCase(coinName)) {
                return 0.0;
            }
            if (coinName.startsWith("0x")) {
                coinName = ContractUtils.getNameByAddress(coinName)
                    .orElseThrow(() -> new IllegalStateException("Wrong input"));
            }
            String coinNameSimple = ContractConstants.simplifyName(coinName);
            updateUSDPrice(coinNameSimple, block);
       
            return getLastPrice(coinNameSimple, block);
    }

    private void updateUSDPrice(String coinName, long block) {
        String tokenAdr = ContractUtils.getAddressByName(coinName, ContractType.TOKEN)
            .orElseGet(() -> ContractUtils.getAddressByName(coinName, ContractType.UNI_PAIR)
            .orElseThrow(() -> new IllegalStateException("Not found address for " + coinName)));

        //if (!ContractUtils.isTokenCreated(coinName, block)) {
        //    savePrice(0.0, coinName, block, tokenAdr);
        //    return;
        //}
        
        if (hasFreshPrice(coinName, block)) {
            return;
        }

        double price = getPriceForCoinWithoutCache(coinName, block, tokenAdr);

        savePrice(price, coinName, block, tokenAdr);
    }

    private double getPriceForCoinWithoutCache(String name, Long block, String tokenAdr) {

        PriceDTO priceDTO = silentCall(() -> priceRepository.fetchLastPrice("ORACLE", block, limitOne))
            .filter(Caller::isFilledList)
            .map(l -> l.get(0))
            .orElse(null);
        if (priceDTO == null) {
            log.warn("Saved price not found for " + name + " at block " + block);
            return getPriceForCoinFromEth(name, block, tokenAdr);
        }
        if (block - priceDTO.getBlock() > 1000) {
            log.warn("Price have not updated more then {} for {}", block - priceDTO.getBlock(), name);
            return getPriceForCoinFromEth(name, block, tokenAdr);
        }
        
        return getPriceForCoinFromEth(name, block, tokenAdr);
    }

    private double getPriceForCoinFromEth(String name, Long block, String tokenAdr) {
        if (appProperties.isOnlyApi()) {
            return 0.0;
        }
        

        double price = functionsUtils.callIntByName(GET_PRICE, tokenAdr, ORACLE, block)
        .orElseThrow(() -> new IllegalStateException("Can't fetch price for " + name)).doubleValue();
        return price / D18;
    }

    private boolean hasFreshPrice(String name, long block) {
        TreeMap<Long, Double> lastPriceByBlock = lastPrices.get(name);
        if (lastPriceByBlock == null) {
            return false;
        }

        Entry<Long, Double> entry = lastPriceByBlock.floorEntry(block);
        if (entry == null || Math.abs(entry.getKey() - block) >= updateBlockDifference) {
            return false;
        }
        return entry.getValue() != null && entry.getValue() != 0;
    }

    private void savePrice(double price, String name, long block, String tokenAdr) {
        TreeMap<Long, Double> lastPriceByBlock = lastPrices.computeIfAbsent(name, k -> new TreeMap<>());
        lastPriceByBlock.put(block, price);

        if (price > 0.0) {
            PriceDTO dto = enrichPriceDTO(name, block, price, tokenAdr);
            boolean success = priceDBService.savePriceDto(dto);
            if (success) {
                log.info("Saved " + dto.print());
            }
        }

    }

    private double getLastPrice(String name, long block) {
        TreeMap<Long, Double> lastPriceByBlocks = lastPrices.get(name);
        if (lastPriceByBlocks == null) {
            return 0.0;
        }
        Entry<Long, Double> entry = lastPriceByBlocks.floorEntry(requireNonNullElse(block, 0L));
        if (entry != null && entry.getValue() != null) {
            return entry.getValue();
        }
        return 0.0;
    }

    public PriceDTO enrichPriceDTO(String name, Long block, Double price, String tokenAdr) {
        PriceDTO dto = new PriceDTO();
        dto.setSource("ORACLE");
        dto.setId(tokenAdr + "_" + block);
        dto.setBlock(block);
        dto.setPrice(price);
        //dto.setBuy(0); 
        dto.setBlockDate(
            ethBlockService.getTimestampSecForBlockByNumber(block));
        return dto;
    }
}