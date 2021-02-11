package pro.belbix.ethparser.dto;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;


@Entity
@Table(name = "deployer_tx", indexes = {
        @Index(name = "idx_deployer_tx", columnList = "block")
})
@Cacheable(false)
@Data
public class DeployerDTO implements DtoI
{
    @Id
    private String id;
    private String hash;
    private long idx;
    private long block;
    private long blockDate;
    private String toAddress;
    private String fromAddress;
    private BigDecimal value;
    private BigInteger gasLimit;
    private BigInteger gasUsed;
    private BigInteger gasPrice;
    private String methodName;
    private String type;
    private boolean confirmed;

    public String print()
    {
        return Instant.ofEpochSecond(blockDate) + " " + hash;
    }
}
