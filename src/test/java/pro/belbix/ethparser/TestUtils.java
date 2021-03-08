package pro.belbix.ethparser;

import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class TestUtils {

  private static final DecimalFormatSymbols FORMAT =
      new DecimalFormatSymbols(Locale.getDefault(Locale.Category.FORMAT));

  public static String numberFormat(String number) {
    if (number == null) {
      return null;
    }
    number = number.replace(".", FORMAT.getDecimalSeparator() + "");
    return number.replace(",", FORMAT.getDecimalSeparator() + "");
  }

}
