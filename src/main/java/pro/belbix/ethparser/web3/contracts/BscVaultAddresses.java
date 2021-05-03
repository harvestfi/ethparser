package pro.belbix.ethparser.web3.contracts;

import static pro.belbix.ethparser.web3.contracts.models.SimpleContract.createContracts;

import java.util.List;
import pro.belbix.ethparser.web3.contracts.models.SimpleContract;

public class BscVaultAddresses {

  private BscVaultAddresses() {
  }

  static final List<SimpleContract> VAULTS = createContracts(
      new SimpleContract(5992258, "PC_BUSD_BNB", "0xF7A3a95d0f7E8A5EEaE483Cdd7b76aF287283D34"),
      new SimpleContract(5992267, "PC_CAKE", "0x3D5B0a8CD80e2A87953525fC136c33112E4b885a"),
      new SimpleContract(5992279, "PC_ETH_BNB", "0xE1f9A3EE001a2EcC906E8de637DBf20BB2d44633"),
      new SimpleContract(5992286, "PC_USDT_BNB", "0x6D386490e2367Fc31b4aCC99aB7c7D4d998a3121"),
      new SimpleContract(5992293, "PC_XVS_BNB", "0x0bB94083d5718a8cb716faDc016187a0d6C99425"),
      new SimpleContract(5992272, "PC_CAKE_BNB", "0xfFBD102fAFbd9e15C9122d9C62aB299AFD4D3E4F"),
      new SimpleContract(5992238, "VENUS_XVS", "0xCf5F83F8FE0AB0f9E9C1db07E6606dD598b2bbf5"),
      new SimpleContract(5992245, "VENUS_VAI", "0x33DA6B1a05B4afcC5a321aACaA1334BDA4345a14"),
      new SimpleContract(5992218, "VENUS_BETH", "0x394E653bbFC9A3497A0487Abee153CA6498F053D"),
      new SimpleContract(5992231, "VENUS_ETH", "0x2CE34b1bb247f242f1d2A33811E01138968EFBFF"),
      new SimpleContract(5992252, "VENUS_WBNB", "0x1274B70bF34E1a57E78C2A2f3E28a4E1B66cbE48"),
      new SimpleContract(5992224, "VENUS_BTCB", "0xd75ffA16FFbCf4078d55fF246CfBA79Bb8cE3F63"),
      new SimpleContract(5992300, "EGG_BNB", "0xe3f309F151746b3C0953e4C0E455bFf3dc2176AA"),
      new SimpleContract(5992307, "EGG_BUSD", "0xcD8FB1302C30fde56BCE5B34211E84561BBF0dF1"),
      new SimpleContract(5992315, "EGG", "0x0392F36D2896c966E141C8Fd9ECA58a7ca9Fa8AF"),
      new SimpleContract(6191808, "VENUS_DAI", "0x78cF4a86bA3b4C5246D097E5cd0833cB641C1425"),
      new SimpleContract(6191864, "VENUS_USDC", "0x5089EA6c884a03823672888b57EBCE929EcE63ca"),
      new SimpleContract(6191924, "VENUS_USDT", "0x374787234b369b56b3701E0B932051b37726096a"),
      new SimpleContract(6191987, "VENUS_BUSD", "0x1BFB4ed996F4356aa705891DedB7d7776402BeC1"),
      new SimpleContract(5992324, "BDO_BNB", "0xE604Fd5b1317BABd0cF2c72F7F5f2AD8c00Adbe1"),
      new SimpleContract(5992329, "BDO_BUSD", "0x84646F736795a8bC22Ab34E05c8982CD058328C7"),
      new SimpleContract(5992335, "SBDO_BUSD", "0xC97DDAa8091aBaF79A4910b094830CCE5cDd78f4"),
      new SimpleContract(6186977, "ONEINCH_BNB", "0x9090BCcD472b9D11DE302572167DED6632e185AB"),
      new SimpleContract(6186790, "ONEINCH_RENBTC", "0xbF2989575dE9850F0A4b534740A88F5D2b460A4f"),
      new SimpleContract(6534723, "EPS_3POOL", "0x63671425ef4D25Ec2b12C7d05DE855C143f16e3B"),
      new SimpleContract(6534768, "PC_EPS_BNB", "0x0A7d74604b39229D444855eF294F287099774aC8"),
      new SimpleContract(6620038, "BELT_BNB", "0xaD941e12544F49077fc6425CDa1871E11Cea5288"),
      new SimpleContract(6619990, "BELT_VENUS", "0x2427DA81376A0C0a0c654089a951887242D67C92"),
      new SimpleContract(6619825, "SWIRL_BNB", "0x299B00d031Ba65cA3a22A8f7E8059DAb0b072247"),
      new SimpleContract(6619877, "SPACE_BNB", "0x14CB410659b4a4a7CCEa99E6F6C9eac8718160dF"),
      new SimpleContract(6619936, "SPACE_BUSD", "0x129cCeE12A9542Ff77e066E6F8d7DF49F8Cbf89D"),
      new SimpleContract(6736376, "ICE_BNB", "0x1c4ADFf419F6b91E51D0aDe953C9BBf5D16A583F"),
      new SimpleContract(6736428, "POPSICLE_ICE", "0xfeB902DB08E4e1F362866628098D6110DBe3D072"),
      new SimpleContract(6736260, "EPS_FUSDT", "0xe64BFE13AA99335487f1F42a56cddBFfaEC83BBF"),
      new SimpleContract(6736315, "EPS_BTC", "0x5Da237ad194B8BBb008Ac8916DF99A92A8a7c8Eb")
  );

}
