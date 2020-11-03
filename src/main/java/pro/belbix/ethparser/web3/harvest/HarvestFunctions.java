package pro.belbix.ethparser.web3.harvest;

import java.util.Arrays;
import java.util.Collections;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Uint112;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint32;

public class HarvestFunctions {

    public static final Function GET_PRICE_PER_FULL_SHARE = new Function(
        "getPricePerFullShare",
        Collections.emptyList(),
        Collections.singletonList(new TypeReference<Uint256>() {
        }));

    public static final Function GET_RESERVES = new Function(
        "getReserves",
        Collections.emptyList(),
        Arrays.asList(new TypeReference<Uint112>() {
                      },
            new TypeReference<Uint112>() {
            },
            new TypeReference<Uint32>() {
            }
        ));

}
