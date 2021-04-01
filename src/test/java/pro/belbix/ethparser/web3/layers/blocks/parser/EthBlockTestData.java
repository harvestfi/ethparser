package pro.belbix.ethparser.web3.layers.blocks.parser;

import lombok.Builder;

@Builder
public class EthBlockTestData {
  long blockNum;
  long blockTimestamp;
  int txSize;
  int txNum;
  long txIdx;
  String txValue;
  String txInput;
  String txStatus;
  String txHash;
  String txFrom;
  String txTo;
  String txContractAdr;
  int logSize;
  int logNum;
  long logIdx;
  long logTxIdx;
  String logData;
  String logType;
  String logAdr;
  String logTopic;
  String logTopics;
}
