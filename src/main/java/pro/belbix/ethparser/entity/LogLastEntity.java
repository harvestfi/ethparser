package pro.belbix.ethparser.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "log_last")
@Data
public class LogLastEntity {

  @Id
  private String network;
  private Long block;

}
