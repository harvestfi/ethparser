package pro.belbix.ethparser.web3.deployer.transform;

public enum PlatformType {

  UNKNOWN(""),
  CURVE("CRV"),
  UNISWAP("UNI"),
  SUSHISWAP("SUSHI"),
  PANCAKESWAP("PCS"),
  ONEINCH("1INCH"),
  VENUS("VENUS"),
  ELLIPSIS("EPS"),
  BELT("BELT"),
  COMPOUND("COMP"),
  BALANCER("BPT");

  private final String prettyName;

  PlatformType(String prettyName) {
    this.prettyName = prettyName;
  }

  public String getPrettyName() {
    return prettyName;
  }

  public boolean isUnknown() {
    return UNKNOWN == this;
  }

  public static PlatformType valueOfName(String name) {
    if (name == null) {
      return UNKNOWN;
    }
    if (name.startsWith("Curve")) {
      return PlatformType.CURVE;
    } else if (name.startsWith("Uniswap")) {
      return PlatformType.UNISWAP;
    } else if (name.startsWith("SushiSwap")) {
      return PlatformType.SUSHISWAP;
    } else if (name.startsWith("1inch")) {
      return PlatformType.ONEINCH;
    } else if (name.startsWith("Balancer")) {
      return PlatformType.BALANCER;
    } else if (name.startsWith("Ellipsis")) {
      return PlatformType.ELLIPSIS;
    } else if (name.startsWith("Pancake")) {
      return PlatformType.PANCAKESWAP;
    } else if (name.startsWith("Belt.fi")
        || name.startsWith("bDAI+bUSDC+bUSDT+bBUSD") // belt used another symbol/name than curve
    ) {
      return PlatformType.BELT;
    } else if (name.startsWith("Compound")) {
      return PlatformType.COMPOUND;
    }
    return PlatformType.UNKNOWN;
  }

  public boolean isCurveFork() {
    return this == CURVE
        || this == ELLIPSIS
        || this == BELT;
  }
}
