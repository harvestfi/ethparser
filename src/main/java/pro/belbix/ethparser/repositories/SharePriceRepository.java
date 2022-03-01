package pro.belbix.ethparser.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.belbix.ethparser.entity.profit.SharePrice;

public interface SharePriceRepository extends JpaRepository<SharePrice, String> {

}
