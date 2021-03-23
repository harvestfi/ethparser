package pro.belbix.ethparser.codegen;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import lombok.Data;

@Data
public class AbiModel {

  private boolean payable;
  private String stateMutability;
  private String type;
  private List<AbuInputModel> inputs;
  private boolean anonymous;

  public static AbiModel create(String abi) throws IOException {
    return new ObjectMapper().readValue(abi, AbiModel.class);
  }

}
