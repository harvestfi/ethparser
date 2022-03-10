package pro.belbix.ethparser.entity.contracts;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@Table(name = "eth_token_to_uni_pair")
@Data
public class TokenToUniPairEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Long blockStart;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="token_id", nullable=false)
    @Fetch(FetchMode.JOIN)
    private TokenEntity token;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="uni_pair_id", nullable=false)
    @Fetch(FetchMode.JOIN)
    private UniPairEntity uniPair;



}
