package pro.belbix.ethparser.web3.harvest.log;

import static pro.belbix.ethparser.web3.contracts.ContractConstants.CONTROLLERS;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.CONTROLLER_CREATION_BLOCK;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.web3j.abi.TypeEncoder;
import org.web3j.abi.datatypes.Address;
import org.web3j.protocol.core.methods.response.Log;
import pro.belbix.ethparser.codegen.GeneratedContract;
import pro.belbix.ethparser.codegen.SimpleContractGenerator;
import pro.belbix.ethparser.model.tx.HardWorkTx;
import pro.belbix.ethparser.web3.Web3Functions;
import pro.belbix.ethparser.web3.harvest.decoder.HardWorkLogDecoder;

@Log4j2
@Service
public class IdleTimeService {

  private final Web3Functions web3Functions;
  private final HardWorkLogDecoder hardWorkLogDecoder = new HardWorkLogDecoder();
  private final SimpleContractGenerator simpleContractGenerator;


  public IdleTimeService(Web3Functions web3Functions,
      SimpleContractGenerator simpleContractGenerator
  ) {
    this.web3Functions = web3Functions;
    this.simpleContractGenerator = simpleContractGenerator;
  }

  public long getLastEventBlockDate(String network, String vault, int toBlock) {
    log.info("Reading last call time from blockchain");

    GeneratedContract controllerContract = simpleContractGenerator.getContract(
        CONTROLLERS.get(network),null, null, network);

    Address address = new Address(vault);
    String vaultAddressEncoded = "0x" + TypeEncoder.encode(address);

    var logs = web3Functions.findLastLogBatchByEventMandatoryTopics(
        CONTROLLERS.get(network),
        CONTROLLER_CREATION_BLOCK.get(network), toBlock, network,
        controllerContract.getEventHashByName("SharePriceChangeLog"),
        vaultAddressEncoded);

    if (logs.size() > 0) {
      log.info("Loaded " + logs.size() + " log entries");
      var lastLog = logs.get(logs.size() - 1);
      HardWorkTx tx = hardWorkLogDecoder.decode((Log) lastLog);
      return tx.getBlockDate();
    }else {
      log.info("Unable to events for controller contact with params : "
          + controllerContract.getEventHashByName("SharePriceChangeLog"),
          " " + vaultAddressEncoded);
      return 0;
    }

  }

}
