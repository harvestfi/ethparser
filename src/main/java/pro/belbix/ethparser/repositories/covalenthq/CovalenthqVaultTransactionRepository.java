package pro.belbix.ethparser.repositories.covalenthq;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import pro.belbix.ethparser.entity.profit.CovalenthqVaultTransaction;

public interface CovalenthqVaultTransactionRepository extends JpaRepository<CovalenthqVaultTransaction, Integer> {
  List<CovalenthqVaultTransaction> findAllByNetworkAndContractAddress(String network, String contractAddress, Pageable pageable);
  List<CovalenthqVaultTransaction> findAllByTransactionHashIn(List<String> transactionHashes);
}
