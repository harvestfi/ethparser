package pro.belbix.ethparser.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pro.belbix.ethparser.error.exceptions.CanNotCalculateProfitException;
import pro.belbix.ethparser.model.ErrorResponse;

@RestControllerAdvice
public class GlobalControllerAdvice {

  @ExceptionHandler(CanNotCalculateProfitException.class)
  public ResponseEntity<ErrorResponse> canNotCalculateProfitException() {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
        ErrorResponse.builder()
            .message("Can not calculate profit, try later again")
            .code("500")
            .build()
    );
  }
}
