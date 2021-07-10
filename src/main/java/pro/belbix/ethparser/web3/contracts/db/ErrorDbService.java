package pro.belbix.ethparser.web3.contracts.db;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.entity.ErrorEntity;
import pro.belbix.ethparser.model.Web3Model;
import pro.belbix.ethparser.repositories.ErrorsRepository;

@Log4j2
@Service
public class ErrorDbService {

  private final ErrorsRepository errorsRepository;

  public ErrorDbService(ErrorsRepository errorsRepository) {
    this.errorsRepository = errorsRepository;
  }

  public List<ErrorEntity> getAllErrors() {
    return errorsRepository.findAll();
  }

  public synchronized void delete(ErrorEntity errorEntity) {
    errorsRepository.delete(errorEntity);
  }

  public synchronized void saveErrorWeb3ModelToDb(Web3Model web3Model, String errorClassName) {
    ErrorEntity error = new ErrorEntity();
    error.setErrorClass(errorClassName);
    error.setJson(web3ModelValueToJson(web3Model));
    error.setNetwork(web3Model.getNetwork());
    errorsRepository.save(error);
  }

  public String web3ModelToJson(Web3Model web3Model) {
    ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
    try {
      return ow.writeValueAsString(web3Model);
    } catch (JsonProcessingException e) {
      log.error("Can't convertWeb3ModelToJson:"+e.getMessage());
    }
    return null;
  }

  public String web3ModelValueToJson(Web3Model web3Model) {
    ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
    try {
      return ow.writeValueAsString(web3Model.getValue());
    } catch (JsonProcessingException e) {
      log.error("Can't convertWeb3ModelValueToJson:"+e.getMessage());
    }
    return null;
  }
}
