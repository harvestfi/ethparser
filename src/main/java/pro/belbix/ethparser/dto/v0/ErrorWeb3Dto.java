package pro.belbix.ethparser.dto.v0;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.web3j.protocol.core.methods.response.Transaction;

@Entity
@Table(name="error_parse")
@Data
@AllArgsConstructor
@NoArgsConstructor

public class ErrorWeb3Dto {
  @Id
  private String id;
  private String network;
  private String address;
  private String transactionHash;
//  public Transaction findTransaction(String hash, String network)


}
