package pro.belbix.ethparser.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.belbix.ethparser.entity.profit.TokenPrice;

public interface TokenPriceRepository extends JpaRepository<TokenPrice, String> {

}
