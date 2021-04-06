package pro.belbix.ethparser.model;

import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pro.belbix.ethparser.dto.v0.HardWorkDTO;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedResponse<T extends List<?>> implements Serializable {

  private int currentPage;
  private int nextPage;
  private int previousPage;
  private int totalPages;
  private T data;

  public static class PaginatedResponseHardWork extends PaginatedResponse<List<HardWorkDTO>> {

  }
}
