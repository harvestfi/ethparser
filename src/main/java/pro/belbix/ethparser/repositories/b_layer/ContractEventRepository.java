package pro.belbix.ethparser.repositories.b_layer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.belbix.ethparser.entity.a_layer.EthAddressEntity;
import pro.belbix.ethparser.entity.a_layer.EthBlockEntity;
import pro.belbix.ethparser.entity.a_layer.EthHashEntity;
import pro.belbix.ethparser.entity.b_layer.ContractEventEntity;
import pro.belbix.ethparser.entity.contracts.ContractEntity;

public interface ContractEventRepository extends JpaRepository<ContractEventEntity, Long> {

  @Query("select t from ContractEventEntity t "
      + "left join fetch t.contract c "
      + "left join fetch t.block b "
      + "left join fetch t.states s "
      + "left join fetch t.txs tx "
      + "where t.contract = :contract and t.block = :block")
  ContractEventEntity findByContractAndBlock(@Param("contract") EthAddressEntity contract, @Param("block") EthBlockEntity block);
}
