package pro.belbix.ethparser.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
public class WsInfoController {

    private final static Logger log = LoggerFactory.getLogger(WsInfoController.class);

    @MessageMapping("/info")
    public String info(String info) {
        log.info("New ws connect " + info);
        return "Hello " + info;
    }

}
