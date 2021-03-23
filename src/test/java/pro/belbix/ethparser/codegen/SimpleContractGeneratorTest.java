package pro.belbix.ethparser.codegen;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.generated.Uint256;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.web3.MethodDecoder;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
class SimpleContractGeneratorTest {

  @Autowired
  private SimpleContractGenerator simpleContractGenerator;

  @Test
  void getContract_USDT() {
    GeneratedContract contract = simpleContractGenerator.getContract(
        "0xdac17f958d2ee523a2206206994597c13d831ec7", 10800000);
    assertAll(
        () -> assertNotNull(contract, "contract is not null"),
        () -> assertEquals(11, contract.getEvents().size(), "Events size"),
        () -> assertEquals(13, contract.getFunctions().size(), "Functions size")
    );
  }

  @Test
  void getContract_USDC() {
    GeneratedContract contract = simpleContractGenerator.getContract(
        "0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48", 10800000);
    assertAll(
        () -> assertNotNull(contract, "contract is not null"),
        () -> assertEquals(17, contract.getEvents().size(), "Events size"),
        () -> assertEquals(18, contract.getFunctions().size(), "Functions size")
    );
  }

  @Test
  void getContract_LINK() {
    GeneratedContract contract = simpleContractGenerator.getContract(
        "0x514910771AF9Ca656af840dff83E8264EcF986CA", 10800000);
    assertAll(
        () -> assertNotNull(contract, "contract is not null"),
        () -> assertEquals(2, contract.getEvents().size(), "Events size"),
        () -> assertEquals(4, contract.getFunctions().size(), "Functions size")
    );
  }

  @Test
  void getContract_fSUSHI() {
    GeneratedContract contract = simpleContractGenerator.getContract(
        "0x274AA8B58E8C57C4e347C8768ed853Eb6D375b48", 11954198);
    assertAll(
        () -> assertNotNull(contract, "contract is not null"),
        () -> assertEquals(7, contract.getEvents().size(), "Events size"),
        () -> assertEquals(22, contract.getFunctions().size(), "Functions size")
    );
  }
}
