package pro.belbix.ethparser.repositories;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.belbix.ethparser.entity.IncomeEntity;

public interface IncomeRepository extends JpaRepository<IncomeEntity, String> {

    @Query("select sum(t.perc) from IncomeEntity t where t.timestamp >= :from and t.timestamp < :to")
    List<Double> fetchPercentFroPeriod(@Param("from") long from, @Param("to") long to, Pageable pageable);

}
