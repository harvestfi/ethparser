package pro.belbix.ethparser.web3.harvest;

import static pro.belbix.ethparser.web3.harvest.Vaults.CRVRENWBTC;
import static pro.belbix.ethparser.web3.harvest.Vaults.CRVRENWBTC_V0;
import static pro.belbix.ethparser.web3.harvest.Vaults.CRV_TBTC;
import static pro.belbix.ethparser.web3.harvest.Vaults.DAI;
import static pro.belbix.ethparser.web3.harvest.Vaults.DAI_V0;
import static pro.belbix.ethparser.web3.harvest.Vaults.RENBTC;
import static pro.belbix.ethparser.web3.harvest.Vaults.RENBTC_V0;
import static pro.belbix.ethparser.web3.harvest.Vaults.SUSHI_WBTC_TBTC;
import static pro.belbix.ethparser.web3.harvest.Vaults.TUSD;
import static pro.belbix.ethparser.web3.harvest.Vaults.UNI_ETH_DAI;
import static pro.belbix.ethparser.web3.harvest.Vaults.UNI_ETH_DAI_V0;
import static pro.belbix.ethparser.web3.harvest.Vaults.UNI_ETH_USDC;
import static pro.belbix.ethparser.web3.harvest.Vaults.UNI_ETH_USDC_V0;
import static pro.belbix.ethparser.web3.harvest.Vaults.UNI_ETH_USDT;
import static pro.belbix.ethparser.web3.harvest.Vaults.UNI_ETH_USDT_V0;
import static pro.belbix.ethparser.web3.harvest.Vaults.UNI_ETH_WBTC;
import static pro.belbix.ethparser.web3.harvest.Vaults.UNI_ETH_WBTC_V0;
import static pro.belbix.ethparser.web3.harvest.Vaults.USDC;
import static pro.belbix.ethparser.web3.harvest.Vaults.USDC_V0;
import static pro.belbix.ethparser.web3.harvest.Vaults.USDT;
import static pro.belbix.ethparser.web3.harvest.Vaults.USDT_V0;
import static pro.belbix.ethparser.web3.harvest.Vaults.WBTC;
import static pro.belbix.ethparser.web3.harvest.Vaults.WBTC_V0;
import static pro.belbix.ethparser.web3.harvest.Vaults.WETH;
import static pro.belbix.ethparser.web3.harvest.Vaults.WETH_V0;
import static pro.belbix.ethparser.web3.harvest.Vaults.YCRV;
import static pro.belbix.ethparser.web3.harvest.Vaults._3CRV;

import java.util.LinkedHashMap;
import java.util.Map;

public class Stackings {

    public static final String ST_WETH_V0 = "0xE11c81b924bB91B44BaE19793539054b48158a9d".toLowerCase();
    public static final String ST_USDC_V0 = "0xe1f9a3ee001a2ecc906e8de637dbf20bb2d44633".toLowerCase();
    public static final String ST_USDT_V0 = "".toLowerCase();
    public static final String ST_DAI_V0 = "".toLowerCase();
    public static final String ST_WBTC_V0 = "".toLowerCase();
    public static final String ST_RENBTC_V0 = "".toLowerCase();
    public static final String ST_CRVRENWBTC_V0 = "".toLowerCase();
    public static final String ST_UNI_ETH_DAI_V0 = "".toLowerCase();
    public static final String ST_UNI_ETH_USDC_V0 = "".toLowerCase();
    public static final String ST_UNI_ETH_USDT_V0 = "".toLowerCase();
    public static final String ST_UNI_ETH_WBTC_V0 = "".toLowerCase();
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
    public static final String ST_YCRV = "".toLowerCase();
    public static final String ST__3CRV = "".toLowerCase();
    public static final String ST_TUSD = "0xeC56a21CF0D7FeB93C25587C12bFfe094aa0eCdA".toLowerCase();
    public static final String ST_PS = "0x25550Cccbd68533Fa04bFD3e3AC4D09f9e00Fc50".toLowerCase();
    public static final String ST_CRV_TBTC = "".toLowerCase();

    public static final Map<String, String> hashToName = new LinkedHashMap<>();
    public static final Map<String, String> vaultHashToStackingHash = new LinkedHashMap<>();

    static {
        hashToName.put(ST_WETH_V0, "ST_WETH_V0");
        hashToName.put(ST_USDC_V0, "ST_USDC_V0");
        hashToName.put(ST_USDT_V0, "ST_USDT_V0");
        hashToName.put(ST_DAI_V0, "ST_DAI_V0");
        hashToName.put(ST_WBTC_V0, "ST_WBTC_V0");
        hashToName.put(ST_RENBTC_V0, "ST_RENBTC_V0");
        hashToName.put(ST_CRVRENWBTC_V0, "ST_CRVRENWBTC_V0");
        hashToName.put(ST_UNI_ETH_DAI_V0, "ST_UNI_ETH_DAI_V0");
        hashToName.put(ST_UNI_ETH_USDC_V0, "ST_UNI_ETH_USDC_V0");
        hashToName.put(ST_UNI_ETH_USDT_V0, "ST_UNI_ETH_USDT_V0");
        hashToName.put(ST_UNI_ETH_WBTC_V0, "ST_UNI_ETH_WBTC_V0");
        hashToName.put(ST_UNI_ETH_DAI, "ST_UNI_ETH_DAI");
        hashToName.put(ST_UNI_ETH_USDC, "ST_UNI_ETH_USDC");
        hashToName.put(ST_UNI_ETH_USDT, "ST_UNI_ETH_USDT");
        hashToName.put(ST_UNI_ETH_WBTC, "ST_UNI_ETH_WBTC");
        hashToName.put(ST_WETH, "ST_WETH");
        hashToName.put(ST_USDC, "ST_USDC");
        hashToName.put(ST_USDT, "ST_USDT");
        hashToName.put(ST_DAI, "ST_DAI");
        hashToName.put(ST_WBTC, "ST_WBTC");
        hashToName.put(ST_RENBTC, "ST_RENBTC");
        hashToName.put(ST_CRVRENWBTC, "ST_CRVRENWBTC");
        hashToName.put(ST_SUSHI_WBTC_TBTC, "ST_SUSHI_WBTC_TBTC");
        hashToName.put(ST_YCRV, "ST_YCRV");
        hashToName.put(ST__3CRV, "ST__3CRV");
        hashToName.put(ST_TUSD, "ST_TUSD");
        hashToName.put(ST_PS, "ST_PS");

        vaultHashToStackingHash.put(WETH_V0, ST_WETH_V0);
        vaultHashToStackingHash.put(USDC_V0, ST_USDC_V0);
        vaultHashToStackingHash.put(USDT_V0, ST_USDT_V0);
        vaultHashToStackingHash.put(DAI_V0, ST_DAI_V0);
        vaultHashToStackingHash.put(WBTC_V0, ST_WBTC_V0);
        vaultHashToStackingHash.put(RENBTC_V0, ST_RENBTC_V0);
        vaultHashToStackingHash.put(CRVRENWBTC_V0, ST_CRVRENWBTC_V0);
        vaultHashToStackingHash.put(UNI_ETH_DAI_V0, ST_UNI_ETH_DAI_V0);
        vaultHashToStackingHash.put(UNI_ETH_USDC_V0, ST_UNI_ETH_USDC_V0);
        vaultHashToStackingHash.put(UNI_ETH_USDT_V0, ST_UNI_ETH_USDT_V0);
        vaultHashToStackingHash.put(UNI_ETH_WBTC_V0, ST_UNI_ETH_WBTC_V0);
        vaultHashToStackingHash.put(UNI_ETH_DAI, ST_UNI_ETH_DAI);
        vaultHashToStackingHash.put(UNI_ETH_USDC, ST_UNI_ETH_USDC);
        vaultHashToStackingHash.put(UNI_ETH_USDT, ST_UNI_ETH_USDT);
        vaultHashToStackingHash.put(UNI_ETH_WBTC, ST_UNI_ETH_WBTC);
        vaultHashToStackingHash.put(WETH, ST_WETH);
        vaultHashToStackingHash.put(USDC, ST_USDC);
        vaultHashToStackingHash.put(USDT, ST_USDT);
        vaultHashToStackingHash.put(DAI, ST_DAI);
        vaultHashToStackingHash.put(WBTC, ST_WBTC);
        vaultHashToStackingHash.put(RENBTC, ST_RENBTC);
        vaultHashToStackingHash.put(CRVRENWBTC, ST_CRVRENWBTC);
        vaultHashToStackingHash.put(SUSHI_WBTC_TBTC, ST_SUSHI_WBTC_TBTC);
        vaultHashToStackingHash.put(YCRV, ST_YCRV);
        vaultHashToStackingHash.put(_3CRV, ST__3CRV);
        vaultHashToStackingHash.put(TUSD, ST_TUSD);
        vaultHashToStackingHash.put(CRV_TBTC, ST_CRV_TBTC);
    }

}
