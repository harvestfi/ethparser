package pro.belbix.ethparser.codegen;

import static org.web3j.abi.Utils.convert;

import java.util.List;
import lombok.Data;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;

@Data
public class FunctionWrapper {

  private final Function function;
  private final boolean view;
  private final List<TypeReference<Type>> input;

  public FunctionWrapper(Function function, boolean view, List<TypeReference<?>> input) {
    this.function = function;
    this.view = view;
    this.input = convert(input);
  }
}
