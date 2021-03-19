package pro.belbix.ethparser.web3.abi;

import java.math.BigInteger;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.StaticStruct;

@SuppressWarnings("rawtypes")
public class DynamicStructures {

  public static TypeReference swapTypeReference() {
    return new TypeReference<Swap>() {
    };
  }

  public static TypeReference swapTypeReferenceDoubleArray() {
    return new TypeReference<DynamicArray<Swap>>() {
    };
  }

  //balancer
  public static class Swap extends StaticStruct {

    public String pool;
    public BigInteger tokenInParam;
    public BigInteger tokenOutParam;
    public BigInteger maxPrice;

    public Swap(
        String pool,
        BigInteger tokenInParam,
        BigInteger tokenOutParam,
        BigInteger maxPrice
    ) {
      super(
          new org.web3j.abi.datatypes.Address(pool),
          new org.web3j.abi.datatypes.Uint(tokenInParam),
          new org.web3j.abi.datatypes.Uint(tokenOutParam),
          new org.web3j.abi.datatypes.Uint(maxPrice)
      );
      this.pool = pool;
      this.tokenInParam = tokenInParam;
      this.tokenOutParam = tokenOutParam;
      this.maxPrice = maxPrice;
    }

    public Swap(
        org.web3j.abi.datatypes.Address pool,
        org.web3j.abi.datatypes.Uint tokenInParam,
        org.web3j.abi.datatypes.Uint tokenOutParam,
        org.web3j.abi.datatypes.Uint maxPrice
    ) {
      super(
          pool,
          tokenInParam,
          tokenOutParam,
          maxPrice
      );
      this.pool = pool.getValue();
      this.tokenInParam = tokenInParam.getValue();
      this.tokenOutParam = tokenOutParam.getValue();
      this.maxPrice = maxPrice.getValue();
    }
  }

}
