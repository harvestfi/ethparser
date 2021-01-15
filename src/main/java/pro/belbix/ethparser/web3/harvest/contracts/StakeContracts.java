package pro.belbix.ethparser.web3.harvest.contracts;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
import pro.belbix.ethparser.web3.uniswap.contracts.LpContracts;

@SuppressWarnings("unused")
public class StakeContracts {

    public static final String ST_UNI_ETH_DAI = "0x7aeb36e22e60397098C2a5C51f0A5fB06e7b859c".toLowerCase();
    public static final String ST_UNI_ETH_USDC = "0x156733b89Ac5C704F3217FEe2949A9D4A73764b5".toLowerCase();
    public static final String ST_UNI_ETH_USDT = "0x75071F2653fBC902EBaff908d4c68712a5d1C960".toLowerCase();
    public static final String ST_UNI_ETH_WBTC = "0xF1181A71CC331958AE2cA2aAD0784Acfc436CB93".toLowerCase();
    public static final String ST_WETH = "0x3DA9D911301f8144bdF5c3c67886e5373DCdff8e".toLowerCase();
    public static final String ST_USDC = "0x4F7c28cCb0F1Dbd1388209C67eEc234273C878Bd".toLowerCase();
    public static final String ST_USDT = "0x6ac4a7ab91e6fd098e13b7d347c6d4d1494994a2".toLowerCase();
    public static final String ST_DAI = "0x15d3A64B2d5ab9E152F16593Cdebc4bB165B5B4A".toLowerCase();
    public static final String ST_WBTC = "0x917d6480Ec60cBddd6CbD0C8EA317Bcc709EA77B".toLowerCase();
    public static final String ST_RENBTC = "0x7b8Ff8884590f44e10Ea8105730fe637Ce0cb4F6".toLowerCase();
    public static final String ST_CRVRENWBTC = "0xA3Cf8D1CEe996253FAD1F8e3d68BDCba7B3A3Db5".toLowerCase();
    public static final String ST_SUSHI_WBTC_TBTC = "0x9523FdC055F503F73FF40D7F66850F409D80EF34".toLowerCase();
    public static final String ST_YCRV = "0x6D1b6Ea108AA03c6993d8010690264BA96D349A8".toLowerCase();
    public static final String ST__3CRV = "0x27F12d1a08454402175b9F0b53769783578Be7d9".toLowerCase();
    public static final String ST_TUSD = "0xeC56a21CF0D7FeB93C25587C12bFfe094aa0eCdA".toLowerCase();
    public static final String ST_PS = "0x8f5adC58b32D4e5Ca02EAC0E293D35855999436C".toLowerCase();
    public static final String ST_PS_V0 = "0x59258F4e15A5fC74A7284055A8094F58108dbD4f".toLowerCase();
    public static final String ST_CRV_TBTC = "0x017eC1772A45d2cf68c429A820eF374f0662C57c".toLowerCase();
    public static final String ST_SUSHI_ETH_DAI = "0x76Aef359a33C02338902aCA543f37de4b01BA1FA".toLowerCase();
    public static final String ST_SUSHI_ETH_USDC = "0x6B4e1E0656Dd38F36c318b077134487B9b0cf7a6".toLowerCase();
    public static final String ST_SUSHI_ETH_USDT = "0xA56522BCA0A09f57B85C52c0Cc8Ba1B5eDbc64ef".toLowerCase();
    public static final String ST_SUSHI_ETH_WBTC = "0xE2D9FAe95f1e68afca7907dFb36143781f917194".toLowerCase();
    public static final String ST_IDX_ETH_DPI = "0xad91695b4bec2798829ac7a4797e226c78f22abd".toLowerCase();
    public static final String ST_CRV_CMPND = "0xC0f51a979e762202e9BeF0f62b07F600d0697DE1".toLowerCase();
    public static final String ST_CRV_BUSD = "0x093C2ae5E6F3D2A897459aa24551289D462449AD".toLowerCase();
    public static final String ST_CRV_USDN = "0xef4Da1CE3f487DA2Ed0BE23173F76274E0D47579".toLowerCase();
    public static final String ST_CRV_HUSD = "0x72C50e6FD8cC5506E166c273b6E814342Aa0a3c1".toLowerCase();
    public static final String ST_CRV_HBTC = "0x01f9CAaD0f9255b0C0Aa2fBD1c1aA06ad8Af7254".toLowerCase();
    public static final String ST_UNI_LP_USDC_FARM = "0x99b0d6641A63Ce173E6EB063b3d3AED9A35Cf9bf".toLowerCase();
    public static final String ST_UNI_LP_WETH_FARM = "0x6555c79a8829b793F332f1535B0eFB1fE4C11958".toLowerCase();
    public static final String ST_UNI_LP_GRAIN_FARM = "0xe58f0d2956628921cdEd2eA6B195Fc821c3a2b16".toLowerCase();
    public static final String ST_UNI_BAC_DAI = "0x797F1171DC5001B7A79ff7Dca68c9539329ccE48".toLowerCase();
    public static final String ST_UNI_DAI_BAS = "0xf3B2B174E7f36e43246Ef33Fc58cE5821f21F799".toLowerCase();
    public static final String ST_SUSHI_MIC_USDT = "0x98Ba5E432933E2D536e24A2C021deBb8404588C1".toLowerCase();
    public static final String ST_SUSHI_MIS_USDT = "0xf4784d07136b5b10c6223134E8B36E1DC190725b".toLowerCase();
    public static final String ST_CRV_OBTC = "0x91B5cD52fDE8dbAC37C95ECafEF0a70bA4c182fC".toLowerCase();
    public static final String ST_ONEINCH_ETH_DAI = "0xda5e9706274d88bbf1bd1877a9b462fc40cdcfad".toLowerCase();
    public static final String ST_ONEINCH_ETH_USDC = "0x9a9a6148f7b0a9767ac1fdc67343d1e3e219fddf".toLowerCase();
    public static final String ST_ONEINCH_ETH_USDT = "0x2a80e0b572e7ef61ef81ef05ec024e1e52bd70bd".toLowerCase();
    public static final String ST_ONEINCH_ETH_WBTC = "0x747318cf3171d4e2a1a52bbd3fcc9f9c690448b4".toLowerCase();

    public static final Map<String, String> hashToName = new LinkedHashMap<>();
    public static final Map<String, String> nameToHash = new LinkedHashMap<>();
    public static final Map<String, String> vaultHashToStakeHash = new LinkedHashMap<>();

    static {
        try {
            initMaps();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isST_PS(String contract) {
        return StakeContracts.ST_PS.equalsIgnoreCase(contract) || StakeContracts.ST_PS_V0.equalsIgnoreCase(contract);
    }

    //dangerous, but useful
    private static void initMaps() throws IllegalAccessException, NoSuchFieldException {
        for (Field field : StakeContracts.class.getDeclaredFields()) {
            if(!(field.get(null) instanceof String)) {
                continue;
            }
            hashToName.put((String) field.get(null), field.getName());
            nameToHash.put(field.getName(), (String) field.get(null));
            String baseName = field.getName().replaceFirst("ST_", "");

            Field vault = null;
            try {
                vault = Vaults.class.getDeclaredField(baseName);
            } catch (NoSuchFieldException ignored) {
            }
            if(vault != null) {
                vaultHashToStakeHash.put((String) vault.get(null), (String) field.get(null));
            } else {
                Field lp = LpContracts.class.getDeclaredField(baseName);
                vaultHashToStakeHash.put((String) lp.get(null), (String) field.get(null));
            }
        }
    }

}
