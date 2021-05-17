package pro.belbix.ethparser.web3.deployer.transform;

import static pro.belbix.ethparser.web3.abi.FunctionsNames.GET_CURRENT_TOKENS;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.SYMBOL;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.TOKEN0;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.TOKEN1;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.ZERO_ADDRESS;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.protocol.ObjectMapperFactory;
import pro.belbix.ethparser.web3.abi.FunctionsNames;
import pro.belbix.ethparser.web3.abi.FunctionsUtils;
import pro.belbix.ethparser.web3.contracts.ContractUtils;
import pro.belbix.ethparser.web3.deployer.ContractInfo;

@Service
public class ContractNameCreator {

  private final FunctionsUtils functionsUtils;

  public ContractNameCreator(FunctionsUtils functionsUtils) {
    this.functionsUtils = functionsUtils;
  }

  public String tokenNames(ContractInfo contractInfo) {
    String tokenNames = uniTokenNames(contractInfo);
    if (tokenNames.isBlank()) {
      try {
        tokenNames = bptTokenNames(contractInfo);
      } catch (IOException | ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
    }
    return tokenNames;
  }

  private String bptTokenNames(ContractInfo contractInfo)
      throws IOException, ClassNotFoundException {
    String address = contractInfo.getUnderlyingAddress();
    long block = contractInfo.getBlock();
    String network = contractInfo.getNetwork();

    //noinspection unchecked
    String tokens = functionsUtils.callViewFunction(new Function(
            GET_CURRENT_TOKENS,
            List.of(),
            Collections.singletonList(TypeReference.makeTypeReference("address[]"))
        ),
        address, block, network)
        .orElse("");
    if (tokens.isBlank()) {
      return "";
    }

    //noinspection unchecked
    List<String> tokenAddresses = ObjectMapperFactory.getObjectMapper().readValue(
        (String) ObjectMapperFactory.getObjectMapper().readValue(tokens, List.class).get(0)
        , List.class);
    contractInfo.getUnderlyingTokens().addAll(tokenAddresses);
    return tokenAddresses.stream()
        .map(adr -> functionsUtils.callStrByName(
            SYMBOL, adr, block, network)
            .orElse("?"))
        .collect(Collectors.joining("_"));
  }

  public String uniTokenNames(ContractInfo contractInfo) {
    String address = contractInfo.getUnderlyingAddress();
    long block = contractInfo.getBlock();
    String network = contractInfo.getNetwork();

    String token0Adr = functionsUtils.callAddressByName(
        TOKEN0, address, block, network)
        .orElse(null);
    if (token0Adr == null) {
      return "";
    }
    String token1Adr = functionsUtils.callAddressByName(
        TOKEN1, address, block, network)
        .orElse(null);
    if (token1Adr == null) {
      return "";
    }

    contractInfo.getUnderlyingTokens().add(token0Adr);
    contractInfo.getUnderlyingTokens().add(token1Adr);

    String token0Name;
    if (ZERO_ADDRESS.equalsIgnoreCase(token0Adr)) {
      token0Name = ContractUtils.getBaseNetworkWrappedTokenName(network);
    } else {
      token0Name = functionsUtils.callStrByName(
          FunctionsNames.SYMBOL, token0Adr, block, network)
          .orElse("?");
    }

    String token1Name;
    if (ZERO_ADDRESS.equalsIgnoreCase(token1Adr)) {
      token1Name = ContractUtils.getBaseNetworkWrappedTokenName(network);
    } else {
      token1Name = functionsUtils.callStrByName(
          FunctionsNames.SYMBOL, token1Adr, block, network)
          .orElse("?");
    }
    return token0Name + "_" + token1Name;
  }

}
