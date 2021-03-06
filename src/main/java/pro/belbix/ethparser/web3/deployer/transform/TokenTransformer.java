package pro.belbix.ethparser.web3.deployer.transform;

import static pro.belbix.ethparser.web3.abi.FunctionsNames.NAME;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.ZERO_ADDRESS;
import static pro.belbix.ethparser.web3.contracts.ContractType.TOKEN;
import static pro.belbix.ethparser.web3.contracts.ContractType.UNI_PAIR;

import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.web3.EthBlockService;
import pro.belbix.ethparser.web3.abi.FunctionService;
import pro.belbix.ethparser.web3.abi.FunctionsNames;
import pro.belbix.ethparser.web3.abi.FunctionsUtils;
import pro.belbix.ethparser.web3.contracts.ContractUtils;
import pro.belbix.ethparser.web3.contracts.models.LpContract;
import pro.belbix.ethparser.web3.contracts.models.PureEthContractInfo;
import pro.belbix.ethparser.web3.contracts.models.TokenContract;
import pro.belbix.ethparser.web3.deployer.ContractInfo;
import pro.belbix.ethparser.web3.prices.LPSeeker;

@Service
@Log4j2
public class TokenTransformer {

  private final FunctionsUtils functionsUtils;
  private final UnderlyingTransformer underlyingTransformer;
  private final FunctionService functionService;
  private final LPSeeker lpSeeker;
  private final EthBlockService ethBlockService;
  final ContractNameCreator contractNameCreator;

  public TokenTransformer(
      FunctionsUtils functionsUtils,
      UnderlyingTransformer underlyingTransformer,
      FunctionService functionService,
      LPSeeker lpSeeker,
      EthBlockService ethBlockService,
      ContractNameCreator contractNameCreator) {
    this.functionsUtils = functionsUtils;
    this.underlyingTransformer = underlyingTransformer;
    this.functionService = functionService;
    this.lpSeeker = lpSeeker;
    this.ethBlockService = ethBlockService;
    this.contractNameCreator = contractNameCreator;
  }

  public void createTokenAndLpContracts(
      String address,
      long block,
      String network,
      List<PureEthContractInfo> contracts
  ) {
    if (Strings.isBlank(address)) {
      return;
    }
    if (functionService.isLp(address, block, network)) {
      createSimpleLpContract(address, block, network, contracts);
    } else {
      TokenContract tokenContract =
          createTokenContracts(address, block, network, contracts);
      if (tokenContract != null) {
        createLpContracts(address, block, network, contracts, tokenContract);
      }
    }
  }

  private TokenContract createTokenContracts(
      String address,
      long block,
      String network,
      List<PureEthContractInfo> contracts
  ) {
    if (ZERO_ADDRESS.equalsIgnoreCase(address)
        || contracts.stream().anyMatch(c -> c.getAddress().equalsIgnoreCase(address))) {
      return null;
    }

    String curveUnderlying = underlyingTransformer
        .curveUnderlyingContracts(address, block, network);
    if (curveUnderlying != null) {
      createTokenAndLpContracts(curveUnderlying, block, network, contracts);
    }
    String symbol = functionsUtils.callStrByName(
        FunctionsNames.SYMBOL, address, block, network)
        .orElse("?");

    TokenContract tokenContract = new TokenContract((int) block, symbol, address);
    tokenContract.setNetwork(network);
    tokenContract.setContractType(TOKEN);
    tokenContract.setCurveUnderlying(curveUnderlying);
    addInContracts(contracts, tokenContract);
    return tokenContract;
  }


  private void createSimpleLpContract(
      String address,
      long block,
      String network,
      List<PureEthContractInfo> contracts
  ) {
    if (ZERO_ADDRESS.equalsIgnoreCase(address)
        || contracts.stream().anyMatch(c -> c.getAddress().equalsIgnoreCase(address))) {
      return;
    }

    String symbol = lpName(address, block, network);

    LpContract lpContract = new LpContract((int) block, symbol, null, address);
    lpContract.setNetwork(network);
    lpContract.setContractType(UNI_PAIR);
    addInContracts(contracts, lpContract);
  }

  private void createLpContracts(
      String address,
      long block,
      String network,
      List<PureEthContractInfo> contracts,
      TokenContract tokenContract
  ) {
    if (tokenContract.getContractType() == UNI_PAIR // lp for lp is forbidden
        || tokenContract.getCurveUnderlying() != null // curve token doesn't have UNI LP
        || ContractUtils.isStableCoin(address) // lp for stablecoin is forbidden
    ) {
      return;
    }

//     use +100k blocks for more accurate LP fetching
//    block = Math.min(block + 100_000, ethBlockService.getLastBlock(network));
    String lpAddress = lpSeeker.findLargestLP(address, block, network, contracts);
    if (lpAddress == null) {
      log.error("Not found lp for {} {} {}", address, block, network);
      return;
    }

    tokenContract.addLp((int) block, lpAddress);

    if (contracts.stream()
        .anyMatch(c -> c.getAddress().equalsIgnoreCase(lpAddress))) {
      return;
    }
    ContractInfo lpContractInfo = new ContractInfo(lpAddress, block, network, UNI_PAIR);
    lpContractInfo.setUnderlyingAddress(lpAddress);
    String lpFullName = lpName(lpAddress, block, network);

    LpContract lpContract = new LpContract((int) block, lpFullName, address, lpAddress);
    lpContract.setContractType(UNI_PAIR);
    lpContract.setNetwork(network);
    addInContracts(contracts, lpContract);

    contractNameCreator.tokenNames(lpContractInfo);
    for (var t : lpContractInfo.getUnderlyingTokens()) {
      if (t.equalsIgnoreCase(address)) {
        return;
      }
      createTokenAndLpContracts(t, block, network, contracts);
    }

  }

  private String lpName(String lpAddress, long block, String network) {
    String lpName = functionsUtils.callStrByName(
        NAME, lpAddress, block, network)
        .orElse("");

    PlatformType lpPlatformType = PlatformType.valueOfName(lpName);
    ContractInfo lpContractInfo =
        new ContractInfo(lpAddress, block, network, UNI_PAIR);
    lpContractInfo.setUnderlyingAddress(lpAddress);
    String tokenNames = contractNameCreator.uniTokenNames(lpContractInfo);
    return lpPlatformType.getPrettyName() + "_LP_" + tokenNames;
  }

  private void addInContracts(List<PureEthContractInfo> contracts, PureEthContractInfo c) {
    if (contracts.stream()
        .anyMatch(exC -> exC.getAddress().equalsIgnoreCase(c.getAddress()))
    ) {
      return;
    }
    contracts.add(c);
  }

}
