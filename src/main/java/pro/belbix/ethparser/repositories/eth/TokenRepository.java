package pro.belbix.ethparser.repositories.eth;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.belbix.ethparser.entity.eth.ContractEntity;
import pro.belbix.ethparser.entity.eth.TokenEntity;

public interface TokenRepository extends JpaRepository<TokenEntity, Integer> {

    TokenEntity findFirstByContract(ContractEntity tokenContract);
}
