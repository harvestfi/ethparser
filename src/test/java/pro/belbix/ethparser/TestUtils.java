package pro.belbix.ethparser;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class TestUtils {

  private static final DecimalFormatSymbols FORMAT =
      new DecimalFormatSymbols(Locale.getDefault(Locale.Category.FORMAT));

  public static String numberFormat(String number) {
    if (number == null) {
      return null;
    }
    return number.replace(".", FORMAT.getDecimalSeparator() + "")
        .replace(",", FORMAT.getDecimalSeparator() + "");
  }

  public static void assertTwoArrays(List<String> actual, List<String> _expected) {
    List<String> expected = new ArrayList<>(_expected);
    Set<String> notExpected = new LinkedHashSet<>();
    actual.forEach(a -> {
//      System.out.println(a);
      boolean removed = expected.remove(a);
      if (!removed) {
        notExpected.add(a);
      }
    });
    assertAll(
        () -> assertTrue(notExpected.isEmpty(), "not expected, but exist: " + notExpected),
        () -> assertTrue(expected.isEmpty(), "expected, but absent: " + expected)
    );
  }

}
