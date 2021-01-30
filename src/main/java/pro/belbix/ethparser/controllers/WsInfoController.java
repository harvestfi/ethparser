package pro.belbix.ethparser.controllers;

import lombok.extern.log4j.Log4j2;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@Log4j2
public class WsInfoController {

    @MessageMapping("/info")
    public String info(String info) {
        log.info("New ws connect " + info);
        return "Hello " + info;
    }

}
