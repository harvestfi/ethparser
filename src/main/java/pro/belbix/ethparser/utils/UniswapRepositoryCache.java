package pro.belbix.ethparser.utils;

import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import pro.belbix.ethparser.dto.UniswapDTO;
import pro.belbix.ethparser.repositories.UniswapRepository;

public class UniswapRepositoryCache implements UniswapRepository {
    private final TreeMap<Long, UniswapDTO> uniswapDTOTreeMap;

    public UniswapRepositoryCache(
        TreeMap<Long, UniswapDTO> uniswapDTOTreeMap) {
        this.uniswapDTOTreeMap = uniswapDTOTreeMap;
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
    public Integer fetchOwnerCount(long blockDate) {
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

    @Override
    public List<UniswapDTO> fetchAllWithoutOwnerBalance() {
        return null;
    }
}
