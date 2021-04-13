package pro.belbix.ethparser.dto.v0;

import java.time.Instant;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import lombok.Data;
import pro.belbix.ethparser.dto.DtoI;

@Entity
@Table(name = "events_tx", indexes = {
    @Index(name = "idx_events_tx", columnList = "blockDate"),
    @Index(name = "idx_events_network", columnList = "network")
})
@Cacheable(false)
@Data
public class ImportantEventsDTO implements DtoI {

  @Id
  private String id;
  private String hash;
  private Long block;
  private Long blockDate;
  private String network;
  private String event;
  private String oldStrategy;
  private String newStrategy;
  private String vault;
  private Double mintAmount;
  @Column(columnDefinition = "TEXT")
  private String info;

  public String print() {
    return Instant.ofEpochSecond(blockDate) + " "
        + event + " "
        + vault + " "
        + "old: " + oldStrategy + " "
        + "new: " + newStrategy + " "
        + "minted: " + mintAmount + " "
        + info;
  }
}
