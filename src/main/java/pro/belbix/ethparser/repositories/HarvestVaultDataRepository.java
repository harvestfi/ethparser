package pro.belbix.ethparser.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.belbix.ethparser.entity.HarvestVaultData;

public interface HarvestVaultDataRepository extends JpaRepository<HarvestVaultData, String> {

}
