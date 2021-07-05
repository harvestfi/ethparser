package pro.belbix.ethparser.dto.v0;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "error_parse")
@Data
@AllArgsConstructor
@NoArgsConstructor

public class ErrorWeb3Dto {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;
  @Lob
  private String json;
  private String errorClass;
  private String network;
}
