package pro.belbix.ethparser.web3.contracts;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import org.web3j.tuples.generated.Tuple2;
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

    public static Optional<String> getAddressByName(String name, ContractType type) {
        if (type == ContractType.VAULT) {
            return ContractLoader.getVaultByName(name)
                .map(VaultEntity::getContract)
                .map(ContractEntity::getAddress);
        }
        if (type == ContractType.POOL) {
            return ContractLoader.getPoolByName(name)
                .map(PoolEntity::getContract)
                .map(ContractEntity::getAddress);
        }
        if (type == ContractType.UNI_PAIR) {
            return ContractLoader.getUniPairByName(name)
                .map(UniPairEntity::getContract)
                .map(ContractEntity::getAddress);
        }
        if (type == ContractType.TOKEN) {
            return ContractLoader.getTokenByName(name)
                .map(TokenEntity::getContract)
                .map(ContractEntity::getAddress);
        }
        throw new IllegalStateException("unknown type" + type);
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

    public static Tuple2<String, String> uniPairTokensByAddress(String address) {
        UniPairEntity uniPair = ContractLoader.getUniPairByAddress(address)
            .orElseThrow(() -> new IllegalStateException("Not found uni pair by " + address));
        return new Tuple2<>(
            uniPair.getToken0().getAddress(),
            uniPair.getToken1().getAddress()
        );
    }

    public static String findUniPairForTokens(String token0, String token1) {
        for (UniPairEntity uniPair :
            ContractLoader.uniPairsCacheByAddress.values()) {
            String t0 = uniPair.getToken0().getAddress();
            String t1 = uniPair.getToken1().getAddress();
            if (
                (t0.equalsIgnoreCase(token0)
                    || t1.equalsIgnoreCase(token0))
                    && (
                    t0.equalsIgnoreCase(token1)
                        || t1.equalsIgnoreCase(token1)
                )
            ) {
                return uniPair.getContract().getAddress();
            }
        }
        throw new IllegalStateException("Not found LP for " + token0 + " and " + token1);
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
                vaultAddress = getAddressByName(vaultName.replace("_V0", ""), ContractType.VAULT)
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

    public static Tuple2<TokenEntity, TokenEntity> getUniPairTokens(String address) {
        UniPairEntity uniPair = Optional.ofNullable(ContractLoader.uniPairsCacheByAddress.get(address))
            .orElseThrow(() -> new IllegalStateException("Not found uniPair by " + address));
        return new Tuple2<>(
            Optional.ofNullable(ContractLoader.tokensCacheByAddress.get(uniPair.getToken0().getAddress()))
                .orElseThrow(() -> new IllegalStateException("Not found token by " + uniPair.getToken0().getAddress())),
            Optional.ofNullable(ContractLoader.tokensCacheByAddress.get(uniPair.getToken1().getAddress()))
                .orElseThrow(() -> new IllegalStateException("Not found token by " + uniPair.getToken1().getAddress()))
        );
    }

    public static boolean isDivisionSequenceSecondDividesFirst(String uniPairAddress, String tokenAddress) {
        Tuple2<TokenEntity, TokenEntity> tokens = getUniPairTokens(uniPairAddress);
        if (tokens.component1().getContract().getAddress().equalsIgnoreCase(tokenAddress)) {
            return true;
        } else if (tokens.component2().getContract().getAddress().equalsIgnoreCase(tokenAddress)) {
            return false;
        } else {
            throw new IllegalStateException("UniPair doesn't contain " + tokenAddress);
        }
    }

    public static Optional<String> findKeyTokenForUniPair(String address) {
        return Optional.ofNullable(ContractLoader.uniPairsCacheByAddress.get(address))
            .map(UniPairEntity::getKeyToken)
            .map(TokenEntity::getContract)
            .map(ContractEntity::getAddress);
    }

    public static int getUniPairType(String address) {
        return Optional.ofNullable(ContractLoader.uniPairsCacheByAddress.get(address))
            .map(UniPairEntity::getType)
            .orElse(0);
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

    public static Collection<String> getAllUniPairAddressesWithKeys() {
        return ContractLoader.uniPairsCacheByAddress.values().stream()
            .filter(u -> u.getKeyToken() != null)
            .map(UniPairEntity::getContract)
            .map(ContractEntity::getAddress)
            .collect(Collectors.toList());
    }
}
