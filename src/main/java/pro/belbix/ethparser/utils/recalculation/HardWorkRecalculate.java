package pro.belbix.ethparser.utils.recalculation;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${hardwork-recalculate.from:}")
    private Integer from;
    @Value("${hardwork-recalculate.to:}")
    private Integer to;

    public HardWorkRecalculate(HardWorkRepository hardWorkRepository,
                               HardWorkDbService hardWorkDbService) {
        this.hardWorkRepository = hardWorkRepository;
        this.hardWorkDbService = hardWorkDbService;
    }

    public void start() {
        if (from == null) {
            from = 0;
        }
        if (to == null) {
            to = Integer.MAX_VALUE;
        }
        List<HardWorkDTO> dtos = hardWorkRepository.fetchAllInRange(from, to);
        for (HardWorkDTO dto : dtos) {
            hardWorkDbService.enrich(dto);
            hardWorkRepository.saveAndFlush(dto);
            log.info("Save hardwork for " + dto.print());
        }
    }


}
