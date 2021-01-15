package pro.belbix.ethparser.web3.harvest.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.dto.ImportantEventsDTO;

import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.repositories.ImportantEventsRepository;


@Service
public class ImportantEventsDbService {

    private static final Logger log = LoggerFactory.getLogger(ImportantEventsDbService.class);


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
