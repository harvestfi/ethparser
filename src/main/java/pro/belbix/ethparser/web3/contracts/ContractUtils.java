package pro.belbix.ethparser.web3.contracts;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;
import pro.belbix.ethparser.entity.eth.ContractEntity;
import pro.belbix.ethparser.entity.eth.PoolEntity;
import pro.belbix.ethparser.entity.eth.TokenEntity;
import pro.belbix.ethparser.entity.eth.UniPairEntity;
import pro.belbix.ethparser.entity.eth.VaultEntity;

public class ContractUtils {

    private ContractUtils() {
    }

    public static Optional<String> getNameByAddress(String address) {
        Optional<String> name = ContractLoader.getVaultByAddress(address)
            .map(VaultEntity::getContract)
            .map(ContractEntity::getName);

        if (name.isEmpty()) {
            name = ContractLoader.getPoolByAddress(address)
                .map(PoolEntity::getContract)
                .map(ContractEntity::getName);
        }
        if (name.isEmpty()) {
            name = ContractLoader.getUniPairByAddress(address)
                .map(UniPairEntity::getContract)
                .map(ContractEntity::getName);
        }
        if (name.isEmpty()) {
            name = ContractLoader.getTokenByAddress(address)
                .map(TokenEntity::getContract)
                .map(ContractEntity::getName);
        }
        return name;
    }

    public static Optional<String> getAddressByName(String name) {
        Optional<String> address = ContractLoader.getVaultByName(name)
            .map(VaultEntity::getContract)
            .map(ContractEntity::getAddress);
        if (address.isEmpty()) {
            address = ContractLoader.getPoolByName(name)
                .map(PoolEntity::getContract)
                .map(ContractEntity::getAddress);
        }

        if (address.isEmpty()) {
            address = ContractLoader.getUniPairByName(name)
                .map(UniPairEntity::getContract)
                .map(ContractEntity::getAddress);
        }
        if (address.isEmpty()) {
            address = ContractLoader.getUniPairByName(name)
                .map(UniPairEntity::getContract)
                .map(ContractEntity::getAddress);
        }
        if (address.isEmpty()) {
            address = ContractLoader.getTokenByName(name)
                .map(TokenEntity::getContract)
                .map(ContractEntity::getAddress);
        }
        return address;

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

    public static Optional<PoolEntity> poolByVaultName(String name) {
        if (name.endsWith("_V0")) {
            name = name.replace("_V0", "");
        }
        return ContractLoader.getVaultByName(name)
            .map(VaultEntity::getContract)
            .map(ContractEntity::getAddress)
            .flatMap(adr -> ContractLoader.poolsCacheByAddress.values().stream()
                .filter(pool -> pool.getLpToken().getAddress().equals(adr))
                .findFirst());
    }

    public static Optional<PoolEntity> poolByVaultAddress(String address) {
        Optional<PoolEntity> poolEntity = ContractLoader.poolsCacheByAddress.values().stream()
            .filter(pool -> pool.getLpToken() != null
                && pool.getLpToken().getAddress().equalsIgnoreCase(address))
            .findFirst();
        if (poolEntity.isPresent()) {
            return poolEntity;
        }
        // try to find pool by name, it should work for old vaults and PS pools
        String vaultName = getNameByAddress(address)
            .orElseThrow(() -> new IllegalStateException("Vault not found for " + address));
        return ContractLoader.getPoolByName("ST_" + vaultName);
    }

    public static Optional<VaultEntity> vaultByPoolAddress(String address) {
        return Optional.ofNullable(ContractLoader.poolsCacheByAddress.get(address.toLowerCase()))
            .map(PoolEntity::getLpToken)
            .flatMap(c -> ContractLoader.getVaultByAddress(c.getAddress()));
    }

    public static boolean isVaultName(String name) {
        return ContractLoader.vaultsCacheByName.containsKey(name);
    }

    public static boolean isPoolName(String name) {
        return ContractLoader.poolsCacheByName.containsKey(name);
    }

    public static boolean isUniPairName(String name) {
        return ContractLoader.uniPairsCacheByName.containsKey(name);
    }

    public static boolean isTokenName(String name) {
        return ContractLoader.tokensCacheByName.containsKey(name);
    }

    public static boolean isPsName(String vaultName) {
        return "PS".equalsIgnoreCase(vaultName)
            || "PS_V0".equalsIgnoreCase(vaultName)
            || "ST_PS".equalsIgnoreCase(vaultName)
            || "ST_PS_V0".equalsIgnoreCase(vaultName)
            ;
    }

    public static boolean isVaultAddress(String address) {
        return ContractLoader.vaultsCacheByAddress.containsKey(address.toLowerCase());
    }

    public static boolean isPoolAddress(String address) {
        return ContractLoader.poolsCacheByAddress.containsKey(address.toLowerCase());
    }

    public static boolean isUniPairAddress(String address) {
        return ContractLoader.uniPairsCacheByAddress.containsKey(address.toLowerCase());
    }

    public static boolean isTokenAddress(String address) {
        return ContractLoader.tokensCacheByAddress.containsKey(address.toLowerCase());
    }

    public static boolean isPsAddress(String address) {
        return "0x8f5adC58b32D4e5Ca02EAC0E293D35855999436C".equalsIgnoreCase(address) // ST_PS
            || "0xa0246c9032bc3a600820415ae600c6388619a14d".equalsIgnoreCase(address) // ST_PS_V0
            || "0x25550Cccbd68533Fa04bFD3e3AC4D09f9e00Fc50".equalsIgnoreCase(address) // PS
            || "0x59258F4e15A5fC74A7284055A8094F58108dbD4f".equalsIgnoreCase(address) // PS_V0
            ;
    }

    public static BigDecimal getDividerByAddress(String address) {
        address = address.toLowerCase();
        long decimals;
        // unique addresses
        if (isPsAddress(address)) {
            decimals = 18L;
        } else if (ContractLoader.poolsCacheByAddress.containsKey(address)) {
            String vaultAddress = ContractLoader.poolsCacheByAddress.get(address)
                .getLpToken().getAddress();
            String vaultName = getNameByAddress(vaultAddress).orElseThrow();
            if (vaultName.endsWith("_V0")) {
                vaultAddress = getAddressByName(vaultName.replace("_V0", ""))
                    .orElseThrow(() -> new IllegalStateException("Not found address for " + vaultName));
            }
            decimals = ContractLoader.vaultsCacheByAddress.get(vaultAddress).getDecimals();
        } else if (ContractLoader.vaultsCacheByAddress.containsKey(address)) {
            decimals = ContractLoader.vaultsCacheByAddress.get(address).getDecimals();
        } else if (ContractLoader.uniPairsCacheByAddress.containsKey(address)) {
            decimals = ContractLoader.uniPairsCacheByAddress.get(address).getDecimals();
        } else if (ContractLoader.tokensCacheByAddress.containsKey(address)) {
            decimals = ContractLoader.tokensCacheByAddress.get(address).getDecimals();
        } else {
            throw new IllegalStateException("Unknown address " + address);
        }
        return new BigDecimal(10L).pow((int) decimals);
    }

    public static Collection<String> getAllPoolAddresses() {
        return ContractLoader.poolsCacheByAddress.keySet();
    }

    public static Collection<String> getAllPoolNames() {
        return ContractLoader.poolsCacheByName.keySet();
    }

    public static Collection<String> getAllVaultAddresses() {
        return ContractLoader.vaultsCacheByAddress.keySet();
    }

    public static Collection<String> getAllVaultNames() {
        return ContractLoader.vaultsCacheByName.keySet();
    }

    public static Collection<VaultEntity> getAllVaults() {
        return ContractLoader.vaultsCacheByAddress.values();
    }
    
    public static Optional<VaultEntity> getVaultByName(String name) {
        return Optional.ofNullable(ContractLoader.vaultsCacheByName.get(name.toUpperCase()));
    }

    public static Optional<VaultEntity> getVaultByAddress(String address) {
        return Optional.ofNullable(ContractLoader.vaultsCacheByAddress.get(address.toLowerCase()));
    }
}
