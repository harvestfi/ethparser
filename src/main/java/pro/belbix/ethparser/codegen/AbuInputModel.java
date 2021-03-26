package pro.belbix.ethparser.codegen;

import lombok.Data;

@Data
public class AbuInputModel {

  private String internalType;
  private String name;
  private String type;
  private boolean indexed;

}
