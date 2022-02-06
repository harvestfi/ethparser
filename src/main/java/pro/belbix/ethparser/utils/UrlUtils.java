package pro.belbix.ethparser.utils;

public class UrlUtils {

  public interface CovalenthqUrl {
    String TRANSACTION = "%s%s/address/%s/transactions_v2/?"
        + "quote-currency=USD"
        + "&format=JSON"
        + "&block-signed-at-asc=%s"
        + "&no-logs=%s"
        + "&key=%s"
        + "&page-number=%s"
        + "&page-size=%s";
  }

  public interface HarvestUrl {
    String POOLS = "%spools?key=%s";
    String VAULTS = "%svaults?key=%s";
  }
}
