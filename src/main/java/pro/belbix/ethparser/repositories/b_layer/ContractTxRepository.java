package pro.belbix.ethparser.repositories.b_layer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pro.belbix.ethparser.entity.a_layer.EthTxEntity;
import pro.belbix.ethparser.entity.b_layer.ContractTxEntity;

public interface ContractTxRepository extends JpaRepository<ContractTxEntity, Long> {

//  @Query(" select t from ContractTxEntity t "
//      + "left join fetch t.funcHash f"
//      + "left join fetch t.logs l"
//  )
  ContractTxEntity findFirstByTx(EthTxEntity tx);

}
