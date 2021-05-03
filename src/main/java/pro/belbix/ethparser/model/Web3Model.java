package pro.belbix.ethparser.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Web3Model<T> {

  private T value;
  private String network;

}
