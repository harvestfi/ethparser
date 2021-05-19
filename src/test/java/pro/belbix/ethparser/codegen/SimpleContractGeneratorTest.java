package pro.belbix.ethparser.codegen;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.dto.v0.ContractSourceCodeDTO;
import pro.belbix.ethparser.repositories.eth.ContractSourceCodeRepository;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
class SimpleContractGeneratorTest {

  @Autowired
  private SimpleContractGenerator simpleContractGenerator;

  @Autowired
  private ContractSourceCodeRepository contractSourceCodeRepository;

  @Test
  void getContract_USDT() {
    GeneratedContract contract = simpleContractGenerator.getContract(
        "0xdac17f958d2ee523a2206206994597c13d831ec7", 10800000L, null, ETH_NETWORK);
    assertAll(
        () -> assertNotNull(contract, "contract is not null"),
        () -> assertEquals(11, contract.getEvents().size(), "Events size"),
        () -> assertEquals(32, contract.getFunctions().size(), "Functions size")
    );
  }

  @Test
  void getContract_USDC() {
    GeneratedContract contract = simpleContractGenerator.getContract(
        "0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48", 10800000L, null, ETH_NETWORK);
    assertAll(
        () -> assertNotNull(contract, "contract is not null"),
        () -> assertEquals(17, contract.getEvents().size(), "Events size"),
        () -> assertEquals(50, contract.getFunctions().size(), "Functions size")
    );
  }

  @Test
  void getContract_LINK() {
    GeneratedContract contract = simpleContractGenerator.getContract(
        "0x514910771AF9Ca656af840dff83E8264EcF986CA", 10800000L, null,ETH_NETWORK);
    assertAll(
        () -> assertNotNull(contract, "contract is not null"),
        () -> assertEquals(3, contract.getEvents().size(), "Events size"),
        () -> assertEquals(12, contract.getFunctions().size(), "Functions size")
    );
  }

  @Test
  void getContract_fSUSHI() {
    GeneratedContract contract = simpleContractGenerator.getContract(
        "0x274AA8B58E8C57C4e347C8768ed853Eb6D375b48", 11954198L, null,ETH_NETWORK);
    assertAll(
        () -> assertNotNull(contract, "contract is not null"),
        () -> assertEquals(7, contract.getEvents().size(), "Events size"),
        () -> assertEquals(48, contract.getFunctions().size(), "Functions size")
    );
  }

  @Test
  void getContract_YPoolDelegator() {
    GeneratedContract contract = simpleContractGenerator.getContract(
        "0x329239599afB305DA0A2eC69c58F8a6697F9F88d", 11954198L, null,ETH_NETWORK);
    assertAll(
        () -> assertNotNull(contract, "contract is not null"),
        () -> assertEquals(9, contract.getEvents().size(), "Events size"),
        () -> assertEquals(31, contract.getFunctions().size(), "Functions size")
    );
  }

  @Test
  void getContract_ZeroEx() {
    GeneratedContract contract = simpleContractGenerator.getContract(
        "0xDef1C0ded9bec7F1a1670819833240f027b25EfF", 11954198L, "0xd9627aa4", ETH_NETWORK);
    assertAll(
        () -> assertNotNull(contract, "contract is not null"),
        () -> assertEquals(0, contract.getEvents().size(), "Events size"),
        () -> assertEquals(5, contract.getFunctions().size(), "Functions size")
    );
  }

  @Test
  void getCachedContract_USDT() {
    String address = "0xdac17f958d2ee523a2206206994597c13d831ec7";
    simpleContractGenerator.getContract(address, 10800000L,null, ETH_NETWORK);
    ContractSourceCodeDTO cachedContractSource =
        contractSourceCodeRepository.findByAddressNetwork(address, ETH_NETWORK);
    assertNotNull(cachedContractSource);
    assertEquals(address, cachedContractSource.getAddress());
    contractSourceCodeRepository.deleteById(cachedContractSource.getId());
  }

  @Test
  void getEmptySourceCodeContract() {
    String address = "0x0000000000007F150Bd6f54c40A34d7C3d5e9f56";
    simpleContractGenerator.getContract(address, 10800000L,null, ETH_NETWORK);
    ContractSourceCodeDTO cachedContractSource =
        contractSourceCodeRepository.findByAddressNetwork(address, ETH_NETWORK);
    assertNotNull(cachedContractSource);
    GeneratedContract cachedContract = simpleContractGenerator.getContract(address, 10800000L,
        null, ETH_NETWORK);

    // when empty contract cached it handled as null
    assertNull(cachedContract);

  }

}
