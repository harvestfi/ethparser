package pro.belbix.ethparser.codegen.abi;

import java.util.Map;

public class StaticAbiMap {

  public static final Map<String, String> MAP = Map.of(
      "0x514910771af9ca656af840dff83e8264ecf986ca".toLowerCase(), LinkAbi.ABI,
      "0x5fD7a4D33f23967E890Ae77DD4E065bC01db343B".toLowerCase(), Bsc1Inch.ABI
  );

}
