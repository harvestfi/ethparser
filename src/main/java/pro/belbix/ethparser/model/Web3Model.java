package pro.belbix.ethparser.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Web3Model<T> {

  private T value;
  private String network;

}
