package pro.belbix.ethparser.dto;

import java.time.Instant;
import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "events_tx", indexes = {
    @Index(name = "idx_events_tx", columnList = "blockDate"),
    @Index(name = "idx_events_tx2", columnList = "event, vault")
})
@Cacheable(false)
@Data
public class ImportantEventsDTO implements DtoI {

    @Id
    private String id;
    private String hash;
    private Long block;
    private Long blockDate;
    private String event;
    private String oldStrategy;
    private String newStrategy;
    private Double fee;
    private String vault;
    private Double mintAmount;


    public String print() {
        return Instant.ofEpochSecond(blockDate) + " "
            + event + " "
            + vault + " "
            + "old: " + oldStrategy + " "
            + "new: " + newStrategy + " "
            + "minted: " + mintAmount;
    }
}
