package pro.belbix.ethparser.web3.contracts;

import static pro.belbix.ethparser.web3.contracts.models.SimpleContract.createContracts;

import java.util.List;
import pro.belbix.ethparser.web3.contracts.models.SimpleContract;

public class BscPoolAddresses {

  private BscPoolAddresses() {
  }

  static final List<SimpleContract> POOLS = createContracts(
      new SimpleContract(5992750, "ST_PC_BUSD_BNB", "0xeaB819E2BE63FFC0dF64E7BBA4DDB3bDEa280310"),
      new SimpleContract(5992754, "ST_PC_CAKE", "0x78963b538c4835E00648DF764029196700ea8eE9"),
      new SimpleContract(5992762, "ST_PC_ETH_BNB", "0x221ED06024Ee4296fB544a44cfEDDf7c9f882cF3"),
      new SimpleContract(5992765, "ST_PC_USDT_BNB", "0xf1121f56961C6DFD40355dDe61404D51b3f1c34E"),
      new SimpleContract(5992769, "ST_PC_XVS_BNB", "0x063eB32430bD63f4144f3e87D3339E4D2a318C52"),
      new SimpleContract(5992758, "ST_PC_CAKE_BNB", "0xD4bC6001937C6fF493e4bae3BA0F812799f86AB0"),
      new SimpleContract(5992738, "ST_VENUS_XVS", "0x8709B440C0F4F6830a468c6f696D010e85c9510B"),
      new SimpleContract(5992742, "ST_VENUS_VAI", "0xDA88e38735e75B58fee6ea4FC5Be576c1e22F6cd"),
      new SimpleContract(5992726, "ST_VENUS_BETH", "0xb3b56c7BDc87F9DeB7972cD8b5c09329ce421F89"),
      new SimpleContract(5992734, "ST_VENUS_ETH", "0x3331039530DD04B5DF06c2D226AC28E958BACc0f"),
      new SimpleContract(5992746, "ST_VENUS_WBNB", "0xE83f395B076F9b95200f9bEC40f5E446599F4F06"),
      new SimpleContract(5992730, "ST_VENUS_BTCB", "0xC6f39CFf6797baC5e29275177b6E8e315cF87D95"),
      new SimpleContract(5992773, "ST_EGG_BNB", "0x26A4FE4c858f0d8a11442d358d182F2832A7F84c"),
      new SimpleContract(5992777, "ST_EGG_BUSD", "0x7CAA01B3DC8eE91aA6FA7093e184f045e0DA8792"),
      new SimpleContract(5992780, "ST_EGG", "0xfc8c1f0E25d91cB10db243acBcA5ac3c422A6277"),
      new SimpleContract(6191845, "ST_VENUS_DAI", "0xf53c6789F35C4eCf152d2168Ca203808595CB524"),
      new SimpleContract(6191897, "ST_VENUS_USDC", "0x08b6732e1D07726e8F398a0ea97200d26E172066"),
      new SimpleContract(6191961, "ST_VENUS_USDT", "0xE637E7d1e37875c787b773dBEb28Fcc55FE91C16"),
      new SimpleContract(6192021, "ST_VENUS_BUSD", "0x76aAddf1eBaF9300dAd18469D01A0ee62633abe7"),
      new SimpleContract(5992784, "ST_BDO_BNB", "0xdEb314a2222884B6c1e848BfFcf68dcfBc5c1406"),
      new SimpleContract(5992788, "ST_BDO_BUSD", "0x03b58CE34E9Cb6a908b019228778CBB9f3A1A2Ba"),
      new SimpleContract(5992791, "ST_SBDO_BUSD", "0x9178f402453b118b6B897Ff520256Ed63b2d9998"),
      new SimpleContract(6187007, "ST_ONEINCH_BNB", "0x57C30567e0dbd1c308FD2c5665c4084F368560a2"),
      new SimpleContract(6186861, "ST_ONEINCH_RENBTC", "0xE9E03506A088eaCDfa1A690cD3019ac105D7B871"),
      new SimpleContract(6534746, "ST_EPS_3POOL", "0x2Fee56e039AcCceFA3cB1f3051aD00fe550a472c"),
      new SimpleContract(6534788, "ST_PC_EPS_BNB", "0x0694e3cB1055Ff33d774d51a55272DDEE305f745"),
      new SimpleContract(6620063, "ST_BELT_BNB", "0x8E8Ca3719360809cd4BFe175De58992E7d3e7874"),
      new SimpleContract(6620015, "ST_BELT_VENUS", "0x5c6Fe09FcEfeaCa84DC18018cF8aCf7476B2498F"),
      new SimpleContract(6619853, "ST_SWIRL_BNB", "0xEa2c3C25985fBB5418c61451C2cbD1311e0EbE9e"),
      new SimpleContract(6619902, "ST_SPACE_BNB", "0xC2A1fa5753B7c3272F32aBec19140658D539E61C"),
      new SimpleContract(6619965, "ST_SPACE_BUSD", "0x03292bdfE36591F70575C77847d7f004FFd0966A"),
      new SimpleContract(6736402, "ST_ICE_BNB", "0xfE7f456F8274355f17243b7282bd88129e894b1A"),
      new SimpleContract(6736455, "ST_POPSICLE_ICE", "0x1Bb6fDaf6258071F4d2E96d70ffEB8AD392f299B"),
      new SimpleContract(6736290, "ST_EPS_FUSDT", "0x9B36E1DcBb21DfA6863b2711Ed6f0F080888072c"),
      new SimpleContract(6736344, "ST_EPS_BTC", "0x4165884840eE7e6280c512C75B23B098F7e420fc")
  );

}
