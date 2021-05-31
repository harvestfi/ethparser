package pro.belbix.ethparser.web3.harvest.strategy;

import static java.math.RoundingMode.HALF_UP;
import static pro.belbix.ethparser.utils.Caller.silentCall;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.BALANCE_OF;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.COMP_ACCRUED;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.COMP_SUPPLIER_INDEX;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.COMP_SUPPLY_STATE;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.protocol.ObjectMapperFactory;
import pro.belbix.ethparser.web3.abi.FunctionsUtils;

public class CompUtils {

  private final static ObjectMapper om = ObjectMapperFactory.getObjectMapper();

  public static BigDecimal calculateRewards(
      FunctionsUtils functionsUtils,
      String holder,
      String cToken,
      String comptroller,
      long block,
      String network
  ) {

    @SuppressWarnings("unchecked")
    BigDecimal supplyIndex = new BigDecimal(functionsUtils.callViewFunction(
        new Function(
            COMP_SUPPLY_STATE,
            List.of(new Address(cToken)),
            List.of(
                silentCall(() -> TypeReference.makeTypeReference("uint224")).orElseThrow(),
                silentCall(() -> TypeReference.makeTypeReference("uint32")).orElseThrow()
            )
        ),
        comptroller,
        block,
        network)
        .flatMap(raw -> silentCall(() -> (String) om.readValue(raw, List.class).get(0)))
        .map(BigInteger::new)
        .orElseThrow());

    @SuppressWarnings("unchecked")
    BigDecimal supplierIndex = new BigDecimal(functionsUtils.callViewFunction(
        new Function(
            COMP_SUPPLIER_INDEX,
            List.of(
                new Address(cToken),
                new Address(holder)
            ),
            List.of(silentCall(() -> TypeReference.makeTypeReference("uint256")).orElseThrow())
        ),
        comptroller,
        block,
        network)
        .flatMap(raw -> silentCall(() -> (String) om.readValue(raw, List.class).get(0)))
        .map(BigInteger::new)
        .orElseThrow());

    if (supplierIndex.doubleValue() == 0 && supplyIndex.doubleValue() > 0) {
      supplierIndex = BigDecimal.valueOf(1e36);
    }
    BigDecimal deltaIndex = supplyIndex.subtract(supplierIndex);
    BigDecimal supplierTokens = new BigDecimal(functionsUtils.callIntByNameWithAddressArg(
        BALANCE_OF,
        holder,
        cToken,
        block,
        network
    ).orElseThrow());

    BigDecimal supplierDelta = supplierTokens.multiply(deltaIndex)
        .divide(BigDecimal.valueOf(1e36), 999, HALF_UP);

    BigDecimal compAccrued = new BigDecimal(functionsUtils.callIntByNameWithAddressArg(
        COMP_ACCRUED,
        holder,
        comptroller,
        block,
        network
    ).orElseThrow());

    return compAccrued.add(supplierDelta);
  }

}
