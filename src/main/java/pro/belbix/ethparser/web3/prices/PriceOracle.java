package pro.belbix.ethparser.web3.prices;

import static pro.belbix.ethparser.service.AbiProviderService.BSC_NETWORK;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.GET_LARGEST_POOL;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.GET_PRICE;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.D18;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.Function;
import org.web3j.protocol.ObjectMapperFactory;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.web3.abi.FunctionsUtils;
import pro.belbix.ethparser.web3.contracts.ContractConstants;
import pro.belbix.ethparser.web3.contracts.ContractUtils;

@Service
@Log4j2
public class PriceOracle {

    private final FunctionsUtils functionsUtils;
    private final AppProperties appProperties;

    public PriceOracle(FunctionsUtils functionsUtils,
                         AppProperties appProperties) {
        this.functionsUtils = functionsUtils;
        this.appProperties = appProperties;

    }

    public double getPriceForCoinOnChain(String tokenAdr, Long block, String network) {
        if (appProperties.isOnlyApi()) {
            return 0.0;
        }
        String oracleAddress = getOracleAddress(tokenAdr, block, network);
        if (oracleAddress == null) {
            throw new IllegalStateException("Not found oracle for " + tokenAdr + " at " + block);
        }
        double price = functionsUtils
            .callIntByNameWithAddressArg(GET_PRICE, tokenAdr, oracleAddress, block, network)
            .orElseThrow(() -> new IllegalStateException(
                "Can't fetch price for " + tokenAdr))
            .doubleValue();

        return price / D18;
    }

    public String getLargestKeyToken(String tokenAddress, long block, String network) {
        String oracleAddress = getOracleAddress(tokenAddress, block, network);

        List<Address> tokenList = ContractConstants.KEY_TOKENS.get(network).stream()
            .map(Address::new)
            .collect(Collectors.toList());
        try {
            // noinspection unchecked
            String resultRaw = functionsUtils
                .callViewFunction(new Function(
                        GET_LARGEST_POOL,
                        List.of(new Address(tokenAddress),
                            new DynamicArray<Address>(Address.class, tokenList)),
                        List.of(
                            TypeReference.makeTypeReference("address"),
                            TypeReference.makeTypeReference("address"),
                            TypeReference.makeTypeReference("bool"),
                            TypeReference.makeTypeReference("bool")
                        )
                    ),
                    oracleAddress, block, network)
                .orElseThrow(() -> new IllegalStateException(
                    "Can't fetch pool for " + tokenAddress));
            List<?> results = ObjectMapperFactory.getObjectMapper()
                .readValue(resultRaw, List.class);
            if (results.get(2).equals("false") && results.get(3).equals("false")) {
                // it is curve
                return null;
            }
            return (String) results.get(0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getOracleAddress(String tokenAddress, long block, String network) {
        if (BSC_NETWORK.equals(network)) {
            Optional<String> factory =
                functionsUtils.callStrByName("factory", tokenAddress, block, network);
            //noinspection OptionalIsPresent
            if (factory.isPresent()) {
                return ContractUtils.getPriceOracleByFactory(factory.get(), network);
            } else {
                return ContractUtils.getPriceOracle(block, network);
            }
        } else {
            return ContractUtils.getPriceOracle(block, network);
        }
    }

    public static boolean isAvailable(long block, String network) {
        return ContractUtils.getPriceOracle(block, network) != null;
    }
}
