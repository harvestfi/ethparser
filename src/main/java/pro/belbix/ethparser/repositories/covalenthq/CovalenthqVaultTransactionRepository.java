package pro.belbix.ethparser.repositories.covalenthq;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pro.belbix.ethparser.entity.profit.CovalenthqVaultTransaction;

public interface CovalenthqVaultTransactionRepository extends JpaRepository<CovalenthqVaultTransaction, Integer> {
  List<CovalenthqVaultTransaction> findAllByNetworkAndContractAddress(String network, String contractAddress, Pageable pageable);
  List<CovalenthqVaultTransaction> findAllByTransactionHashIn(List<String> transactionHashes);
  List<CovalenthqVaultTransaction> findAllByOwnerAddressAndNetwork(String ownerAddress, String network);

  @Query("select c from CovalenthqVaultTransaction c "
      + "where c.network = :network AND c.contractAddress = :address AND c.block BETWEEN :blockFrom AND :blockTo")
  List<CovalenthqVaultTransaction> findAllByContractAddressAndBlockBetween(String address, String network, long blockFrom, long blockTo);
}
