package pro.belbix.ethparser.web3.contracts;

import static pro.belbix.ethparser.web3.contracts.Contract.createContracts;

import java.util.List;

public class BscVaultAddresses {

  private BscVaultAddresses() {
  }

  static final List<Contract> VAULTS = createContracts(
      new Contract(5992258, "PC_BUSD_BNB", "0xF7A3a95d0f7E8A5EEaE483Cdd7b76aF287283D34"),
      new Contract(5992267, "PC_CAKE", "0x3D5B0a8CD80e2A87953525fC136c33112E4b885a"),
      new Contract(5992279, "PC_ETH_BNB", "0xE1f9A3EE001a2EcC906E8de637DBf20BB2d44633"),
      new Contract(5992286, "PC_USDT_BNB", "0x6D386490e2367Fc31b4aCC99aB7c7D4d998a3121"),
      new Contract(5992293, "PC_XVS_BNB", "0x0bB94083d5718a8cb716faDc016187a0d6C99425"),
      new Contract(5992272, "PC_CAKE_BNB", "0xfFBD102fAFbd9e15C9122d9C62aB299AFD4D3E4F"),
      new Contract(5992238, "VENUS_XVS", "0xCf5F83F8FE0AB0f9E9C1db07E6606dD598b2bbf5"),
      new Contract(5992245, "VENUS_VAI", "0x33DA6B1a05B4afcC5a321aACaA1334BDA4345a14"),
      new Contract(5992218, "VENUS_BETH", "0x394E653bbFC9A3497A0487Abee153CA6498F053D"),
      new Contract(5992231, "VENUS_ETH", "0x2CE34b1bb247f242f1d2A33811E01138968EFBFF"),
      new Contract(5992252, "VENUS_WBNB", "0x1274B70bF34E1a57E78C2A2f3E28a4E1B66cbE48"),
      new Contract(5992224, "VENUS_BTCB", "0xd75ffA16FFbCf4078d55fF246CfBA79Bb8cE3F63")
  );

}
