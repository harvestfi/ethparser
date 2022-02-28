package pro.belbix.ethparser.entity.profit;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(makeFinal = true, level = AccessLevel.PUBLIC)
public enum CovalenthqVaultTransactionType {
  WITHDRAW_UNI("account", "writeAmount", "Withdraw"),
  WITHDRAW("provider", "value", "Withdraw"),
  DEPOSIT_UNI("user", "amount", "Deposit"),
  DEPOSIT("dst", "wad", "Deposit");

  CovalenthqVaultTransactionType(String address, String value, String type) {
    this.address = address;
    this.value = value;
    this.type = type;
  }

  String address;
  String value;
  String type;
}
