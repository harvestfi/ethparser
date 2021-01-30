package pro.belbix.ethparser.web3.prices.db;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.dto.PriceDTO;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.repositories.PriceRepository;

@Service
@Log4j2
public class PriceDBService {

    private final PriceRepository priceRepository;
    private final AppProperties appProperties;

    public PriceDBService(PriceRepository priceRepository, AppProperties appProperties) {
        this.priceRepository = priceRepository;
        this.appProperties = appProperties;
    }

    public boolean savePriceDto(PriceDTO dto) {
        if (!appProperties.isOverrideDuplicates() && priceRepository.existsById(dto.getId())) {
            log.warn("Duplicate Price entry " + dto.getId());
            return false;
        }
        priceRepository.save(dto);
        return true;
    }

}
