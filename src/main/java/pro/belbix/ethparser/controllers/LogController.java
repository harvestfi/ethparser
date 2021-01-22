package pro.belbix.ethparser.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import pro.belbix.ethparser.model.WebErrorModel;

@RestController
public class LogController {

    private static final Logger log = LogManager.getRootLogger();

    @RequestMapping(value = "api/logs", method = RequestMethod.POST)
    public ResponseEntity<String> postingStatus(@RequestBody WebErrorModel error) {
        log.info("Web error:" + error);
        return ResponseEntity.ok().build();
    }
}
