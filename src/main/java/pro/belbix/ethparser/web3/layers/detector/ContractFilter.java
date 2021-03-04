package pro.belbix.ethparser.web3.layers.detector;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.web3.contracts.ContractUtils;

@Service
public class ContractFilter {

    private final Set<String> eligibleContracts = new HashSet<>();

    @PostConstruct
    private void init() {
        //todo get contracts from deployer
        eligibleContracts.addAll(ContractUtils.getAllVaultAddresses());
        eligibleContracts.addAll(ContractUtils.getAllPoolAddresses());
        eligibleContracts.addAll(ContractUtils.getAllUniPairs().stream()
            .map(u -> u.getContract().getAddress())
            .collect(Collectors.toSet())
        );
        eligibleContracts.addAll(ContractUtils.getAllTokens().stream()
            .map(u -> u.getContract().getAddress())
            .collect(Collectors.toSet())
        );
    }

    public boolean isEligible(String address) {
        return eligibleContracts.contains(address);
    }
}
