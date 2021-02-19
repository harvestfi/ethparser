package pro.belbix.ethparser.comparator;

import pro.belbix.ethparser.dto.v0.TransferDTO;

import java.util.Comparator;

public class TransferDTOComparator implements Comparator<TransferDTO> {
  @Override
  public int compare(TransferDTO firstTransfer, TransferDTO secondTransfer) {
    if (firstTransfer.getBlockDate() == secondTransfer.getBlockDate()) {
      // Same block sort by log id
      return Integer.compare(
          Integer.parseInt(firstTransfer.getId().substring(firstTransfer.getId().indexOf("_") + 1)),
          Integer.parseInt(
              secondTransfer.getId().substring(firstTransfer.getId().indexOf("_") + 1)));
    } else {
      // Different block sort by block date
      return Long.compare(firstTransfer.getBlockDate(), secondTransfer.getBlockDate());
    }
  }
}
