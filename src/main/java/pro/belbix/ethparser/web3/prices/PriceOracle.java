package pro.belbix.ethparser.web3.prices;

import static pro.belbix.ethparser.utils.Caller.silentCall;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.GET_PRICE;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.ORACLE;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.D18;

import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.dto.v0.PriceDTO;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.repositories.v0.PriceRepository;
import pro.belbix.ethparser.utils.Caller;
import pro.belbix.ethparser.web3.abi.FunctionsUtils;
import pro.belbix.ethparser.web3.contracts.ContractType;
import pro.belbix.ethparser.web3.contracts.ContractUtils;
import pro.belbix.ethparser.web3.prices.db.PriceDBService;
import pro.belbix.ethparser.web3.EthBlockService;

@Service
@Log4j2
public class PriceOracle {

    private final Pageable limitOne = PageRequest.of(0, 1);
    public static final String ORACLE_SOURCE = "ORACLE";

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
    
    // you can use token or LP tokenName, or address
    public double getPriceForCoin(String coin, Long block) {
        String tokenName;
        String tokenAdr;
        if (coin.startsWith("0x")) {
            tokenName = ContractUtils.getNameByAddress(coin)
            .orElseThrow(() -> new IllegalStateException("Not found lp tokenName for " + coin));
            tokenAdr = coin;
        } else {
            tokenAdr = ContractUtils.getAddressByName(coin, ContractType.TOKEN)
            .orElseGet(() -> ContractUtils.getAddressByName(coin, ContractType.UNI_PAIR)
            .orElseThrow(() -> new IllegalStateException("Not found address for " + coin)));
            tokenName = coin;
        }
        
        PriceDTO priceDTO = silentCall(() -> priceRepository.fetchLastOraclePrice(tokenName, block, ORACLE_SOURCE, limitOne))
            .filter(Caller::isFilledList)
            .map(l -> l.get(0))
            .orElse(null);
        if (priceDTO == null) {
            log.warn("Saved price not found for " + tokenName + " at block " + block);
            return getPriceForCoinFromEth(tokenName, tokenAdr, block);
        }
        if (block - priceDTO.getBlock() > 1000) {
            log.warn("Price have not updated more then {} for {}", block - priceDTO.getBlock(), tokenName);
            return getPriceForCoinFromEth(tokenName, tokenAdr, block);
        }
        
        return priceDTO.getPrice();
    }

    private double getPriceForCoinFromEth(String tokenName, String tokenAdr, Long block) {
        if (appProperties.isOnlyApi()) {
            return 0.0;
        }

        double price = functionsUtils.callIntByName(GET_PRICE, tokenAdr, ORACLE, block)
        .orElseThrow(() -> new IllegalStateException("Can't fetch price for " + tokenName)).doubleValue();
        
        price = price / D18;
        savePrice(price, tokenName, block, tokenAdr);
        return price;
    }

    private void savePrice(double price, String tokenName, long block, String tokenAdr) {
        PriceDTO dto = enrichPriceDTO(tokenName, block, price, tokenAdr);
        boolean success = priceDBService.savePriceDto(dto);
        if (success) {
            log.info("Saved " + dto.print());
        }
    }

    public PriceDTO enrichPriceDTO(String tokenName, Long block, Double price, String tokenAdr) {
        PriceDTO dto = new PriceDTO();
        dto.setSource(ORACLE_SOURCE);
        dto.setId(tokenAdr + "_" + block);
        dto.setBlock(block);
        dto.setPrice(price);
        dto.setToken(tokenName); 
        dto.setBlockDate(
            ethBlockService.getTimestampSecForBlockByNumber(block));
        return dto;
    }
}