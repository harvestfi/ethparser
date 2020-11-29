package pro.belbix.ethparser.web3.harvest.contracts;

import java.util.LinkedHashMap;
import java.util.Map;

public class RewardVaults {

    public final static String fYCRV_V0 = "".toLowerCase();
    public final static String fWETH_V0 = "".toLowerCase();
    public final static String fUSDC_V0 = "".toLowerCase();
    public final static String fUSDT_V0 = "".toLowerCase();
    public final static String fDAI_V0 = "".toLowerCase();
    public final static String fWBTC_V0 = "".toLowerCase();
    public final static String fRENBTC_V0 = "".toLowerCase();
    public final static String fCRVRENWBTC_V0 = "".toLowerCase();
    public final static String fUNI_ETH_DAI_V0 = "".toLowerCase();
    public final static String fUNI_ETH_USDC_V0 = "".toLowerCase();
    public final static String fUNI_ETH_USDT_V0 = "".toLowerCase();
    public final static String fUNI_ETH_WBTC_V0 = "".toLowerCase();
    public final static String fUNI_ETH_DAI = "0x7aeb36e22e60397098C2a5C51f0A5fB06e7b859c".toLowerCase();
    public final static String fUNI_ETH_USDC = "0x156733b89Ac5C704F3217FEe2949A9D4A73764b5".toLowerCase();
    public final static String fUNI_ETH_USDT = "0x75071F2653fBC902EBaff908d4c68712a5d1C960".toLowerCase();
    public final static String fUNI_ETH_WBTC = "0xF1181A71CC331958AE2cA2aAD0784Acfc436CB93".toLowerCase();
    public final static String fWETH = "0x3DA9D911301f8144bdF5c3c67886e5373DCdff8e".toLowerCase();
    public final static String fUSDC = "0x4F7c28cCb0F1Dbd1388209C67eEc234273C878Bd".toLowerCase();
    public final static String fUSDT = "0x6ac4a7AB91E6fD098E13B7d347c6d4d1494994a2".toLowerCase();
    public final static String fDAI = "0x15d3A64B2d5ab9E152F16593Cdebc4bB165B5B4A".toLowerCase();
    public final static String fWBTC = "0x917d6480Ec60cBddd6CbD0C8EA317Bcc709EA77B".toLowerCase();
    public final static String fRENBTC = "0x7b8Ff8884590f44e10Ea8105730fe637Ce0cb4F6".toLowerCase();
    public final static String fCRVRENWBTC = "0xA3Cf8D1CEe996253FAD1F8e3d68BDCba7B3A3Db5".toLowerCase();
    public final static String fSUSHI_WBTC_TBTC = "0x9523FdC055F503F73FF40D7F66850F409D80EF34".toLowerCase();
    public final static String fYCRV = "0x6D1b6Ea108AA03c6993d8010690264BA96D349A8".toLowerCase();
    public final static String f_3CRV = "0x27F12d1a08454402175b9F0b53769783578Be7d9".toLowerCase();
    public final static String fTUSD = "0xeC56a21CF0D7FeB93C25587C12bFfe094aa0eCdA".toLowerCase();
    public final static String fCRV_TBTC = "0x017eC1772A45d2cf68c429A820eF374f0662C57c".toLowerCase();
    public final static String fPS = "".toLowerCase();
    public final static String fPS_V0 = "".toLowerCase();
    public final static String fCRV_CMPND = "0xC0f51a979e762202e9BeF0f62b07F600d0697DE1".toLowerCase();
    public final static String fCRV_BUSD = "0x093C2ae5E6F3D2A897459aa24551289D462449AD".toLowerCase();
    public final static String fCRV_USDN = "0xef4Da1CE3f487DA2Ed0BE23173F76274E0D47579".toLowerCase();
    public final static String fSUSHI_ETH_DAI = "0x76Aef359a33C02338902aCA543f37de4b01BA1FA".toLowerCase();
    public final static String fSUSHI_ETH_USDC = "0x6B4e1E0656Dd38F36c318b077134487B9b0cf7a6".toLowerCase();
    public final static String fSUSHI_ETH_USDT = "0xA56522BCA0A09f57B85C52c0Cc8Ba1B5eDbc64ef".toLowerCase();
    public final static String fSUSHI_ETH_WBTC = "0xE2D9FAe95f1e68afca7907dFb36143781f917194".toLowerCase();

    public final static Map<String, String> hashToName = new LinkedHashMap<>();

    static {
        hashToName.put(fYCRV_V0, "fYCRV_V0");
        hashToName.put(fWETH_V0, "fWETH_V0");
        hashToName.put(fUSDC_V0, "fUSDC_V0");
        hashToName.put(fUSDT_V0, "fUSDT_V0");
        hashToName.put(fDAI_V0, "fDAI_V0");
        hashToName.put(fWBTC_V0, "fWBTC_V0");
        hashToName.put(fRENBTC_V0, "fRENBTC_V0");
        hashToName.put(fCRVRENWBTC_V0, "fCRVRENWBTC_V0");
        hashToName.put(fUNI_ETH_DAI_V0, "fUNI_ETH_DAI_V0");
        hashToName.put(fUNI_ETH_USDC_V0, "fUNI_ETH_USDC_V0");
        hashToName.put(fUNI_ETH_USDT_V0, "fUNI_ETH_USDT_V0");
        hashToName.put(fUNI_ETH_WBTC_V0, "fUNI_ETH_WBTC_V0");
        hashToName.put(fUNI_ETH_DAI, "fUNI_ETH_DAI");
        hashToName.put(fUNI_ETH_USDC, "fUNI_ETH_USDC");
        hashToName.put(fUNI_ETH_USDT, "fUNI_ETH_USDT");
        hashToName.put(fUNI_ETH_WBTC, "fUNI_ETH_WBTC");
        hashToName.put(fWETH, "fWETH");
        hashToName.put(fUSDC, "fUSDC");
        hashToName.put(fUSDT, "fUSDT");
        hashToName.put(fDAI, "fDAI");
        hashToName.put(fWBTC, "fWBTC");
        hashToName.put(fRENBTC, "fRENBTC");
        hashToName.put(fCRVRENWBTC, "fCRVRENWBTC");
        hashToName.put(fSUSHI_WBTC_TBTC, "fSUSHI_WBTC_TBTC");
        hashToName.put(fYCRV, "fYCRV");
        hashToName.put(f_3CRV, "f_3CRV");
        hashToName.put(fTUSD, "fTUSD");
        hashToName.put(fCRV_TBTC, "fCRV_TBTC");
        hashToName.put(fPS, "fPS");
        hashToName.put(fPS_V0, "fPS_V0");
        hashToName.put(fCRV_CMPND, "fCRV_CMPND");
        hashToName.put(fCRV_BUSD, "fCRV_BUSD");
        hashToName.put(fCRV_USDN, "fCRV_USDN");
        hashToName.put(fSUSHI_ETH_DAI, "fSUSHI_ETH_DAI");
        hashToName.put(fSUSHI_ETH_USDC, "fSUSHI_ETH_USDC");
        hashToName.put(fSUSHI_ETH_USDT, "fSUSHI_ETH_USDT");
        hashToName.put(fSUSHI_ETH_WBTC, "fSUSHI_ETH_WBTC");

    }

}
