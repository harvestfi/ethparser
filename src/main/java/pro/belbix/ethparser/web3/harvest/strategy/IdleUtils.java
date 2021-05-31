package pro.belbix.ethparser.web3.harvest.strategy;

import static java.math.RoundingMode.HALF_UP;
import static pro.belbix.ethparser.utils.Caller.silentCall;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.IDLE_ACCRUED;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.IDLE_SPEEDS;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.IDLE_SUPPLIER_INDEX;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.IDLE_SUPPLY_STATE;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.TOTAL_SUPPLY;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.protocol.ObjectMapperFactory;
import pro.belbix.ethparser.web3.abi.FunctionsUtils;

public class IdleUtils {

  private final static ObjectMapper om = ObjectMapperFactory.getObjectMapper();
  private final static BigDecimal DOUBLE_SCALE = BigDecimal.valueOf(1e36);

  public static BigDecimal calculateRewards(
      FunctionsUtils functionsUtils,
      String idleToken,
      String idleController,
      String supplier,
      long block,
      String network
  ) {

    List supplyState = functionsUtils.callViewFunction(
        new Function(
            IDLE_SUPPLY_STATE,
            List.of(new Address(idleToken)),
            List.of(
                silentCall(() -> TypeReference.makeTypeReference("uint256")).orElseThrow(),
                silentCall(() -> TypeReference.makeTypeReference("uint256")).orElseThrow()
            )
        ),
        idleController,
        block,
        network
    ).flatMap(raw -> silentCall(() -> om.readValue(raw, List.class)))
        .orElseThrow();

    BigDecimal supplySpeed = new BigDecimal(functionsUtils.callIntByNameWithAddressArg(
        IDLE_SPEEDS,
        idleToken,
        idleController,
        block,
        network
    ).orElseThrow());

    BigDecimal deltaBlocks = BigDecimal.valueOf(block)
        .subtract(new BigDecimal((String) supplyState.get(1)));

    BigDecimal currentIdleSupplyState;
    if (deltaBlocks.doubleValue() > 0 && supplySpeed.doubleValue() > 0) {

      BigDecimal supplyTokens = new BigDecimal(functionsUtils.callIntByName(
          TOTAL_SUPPLY,
          idleToken,
          block,
          network
      ).orElseThrow());

      BigDecimal idleAccrued = deltaBlocks.multiply(supplySpeed);

      BigDecimal ratio = supplyTokens.doubleValue() > 0 ?
          idleAccrued.multiply(DOUBLE_SCALE).divide(supplyTokens, 999, HALF_UP)
          : BigDecimal.ZERO;
      currentIdleSupplyState = new BigDecimal((String) supplyState.get(0)).add(ratio);
    } else {
      currentIdleSupplyState = new BigDecimal((String) supplyState.get(0));
    }

    BigDecimal supplyIndex = currentIdleSupplyState;
    BigDecimal supplierIndex = functionsUtils.callViewFunction(
        new Function(
            IDLE_SUPPLIER_INDEX,
            List.of(new Address(idleToken), new Address(supplier)),
            List.of(silentCall(() -> TypeReference.makeTypeReference("uint256")).orElseThrow())
        ),
        idleController,
        block,
        network
    ).flatMap(raw -> silentCall(() -> (String) om.readValue(raw, List.class).get(0)))
        .map(BigDecimal::new)
        .orElseThrow();

    if (supplierIndex.doubleValue() == 0 && supplyIndex.doubleValue() > 0) {
      supplierIndex = BigDecimal.valueOf(1e36);
    }

    BigDecimal deltaIndex = supplyIndex.subtract(supplierIndex);
    BigDecimal supplierTokens = new BigDecimal(functionsUtils.callIntByName(
        TOTAL_SUPPLY,
        idleToken,
        block,
        network
    ).orElseThrow());
    BigDecimal supplierDelta = supplierTokens.multiply(deltaIndex)
        .divide(DOUBLE_SCALE, 999, HALF_UP);
    BigDecimal idleAccrued = new BigDecimal(functionsUtils.callIntByNameWithAddressArg(
        IDLE_ACCRUED,
        supplier,
        idleController,
        block,
        network
    ).orElseThrow());

    return idleAccrued.add(supplierDelta);
  }

}
