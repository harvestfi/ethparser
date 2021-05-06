package pro.belbix.ethparser.entity.contracts;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.Data;

@Entity
@Table(name = "eth_contracts", indexes = {
    @Index(name = "idx_eth_contracts_address", columnList = "address"),
    @Index(name = "idx_eth_contracts_name", columnList = "name")
}, uniqueConstraints = {
    @UniqueConstraint(name = "eth_c_adr_net_pk", columnNames = {"address", "network"})
})
@Data
public class ContractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String address;
    private String name;
    private Long created;
    private int type;
    private String network;
    private int curve;

}
