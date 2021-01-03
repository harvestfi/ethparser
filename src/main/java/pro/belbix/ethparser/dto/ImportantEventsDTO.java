package pro.belbix.ethparser.dto;

import java.math.BigInteger;
import java.time.Instant;
import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Transient;
import lombok.Data;

@Entity
@Table(name = "events_tx", indexes = {
    @Index(name = "idx_events_tx", columnList = "blockDate"),
    @Index(name = "idx_events_tx2", columnList = "oldStrategy, vault"),
    @Index(name = "idx_events_tx3", columnList = "newStrategy, vault")
})
@Cacheable(false)
@Data
public class ImportantEventsDTO implements DtoI {

    @Id
    private String id;
    private String hash;
    private Long block;
    private Long blockDate;
    private String methodName;
    private String oldStrategy;
    private String newStrategy;
    private Long earliestEffective;
    private Double fee;
    private String vault;
    private Double mintAmount;


    public String print() {
        return Instant.ofEpochSecond(blockDate) + " "
            + methodName + " "
            + vault + " "
            + "old: " + oldStrategy + " "
            + "new: " + newStrategy + " "
            + "minted: " + mintAmount;
    }
}
