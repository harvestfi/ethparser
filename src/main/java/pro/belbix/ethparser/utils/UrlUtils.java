package pro.belbix.ethparser.utils;

public class UrlUtils {

  public interface Covalenthq {
    interface Url {
      String TRANSACTION_HISTORY = "%s%s/address/%s/transactions_v2/?%s";
    }
    interface Params {
      String QUOTE_CURRENCY = "quote-currency=USD&";
      String FORMAT = "format=JSON&";
      String BLOCK_SIGNED_AT_ASC = "block-signed-at-asc=%s&";
      String NO_LOGS = "no-logs=%s&";
      String KEY = "key=%s&";
      String PAGE_NUMBER = "page-number=%s&";
      String PAGE_SIZE = "page-size=%s&";
      String START_BLOCK = "starting-block=%s&";
      String END_BLOCK = "ending-block=%s&";
    }

  }

  public interface HarvestUrl {
    String POOLS = "%spools?key=%s";
    String VAULTS = "%svaults?key=%s";
  }
}
