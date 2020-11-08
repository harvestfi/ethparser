package pro.belbix.ethparser.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pro.belbix.ethparser.dto.UniswapDTO;

public interface UniswapRepository extends JpaRepository<UniswapDTO, String> {

    @Query(nativeQuery = true, value = ""
        + "select count(*) from ("
        + "select t.owner from uni_tx t group by t.owner) tt"
    )
    Integer fetchOwnerCount();

    UniswapDTO findFirstByOrderByBlockDesc();

}
