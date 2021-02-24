package pro.belbix.ethparser.controllers;

import static pro.belbix.ethparser.utils.CommonUtils.parseLong;

import java.io.IOException;
import java.util.List;
import lombok.extern.log4j.Log4j2;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletResponse;
import pro.belbix.ethparser.dto.DtoI;
import pro.belbix.ethparser.dto.HarvestDTO;
import pro.belbix.ethparser.dto.RewardDTO;
import pro.belbix.ethparser.dto.HardWorkDTO;
import pro.belbix.ethparser.model.TvlHistory;
import pro.belbix.ethparser.repositories.HarvestRepository;
import pro.belbix.ethparser.repositories.HardWorkRepository;
import pro.belbix.ethparser.repositories.RewardsRepository;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;
import pro.belbix.ethparser.model.RestResponse;
import pro.belbix.ethparser.service.HarvestTvlDBService;


@RestController
@Log4j2
@RequestMapping(value = "/csv")
public class CSVController {

    private static int RETURN_LIMIT = 1000000;

    private final HarvestRepository harvestRepository;
    private final RewardsRepository rewardsRepository;
    private final HardWorkRepository hardWorkRepository;
    private final HarvestTvlDBService harvestTvlDBService;

    public CSVController(HarvestRepository harvestRepository, RewardsRepository rewardsRepository, HardWorkRepository hardWorkRepository, HarvestTvlDBService harvestTvlDBService) {
        this.harvestRepository = harvestRepository;
        this.rewardsRepository = rewardsRepository;
        this.hardWorkRepository = hardWorkRepository;
        this.harvestTvlDBService = harvestTvlDBService;
    }

    @RequestMapping(value = "/transactions/history/harvest/{name}", method = RequestMethod.GET)
    public RestResponse harvestHistoryDataForVault(HttpServletResponse response, @PathVariable("name") String name,
                                                           @RequestParam(value = "start", required = false) String start,
                                                           @RequestParam(value = "end", required = false) String end) {

        List<HarvestDTO> transactions = harvestRepository.findAllByVaultOrderByBlockDate(name, parseLong(start, 0), parseLong(end, Long.MAX_VALUE));
        String[] headers = { "Block", "BlockDate", "MethodName", "Owner", "Vault", "Amount", "UsdAmount"};

        try {
            writeCSV(response, transactions, headers);
        } catch (IOException e) {
            log.error("Error while converting to CSV Harvest", e);
            return RestResponse.error("Error while converting to CSV Harvest");
        }

        return RestResponse.ok("ok");
    }

    @RequestMapping(value = "/transactions/history/reward/{name}", method = RequestMethod.GET)
    public RestResponse rewardHistoryDataForVault(HttpServletResponse response, @PathVariable("name") String name,
                                                           @RequestParam(value = "start", required = false) String start,
                                                           @RequestParam(value = "end", required = false) String end) {

        List<RewardDTO> transactions = rewardsRepository.getAllByVaultOrderByBlockDate(name, parseLong(start, 0), parseLong(end, Long.MAX_VALUE));
        String[] headers = { "Block", "BlockDate", "Reward", "PeriodFinish", "Apy", "WeeklyApy", "Tvl"};

        try {
            writeCSV(response, transactions, headers);
        } catch (IOException e) {
            log.error("Error while converting to CSV Rewards", e);
            return RestResponse.error("Error while converting to CSV Rewards");
        }

        return RestResponse.ok("ok");
    }

    @RequestMapping(value = "/transactions/history/hardwork/{name}", method = RequestMethod.GET)
    public RestResponse hardworkHistoryDataForVault(HttpServletResponse response, @PathVariable("name") String name,
                                                           @RequestParam(value = "start", required = false) String start,
                                                           @RequestParam(value = "end", required = false) String end) {

        List<HardWorkDTO> transactions = hardWorkRepository.findAllByVaultOrderByBlockDate(name, parseLong(start, 0), parseLong(end, Long.MAX_VALUE));
        String[] headers = { "Block", "BlockDate", "ShareChange", "ShareUsdTotal", "AllProfit", "PeriodOfWork", "PsPeriodOfWork", "WeeklyAllProfit", "PsTvlUsd", "PsApr", "Apr", "FarmBuybackSum", "PoolUsers", "SavedGasFees", "Fee", "WeeklyAverageTvl", "Tvl"};

        try {
            writeCSV(response, transactions, headers);
        } catch (IOException e) {
            log.error("Error while converting to CSV Rewards", e);
            return RestResponse.error("Error while converting to CSV Rewards");
        }

        return RestResponse.ok("ok");
    }

    @RequestMapping(value = "/transactions/history/tvl/{name}", method = RequestMethod.GET)
    public RestResponse tvlHistoryDataForVault(HttpServletResponse response, @PathVariable("name") String name,
                                                           @RequestParam(value = "start", required = false) String start,
                                                           @RequestParam(value = "end", required = false) String end) {

        List<TvlHistory> transactions = harvestTvlDBService.fetchTvlByVault(name, parseLong(start, 0), parseLong(end, Long.MAX_VALUE));
        String[] headers = { "CalculateTime", "LastTvl", "SharePrice", "LastOwnersCount", "LastPrice", "LastTvlNative"};

        try {
            writeCSV(response, transactions, headers);
        } catch (IOException e) {
            log.error("Error while converting to CSV Rewards", e);
            return RestResponse.error("Error while converting to CSV Rewards");
        }

        return RestResponse.ok("ok");
    }

    private void writeCSV(HttpServletResponse response, List transactions, String[] headers) throws IOException {
        String csvFileName = "transactions.csv";
        response.setContentType("text/csv");

        String headerKey = "Content-Disposition";
        String headerValue = String.format("attachment; filename=\"%s\"", csvFileName);

        response.setHeader(headerKey, headerValue);

        ICsvBeanWriter csvWriter = new CsvBeanWriter(response.getWriter(), CsvPreference.STANDARD_PREFERENCE);

        csvWriter.writeHeader(headers);
        
        int listSize = transactions.size();
        int returnLimit = listSize<RETURN_LIMIT? 0:listSize-RETURN_LIMIT;

        for (Object harvest : transactions.subList(returnLimit, listSize)) {
            csvWriter.write(harvest, headers);
        }

        csvWriter.close();
    }
}
