package pro.belbix.ethparser.web3.layers.detector;

import lombok.Builder;

@Builder
public class ContractEventAssertion {

  int eventSize;
  String eventContractAddress;
  int txSize;
  String txAddress;
  int stateSize;
  String stateName;
  String stateValue;
  int logSize;
  int logIdx;
  String logAddress;
  String logName;
  String logMethodId;
  String logValues;
  String funcHex;
  String funcName;
  String funcData;

}
