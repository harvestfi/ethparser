package pro.belbix.ethparser.web3.harvest;

import org.springframework.stereotype.Service;
import pro.belbix.ethparser.dto.HarvestDTO;
import pro.belbix.ethparser.repositories.HardWorkRepository;
import pro.belbix.ethparser.repositories.HarvestRepository;
import pro.belbix.ethparser.web3.EthBlockService;
import pro.belbix.ethparser.web3.prices.PriceProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HardWorkCalculator {

    private final double ETH_ESTIMATE = 0.1;
    private final PriceProvider priceProvider;
    private final HarvestRepository harvestRepository;
    private final HardWorkRepository hardworkRepository;
    private final EthBlockService ethBlockService;

    public HardWorkCalculator(PriceProvider priceProvider, HarvestRepository harvestRepository, HardWorkRepository hardWorkRepository, EthBlockService ethBlockService) {
        this.priceProvider = priceProvider;
        this.harvestRepository = harvestRepository;
        this.hardworkRepository = hardWorkRepository;
        this.ethBlockService = ethBlockService;
    }

    public double calculateTotalHardWorksFeeByOwner(String ownerAddress) {
        long lastBlock = ethBlockService.getLastBlock();
        long lastBlockDate = ethBlockService.getTimestampSecForBlock(null, lastBlock);
        
        List<HarvestDTO> harvests = harvestRepository.fetchAllByOwner(ownerAddress, 0, lastBlockDate);
        List<HarvestDTO> sortedHarvests = harvests
            .stream()
            .sorted(Comparator.comparingLong(HarvestDTO::getBlockDate))
            .collect(Collectors.toList());
        
        HashMap<String, ArrayList<ArrayList<Long>>> blockRangeByVault = new HashMap<>();
        for (HarvestDTO harvest : sortedHarvests) {
            String vault = harvest.getVault();
            double balance = harvest.getOwnerBalance();

            if (!blockRangeByVault.containsKey(vault)) {
                if (balance == 0) {
                    continue;
                }
                ArrayList<ArrayList<Long>> blockPeriods = new ArrayList<>();
                blockRangeByVault.put(vault, blockPeriods);
            }

            updateBlockPeriods(blockRangeByVault.get(vault), harvest);
        }

        return blockRangeByVault
            .entrySet()
            .stream()
            .map(entry -> {
                String vault = entry.getKey();

                ArrayList<ArrayList<Long>> blockPeriods = entry.getValue();
                return blockPeriods
                    .stream()
                    .map(blockPeriod -> {
                        Long blockStartDate = blockPeriod.get(0);
                        Long blockEndDate = lastBlockDate;
                        if (blockPeriod.size() > 1) {
                            blockEndDate = blockPeriod.get(1);
                        }

                        return hardworkRepository.findAllByVaultOrderByBlockDate(vault, blockStartDate, blockEndDate);
                    })
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());

            })
            .flatMap(Collection::stream)
            .map(hardwork -> {
                long block = hardwork.getBlock();
                double ethPrice = priceProvider.getPriceForCoin("ETH", block);

                return ethPrice * ETH_ESTIMATE;
            })
            .reduce(0D, Double::sum);
    }

    private ArrayList<Long> createBlockPeriod(HarvestDTO harvest) {
        long startBlockDate = harvest.getBlockDate();
        ArrayList<Long> blockPeriod = new ArrayList<>();
        blockPeriod.add(startBlockDate);
        return blockPeriod;
    }

    private void updateBlockPeriods(ArrayList<ArrayList<Long>> blockPeriods, HarvestDTO harvest) {
        if (blockPeriods.size() == 0) {
            ArrayList<Long> blockPeriod = createBlockPeriod(harvest);
            blockPeriods.add(blockPeriod);
            return;
        }

        ArrayList<Long> lastBlockPeriod = blockPeriods.get(blockPeriods.size() - 1);
        double balance = harvest.getOwnerBalance();

        if (lastBlockPeriod.size() == 1 && balance == 0) {
            long endBlockDate = harvest.getBlockDate();
            lastBlockPeriod.add(endBlockDate);
        }

        if (lastBlockPeriod.size() == 2 && balance > 0) {
            ArrayList<Long> blockPeriod = createBlockPeriod(harvest);
            blockPeriods.add(blockPeriod);
        }
    }

}
