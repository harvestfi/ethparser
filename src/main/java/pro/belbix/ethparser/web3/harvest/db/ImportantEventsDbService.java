package pro.belbix.ethparser.web3.harvest.db;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.dto.ImportantEventsDTO;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.repositories.ImportantEventsRepository;

@Service
@Log4j2
public class ImportantEventsDbService {

    private final ImportantEventsRepository importantEventsRepository;
    private final AppProperties appProperties;

    public ImportantEventsDbService(ImportantEventsRepository importantEventsRepository,
                                    AppProperties appProperties) {
        this.importantEventsRepository = importantEventsRepository;
        this.appProperties = appProperties;
    }

    public boolean save(ImportantEventsDTO dto) {
        if (!appProperties.isOverrideDuplicates() && importantEventsRepository.existsById(dto.getId())) {
            log.info("Duplicate ImportantEvents entry " + dto.getId());
            return false;
        }
        importantEventsRepository.save(dto);
        importantEventsRepository.flush();

        return true;
    }
}
