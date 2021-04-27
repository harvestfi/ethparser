package pro.belbix.ethparser.dto.v0;

import java.time.Instant;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import lombok.Data;
import pro.belbix.ethparser.dto.DtoI;

@Entity
@Table(name = "transfers", indexes = {
    @Index(name = "idx_transfers_date", columnList = "blockDate"),
    @Index(name = "idx_transfers_owner", columnList = "owner"),
    @Index(name = "idx_transfers_type", columnList = "type"),
    @Index(name = "idx_transfers_name", columnList = "name"),
    @Index(name = "idx_transfers_network", columnList = "network")
})
@Data
public class TransferDTO implements DtoI {

  @Id
  private String id;
  private String name;
  private long block;
  private long blockDate;
  private String network;
  private String owner;
  private String recipient;
  private double value;
  private double balanceOwner;
  private double balanceRecipient;
  private double price;
  private String type;
  private String methodName;
  private Double profit;
  private Double profitUsd;

  public String print() {
    return Instant.ofEpochSecond(blockDate) + " "
        + network + " "
        + type + " "
        + methodName + " "
        + value + " "
        + name + " "
        + " " + id;
  }

  public double getBalance(String address) {
    if (address.equalsIgnoreCase(owner)) {
      return balanceOwner;
    } else if (address.equalsIgnoreCase(recipient)) {
      return balanceRecipient;
    } else {
      throw new IllegalStateException("Not found address " + address + " in " + toString());
    }
  }

}
