package pro.belbix.ethparser;

import static pro.belbix.ethparser.model.UniswapTx.ADD_LIQ;
import static pro.belbix.ethparser.model.UniswapTx.REMOVE_LIQ;
import static pro.belbix.ethparser.utils.MockUtils.createHardWorkDTO;
import static pro.belbix.ethparser.utils.MockUtils.createHarvestDTO;
import static pro.belbix.ethparser.utils.MockUtils.createImportantEventsDTO;
import static pro.belbix.ethparser.utils.MockUtils.createUniswapDTO;
import static pro.belbix.ethparser.ws.WsService.HARDWORK_TOPIC_NAME;
import static pro.belbix.ethparser.ws.WsService.HARVEST_TRANSACTIONS_TOPIC_NAME;
import static pro.belbix.ethparser.ws.WsService.REWARDS_TOPIC_NAME;
import static pro.belbix.ethparser.ws.WsService.IMPORTANT_EVENTS_TOPIC_NAME;
import static pro.belbix.ethparser.ws.WsService.TRANSFERS_TOPIC_NAME;
import static pro.belbix.ethparser.ws.WsService.UNI_TRANSACTIONS_TOPIC_NAME;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import pro.belbix.ethparser.dto.DtoI;
import pro.belbix.ethparser.dto.HardWorkDTO;
import pro.belbix.ethparser.dto.HarvestDTO;
import pro.belbix.ethparser.dto.UniswapDTO;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.web3.Web3Parser;
import pro.belbix.ethparser.web3.Web3Service;
import pro.belbix.ethparser.web3.erc20.parser.TransferParser;
import pro.belbix.ethparser.web3.harvest.contracts.Vaults;
import pro.belbix.ethparser.web3.harvest.decoder.HarvestVaultLogDecoder;
import pro.belbix.ethparser.web3.harvest.parser.HardWorkParser;
import pro.belbix.ethparser.web3.harvest.parser.HarvestTransactionsParser;
import pro.belbix.ethparser.web3.harvest.parser.HarvestVaultParserV2;
import pro.belbix.ethparser.web3.harvest.parser.RewardParser;
import pro.belbix.ethparser.web3.harvest.parser.ImportantEventsParser;
import pro.belbix.ethparser.web3.harvest.parser.UniToHarvestConverter;
import pro.belbix.ethparser.web3.uniswap.parser.UniswapLpLogParser;
import pro.belbix.ethparser.web3.uniswap.parser.UniswapTransactionsParser;
import pro.belbix.ethparser.ws.WsService;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        context.getBean(AppStarter.class).start();
    }

}
