package pro.belbix.ethparser.controllers;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import pro.belbix.ethparser.model.WebErrorModel;

@RestController
@Log4j2
public class LogController {

    @RequestMapping(value = "api/logs", method = RequestMethod.POST)
    public ResponseEntity<String> postingStatus(@RequestBody WebErrorModel error) {
        log.info("Web error:" + error);
        return ResponseEntity.ok().build();
    }
}
