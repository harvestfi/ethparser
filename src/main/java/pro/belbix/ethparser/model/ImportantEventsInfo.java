package pro.belbix.ethparser.model;

import lombok.Data;
import java.math.BigInteger;

@Data
public class ImportantEventsInfo {
    private String vaultAddress;
    private Long strategyTimeLock;
}
