package pro.belbix.ethparser.web3.deployer.transform;

import static pro.belbix.ethparser.web3.abi.FunctionsNames.COINS;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.GET_POOL_FROM_LP_TOKEN;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.MINTER;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.NAME;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.BELT_POOL_ADDRESS;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.CURVE_REGISTRY_ADDRESS;

import java.util.List;
import java.util.Set;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.IntType;
import org.web3j.abi.datatypes.generated.Int128;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.ObjectMapperFactory;
import pro.belbix.ethparser.web3.abi.FunctionsNames;
import pro.belbix.ethparser.web3.abi.FunctionsUtils;
import pro.belbix.ethparser.web3.contracts.ContractUtils;

@Service
@Log4j2
public class UnderlyingTransformer {

  private final static Set<String> excluded = Set.of(
      "0xD533a949740bb3306d119CC777fa900bA034cd52".toLowerCase() // CRV token
  );

  private final FunctionsUtils functionsUtils;

  public UnderlyingTransformer(FunctionsUtils functionsUtils) {
    this.functionsUtils = functionsUtils;
  }

  public String curveUnderlyingContracts(
      String address,
      long block,
      String network
  ) {
    if (excluded.contains(address)) {
      return null;
    }

    String name = functionsUtils.callStrByName(NAME, address, block, network).orElse("");
    PlatformType platformType = PlatformType.valueOfName(name);
    if (!platformType.isCurveFork()) {
      return null;
    }
    String underlyingToken = getFirstCurveUnderlyingToken(
        platformType, address, block, network);
    if (underlyingToken == null) {
      return null;
    }

    //try to determinate underlying
    String deeperUnderlyingAddress = getPotentiallyUnderlying(underlyingToken, block, network);
    if (!Strings.isBlank(deeperUnderlyingAddress)) {
      underlyingToken = deeperUnderlyingAddress;
    }
    return underlyingToken;
  }

  private String getFirstCurveUnderlyingToken(
      PlatformType platformType,
      String address,
      long block,
      String network
  ) {
    String minterAddress = functionsUtils.callAddressByName(MINTER, address, block, network)
        .orElse(null);

    if (minterAddress == null) {
      minterAddress = getMinterAddress(platformType, address, block, network);
      if (minterAddress == null) {
        //better than nothing -  use USD as underlying
        return ContractUtils.getUsdAddress(network);
      }
    }

    // curve has different attribute (they are killing me)
    String underlyingToken = getUnderlyingToken(
        minterAddress, block, network, new Int128(0));
    if (underlyingToken == null) {
      underlyingToken = getUnderlyingToken(
          minterAddress, block, network, new Uint256(0));
    }

    //better than nothing -  use USD as underlying
    if (underlyingToken == null) {
      return ContractUtils.getUsdAddress(network);
    }
    return underlyingToken;
  }

  private String getPotentiallyUnderlying(String address, long block, String network) {
    String result = functionsUtils.callAddressByName(FunctionsNames.TOKEN,
        address, block, network)
        .orElse(null);
    if (result == null) {
      // aave tokens
      result = functionsUtils.callAddressByName(FunctionsNames.UNDERLYING_ASSET_ADDRESS,
          address, block, network)
          .orElse(null);
    }
    return result;
  }

  private String getMinterAddress(
      PlatformType platformType,
      String address,
      long block,
      String network) {
    if (platformType == PlatformType.CURVE) {
      // use future block because it maybe not created yet
      return functionsUtils.callAddressByNameWithArg(
          GET_POOL_FROM_LP_TOKEN, address, CURVE_REGISTRY_ADDRESS,
          Math.max(block, 12_000_000L), network)
          .orElse(null);
    } else if (platformType == PlatformType.BELT) {
      // todo improve if we will have another pool
      return BELT_POOL_ADDRESS;
    }
    return null;
  }

  private String getUnderlyingToken(
      String minterAddress,
      long block,
      String network,
      IntType attr
  ) {
    try {
      //noinspection unchecked
      String coinRaw = functionsUtils.callViewFunction(new Function(
              COINS,
              List.of(attr),
              List.of(TypeReference.makeTypeReference("address"))
          ),
          minterAddress, block, network).orElse(null);
      if (coinRaw == null) {
        return null;
      }

      return (String) ObjectMapperFactory.getObjectMapper()
          .readValue(coinRaw, List.class)
          .get(0);
    } catch (Exception e) {
      log.info("Error get underlyingToken");
      return null;
    }
  }

}
