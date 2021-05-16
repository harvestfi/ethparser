package pro.belbix.ethparser.dto.v0;

import java.util.Date;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "eth_contract_source_codes", indexes = {
    @Index(name = "idx_eth_contracts_address", columnList = "address"),
    @Index(name = "idx_eth_contracts_name", columnList = "contractName")
}, uniqueConstraints = {
    @UniqueConstraint(name = "eth_c_adr_net_pk", columnNames = {"address", "network"})
})
@Cacheable(false)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContractSourceCodeDTO {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(nullable = false)
  private String address;

  @Column(nullable = false)
  private String network;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String sourceCode;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String abi;

  @Column(nullable = false)
  private String contractName;
  private String compilerVersion;
  private Boolean optimizationUsed;
  private String runs;

  @Column(columnDefinition = "TEXT")
  private String constructorArguments;
  private String eVMVersion;
  private String library;
  private String licenseType;
  private Boolean proxy;
  private String implementation;
  private String swarmSource;

  @CreationTimestamp
  private Date createdAt;

}
