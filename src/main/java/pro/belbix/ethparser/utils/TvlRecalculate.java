package pro.belbix.ethparser.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.dto.HarvestDTO;
import pro.belbix.ethparser.dto.UniswapDTO;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.repositories.HarvestRepository;
import pro.belbix.ethparser.repositories.HarvestTvlRepository;
import pro.belbix.ethparser.repositories.UniswapRepository;
import pro.belbix.ethparser.web3.harvest.db.HarvestDBService;

@Service
public class TvlRecalculate {

    private static final Logger log = LoggerFactory.getLogger(TvlRecalculate.class);
    private final Map<String, TreeMap<Long, HarvestDTO>> harvestVaults = new HashMap<>();
    private final TreeMap<Long, UniswapDTO> uniswapDTOTreeMap = new TreeMap<>();

    private final HarvestRepository harvestRepository;
    private final AppProperties appProperties;
    private final HarvestTvlRepository harvestTvlRepository;
    private final UniswapRepository uniswapRepository;

    @Value("${tvl-recalculate.from:}")
    private Integer from;

    public TvlRecalculate(HarvestRepository harvestRepository,
                          AppProperties appProperties,
                          HarvestTvlRepository harvestTvlRepository,
                          UniswapRepository uniswapRepository) {
        this.harvestRepository = harvestRepository;
        this.appProperties = appProperties;
        this.harvestTvlRepository = harvestTvlRepository;
        this.uniswapRepository = uniswapRepository;
    }

    public void start() {
        HarvestDBService harvestDBService = new HarvestDBService(
            createHarvestRepository(),
            appProperties,
            harvestTvlRepository,
            createUniswapRepository());

        List<HarvestDTO> harvestDTOList;
        if (from == null) {
            harvestDTOList = harvestRepository.findAllByOrderByBlockDate();
        } else {
            harvestDTOList = harvestRepository.findAllByBlockDateGreaterThanOrderByBlockDate(from);
        }

        harvestDtosToMap(harvestDTOList);
        uniDtosToMap(uniswapRepository.findAll());
        int count = 0;
        for (HarvestDTO harvestDTO : harvestDTOList) {
            count++;
            harvestDBService.saveHarvestTvl(harvestDTO, false);
            if (count % 10000 == 0) {
                log.info("Save for " + harvestDTO.print());
            }
        }
    }

    private void harvestDtosToMap(List<HarvestDTO> harvestDTOList) {
        for (HarvestDTO dto : harvestDTOList) {
            TreeMap<Long, HarvestDTO> map = harvestVaults.get(dto.getVault());
            if (map == null) {
                map = new TreeMap<>();
                map.put(dto.getBlockDate(), dto);
                harvestVaults.put(dto.getVault(), map);
            } else {
                map.put(dto.getBlockDate(), dto);
            }
        }
    }

    private void uniDtosToMap(List<UniswapDTO> uniswapDTOS) {
        for (UniswapDTO dto : uniswapDTOS) {
            if ("FARM".equals(dto.getCoin())) {
                uniswapDTOTreeMap.put(dto.getBlockDate(), dto);
            }
        }
    }

    private UniswapRepository createUniswapRepository() {
        return new UniswapRepository() {
            @Override
            public Integer fetchOwnerCount() {
                return null;
            }

            @Override
            public UniswapDTO findFirstByBlockDateBeforeAndCoinOrderByBlockDesc(long blockDate, String coin) {
                Entry<Long, UniswapDTO> entry = uniswapDTOTreeMap.lowerEntry(blockDate);
                if (entry == null) {
                    return null;
                }
                return entry.getValue();
            }

            @Override
            public List<Double> fetchAmountSum(long from, String owner, Pageable pageable) {
                return null;
            }

            @Override
            public List<Double> fetchAmountSumUsd(long from, String owner, Pageable pageable) {
                return null;
            }

            @Override
            public UniswapDTO findFirstByCoinOrderByBlockDesc(String coin) {
                return null;
            }

            @Override
            public List<UniswapDTO> findAllByOwnerAndCoinOrderByBlockDate(String owner, String coin) {
                return null;
            }

            @Override
            public List<UniswapDTO> fetchAllByOwner(String owner, long from, long to) {
                return null;
            }

            @Override
            public List<UniswapDTO> findAll() {
                return null;
            }

            @Override
            public List<UniswapDTO> findAll(Sort sort) {
                return null;
            }

            @Override
            public List<UniswapDTO> findAllById(Iterable<String> iterable) {
                return null;
            }

            @Override
            public <S extends UniswapDTO> List<S> saveAll(Iterable<S> iterable) {
                return null;
            }

            @Override
            public void flush() {

            }

            @Override
            public <S extends UniswapDTO> S saveAndFlush(S s) {
                return null;
            }

            @Override
            public void deleteInBatch(Iterable<UniswapDTO> iterable) {

            }

            @Override
            public void deleteAllInBatch() {

            }

            @Override
            public UniswapDTO getOne(String s) {
                return null;
            }

            @Override
            public <S extends UniswapDTO> List<S> findAll(Example<S> example) {
                return null;
            }

            @Override
            public <S extends UniswapDTO> List<S> findAll(Example<S> example, Sort sort) {
                return null;
            }

            @Override
            public Page<UniswapDTO> findAll(Pageable pageable) {
                return null;
            }

            @Override
            public <S extends UniswapDTO> S save(S s) {
                return null;
            }

            @Override
            public Optional<UniswapDTO> findById(String s) {
                return Optional.empty();
            }

            @Override
            public boolean existsById(String s) {
                return false;
            }

            @Override
            public long count() {
                return 0;
            }

            @Override
            public void deleteById(String s) {

            }

            @Override
            public void delete(UniswapDTO uniswapDTO) {

            }

            @Override
            public void deleteAll(Iterable<? extends UniswapDTO> iterable) {

            }

            @Override
            public void deleteAll() {

            }

            @Override
            public <S extends UniswapDTO> Optional<S> findOne(Example<S> example) {
                return Optional.empty();
            }

            @Override
            public <S extends UniswapDTO> Page<S> findAll(Example<S> example, Pageable pageable) {
                return null;
            }

            @Override
            public <S extends UniswapDTO> long count(Example<S> example) {
                return 0;
            }

            @Override
            public <S extends UniswapDTO> boolean exists(Example<S> example) {
                return false;
            }
            
            @Override
            public List<UniswapDTO> findAllByOrderByBlockDate() {
                return null;
            }
            
            @Override
            public List<UniswapDTO> findAllByBlockDateGreaterThanOrderByBlockDate(long from) {
                return null;
            }
        
        };
    }

    private HarvestRepository createHarvestRepository() {
        return new HarvestRepository() {
            @Override
            public List<HarvestDTO> findAllByOrderByBlockDate() {
                return null;
            }

            @Override
            public List<HarvestDTO> findAllByBlockDateGreaterThanOrderByBlockDate(long blockDate) {
                return null;
            }

            @Override
            public Double fetchTVL(String vault, long blockDate) {
                return null;
            }

            @Override
            public HarvestDTO fetchLastByVaultAndDate(String vault, long blockDate) {
                TreeMap<Long, HarvestDTO> map = harvestVaults.get(vault);
                if (map == null) {
                    return null;
                }
                Entry<Long, HarvestDTO> entry = map.lowerEntry(blockDate);
                if (entry == null) {
                    return null;
                }
                return entry.getValue();
            }

            @Override
            public HarvestDTO fetchLastByVaultAndDateNotZero(String vault, long blockDate) {
                return null;
            }

            @Override
            public Integer fetchOwnerCount(String vault, long blockDate) {
                return null;
            }

            @Override
            public Integer fetchActualOwnerCount(String vault, String oldVault, long blockDate) {
                return null;
            }

            @Override
            public HarvestDTO findFirstByOrderByBlockDesc() {
                return null;
            }

            @Override
            public HarvestDTO findFirstByVaultAndBlockDateLessThanEqualAndIdNotOrderByBlockDateDesc(String vault,
                                                                                                    long date,
                                                                                                    String id) {
                return null;
            }

            @Override
            public List<Double> fetchTvlFrom(long from, String vault, Pageable pageable) {
                return null;
            }

            @Override
            public List<Double> fetchUsdTvlFrom(long from, String vault, Pageable pageable) {
                return null;
            }

            @Override
            public List<Long> fetchPeriodOfWork(String vault, long to, Pageable pageable) {
                return null;
            }

            @Override
            public List<HarvestDTO> fetchAllByOwner(String owner, long from, long to) {
                return null;
            }

            @Override
            public List<HarvestDTO> fetchAllWithoutOwnerBalance() {
                return null;
            }

            @Override
            public List<HarvestDTO> findAll() {
                return null;
            }

            @Override
            public List<HarvestDTO> findAll(Sort sort) {
                return null;
            }

            @Override
            public List<HarvestDTO> findAllById(Iterable<String> iterable) {
                return null;
            }

            @Override
            public <S extends HarvestDTO> List<S> saveAll(Iterable<S> iterable) {
                return null;
            }

            @Override
            public void flush() {

            }

            @Override
            public <S extends HarvestDTO> S saveAndFlush(S s) {
                return null;
            }

            @Override
            public void deleteInBatch(Iterable<HarvestDTO> iterable) {

            }

            @Override
            public void deleteAllInBatch() {

            }

            @Override
            public HarvestDTO getOne(String s) {
                return null;
            }

            @Override
            public <S extends HarvestDTO> List<S> findAll(Example<S> example) {
                return null;
            }

            @Override
            public <S extends HarvestDTO> List<S> findAll(Example<S> example, Sort sort) {
                return null;
            }

            @Override
            public Page<HarvestDTO> findAll(Pageable pageable) {
                return null;
            }

            @Override
            public <S extends HarvestDTO> S save(S s) {
                return null;
            }

            @Override
            public Optional<HarvestDTO> findById(String s) {
                return Optional.empty();
            }

            @Override
            public boolean existsById(String s) {
                return false;
            }

            @Override
            public long count() {
                return 0;
            }

            @Override
            public void deleteById(String s) {

            }

            @Override
            public void delete(HarvestDTO dto) {

            }

            @Override
            public void deleteAll(Iterable<? extends HarvestDTO> iterable) {

            }

            @Override
            public void deleteAll() {

            }

            @Override
            public <S extends HarvestDTO> Optional<S> findOne(Example<S> example) {
                return Optional.empty();
            }

            @Override
            public <S extends HarvestDTO> Page<S> findAll(Example<S> example, Pageable pageable) {
                return null;
            }

            @Override
            public <S extends HarvestDTO> long count(Example<S> example) {
                return 0;
            }

            @Override
            public <S extends HarvestDTO> boolean exists(Example<S> example) {
                return false;
            }
        };
    }

}
