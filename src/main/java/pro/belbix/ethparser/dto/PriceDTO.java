package pro.belbix.ethparser.dto;

import java.math.BigInteger;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "prices", indexes = {
    @Index(name = "idx_prices", columnList = "block")
})
@Data
public class PriceDTO implements DtoI {

    @Id
    private String id;
    private Long block;
    private Long blockDate;
    private String token;
    private Double tokenAmount;
    private String otherToken;
    private Double otherTokenAmount;
    private Double price;
    private Boolean buy;
    private String source;

}
