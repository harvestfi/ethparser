package pro.belbix.ethparser.entity.b_layer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

@Entity
@Table(name = "b_contract_states",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"contract_event_id", "name"})
    })
@Data
@JsonInclude(Include.NON_NULL)
@EqualsAndHashCode(exclude = {"contractEvent"})
@ToString(exclude = {"contractEvent"})
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class ContractStateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private String value;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ContractEventEntity contractEvent;
}
