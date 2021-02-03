package pro.belbix.ethparser.web3.contracts;

import java.util.Collection;
import java.util.Optional;
import pro.belbix.ethparser.entity.eth.ContractEntity;
import pro.belbix.ethparser.entity.eth.ContractTypeEntity.Type;
import pro.belbix.ethparser.entity.eth.PoolEntity;
import pro.belbix.ethparser.entity.eth.TokenEntity;
import pro.belbix.ethparser.entity.eth.UniPairEntity;
import pro.belbix.ethparser.entity.eth.VaultEntity;

public class ContractUtils {

    private ContractUtils() {
    }

    public static Optional<String> getNameByAddress(String address, Type type) {
        if (type == null) {
            return Optional.empty();
        }
        switch (type) {
            case VAULT:
                return ContractLoader.getVaultByAddress(address)
                    .map(VaultEntity::getAddress)
                    .map(ContractEntity::getName);
            case POOL:
                return ContractLoader.getPoolByAddress(address)
                    .map(PoolEntity::getAddress)
                    .map(ContractEntity::getName);
            case UNI_PAIR:
                return ContractLoader.getUniPairByAddress(address)
                    .map(UniPairEntity::getAddress)
                    .map(ContractEntity::getName);
            case TOKEN:
                return ContractLoader.getTokenByAddress(address)
                    .map(TokenEntity::getAddress)
                    .map(ContractEntity::getName);
            default:
                throw new IllegalStateException("Unknown type " + type);
        }
    }

    public static Optional<String> getAddressByName(String name, Type type) {
        if (type == null) {
            return Optional.empty();
        }
        switch (type) {
            case VAULT:
                return ContractLoader.getVaultByName(name)
                    .map(VaultEntity::getAddress)
                    .map(ContractEntity::getAddress);
            case POOL:
                return ContractLoader.getPoolByName(name)
                    .map(PoolEntity::getAddress)
                    .map(ContractEntity::getAddress);
            case UNI_PAIR:
                return ContractLoader.getUniPairByName(name)
                    .map(UniPairEntity::getAddress)
                    .map(ContractEntity::getAddress);
            case TOKEN:
                return ContractLoader.getTokenByName(name)
                    .map(TokenEntity::getAddress)
                    .map(ContractEntity::getAddress);
            default:
                throw new IllegalStateException("Unknown type " + type);
        }
    }

    public static boolean isLp(String vaultName) {
        VaultEntity vaultEntity = ContractLoader.getVaultByName(vaultName)
            .orElseThrow(() -> new IllegalStateException("Not found vault for name " + vaultName));
        return ContractLoader.getUniPairByAddress(
            vaultEntity.getUnderlying().getAddress())
            .isPresent();
    }

    public static String vaultUnderlyingToken(String vaultAddress) {
        return ContractLoader.getVaultByAddress(vaultAddress)
            .orElseThrow(() -> new IllegalStateException("Not found vault for name " + vaultAddress))
            .getUnderlying().getAddress();
    }

    public static Collection<String> getAllPoolAddresses() {
        return HarvestPoolAddresses.POOLS.values();
    }

    public static Collection<String> getAllPoolNames() {
        return HarvestPoolAddresses.POOLS.keySet();
    }

    public static Collection<String> getAllVaultAddresses() {
        return HarvestVaultAddresses.VAULTS.values();
    }

    public static Collection<String> getAllVaultNames() {
        return HarvestVaultAddresses.VAULTS.keySet();
    }

}
