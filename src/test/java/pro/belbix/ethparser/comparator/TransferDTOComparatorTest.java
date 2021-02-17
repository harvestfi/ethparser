package pro.belbix.ethparser.comparator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import pro.belbix.ethparser.dto.TransferDTO;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class TransferDTOComparatorTest {
  @Test
  public void testCompare() {
    String id1 = "0x00d4299a66f3c04e54a0fb4a4ffc3c3cfc79c229da8fef579567f348b681133b_15";
    TransferDTO transferDto1 = new TransferDTO();
    transferDto1.setId(id1);
    transferDto1.setBlockDate(1613413886);

    String id2 = "0x632d0f3ee796d5f57bf6eab5cbc621a26a39561c5c9ac3541c3ad210b0791bfc_27";
    TransferDTO transferDto2 = new TransferDTO();
    transferDto2.setId(id2);
    transferDto2.setBlockDate(1613413887);

    String id3 = "0xcf68779e0cd99e5d89c4bff4b3f38036217ddda60bcb536210bb7aee3be6dea1_11";
    TransferDTO transferDto3 = new TransferDTO();
    transferDto3.setId(id3);
    transferDto3.setBlockDate(1613413887);

    String id4 = "0x00e347f5eb0f410173f8ed324fdc536932b3f0d0a52bd36b0a5ba60c599b261c_9";
    TransferDTO transferDto4 = new TransferDTO();
    transferDto4.setId(id4);
    transferDto4.setBlockDate(1613413886);

    List<TransferDTO> transfers =
        Arrays.asList(transferDto1, transferDto2, transferDto3, transferDto4);
    transfers.sort(new TransferDTOComparator());

    assertEquals(id4, transfers.get(0).getId());
    assertEquals(id1, transfers.get(1).getId());
    assertEquals(id3, transfers.get(2).getId());
    assertEquals(id2, transfers.get(3).getId());
  }
}
