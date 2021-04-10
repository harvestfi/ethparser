package pro.belbix.ethparser;

import static pro.belbix.ethparser.utils.MockUtils.createHardWorkDTO;
import static pro.belbix.ethparser.utils.MockUtils.createHarvestDTO;
import static pro.belbix.ethparser.utils.MockUtils.createImportantEventsDTO;
import static pro.belbix.ethparser.utils.MockUtils.createUniswapDTO;
import static pro.belbix.ethparser.ws.WsService.DEPLOYER_TRANSACTIONS_TOPIC_NAME;
import static pro.belbix.ethparser.ws.WsService.HARDWORK_TOPIC_NAME;
import static pro.belbix.ethparser.ws.WsService.HARVEST_TRANSACTIONS_TOPIC_NAME;
import static pro.belbix.ethparser.ws.WsService.IMPORTANT_EVENTS_TOPIC_NAME;
import static pro.belbix.ethparser.ws.WsService.PRICES_TOPIC_NAME;
import static pro.belbix.ethparser.ws.WsService.REWARDS_TOPIC_NAME;
import static pro.belbix.ethparser.ws.WsService.TRANSFERS_TOPIC_NAME;
import static pro.belbix.ethparser.ws.WsService.UNI_TRANSACTIONS_TOPIC_NAME;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import pro.belbix.ethparser.dto.DtoI;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.web3.Web3Parser;
import pro.belbix.ethparser.web3.Web3Functions;
import pro.belbix.ethparser.web3.Web3Subscriber;
import pro.belbix.ethparser.web3.contracts.ContractLoader;
import pro.belbix.ethparser.web3.deployer.parser.DeployerTransactionsParser;
import pro.belbix.ethparser.web3.erc20.parser.TransferParser;
import pro.belbix.ethparser.web3.harvest.parser.HardWorkParser;
import pro.belbix.ethparser.web3.harvest.parser.HarvestTransactionsParser;
import pro.belbix.ethparser.web3.harvest.parser.HarvestVaultParserV2;
import pro.belbix.ethparser.web3.harvest.parser.ImportantEventsParser;
import pro.belbix.ethparser.web3.harvest.parser.RewardParser;
import pro.belbix.ethparser.web3.harvest.parser.UniToHarvestConverter;
import pro.belbix.ethparser.web3.layers.blocks.parser.EthBlockParser;
import pro.belbix.ethparser.web3.layers.detector.ContractDetector;
import pro.belbix.ethparser.web3.prices.parser.PriceLogParser;
import pro.belbix.ethparser.web3.uniswap.parser.UniswapLpLogParser;
import pro.belbix.ethparser.ws.WsService;

@Component
@Log4j2
public class AppStarter {

    private final Web3Functions web3Functions;
    private final Web3Subscriber web3Subscriber;
    private final HarvestTransactionsParser harvestTransactionsParser;
    private final UniswapLpLogParser uniswapLpLogParser;
    private final HarvestVaultParserV2 harvestVaultParserV2;
    private final RewardParser rewardParser;
    private final HardWorkParser hardWorkParser;
    private final ImportantEventsParser importantEventsParser;
    private final UniToHarvestConverter uniToHarvestConverter;
    private final TransferParser transferParser;
    private final WsService ws;
    private final AppProperties conf;
    private final PriceLogParser priceLogParser;
    private final ContractLoader contractLoader;
    private final DeployerTransactionsParser deployerTransactionsParser;
    private final EthBlockParser ethBlockParser;
    private final ContractDetector contractDetector;

    public AtomicBoolean run = new AtomicBoolean(true); //for gentle stop
    private boolean web3TransactionsStarted = false;
    private boolean web3LogsStarted = false;
    private boolean web3BlocksStarted = false;

    public AppStarter(Web3Functions web3Functions,
        Web3Subscriber web3Subscriber,
        HarvestTransactionsParser harvestTransactionsParser,
        UniswapLpLogParser uniswapLpLogParser,
        HarvestVaultParserV2 harvestVaultParserV2,
        RewardParser rewardParser, HardWorkParser hardWorkParser,
        ImportantEventsParser importantEventsParser,
        UniToHarvestConverter uniToHarvestConverter,
        TransferParser transferParser, WsService wsService,
        AppProperties appProperties,
        PriceLogParser priceLogParser, ContractLoader contractLoader,
        DeployerTransactionsParser deployerTransactionsParser,
        EthBlockParser ethBlockParser,
        ContractDetector contractDetector) {
        this.web3Functions = web3Functions;
        this.web3Subscriber = web3Subscriber;
        this.harvestTransactionsParser = harvestTransactionsParser;
        this.uniswapLpLogParser = uniswapLpLogParser;
        this.harvestVaultParserV2 = harvestVaultParserV2;
        this.rewardParser = rewardParser;
        this.hardWorkParser = hardWorkParser;
        this.importantEventsParser = importantEventsParser;
        this.uniToHarvestConverter = uniToHarvestConverter;
        this.transferParser = transferParser;
        this.ws = wsService;
        this.conf = appProperties;
        this.priceLogParser = priceLogParser;
        this.contractLoader = contractLoader;
        this.deployerTransactionsParser = deployerTransactionsParser;
        this.ethBlockParser = ethBlockParser;
        this.contractDetector = contractDetector;
    }

    public void start() {
        if (conf.isOnlyApi()) {
            return;
        }

        if (conf.isTestWs()) {
            startFakeDataForWebSocket(ws, conf.getTestWsRate());
        } else {
            contractLoader.load(conf.getNetwork());

            if (conf.isParseHarvest()) {
                startParse(harvestTransactionsParser, ws, HARVEST_TRANSACTIONS_TOPIC_NAME, false);
            }

            if (conf.isParseUniswapLog()) {
                startParse(uniswapLpLogParser, ws, UNI_TRANSACTIONS_TOPIC_NAME, true);
            }

            if (conf.isParseHarvestLog()) {
                startParse(harvestVaultParserV2, ws, HARVEST_TRANSACTIONS_TOPIC_NAME, true);
            }

            if (conf.isParseHardWorkLog()) {
                startParse(hardWorkParser, ws, HARDWORK_TOPIC_NAME, true);
            }

            if (conf.isParseRewardsLog()) {
                startParse(rewardParser, ws, REWARDS_TOPIC_NAME, true);
            }

            if (conf.isParseImportantEvents()) {
                startParse(importantEventsParser, ws, IMPORTANT_EVENTS_TOPIC_NAME, true);
            }

            if (conf.isConvertUniToHarvest()) {
                startParse(uniToHarvestConverter, ws, HARVEST_TRANSACTIONS_TOPIC_NAME, true);
            }
            if (conf.isParseTransfers()) {
                startParse(transferParser, ws, TRANSFERS_TOPIC_NAME, true);
            }
            if (conf.isParsePrices()) {
                startParse(priceLogParser, ws, PRICES_TOPIC_NAME, true);
            }
            if (conf.isParseDeployerTransactions()) {
                startParse(deployerTransactionsParser, ws,
                    DEPLOYER_TRANSACTIONS_TOPIC_NAME, false);
            }
            if (conf.isParseBlocks()) {
                startParseBlocks();
            }
        }
    }

    private void startFakeDataForWebSocket(WsService ws, int rate) {
        contractLoader.load(conf.getNetwork());
        int count = 0;
        while (run.get()) {
            double currentCount = count * new Random().nextDouble();
            ws.send(UNI_TRANSACTIONS_TOPIC_NAME, createUniswapDTO(count));
            ws.send(HARVEST_TRANSACTIONS_TOPIC_NAME, createHarvestDTO(count));
            ws.send(HARDWORK_TOPIC_NAME, createHardWorkDTO(count));
            ws.send(IMPORTANT_EVENTS_TOPIC_NAME, createImportantEventsDTO(count));
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
            web3Subscriber.subscribeLogFlowable(conf.getNetwork());
            web3LogsStarted = true;
        }
    }

    private void startWeb3SubscribeTx() {
        if (!web3TransactionsStarted) {
            web3Subscriber.subscribeTransactionFlowable(conf.getNetwork());
            web3TransactionsStarted = true;
        }
    }

    private void startParseBlocks() {
        if (!web3BlocksStarted) {
            web3Subscriber.subscribeOnBlocks(conf.getNetwork());
            web3BlocksStarted = true;
        }
        ethBlockParser.startParse();
        contractDetector.start();

    }
}
