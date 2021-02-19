package pro.belbix.ethparser.repositories.a_layer;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.belbix.ethparser.entity.a_layer.EthReceiptEntity;

public interface EthReceiptRepository extends JpaRepository<EthReceiptEntity, String> {

}
