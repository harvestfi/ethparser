package pro.belbix.ethparser.web3.prices;

import static java.util.Objects.requireNonNullElse;
import static pro.belbix.ethparser.utils.Caller.silentCall;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.TOTAL_SUPPLY;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.UNDERLYING;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.GET_PRICE;
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
import pro.belbix.ethparser.web3.abi.FunctionsUtils;
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

    public double getPriceForCoinWithoutCache(String name, Long block) {

        PriceDTO priceDTO = silentCall(() -> priceRepository.fetchLastOraclePrice(name, block, limitOne))
            .filter(Caller::isFilledList)
            .map(l -> l.get(0))
            .orElse(null);
        if (priceDTO == null) {
            log.warn("Saved price not found for " + name + " at block " + block);
            return getPriceForCoinFromEth(name, block);
        }
        if (block - priceDTO.getBlock() > 1000) {
            log.warn("Price have not updated more then {} for {}", block - priceDTO.getBlock(), name);
            return getPriceForCoinFromEth(name, block);
        }
        
        return priceDTO.getPrice();
    }

    private double getPriceForCoinFromEth(String name, Long block) {
        if (appProperties.isOnlyApi()) {
            return 0.0;
        }

        String tokenAdr = ContractUtils.getAddressByName(name, ContractType.TOKEN)
        .orElseGet(() -> ContractUtils.getAddressByName(name, ContractType.UNI_PAIR)
        .orElseThrow(() -> new IllegalStateException("Not found address for " + name)));

        double price = functionsUtils.callIntByName(GET_PRICE, tokenAdr, ORACLE, block)
        .orElseThrow(() -> new IllegalStateException("Can't fetch price for " + name)).doubleValue();
        
        price = price / D18;
        savePrice(price, name, block, tokenAdr);
        return price;
    }

    private void savePrice(double price, String name, long block, String tokenAdr) {
        
        PriceDTO dto = enrichPriceDTO(name, block, price, tokenAdr);
        boolean success = priceDBService.savePriceDto(dto);
        if (success) {
            log.info("Saved " + dto.print());
        }
    }

    public PriceDTO enrichPriceDTO(String name, Long block, Double price, String tokenAdr) {
        PriceDTO dto = new PriceDTO();
        dto.setSource("ORACLE");
        dto.setId(tokenAdr + "_" + block);
        dto.setBlock(block);
        dto.setPrice(price);
        dto.setToken(name); 
        dto.setBlockDate(
            ethBlockService.getTimestampSecForBlockByNumber(block));
        return dto;
    }
}