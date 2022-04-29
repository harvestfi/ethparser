package pro.belbix.ethparser.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ProfitUtils {
  private static final String ID_DELIMITER = "_";

  public static String toId(String vaultAddress, String block, String network) {
    return String.join(ID_DELIMITER, vaultAddress, block, network);
  }
}
