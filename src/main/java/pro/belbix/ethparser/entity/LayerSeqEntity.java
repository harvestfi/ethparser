package pro.belbix.ethparser.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "layer_seq")
@Data
public class LayerSeqEntity {

    @Id
    private Long seq;

}
