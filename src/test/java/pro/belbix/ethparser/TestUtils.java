package pro.belbix.ethparser;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.junit.jupiter.api.function.Executable;

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

  public static <T> void assertModel(T expected, T actual) throws Exception {
    assertModel(expected, actual, null);
  }

  public static <T> void assertModel(T expected, T actual, Set<String> excludeFields)
      throws Exception {
    Collection<Executable> asserts = new ArrayList<>();
    for (PropertyDescriptor propertyDescriptor :
        Introspector.getBeanInfo(expected.getClass()).getPropertyDescriptors()) {
      if (excludeFields != null && excludeFields.contains(propertyDescriptor.getName())) {
        continue;
      }
      Object expectedValue = propertyDescriptor.getReadMethod().invoke(expected);
      if (expectedValue == null) {
        continue;
      }
      Object actualValue = propertyDescriptor.getReadMethod().invoke(actual);

      asserts.add(
          () -> assertEquals(expectedValue, actualValue, propertyDescriptor.getName())
      );
    }
    assertAll(asserts);
  }

}
