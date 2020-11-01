package pro.belbix.ethparser.model;

import java.math.BigInteger;
import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@Table(name = "harvest_tx", indexes = {
    @Index(name = "idx_harvest_tx", columnList = "blockDate")
})
@Cacheable(false)
public class HarvestDTO {

    @Id
    private String hash;
    private BigInteger block;
    private boolean confirmed = false;
    private long blockDate;

    private String methodName;
    private String owner;
    private String timestamp;
    private BigInteger amount;
    private String vault;
    private String addressFromArgs;
    private BigInteger intFromArgs1;
    private BigInteger intFromArgs2;
}
