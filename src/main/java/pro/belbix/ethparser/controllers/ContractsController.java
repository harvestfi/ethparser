package pro.belbix.ethparser.controllers;

import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.util.Strings;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pro.belbix.ethparser.entity.contracts.PoolEntity;
import pro.belbix.ethparser.entity.contracts.TokenEntity;
import pro.belbix.ethparser.entity.contracts.UniPairEntity;
import pro.belbix.ethparser.entity.contracts.VaultEntity;
import pro.belbix.ethparser.model.RestResponse;
import pro.belbix.ethparser.repositories.eth.PoolRepository;
import pro.belbix.ethparser.repositories.eth.TokenRepository;
import pro.belbix.ethparser.repositories.eth.UniPairRepository;
import pro.belbix.ethparser.repositories.eth.VaultRepository;


@RestController
@Log4j2
public class ContractsController {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final VaultRepository vaultRepository;
    private final PoolRepository poolRepository;
    private final UniPairRepository uniPairRepository;
    private final TokenRepository tokenRepository;

    public ContractsController(
        VaultRepository vaultRepository,
        PoolRepository poolRepository,
        UniPairRepository uniPairRepository,
        TokenRepository tokenRepository) {
        this.vaultRepository = vaultRepository;
        this.poolRepository = poolRepository;
        this.uniPairRepository = uniPairRepository;
        this.tokenRepository = tokenRepository;
    }

    @GetMapping(value = "/contracts/vaults")
    RestResponse vaults(@RequestParam(value = "network", required = false) String network) {
        try {
            if (network == null || Strings.isBlank(network)) {
                network = ETH_NETWORK;
            }
            List<VaultEntity> vaults = vaultRepository.fetchAllByNetwork(network);
            return RestResponse.ok(objectMapper.writeValueAsString(vaults));
        } catch (Exception e) {
            log.info("Error vaults request", e.fillInStackTrace());
            return RestResponse.error("Server error");
        }
    }

    @GetMapping(value = "/contracts/pools")
    RestResponse pools(@RequestParam(value = "network", required = false) String network) {
        try {
            if (network == null || Strings.isBlank(network)) {
                network = ETH_NETWORK;
            }
            List<PoolEntity> pools = poolRepository.fetchAllByNetwork(network);
            return RestResponse.ok(objectMapper.writeValueAsString(pools));
        } catch (Exception e) {
            log.info("Error pools request", e.fillInStackTrace());
            return RestResponse.error("Server error");
        }
    }

    @GetMapping(value = "/contracts/tokens")
    RestResponse tokens(@RequestParam(value = "network", required = false) String network) {
        try {
            if (network == null || Strings.isBlank(network)) {
                network = ETH_NETWORK;
            }
            List<TokenEntity> tokens = tokenRepository.fetchAllByNetwork(network);
            return RestResponse.ok(objectMapper.writeValueAsString(tokens));
        } catch (Exception e) {
            log.info("Error tokens request", e.fillInStackTrace());
            return RestResponse.error("Server error");
        }
    }

    @GetMapping(value = "/contracts/lps")
    RestResponse lps(@RequestParam(value = "network", required = false) String network) {
        try {
            if (network == null || Strings.isBlank(network)) {
                network = ETH_NETWORK;
            }
            List<UniPairEntity> uniPairs = uniPairRepository.fetchAllByNetwork(network);
            return RestResponse.ok(objectMapper.writeValueAsString(uniPairs));
        } catch (Exception e) {
            log.info("Error uniPairs request", e.fillInStackTrace());
            return RestResponse.error("Server error");
        }
    }
}
