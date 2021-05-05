package pro.belbix.ethparser.repositories.v0;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.belbix.ethparser.dto.v0.RewardDTO;

public interface RewardsRepository extends JpaRepository<RewardDTO, String> {

    RewardDTO getFirstByVaultAddressAndNetworkOrderByBlockDateDesc(String vaultAddress, String network);

    @Query("select t from RewardDTO t where "
        + "t.blockDate between :from and :to "
        + "and t.network = :network "
        + "order by t.blockDate")
    List<RewardDTO> fetchAllByRange(
        @Param("from") long from,
        @Param("to") long to,
        @Param("network") String network
    );

    @Query("select t from RewardDTO t where "
        + "t.vaultAddress = :vault "
        + "and t.blockDate between :startDate and :endDate "
        + "and t.network = :network")
    List<RewardDTO> fetchRewardsByVaultAfterBlockDate(
        @Param("vault") String vaultAddress,
        @Param("startDate") long startDate,
        @Param("endDate") long endDate,
        @Param("network") String network
    );

    @Query(nativeQuery = true, value = "" +
        "select distinct on (vault_address) * from rewards "
        + "where network = :network "
        + "order by vault_address, block_date desc")
    List<RewardDTO> fetchLastRewards(@Param("network") String network);

    @Query("select t from RewardDTO t where "
        + "t.vaultAddress = :vault "
        + "and t.blockDate between :startTime and :endTime "
        + "and t.network = :network "
        + "order by t.blockDate")
    List<RewardDTO> getAllByVaultOrderByBlockDate(
        @Param("vault") String vaultAddress,
        @Param("startTime") long startTime,
        @Param("endTime") long endTime,
        @Param("network") String network
    );

    @Query("select t from RewardDTO t where "
        + "t.blockDate between :startTime and :endTime "
        + "and t.network = :network "
        + "order by t.blockDate")
    List<RewardDTO> getAllOrderByBlockDate(
        @Param("startTime") long startTime,
        @Param("endTime") long endTime,
        @Param("network") String network
    );
}
