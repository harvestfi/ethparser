package pro.belbix.ethparser.repositories.v0;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.belbix.ethparser.dto.v0.HarvestDTO;
import pro.belbix.ethparser.dto.v0.RewardDTO;

public interface RewardsRepository extends JpaRepository<RewardDTO, String> {
  @Query(nativeQuery = true, value = ""
      + "select * from rewards where "
      + "lower(vault_address) = lower(cast(:vaultAddress as varchar)) "
      + "and network = :network "
      + "order by block_date desc limit 1")
  RewardDTO getFirstByVaultAddressAndNetworkOrderByBlockDateDesc(String vaultAddress,
      String network);

  @Query("select t from RewardDTO t where "
      + "t.blockDate between :from and :to "
      + "and t.network = :network "
      + "order by t.blockDate")
  List<RewardDTO> fetchAllByRange(
      @Param("from") long from,
      @Param("to") long to,
      @Param("network") String network
  );

  @Query(nativeQuery = true, value = "select * from rewards where "
      + "lower(vault_address) = lower(cast(:vault as varchar)) "
      + "and block_date between :startDate and :endDate "
      + "and network = cast(:network as varchar)")
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

  @Query(nativeQuery = true, value = "select * from rewards where "
      + "lower(vault_address) = lower(cast(:vault as varchar)) "
      + "and block_date between :startTime and :endTime "
      + "and network = cast(:network as varchar) "
      + "order by block_date")
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

  @Query("select t from RewardDTO t where "
      + "t.vaultAddress is null or t.vaultAddress = '' "
      + "or t.poolAddress is null or t.poolAddress = ''")
  List<RewardDTO> fetchAllWithoutAddresses();

  @Query("select t from RewardDTO t where "
      + "t.reward >= :minAmount "
      + "and t.isWeeklyReward = :isWeeklyReward "
      + "and t.network = :network")
  Page<RewardDTO> fetchPages(
      @Param("minAmount") double minAmount,
      @Param("isWeeklyReward") int isWeeklyReward,
      @Param("network") String network,
      Pageable pageable);

  @Query("select t from RewardDTO t where "
      + "lower(t.vaultAddress) = lower(:vault) "
      + "and t.reward >= :minAmount "
      + "and t.isWeeklyReward = :isWeeklyReward "
      + "and t.network = :network")
  Page<RewardDTO> fetchPagesByVault(
      @Param("vault") String vaultAddress,
      @Param("network") String network,
      @Param("minAmount") double minAmount,
      @Param("isWeeklyReward") int isWeeklyReward,
      Pageable pageable);
}
