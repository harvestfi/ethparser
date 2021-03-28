package pro.belbix.ethparser.entity.b_layer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "b_func_hashes")
@Data
@JsonInclude(Include.NON_NULL)
public class FunctionHashEntity {


  @Id
  private String methodId;
  private String name;

}
