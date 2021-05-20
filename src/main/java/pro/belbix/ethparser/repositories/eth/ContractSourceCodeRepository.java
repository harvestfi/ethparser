package pro.belbix.ethparser.repositories.eth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.belbix.ethparser.dto.v0.ContractSourceCodeDTO;

public interface ContractSourceCodeRepository extends JpaRepository<ContractSourceCodeDTO, Integer> {
  @Query("select t from ContractSourceCodeDTO t "
      + "where t.address = :address and t.network = :network")
  ContractSourceCodeDTO findByAddressNetwork(
      @Param("address") String address,
      @Param("network") String network
  );
}
