package pro.belbix.ethparser.repositories.eth;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.belbix.ethparser.entity.eth.TokenEntity;
import pro.belbix.ethparser.entity.eth.TokenToUniPairEntity;
import pro.belbix.ethparser.entity.eth.UniPairEntity;

public interface TokenToUniPairRepository extends JpaRepository<TokenToUniPairEntity, Integer> {

    TokenToUniPairEntity findFirstByTokenAndUniPair(TokenEntity token, UniPairEntity uniPair);

    TokenToUniPairEntity findFirstByToken(TokenEntity token);

    TokenToUniPairEntity findFirstByUniPair(UniPairEntity uniPair);

}
