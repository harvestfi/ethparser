package pro.belbix.ethparser.utils;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import pro.belbix.ethparser.dto.HarvestDTO;
import pro.belbix.ethparser.repositories.HarvestRepository;

public class HarvestRepositoryCache implements HarvestRepository {
    private final Map<String, TreeMap<Long, HarvestDTO>> harvestVaults;

    public HarvestRepositoryCache(
        Map<String, TreeMap<Long, HarvestDTO>> harvestVaults) {
        this.harvestVaults = harvestVaults;
    }

    @Override
    public List<HarvestDTO> findAllByOrderByBlockDate() {
        return null;
    }

    @Override
    public List<HarvestDTO> findAllByBlockDateGreaterThanOrderByBlockDate(long blockDate) {
        return null;
    }

    @Override
    public List<HarvestDTO> fetchAllWithoutCounts() {
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
    public Integer fetchActualOwnerQuantity(String vault, String oldVault, long blockDate) {
        return null;
    }

    @Override
    public Integer fetchAllUsersQuantity(long blockDate) {
        return null;
    }

    @Override
    public Integer fetchAllPoolsUsersQuantity(List<String> vaults, long blockDate) {
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
    public List<HarvestDTO> fetchAllMigration() {
        return null;
    }

    @Override
    public HarvestDTO findFirstByVaultAndBlockDateBeforeOrderByBlockDateDesc(String vault, long blockDate) {
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
}
