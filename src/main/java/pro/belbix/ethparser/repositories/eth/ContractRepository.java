package pro.belbix.ethparser.repositories.eth;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.belbix.ethparser.entity.contracts.ContractEntity;

public interface ContractRepository extends JpaRepository<ContractEntity, Integer> {

    @Query("select t from ContractEntity t "
        + "where lower(t.address) = lower(:address) and t.network = :network")
    ContractEntity findFirstByAddress(
        @Param("address") String address,
        @Param("network") String network
    );

    @Query("select t from ContractEntity t "
        + "where lower(t.address) = lower(:address) and t.type = :type and t.network = :network")
    ContractEntity findFirstByAddressAndType(
        @Param("address") String address,
        @Param("type") int type,
        @Param("network") String network
    );

    @Query("select t from ContractEntity t "
        + "where t.name = :name "
        + "and t.type = :type "
        + "and t.network = :network "
        + "order by t.created desc")
    List<ContractEntity> findByNameAndType(
        @Param("name") String name,
        @Param("type") int type,
        @Param("network") String network,
        Pageable pageable
    );

    @Query("select p_ctr from PoolEntity p "
        + "join p.contract p_ctr "
        + "where lower(p.lpToken.address) = lower(:address) "
        + "and p.contract.network = :network "
        + "order by p_ctr.created desc")
    List<ContractEntity> findPoolsByVaultAddress(
        @Param("address") String address,
        @Param("network") String network
    );

    @Query("select c from ContractEntity c "
        + "where c.network = :network "
        + "and lower(c.address) in(:addresses)")
    List<ContractEntity> findAllByNetworkAndInAddress(String network, List<String> addresses);

}
