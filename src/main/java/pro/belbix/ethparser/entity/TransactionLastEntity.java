package pro.belbix.ethparser.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "transaction_last")
@Data
public class TransactionLastEntity {

  @Id
  private String network;
  private Long block;

}
