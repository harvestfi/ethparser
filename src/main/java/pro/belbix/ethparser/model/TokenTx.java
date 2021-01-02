package pro.belbix.ethparser.model;

import java.math.BigInteger;
import lombok.Data;

@Data
public class TokenTx implements EthTransactionI{

    private String hash;
    private String logId;
    private String tokenAddress;
    private String methodName;
    private String blockHash;
    private long block;
    private long blockDate;
    private String owner;
    private String recipient;
    private BigInteger value;

}
