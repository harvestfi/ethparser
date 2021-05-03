package pro.belbix.ethparser.web3.contracts;

public enum PlatformType {

  UNKNOWN(""),
  CURVE("CRV"),
  UNISWAP("UNI"),
  SUSHISWAP("SUSHI"),
  PANCAKESWAP("PCS"),
  ONEINCH("1INCH"),
  VENUS("VENUS"),
  ELLIPSIS("EPS"),
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
}
