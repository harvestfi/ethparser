package pro.belbix.ethparser;

import static pro.belbix.ethparser.ws.WsService.DEPLOYER_TRANSACTIONS_TOPIC_NAME;
import static pro.belbix.ethparser.ws.WsService.HARDWORK_TOPIC_NAME;
import static pro.belbix.ethparser.ws.WsService.HARVEST_TRANSACTIONS_TOPIC_NAME;
import static pro.belbix.ethparser.ws.WsService.IMPORTANT_EVENTS_TOPIC_NAME;
import static pro.belbix.ethparser.ws.WsService.PRICES_TOPIC_NAME;
import static pro.belbix.ethparser.ws.WsService.REWARDS_TOPIC_NAME;
import static pro.belbix.ethparser.ws.WsService.TRANSFERS_TOPIC_NAME;
import static pro.belbix.ethparser.ws.WsService.UNI_TRANSACTIONS_TOPIC_NAME;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import pro.belbix.ethparser.dto.DtoI;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.utils.MockUtils;
import pro.belbix.ethparser.web3.Web3Parser;
import pro.belbix.ethparser.web3.Web3Subscriber;
import pro.belbix.ethparser.web3.deployer.parser.DeployerTransactionsParser;
import pro.belbix.ethparser.web3.erc20.parser.TransferParser;
import pro.belbix.ethparser.web3.harvest.parser.HardWorkParser;
import pro.belbix.ethparser.web3.harvest.parser.ImportantEventsParser;
import pro.belbix.ethparser.web3.harvest.parser.RewardParser;
import pro.belbix.ethparser.web3.harvest.parser.UniToHarvestConverter;
import pro.belbix.ethparser.web3.harvest.parser.VaultActionsParser;
import pro.belbix.ethparser.web3.layers.blocks.parser.EthBlockParser;
import pro.belbix.ethparser.web3.layers.detector.ContractDetector;
import pro.belbix.ethparser.web3.prices.parser.PriceLogParser;
import pro.belbix.ethparser.web3.uniswap.parser.UniswapLpLogParser;
import pro.belbix.ethparser.ws.WsService;

@Component
@Log4j2
public class AppStarter {

    private final Web3Subscriber web3Subscriber;
    private final UniswapLpLogParser uniswapLpLogParser;
    private final VaultActionsParser vaultActionsParser;
    private final RewardParser rewardParser;
    private final HardWorkParser hardWorkParser;
    private final ImportantEventsParser importantEventsParser;
    private final UniToHarvestConverter uniToHarvestConverter;
    private final TransferParser transferParser;
    private final WsService ws;
    private final AppProperties conf;
    private final PriceLogParser priceLogParser;
    private final DeployerTransactionsParser deployerTransactionsParser;
    private final EthBlockParser ethBlockParser;
    private final ContractDetector contractDetector;
    private final MockUtils mockUtils;

    public AtomicBoolean run = new AtomicBoolean(true); //for gentle stop
    private boolean web3TransactionsStarted = false;
    private boolean web3LogsStarted = false;
    private boolean web3BlocksStarted = false;

    public AppStarter(
        Web3Subscriber web3Subscriber,
        UniswapLpLogParser uniswapLpLogParser,
        VaultActionsParser vaultActionsParser,
        RewardParser rewardParser, HardWorkParser hardWorkParser,
        ImportantEventsParser importantEventsParser,
        UniToHarvestConverter uniToHarvestConverter,
        TransferParser transferParser, WsService wsService,
        AppProperties appProperties,
        PriceLogParser priceLogParser,
        DeployerTransactionsParser deployerTransactionsParser,
        EthBlockParser ethBlockParser,
        ContractDetector contractDetector,
        MockUtils mockUtils) {
        this.web3Subscriber = web3Subscriber;
        this.uniswapLpLogParser = uniswapLpLogParser;
        this.vaultActionsParser = vaultActionsParser;
        this.rewardParser = rewardParser;
        this.hardWorkParser = hardWorkParser;
        this.importantEventsParser = importantEventsParser;
        this.uniToHarvestConverter = uniToHarvestConverter;
        this.transferParser = transferParser;
        this.ws = wsService;
        this.conf = appProperties;
        this.priceLogParser = priceLogParser;
        this.deployerTransactionsParser = deployerTransactionsParser;
        this.ethBlockParser = ethBlockParser;
        this.contractDetector = contractDetector;
        this.mockUtils = mockUtils;
    }

    public void start() {
        if (conf.isOnlyApi()) {
            return;
        }

        if (conf.isTestWs()) {
            startFakeDataForWebSocket(ws, conf.getTestWsRate());
        } else {
            startParse(uniswapLpLogParser, ws, UNI_TRANSACTIONS_TOPIC_NAME, true);
            startParse(vaultActionsParser, ws, HARVEST_TRANSACTIONS_TOPIC_NAME, true);
            startParse(hardWorkParser, ws, HARDWORK_TOPIC_NAME, true);
            startParse(rewardParser, ws, REWARDS_TOPIC_NAME, true);
            startParse(importantEventsParser, ws, IMPORTANT_EVENTS_TOPIC_NAME, true);
            startParse(uniToHarvestConverter, ws, HARVEST_TRANSACTIONS_TOPIC_NAME, true);
            startParse(transferParser, ws, TRANSFERS_TOPIC_NAME, true);
            startParse(priceLogParser, ws, PRICES_TOPIC_NAME, true);
            startParse(deployerTransactionsParser, ws,
                DEPLOYER_TRANSACTIONS_TOPIC_NAME, false);
            startParseBlocks();
        }
    }

    private void startFakeDataForWebSocket(WsService ws, int rate) {
        int count = 0;
        while (run.get()) {
            double currentCount = count * new Random().nextDouble();
            ws.send(UNI_TRANSACTIONS_TOPIC_NAME, mockUtils.createUniswapDTO(count));
            ws.send(HARVEST_TRANSACTIONS_TOPIC_NAME, mockUtils.createHarvestDTO(count));
            ws.send(HARDWORK_TOPIC_NAME, mockUtils.createHardWorkDTO(count));
            ws.send(IMPORTANT_EVENTS_TOPIC_NAME, mockUtils.createImportantEventsDTO(count));
            ws.send(PRICES_TOPIC_NAME, mockUtils.createPriceDTO(count));
            log.info("Msg sent " + currentCount);
            count++;
            try {
                //noinspection BusyWait
                Thread.sleep(rate);
            } catch (InterruptedException ignored) {
            }
        }
    }

    public void startParse(Web3Parser parser, WsService ws,
                           String topicName, boolean logs) {
        if (logs) {
            startWeb3SubscribeLog();
        } else {
            startWeb3SubscribeTx();
        }
        parser.startParse();

        new Thread(() -> {
            Thread.currentThread().setName("ParserToWsSender"
                + parser.getClass().getSimpleName());
            while (run.get()) {
                DtoI dto = null;
                try {
                    dto = parser.getOutput().poll(1, TimeUnit.SECONDS);
                } catch (InterruptedException ignored) {
                }
                if (dto != null && topicName != null) {
                    log.debug("Sent to ws {} {}", topicName, dto);
                    ws.send(topicName, dto);
                }
            }
        }).start();

    }

    private void startWeb3SubscribeLog() {
        if (!web3LogsStarted) {
            Arrays.stream(conf.getNetworks())
                .forEach(web3Subscriber::subscribeLogFlowable);
            web3LogsStarted = true;
        }
    }

    private void startWeb3SubscribeTx() {
        if (!web3TransactionsStarted) {
            Arrays.stream(conf.getNetworks())
                .forEach(web3Subscriber::subscribeTransactionFlowable);
            web3TransactionsStarted = true;
        }
    }

    private void startParseBlocks() {
        if (!web3BlocksStarted) {
            Arrays.stream(conf.getNetworks())
                .forEach(web3Subscriber::subscribeOnBlocks);
            web3BlocksStarted = true;
        }
        ethBlockParser.startParse();
        contractDetector.start();

    }
}
