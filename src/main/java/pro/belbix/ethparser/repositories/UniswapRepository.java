package pro.belbix.ethparser.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.belbix.ethparser.model.UniswapDTO;

public interface UniswapRepository extends JpaRepository<UniswapDTO, String> {

    @Query(nativeQuery = true, value = ""
        + "select count(*) from ("
        + "select t.owner from egv.uni_tx t group by t.owner) tt"
    )
    Integer fetchOwnerCount();

}
