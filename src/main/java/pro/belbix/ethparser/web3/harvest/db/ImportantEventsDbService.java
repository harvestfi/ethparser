package pro.belbix.ethparser.web3.harvest.db;

import static pro.belbix.ethparser.web3.harvest.parser.ImportantEventsParser.TOKEN_MINT;

import java.util.List;

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

    public ImportantEventsDTO updateTokenMinted(ImportantEventsDTO dto) {
        ImportantEventsDTO mintDto = new ImportantEventsDTO();
        // sum TokenMintTx amount
        List<Double> amounts = importantEventsRepository.fetchMintAmount(dto.getHash(), dto.getEvent());
        if (amounts != null) {
            mintDto.setMintAmount(amounts.stream().mapToDouble(Double::doubleValue).sum());
        } else {
            return null;
        }

        mintDto.setEvent(TOKEN_MINT);
        mintDto.setId(dto.getHash() + "_sum");
        mintDto.setBlock(dto.getBlock());
        mintDto.setHash(dto.getHash());
        mintDto.setBlockDate(dto.getBlockDate());
        mintDto.setVault(dto.getVault());

        
        return mintDto;
    }
}
