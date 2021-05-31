package pro.belbix.ethparser.controllers;

import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;
import static pro.belbix.ethparser.utils.CommonUtils.reduceListElements;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.util.Strings;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.web3j.protocol.ObjectMapperFactory;
import pro.belbix.ethparser.dto.v0.RewardDTO;
import pro.belbix.ethparser.model.PaginatedResponse;
import pro.belbix.ethparser.model.RestResponse;
import pro.belbix.ethparser.repositories.v0.RewardsRepository;
import pro.belbix.ethparser.service.DtoCache;
import pro.belbix.ethparser.web3.contracts.ContractType;
import pro.belbix.ethparser.web3.contracts.db.ContractDbService;

@ConditionalOnExpression("!${ethparser.onlyParse:false}")
@RestController
@Log4j2
public class RewardController {

  private final RewardsRepository rewardsRepository;
  private final ContractDbService contractDbService;
  private final DtoCache dtoCache;

  public RewardController(RewardsRepository rewardsRepository,
      ContractDbService contractDbService, DtoCache dtoCache) {
    this.rewardsRepository = rewardsRepository;
    this.contractDbService = contractDbService;
    this.dtoCache = dtoCache;
  }

  @GetMapping(value = "/history/rewards/{pool}")
  List<RewardDTO> rewardsHistory(
      @PathVariable("pool") String address,
      @RequestParam(value = "days", required = false) String days,
      @RequestParam(value = "network", required = false, defaultValue = ETH_NETWORK) String network
  ) {
    int daysI = 7;
    if (days != null) {
      daysI = Integer.parseInt(days);
    }
    if (!address.startsWith("0x")) {
      address = contractDbService.getAddressByName(address, ContractType.VAULT, network)
          .orElseThrow();
    }
    return rewardsRepository.fetchRewardsByVaultAfterBlockDate(
        address,
        Instant.now().minus(daysI, ChronoUnit.DAYS).getEpochSecond(),
        Long.MAX_VALUE,
        network
    );
  }

  @RequestMapping(value = "api/transactions/last/reward", method = RequestMethod.GET)
  public List<RewardDTO> lastReward(
      @RequestParam(value = "network", required = false, defaultValue = ETH_NETWORK) String network
  ) {
    return rewardsRepository.fetchLastRewards(network);
  }

  @RequestMapping(value = "api/transactions/history/reward/{name}", method = RequestMethod.GET)
  public List<RewardDTO> historyRewardByVault(
      @PathVariable("name") String _address,
      @RequestParam(value = "reduce", required = false, defaultValue = "1") Integer reduce,
      @RequestParam(value = "start", required = false, defaultValue = "0") Long start,
      @RequestParam(value = "end", required = false, defaultValue = Long.MAX_VALUE + "") Long end,
      @RequestParam(value = "network", required = false, defaultValue = ETH_NETWORK) String network
  ) {
    String address;
    if (!_address.startsWith("0x")) {
      address = contractDbService.getAddressByName(_address, ContractType.VAULT, network)
          .orElseThrow();
    } else {
      address = _address;
    }
    return reduceListElements(
        dtoCache.load("historyRewardByVault" +
            address + start + end + network, () ->
            rewardsRepository
                .getAllByVaultOrderByBlockDate(address,
                    start,
                    end,
                    network)), reduce);
  }

  @RequestMapping(value = "api/transactions/history/reward", method = RequestMethod.GET)
  public List<RewardDTO> historyAllRewards(
      @RequestParam(value = "reduce", required = false, defaultValue = "1") Integer reduce,
      @RequestParam(value = "start", required = false, defaultValue = "0") Long start,
      @RequestParam(value = "end", required = false, defaultValue = Long.MAX_VALUE + "") Long end,
      @RequestParam(value = "network", required = false, defaultValue = ETH_NETWORK) String network
  ) {
    return reduceListElements(
        dtoCache.load("historyAllRewards" +
            start + end + network, () ->
            rewardsRepository
                .getAllOrderByBlockDate(
                    start,
                    end,
                    network)), reduce);
  }

  @GetMapping(value = "/reward/pages")
  public RestResponse rewardPages(
      @RequestParam("pageSize") String pageSize,
      @RequestParam("page") String page,
      @RequestParam(value = "ordering", required = false) String ordering,
      @RequestParam(value = "vault", required = false) String vault,
      @RequestParam(value = "minAmount", required = false) Integer minAmount,
      @RequestParam(value = "isWeeklyReward", required = false, defaultValue = "1") Integer isWeeklyReward,
      @RequestParam(value = "network", required = false, defaultValue = ETH_NETWORK) String network
  ) {
    try {
      int start = Integer.parseInt(page);
      int size = Integer.parseInt(pageSize);
      Sort sorting = Sort.by("blockDate");
      if (!Strings.isBlank(ordering) && "desc".equals(ordering)) {
        sorting = sorting.descending();
      }

      Page<RewardDTO> pages;
      if (minAmount == null) {
        minAmount = Integer.MIN_VALUE;
      }
      if (Strings.isBlank(vault)) {
        pages = rewardsRepository
            .fetchPages(minAmount, isWeeklyReward, network, PageRequest.of(start, size, sorting));
      } else {
        if (!vault.startsWith("0x")) {
          vault = contractDbService.getAddressByName(vault, ContractType.VAULT, network)
              .orElseThrow();
        }
        pages = rewardsRepository
            .fetchPagesByVault(vault, network, minAmount, isWeeklyReward,
                PageRequest.of(start, size, sorting));
      }

      if (!pages.hasContent()) {
        return RestResponse.error("Data not found");
      }
      return RestResponse.ok(
          ObjectMapperFactory.getObjectMapper().writeValueAsString(
              PaginatedResponse.builder()
                  .currentPage(start)
                  .previousPage(pages.hasPrevious() ? start - 1 : -1)
                  .nextPage(pages.hasNext() ? start + 1 : -1)
                  .totalPages(pages.getTotalPages())
                  .data(pages.getContent())
                  .build()
          )
      );

    } catch (Exception e) {
      String msg = "Error get hardwork pages";
      log.warn(msg, e);
      return RestResponse.error(msg);
    }
  }

}
