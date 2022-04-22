package pro.belbix.ethparser.entity.profit;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(makeFinal = true, level = AccessLevel.PUBLIC)
public enum CovalenthqVaultTransactionType {
  WITHDRAW_UNI("account", "writeAmount", "Withdraw", 3),
  WITHDRAW("provider", "value", "Withdraw", 2),
  DEPOSIT_UNI("user", "amount", "Deposit", 3),
  DEPOSIT("dst", "wad", "Deposit", 2),
  TRANSFER("from", "to", "valueTransfer", "Transfer", 3);

  CovalenthqVaultTransactionType(String address, String value, String type, int paramSize) {
    this.address = address;
    this.value = value;
    this.type = type;
    this.paramSize = paramSize;
    this.toAddress = "";
  }

  CovalenthqVaultTransactionType(String address, String toAddress, String value, String type, int paramSize) {
    this.address = address;
    this.value = value;
    this.type = type;
    this.paramSize = paramSize;
    this.toAddress = toAddress;
  }

  String address;
  String toAddress;
  String value;
  String type;
  int paramSize;
}
