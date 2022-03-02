package pro.belbix.ethparser.utils;

public class UrlUtils {

  public interface CovalenthqUrl {
    String TRANSACTION_HISTORY = "%s%s/address/%s/transactions_v2/?"
        + "quote-currency=USD&"
        + "format=JSON&"
        + "block-signed-at-asc=%s&"
        + "no-logs=%s&"
        + "key=%s&"
        + "page-number=%s&"
        + "page-size=%s&";
    // TODO move params to restTemplate
    String TRANSACTION_HISTORY_WITH_BLOCK_RANGE = "%s%s/address/%s/transactions_v2/?"
        + "quote-currency=USD&"
        + "format=JSON&"
        + "block-signed-at-asc=true&"
        + "no-logs=false&"
        + "key=%s&"
        + "page-number=%s&"
        + "page-size=%s&"
        + "starting-block=%s&"
        + "ending-block=%s&";

    String HISTORICAL_PRICE = "%spricing/historical_by_addresses_v2/%s/USD/%s/?"
        + "quote-currency=USD&"
        + "format=JSON&"
        + "key=%s&"
        + "from=%s&"
        + "to=%s";

    String TRANSACTION_BY_CONTRACT_ID = "%s%s/address/%s/transfers_v2/?"
        + "contract-address=%s&"
        + "key=%s&"
        + "page-number=%s&"
        + "page-size=%s&"
        + "starting-block=%s&"
        + "ending-block=%s";
  }

  public interface HarvestUrl {
    String POOLS = "%spools?key=%s";
    String VAULTS = "%svaults?key=%s";
  }
}
