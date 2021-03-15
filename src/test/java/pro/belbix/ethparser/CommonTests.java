package pro.belbix.ethparser;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static pro.belbix.ethparser.web3.harvest.db.HarvestDBService.aprToApy;

import org.junit.jupiter.api.Test;
import pro.belbix.ethparser.web3.abi.generated.WrapperMapper;
import pro.belbix.ethparser.web3.contracts.ContractUtils;

public class CommonTests {

    @Test
    public void testAprToApy() {
        double apr = 0.743;
        double period = 365.0;
        assertEquals(110.06457410361162, aprToApy(apr, period) * 100, 0.0);
    }

    @Test
    public void mapAddressesToStContract() {
        String[] addresses = {
            "0x3DA9D911301f8144bdF5c3c67886e5373DCdff8e",
            "0x2E25800957742C52b4d69b65F9C67aBc5ccbffe6",
            "0x4F7c28cCb0F1Dbd1388209C67eEc234273C878Bd",
            "0x6ac4a7AB91E6fD098E13B7d347c6d4d1494994a2",
            "0x15d3A64B2d5ab9E152F16593Cdebc4bB165B5B4A",
            "0x6D1b6Ea108AA03c6993d8010690264BA96D349A8",
            "0x27F12d1a08454402175b9F0b53769783578Be7d9",
            "0xef4Da1CE3f487DA2Ed0BE23173F76274E0D47579",
            "0x093C2ae5E6F3D2A897459aa24551289D462449AD",
            "0xC0f51a979e762202e9BeF0f62b07F600d0697DE1",
            "0x72C50e6FD8cC5506E166c273b6E814342Aa0a3c1",
            "0xf4d50f60D53a230abc8268c6697972CB255Cd940",
            "0xDdb5D3CCd968Df64Ce48b577776BdC29ebD3120e",
            "0x917d6480Ec60cBddd6CbD0C8EA317Bcc709EA77B",
            "0xA3Cf8D1CEe996253FAD1F8e3d68BDCba7B3A3Db5",
            "0x017eC1772A45d2cf68c429A820eF374f0662C57c",
            "0x01f9CAaD0f9255b0C0Aa2fBD1c1aA06ad8Af7254",
            "0x91B5cD52fDE8dbAC37C95ECafEF0a70bA4c182fC",
            "0xDa5E9706274D88bbf1bD1877a9b462fC40cDcfAd",
            "0x9a9A6148f7b0A9767AC1fdC67343D1e3E219FdDf",
            "0x2A80e0B572e7EF61Ef81EF05eC024e1e52Bd70BD",
            "0x747318Cf3171D4E2a1A52bBD3Fcc9f9c690448B4",
            "0x76Aef359a33C02338902aCA543f37de4b01BA1FA",
            "0xE2D9FAe95f1e68afca7907dFb36143781f917194",
            "0xA56522BCA0A09f57B85C52c0Cc8Ba1B5eDbc64ef",
            "0x6B4e1E0656Dd38F36c318b077134487B9b0cf7a6",
            "0xAd91695b4BeC2798829ac7a4797E226C78f22Abd",
            "0x98Ba5E432933E2D536e24A2C021deBb8404588C1",
            "0xf4784d07136b5b10c6223134E8B36E1DC190725b",
            "0x797F1171DC5001B7A79ff7Dca68c9539329ccE48",
            "0xf330891f02F8182D7D4e1A962ED0F3086D626020",
            "0xf5b221E1d9C3a094Fb6847bC3E241152772BbbF8",
            "0x63e7D3F6e208ccE4967b7a0E6A50A397Af0b0E7A",
            "0xfE83a00DF3A98dE218c08719FAF7e3741b220D0D",
            "0xc02d1Da469d68Adc651Dd135d1A7f6b42F4d1A57",
            "0x40C34B0E1bb6984810E17474c6B0Bcc6A6B46614",
            "0x8Dc427Cbcc75cAe58dD4f386979Eba6662f5C158",
            "0x6555c79a8829b793F332f1535B0eFB1fE4C11958",
            "0xe58f0d2956628921cdEd2eA6B195Fc821c3a2b16"
        };
        for(String address : addresses) {
            System.out.println(address + " - " +
                ContractUtils.getNameByAddress(address.toLowerCase()).orElse("not found"));
        }
    }
}
