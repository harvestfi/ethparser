package pro.belbix.ethparser.web3.contracts;

import static pro.belbix.ethparser.web3.contracts.ContractConstants.D2;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.D6;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.D8;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Tokens {

    public static final String FARM_TOKEN = "0xa0246c9032bc3a600820415ae600c6388619a14d".toLowerCase();
    public static final String BADGER_TOKEN = "0x3472A5A71965499acd81997a54BBA8D852C6E53d".toLowerCase();
    public final static String USDC_TOKEN = "0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48".toLowerCase();
    public final static String WETH_TOKEN = "0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2".toLowerCase();
    public final static String WBTC_TOKEN = "0x2260fac5e5542a773aa44fbcfedf7c193bc2c599".toLowerCase();
    public final static String DAI_TOKEN = "0x6b175474e89094c44da98b954eedeac495271d0f".toLowerCase();
    public final static String TBTC_TOKEN = "0x8daebade922df735c38c80c7ebd708af50815faa".toLowerCase();
    public final static String USDT_TOKEN = "0xdac17f958d2ee523a2206206994597c13d831ec7".toLowerCase();
    public final static String IDX_TOKEN = "0x95b3497bbcccc46a8f45f5cf54b0878b39f8d96c".toLowerCase();
    public final static String DPI_TOKEN = "0x1494ca1f11d487c2bbe4543e90080aeba4ba3c2b".toLowerCase();
    public final static String GRAIN_TOKEN = "0x6589fe1271A0F29346796C6bAf0cdF619e25e58e".toLowerCase();
    public final static String TUSD_TOKEN = "0x0000000000085d4780b73119b644ae5ecd22b376".toLowerCase();
    public final static String BAC_TOKEN = "0x3449fc1cd036255ba1eb19d65ff4ba2b8903a69a".toLowerCase();
    public final static String BAS_TOKEN = "0xa7ED29B253D8B4E3109ce07c80fc570f81B63696".toLowerCase();
    public final static String MIC_TOKEN = "0x368B3a58B5f49392e5C9E4C998cb0bB966752E51".toLowerCase();
    public final static String MIS_TOKEN = "0x4b4D2e899658FB59b1D518b68fe836B100ee8958".toLowerCase();
    public final static String BSG_TOKEN = "0xb34ab2f65c6e4f764ffe740ab83f982021faed6d".toLowerCase();
    public final static String BSGS_TOKEN = "0xa9d232cc381715ae791417b624d7c4509d2c28db".toLowerCase();
    public final static String ESD_TOKEN = "0x36f3fd68e7325a35eb768f1aedaae9ea0689d723".toLowerCase();
    public final static String DSD_TOKEN = "0xbd2f0cd039e0bfcf88901c98c0bfac5ab27566e3".toLowerCase();
    public final static String EURS_TOKEN = "0xdb25f211ab05b1c97d595516f45794528a807ad8".toLowerCase();
    public final static String UST_TOKEN = "0xa47c8bf37f92abed4a126bda807a7b7498661acd".toLowerCase();
    public final static String MAAPL_TOKEN = "0xd36932143f6ebdedd872d5fb0651f4b72fd15a84".toLowerCase();
    public final static String MAMZN_TOKEN = "0x0cae9e4d663793c2a2a0b211c1cf4bbca2b9caa7".toLowerCase();
    public final static String MGOOGL_TOKEN = "0x59A921Db27Dd6d4d974745B7FfC5c33932653442".toLowerCase();
    public final static String MTSLA_TOKEN = "0x21ca39943e91d704678f5d00b6616650f066fd63".toLowerCase();
    public final static String GUSD_TOKEN = "0x056fd409e1d7a124bd7017459dfea2f387b6d5cd".toLowerCase();
    public final static String YCRV_TOKEN = "0xdF5e0e81Dff6FAF3A7e52BA697820c5e32D806A8".toLowerCase();
    public final static String _3CRV_TOKEN = "0x6c3F90f043a72FA612cbac8115EE7e52BDe6E490".toLowerCase();
    public final static String CRV_CMPND_TOKEN = "0x845838DF265Dcd2c412A1Dc9e959c7d08537f8a2".toLowerCase();
    public final static String CRV_BUSD_TOKEN = "0x3B3Ac5386837Dc563660FB6a0937DFAa5924333B".toLowerCase();
    public final static String CRV_USDN_TOKEN = "0x4f3E8F405CF5aFC05D68142F3783bDfE13811522".toLowerCase();
    public final static String HUSD_TOKEN = "0xdF574c24545E5FfEcb9a659c229253D4111d87e1".toLowerCase();
    public final static String CRV_UST_TOKEN = "0x94e131324b6054c0D789b190b2dAC504e4361b53".toLowerCase();
    public final static String CRV_GUSD_TOKEN = "0xD2967f45c4f384DEEa880F807Be904762a3DeA07".toLowerCase();
    public final static String CRV_EURS_TOKEN = "0x194eBd173F6cDacE046C53eACcE9B953F28411d1".toLowerCase();
    public final static String CRV_OBTC_TOKEN = "0x2fE94ea3d5d4a175184081439753DE15AeF9d614".toLowerCase();
    public final static String CRV_STETH_TOKEN = "0x06325440D014e39736583c165C2963BA99fAf14E".toLowerCase();
    public final static String CRV_AAVE_TOKEN = "0xfd2a8fa60abd58efe3eee34dd494cd491dc14900".toLowerCase();
    public final static String SUSHI_TOKEN = "0x6b3595068778dd592e39a122f4f5a5cf09c90fe2".toLowerCase();
    public final static String HBTC_TOKEN = "0x0316eb71485b0ab14103307bf65a021042c6d380".toLowerCase();
    public final static String SBTC_TOKEN = "0xfE18be6b3Bd88A2D2A7f928d00292E7a9963CfC6".toLowerCase();

    public static final String FARM_NAME = "FARM";
    public static final String BADGER_NAME = "BADGER";
    public static final String USDC_NAME = "USDC";
    public static final String WETH_NAME = "ETH";
    public static final String WBTC_NAME = "WBTC";
    public static final String DAI_NAME = "DAI";
    public static final String TBTC_NAME = "TBTC";
    public static final String USDT_NAME = "USDT";
    public static final String IDX_NAME = "IDX";
    public static final String DPI_NAME = "DPI";
    public static final String GRAIN_NAME = "GRAIN";
    public static final String TUSD_NAME = "TUSD";
    public static final String BAC_NAME = "BAC";
    public static final String BAS_NAME = "BAS";
    public static final String MIC_NAME = "MIC";
    public static final String MIS_NAME = "MIS";
    public static final String BSG_NAME = "BSG";
    public static final String BSGS_NAME = "BSGS";
    public static final String ESD_NAME = "ESD";
    public static final String DSD_NAME = "DSD";
    public static final String EURS_NAME = "EURS";
    public static final String UST_NAME = "UST";
    public static final String MAAPL_NAME = "MAAPL";
    public static final String MAMZN_NAME = "MAMZN";
    public static final String MGOOGL_NAME = "MGOOGL";
    public static final String MTSLA_NAME = "MTSLA";
    public static final String GUSD_NAME = "GUSD";
    public static final String YCRV_NAME = "YCRV";
    public static final String _3CRV_NAME = "3CRV";
    public static final String CRV_CMPND_NAME = "CRV_CMPND";
    public static final String CRV_BUSD_NAME = "CRV_BUSD";
    public static final String CRV_USDN_NAME = "CRV_USDN";
    public static final String HUSD_NAME = "HUSD";
    public static final String CRV_HUSD_NAME = "CRV_HUSD";
    public static final String CRV_UST_NAME = "CRV_UST";
    public static final String CRV_GUSD_NAME = "CRV_GUSD";
    public static final String CRV_EURS_NAME = "CRV_EURS";
    public static final String CRV_OBTC_NAME = "CRV_OBTC";
    public static final String CRV_STETH_NAME = "CRV_STETH";
    public static final String CRV_AAVE_NAME = "CRV_AAVE";
    public static final String SUSHI_NAME = "SUSHI";
    public static final String HBTC_NAME = "HBTC";
    public static final String SBTC_NAME = "SBTC";

    public final static Set<TokenInfo> tokenInfos = new HashSet<>();

    static {
        addTokenInfo(new TokenInfo(FARM_NAME, FARM_TOKEN, 10777201)
            .addLp("UNI_LP_USDC_FARM", 0, USDC_NAME)
            .addLp("UNI_LP_WETH_FARM", 11609000, WETH_NAME));

        addTokenInfo(new TokenInfo(DPI_NAME, DPI_TOKEN, 10868422)
            .addLp("UNI_LP_ETH_DPI", 0, WETH_NAME));

        addTokenInfo(new TokenInfo(GRAIN_NAME, GRAIN_TOKEN, 11408083)
            .addLp("UNI_LP_GRAIN_FARM", 0, FARM_NAME));

        addTokenInfo(new TokenInfo(BADGER_NAME, BADGER_TOKEN, 10868422)
            .addLp("UNI_LP_WBTC_BADGER", 0, WBTC_NAME));

        addTokenInfo(new TokenInfo(WETH_NAME, WETH_TOKEN, 0)
            .addLp("UNI_LP_USDC_ETH", 0, USDC_NAME));

        addTokenInfo(new TokenInfo(WBTC_NAME, WBTC_TOKEN, 0).setDivider(D8)
            .addLp("UNI_LP_ETH_WBTC", 0, WETH_NAME));

        addTokenInfo(new TokenInfo(BAC_NAME, BAC_TOKEN, 11356492)
            .addLp("UNI_LP_BAC_DAI", 0, DAI_NAME));

        addTokenInfo(new TokenInfo(BAS_NAME, BAS_TOKEN, 11356501)
            .addLp("UNI_LP_DAI_BAS", 0, DAI_NAME));

        addTokenInfo(new TokenInfo(MIC_NAME, MIC_TOKEN, 11551514)
            .addLp("SUSHI_LP_MIC_USDT", 0, USDT_NAME));

        addTokenInfo(new TokenInfo(MIS_NAME, MIS_TOKEN, 11551671)
            .addLp("SUSHI_LP_MIS_USDT", 0, USDT_NAME));

        addTokenInfo(new TokenInfo(IDX_NAME, IDX_TOKEN, 0)
            .addLp("UNI_LP_IDX_ETH", 0, WETH_NAME));

        addTokenInfo(new TokenInfo(BSG_NAME, BSG_TOKEN, 11644918)
            .addLp("UNI_LP_DAI_BSG", 0, DAI_NAME));

        addTokenInfo(new TokenInfo(BSGS_NAME, BSGS_TOKEN, 11647863)
            .addLp("UNI_LP_DAI_BSGS", 0, DAI_NAME));

        addTokenInfo(new TokenInfo(ESD_NAME, ESD_TOKEN, 10770511)
            .addLp("UNI_LP_ESD_USDC", 0, USDC_NAME));

        addTokenInfo(new TokenInfo(DSD_NAME, DSD_TOKEN, 11334505)
            .addLp("UNI_LP_USDC_DSD", 0, USDC_NAME));

        addTokenInfo(new TokenInfo(TBTC_NAME, TBTC_TOKEN, 0)
            .addLp("SUSHI_LP_WBTC_TBTC", 0, WBTC_NAME));

        addTokenInfo(new TokenInfo(EURS_NAME, EURS_TOKEN, 11386529).setDivider(100)
            .addLp("UNI_LP_USDC_EURS", 0, USDC_NAME));

        addTokenInfo(new TokenInfo(MAAPL_NAME, MAAPL_TOKEN, 11383561)
            .addLp("UNI_LP_MAAPL_UST", 0, UST_NAME));

        addTokenInfo(new TokenInfo(MAMZN_NAME, MAMZN_TOKEN, 11383561)
            .addLp("UNI_LP_MAMZN_UST", 0, UST_NAME));

        addTokenInfo(new TokenInfo(MGOOGL_NAME, MGOOGL_TOKEN, 11383561)
            .addLp("UNI_LP_MGOOGL_UST", 0, UST_NAME));

        addTokenInfo(new TokenInfo(MTSLA_NAME, MTSLA_TOKEN, 11383561)
            .addLp("UNI_LP_MTSLA_UST", 0, UST_NAME));

        addTokenInfo(new TokenInfo(SUSHI_NAME, SUSHI_TOKEN, 10736094)
            .addLp("SUSHI_LP_SUSHI_ETH", 0, WETH_NAME));

        addTokenInfo(new TokenInfo(HBTC_NAME, HBTC_TOKEN, 9076087)
            .addLp("UNI_LP_HBTC_ETH", 0, WETH_NAME));

        // todo implement via curve, don't have UNI liquidity
        addTokenInfo(new TokenInfo(SBTC_NAME, SBTC_TOKEN, Integer.MAX_VALUE)
            .addLp("", Integer.MAX_VALUE, WETH_NAME));

        //todo stablecoins
        addTokenInfo(new TokenInfo(USDC_NAME, USDC_TOKEN, 0).setDivider(D6));
        addTokenInfo(new TokenInfo(DAI_NAME, DAI_TOKEN, 0));
        addTokenInfo(new TokenInfo(USDT_NAME, USDT_TOKEN, 0).setDivider(D6));
        addTokenInfo(new TokenInfo(TUSD_NAME, TUSD_TOKEN, 0));
        addTokenInfo(new TokenInfo(UST_NAME, UST_TOKEN, 0));
        addTokenInfo(new TokenInfo(GUSD_NAME, GUSD_TOKEN, 0).setDivider(D2));
        addTokenInfo(new TokenInfo(YCRV_NAME, YCRV_TOKEN, 0));
        addTokenInfo(new TokenInfo(_3CRV_NAME, _3CRV_TOKEN, 0));
        addTokenInfo(new TokenInfo(CRV_CMPND_NAME, CRV_CMPND_TOKEN, 0));
        addTokenInfo(new TokenInfo(CRV_BUSD_NAME, CRV_BUSD_TOKEN, 0));
        addTokenInfo(new TokenInfo(CRV_USDN_NAME, CRV_USDN_TOKEN, 0));
        addTokenInfo(new TokenInfo(HUSD_NAME, HUSD_TOKEN, 0));
        addTokenInfo(new TokenInfo(CRV_UST_NAME, CRV_UST_TOKEN, 0));
        addTokenInfo(new TokenInfo(CRV_GUSD_NAME, CRV_GUSD_TOKEN, 0));
        addTokenInfo(new TokenInfo(CRV_EURS_NAME, CRV_EURS_TOKEN, 0));
        addTokenInfo(new TokenInfo(CRV_OBTC_NAME, CRV_OBTC_TOKEN, 0));
        addTokenInfo(new TokenInfo(CRV_STETH_NAME, CRV_STETH_TOKEN, 0));
        addTokenInfo(new TokenInfo(CRV_AAVE_NAME, CRV_AAVE_TOKEN, 11497098));
    }

    private static void addTokenInfo(TokenInfo tokenInfo) {
        for (TokenInfo info : tokenInfos) {
            if (tokenInfo.getTokenName().equalsIgnoreCase(info.getTokenName())
                || tokenInfo.getTokenAddress().equalsIgnoreCase(info.getTokenAddress())) {
                throw new IllegalStateException("You try to add token again " + tokenInfo);
            }
        }
        tokenInfos.add(tokenInfo);
    }

    public static Map<String, Double> getTokensDividers() {
        Map<String, Double> d = new HashMap<>();
        for (TokenInfo info : tokenInfos) {
            d.put(info.getTokenAddress(), info.getDivider());
        }
        return d;
    }

    public static boolean isCreated(String tokenName, int block) {
        if (isStableCoin(tokenName)) {
            return true;
        }
        return getTokenInfo(tokenName).getCreatedOnBlock() < block;
    }

    public static boolean isStableCoin(String name) {
        return "USD".equals(name)
            || USDC_NAME.equals(name)
            || USDT_NAME.equals(name)
            || YCRV_NAME.equals(name)
            || "_3CRV".equals(name)
            || _3CRV_NAME.equals(name)
            || TUSD_NAME.equals(name)
            || DAI_NAME.equals(name)
            || CRV_CMPND_NAME.equals(name)
            || CRV_BUSD_NAME.equals(name)
            || CRV_USDN_NAME.equals(name)
            || HUSD_NAME.equals(name)
            || CRV_HUSD_NAME.equals(name)
            || UST_NAME.equals(name)
            || CRV_UST_NAME.equals(name)
            || GUSD_NAME.equals(name)
            || CRV_GUSD_NAME.equals(name)
            || CRV_AAVE_NAME.equals(name)
            ;
    }

    public static TokenInfo getTokenInfo(String tokenName) {
        for (TokenInfo info : tokenInfos) {
            if (tokenName.equals(info.getTokenName())) {
                return info;
            }
        }
        throw new IllegalStateException("Not found token info for " + tokenName);
    }

    public static String findNameForContract(String contract) {
        for (TokenInfo info : tokenInfos) {
            if (info.getTokenAddress().equals(contract)) {
                return info.getTokenName();
            }
        }
        throw new IllegalStateException("Not found name for " + contract);
    }

    public static String findContractForName(String name) {
        return getTokenInfo(name).getTokenAddress();
    }

    public static String simplifyName(String name) {
        name = name.replaceFirst("_V0", "");
        switch (name) {
            case "CRV_STETH":
            case "WETH":
                return "ETH";
            case "PS":
                return "FARM";
            case "RENBTC":
            case "CRVRENWBTC":
            case "TBTC":
            case "BTC":
            case "CRV_OBTC":
            case "CRV_TBTC":
            case "HBTC":
            case "CRV_HBTC":
                return "WBTC";
            case "CRV_EURS":
                return "EURS";
        }
        return name;
    }

}
