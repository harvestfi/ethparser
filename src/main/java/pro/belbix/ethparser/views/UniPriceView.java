package pro.belbix.ethparser.views;

public interface UniPriceView extends ViewI{

  long getId();

  String getName();

  String getAddress();

  long getTxId();

  String getTxHash();

  String getFuncName();

  String getLogName();

  String getLogs();
}
