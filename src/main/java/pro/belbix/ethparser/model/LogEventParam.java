package pro.belbix.ethparser.model;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(makeFinal = true, level = AccessLevel.PUBLIC)
public enum LogEventParam {
  WITHDRAW_TO("provider"),
  WITHDRAW_VALUE("value"),
  DEPOSIT_FROM("dst"),
  DEPOSIT_VALUE("wad");

  LogEventParam(String value) {
    this.value = value;
  }

  String value;
}
