package pro.belbix.ethparser.web3;

import java.time.Instant;
import java.util.concurrent.BlockingQueue;
import pro.belbix.ethparser.dto.DtoI;

public interface Web3Parser {
    void startParse();
    BlockingQueue<DtoI>  getOutput();

    Instant getLastTx();
}
