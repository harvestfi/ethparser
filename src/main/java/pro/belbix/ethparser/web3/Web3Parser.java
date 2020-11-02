package pro.belbix.ethparser.web3;

import java.util.concurrent.BlockingQueue;
import pro.belbix.ethparser.model.DtoI;
import pro.belbix.ethparser.model.TransactionDTO;

public interface Web3Parser {
    void startParse();
    BlockingQueue<DtoI>  getOutput();
}
