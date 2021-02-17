package pro.belbix.ethparser.controllers;

import static pro.belbix.ethparser.utils.CommonUtils.parseLong;

import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pro.belbix.ethparser.dto.TransferDTO;
import pro.belbix.ethparser.repositories.TransferRepository;

@ConditionalOnExpression("!${ethparser.onlyParse:false}")
@RestController
public class TransferController {

    private final TransferRepository transferRepository;

    public TransferController(TransferRepository transferRepository) {
        this.transferRepository = transferRepository;
    }

    @GetMapping("/history/transfer/{address}")
    public List<TransferDTO> addressHistoryTransfers(@PathVariable("address") String address,
                                                     @RequestParam(value = "from", required = false) String from,
                                                     @RequestParam(value = "to", required = false) String to) {
        return transferRepository
            .fetchAllByOwnerAndRecipient(address, address, parseLong(from, 0), parseLong(to, Long.MAX_VALUE));
    }

}
