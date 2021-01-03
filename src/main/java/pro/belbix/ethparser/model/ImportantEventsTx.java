package pro.belbix.ethparser.model;

import lombok.Data;
import java.math.BigInteger;

@Data
public class ImportantEventsTx implements EthTransactionI {
    private String hash;
    private String logId;
    private String vault;
    private String oldStrategy;
    private String newStrategy;
    private String methodName;
    private long block;
    private long blockDate;
    private BigInteger mintAmount = BigInteger.ZERO;

}
