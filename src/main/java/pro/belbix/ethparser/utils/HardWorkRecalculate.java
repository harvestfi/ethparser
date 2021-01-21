package pro.belbix.ethparser.utils;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.dto.HardWorkDTO;
import pro.belbix.ethparser.repositories.HardWorkRepository;
import pro.belbix.ethparser.web3.harvest.db.HardWorkDbService;
import pro.belbix.ethparser.web3.uniswap.downloader.DownloadIncome;

@Service
public class HardWorkRecalculate {

    private final static Logger log = LoggerFactory.getLogger(DownloadIncome.class);
    private final HardWorkRepository hardWorkRepository;
    private final HardWorkDbService hardWorkDbService;

    public HardWorkRecalculate(HardWorkRepository hardWorkRepository,
                               HardWorkDbService hardWorkDbService) {
        this.hardWorkRepository = hardWorkRepository;
        this.hardWorkDbService = hardWorkDbService;
    }

    public void start() {
        List<HardWorkDTO> dtos = hardWorkRepository.findAllByOrderByBlockDate();
        for (HardWorkDTO dto : dtos) {
            hardWorkDbService.enrich(dto);
            log.info("Save hardwork for " + dto.print());
        }
    }


}
