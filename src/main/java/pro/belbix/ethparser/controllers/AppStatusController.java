package pro.belbix.ethparser.controllers;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pro.belbix.ethparser.web3.ParserInfo;

@RestController
@RequestMapping(value = "/status")
public class AppStatusController {

    private final ParserInfo parserInfo;

    public AppStatusController(ParserInfo parserInfo) {
        this.parserInfo = parserInfo;
    }

    @GetMapping(value = "/parsers", produces = MediaType.APPLICATION_JSON_VALUE)
    public String statusAll() {
        return parserInfo.getInfoForAllParsers();
    }
}
