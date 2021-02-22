package pro.belbix.ethparser.repositories.a_layer;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.EntityGraph.EntityGraphType;
import org.springframework.data.jpa.repository.JpaRepository;
import pro.belbix.ethparser.entity.a_layer.EthBlockEntity;

public interface EthBlockRepository extends JpaRepository<EthBlockEntity, Long> {

    EthBlockEntity findFirstByOrderByNumberDesc();

//    @EntityGraph(value = "block-graph.all", type = EntityGraphType.FETCH)
//    EthBlockEntity getByNumber(long number);

}
