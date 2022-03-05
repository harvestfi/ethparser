package pro.belbix.ethparser.error.exceptions;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CanNotFetchPriceException extends RuntimeException{
  String message;

  public CanNotFetchPriceException() {}
  public CanNotFetchPriceException(String message) {
    this.message = message;
  }
}
