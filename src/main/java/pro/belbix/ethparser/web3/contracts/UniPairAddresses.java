package pro.belbix.ethparser.web3.contracts;

import static pro.belbix.ethparser.web3.contracts.Contract.createContracts;

import java.util.List;

public class UniPairAddresses {

    public UniPairAddresses() {
    }

    static final List<Contract> UNI_PAIRS = createContracts(
        new Contract(10042267, "UNI_LP_ETH_DAI", "0xA478c2975Ab1Ea89e8196811F51A7B7Ade33eB11"),
        new Contract(10008355, "UNI_LP_USDC_ETH", "0xB4e16d0168e52d35CaCD2c6185b44281Ec28C9Dc"),
        new Contract(10093341, "UNI_LP_ETH_USDT", "0x0d4a11d5EEaaC28EC3F61d100daF4d40471f1852"),
        new Contract(10091097, "UNI_LP_ETH_WBTC", "0xBb2b8038a1640196FbE3e38816F3e67Cba72D940"),
        new Contract(10994541, "SUSHI_LP_WBTC_TBTC", "0x2Dbc7dD86C6cd87b525BD54Ea73EBeeBbc307F68"),
        new Contract(10092348, "UNI_LP_USDC_WBTC", "0x004375dff511095cc5a197a54140a24efef3a416"),
        new Contract(10777016, "UNI_LP_USDC_FARM", "0x514906fc121c7878424a5c928cad1852cc545892"),
        new Contract(10777652, "UNI_LP_WETH_FARM", "0x56feaccb7f750b997b36a68625c7c596f0b41a58"),
        new Contract(11004221, "UNI_LP_IDX_ETH", "0x3452a7f30a712e415a0674c0341d44ee9d9786f9"),
        new Contract(11096263, "UNI_LP_USDC_IDX", "0xc372089019614e5791b08b5036f298d002a8cbef"),
        new Contract(10836224, "UNI_LP_ETH_DPI", "0x4d5ef58aac27d99935e5b6b4a6778ff292059991"),
        new Contract(11380633, "UNI_LP_WBTC_BADGER", "0xcd7989894bc033581532d2cd88da5db0a4b12859"),
        new Contract(10829331, "SUSHI_LP_ETH_DAI", "0xc3d03e4f041fd4cd388c549ee2a29a9e5075882f"),
        new Contract(10829331, "SUSHI_LP_ETH_USDC", "0x397ff1542f962076d0bfe58ea045ffa2d347aca0"),
        new Contract(10822038, "SUSHI_LP_ETH_USDT", "0x06da0fd433c1a5d7a4faa01111c044910a184553"),
        new Contract(10840845, "SUSHI_LP_ETH_WBTC", "0xceff51756c56ceffca006cd410b03ffc46dd3a58"),
        new Contract(11407008, "UNI_LP_GRAIN_FARM", "0xb9fa44b0911f6d777faab2fa9d8ef103f25ddf49"),
        new Contract(11355401, "UNI_LP_BAC_DAI", "0xd4405f0704621dbe9d4dea60e128e0c3b26bddbd"),
        new Contract(11355401, "UNI_LP_DAI_BAS", "0x0379da7a5895d13037b6937b109fa8607a659adf"),
        new Contract(11549969, "SUSHI_LP_MIC_USDT", "0xC9cB53B48A2f3A9e75982685644c1870F1405CCb"),
        new Contract(11549972, "SUSHI_LP_MIS_USDT", "0x066F3A3B7C8Fa077c71B9184d862ed0A4D5cF3e0"),
        new Contract(11607878, "ONEINCH_LP_ETH_DAI", "0x7566126f2fD0f2Dddae01Bb8A6EA49b760383D5A"),
        new Contract(11607881, "ONEINCH_LP_ETH_USDC", "0xb4dB55a20E0624eDD82A0Cf356e3488B4669BD27"),
        new Contract(11607877, "ONEINCH_LP_ETH_USDT", "0xbBa17b81aB4193455Be10741512d0E71520F43cB"),
        new Contract(11607880, "ONEINCH_LP_ETH_WBTC", "0x6a11F3E5a01D129e566d783A7b6E8862bFD66CcA"),
        new Contract(11644509, "UNI_LP_DAI_BSG", "0x4a9596e5d2f9bef50e4de092ad7181ae3c40353e"),
        new Contract(11644511, "UNI_LP_DAI_BSGS", "0x980a07e4f64d21a0cb2ef8d4af362a79b9f5c0da"),
        new Contract(10722560, "UNI_LP_ESD_USDC", "0x88ff79eb2bc5850f27315415da8685282c7610f9"),
        new Contract(11330245, "UNI_LP_USDC_DSD", "0x66e33d2605c5fb25ebb7cd7528e7997b0afa55e8"),
        new Contract(11380524, "UNI_LP_MAAPL_UST", "0xb022e08adc8ba2de6ba4fecb59c6d502f66e953b"),
        new Contract(11380782, "UNI_LP_MAMZN_UST", "0x0Ae8cB1f57e3b1b7f4f5048743710084AA69E796"),
        new Contract(11380543, "UNI_LP_MGOOGL_UST", "0x4b70ccD1Cf9905BE1FaEd025EADbD3Ab124efe9a"),
        new Contract(11380570, "UNI_LP_MTSLA_UST", "0x5233349957586A8207c52693A959483F9aeAA50C"),
        new Contract(10097736, "UNI_LP_USDC_EURS", "0x767055e2a9f15783b1ec5ef134a89acf3165332f"),
        new Contract(10829340, "SUSHI_LP_SUSHI_ETH", "0x795065dCc9f64b5614C407a6EFDC400DA6221FB0"),
        new Contract(11038392, "UNI_LP_HBTC_ETH", "0xa6f4eae7fdaa20e632c45d4cd39e4f3961892322")
    );
}
