package pro.belbix.ethparser.web3.harvest.strategy;

import static pro.belbix.ethparser.web3.abi.FunctionsNames.INVESTED_UNDERLYING_BALANCE;

import org.springframework.stereotype.Service;
import pro.belbix.ethparser.model.StratInfo;
import pro.belbix.ethparser.web3.Web3Functions;
import pro.belbix.ethparser.web3.abi.FunctionsUtils;

@Service
public class StratInfoCollector {

  private final Web3Functions web3Functions;
  private final FunctionsUtils functionsUtils;


  public StratInfoCollector(Web3Functions web3Functions,
      FunctionsUtils functionsUtils) {
    this.web3Functions = web3Functions;
    this.functionsUtils = functionsUtils;
  }

  public StratInfo collect(String address, long block, String network) {

    functionsUtils.callIntByName(INVESTED_UNDERLYING_BALANCE, address, block, network);


    return null;
  }

}
