package pro.belbix.ethparser.web3;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ParserInfo {

    private final List<Web3Parser> parsers = new ArrayList<>();

    public void addParser(Web3Parser parser) {
        parsers.add(parser);
    }

    public String getInfoForAllParsers() {
        if (parsers.isEmpty()) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        for (Web3Parser parser : parsers) {
            sb.append("\"");
            sb.append(parser.getClass().getSimpleName());
            sb.append("\": ");
            sb.append(parser.getLastTx().getEpochSecond());
            sb.append(",\n");
        }
        sb.setLength(sb.length() - 2);
        sb.append("\n}");
        return sb.toString();
    }

}
