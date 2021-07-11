package pro.belbix.ethparser.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "error_parse")
@Data
@AllArgsConstructor
@NoArgsConstructor

public class ErrorEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;
  @Column(columnDefinition = "TEXT")
  private String json;
  private String errorClass;
  private String network;
}
