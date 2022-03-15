package pro.belbix.ethparser.web3.contracts;

import java.util.stream.Stream;
import lombok.Getter;

@Getter
public enum UniPairType {

  // 1inch ..
  ONE_INCH("1inch"),
  // Balancer ..
  BALANCER("Balancer"),
  // Curve.fi ..
  CURVE("Curve.fi"),
  // SushiSwap LP Token
  SUSHISWAP("SushiSwap"),
  // Pancake ..
  PANCACKE("Pancake"),
  // PancakeSwap ..
  PANCACKE_SWAP("PancakeSwap"),
  // Uniswap V2
  UNISWAP("Uniswap");

  UniPairType(String name) {
    this.name = name.toLowerCase();
  }

  private final String name;

  public static boolean isLpUniPair(String name) {
    if (Stream.of(PANCACKE_SWAP).anyMatch(i -> name.toLowerCase().startsWith(i.getName()))) {
      return false;
    }
    return Stream.of(ONE_INCH, SUSHISWAP, UNISWAP, PANCACKE).anyMatch(i -> name.toLowerCase().startsWith(i.getName()));
  }

  public static boolean isBalancer(String name) {
    return Stream.of(BALANCER).anyMatch(i -> name.toLowerCase().startsWith(i.getName()));
  }

  public static boolean isCurve(String name) {
    return Stream.of(CURVE).anyMatch(i -> name.toLowerCase().startsWith(i.getName()));
  }
}
