package pro.belbix.ethparser;

import static pro.belbix.ethparser.model.UniswapTx.ADD_LIQ;
import static pro.belbix.ethparser.model.UniswapTx.REMOVE_LIQ;
import static pro.belbix.ethparser.ws.WsService.HARVEST_TRANSACTIONS_TOPIC_NAME;
import static pro.belbix.ethparser.ws.WsService.UNI_TRANSACTIONS_TOPIC_NAME;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import pro.belbix.ethparser.model.TransactionDTO;
import pro.belbix.ethparser.properties.Web3Properties;
import pro.belbix.ethparser.web3.Web3Parser;
import pro.belbix.ethparser.web3.Web3Service;
import pro.belbix.ethparser.web3.harvest.HarvestTransactionsParser;
import pro.belbix.ethparser.web3.uniswap.UniswapTransactionsParser;
import pro.belbix.ethparser.ws.WsService;

@SpringBootApplication
public class Application {

    private final static Logger log = LoggerFactory.getLogger(Application.class);
    private static boolean web3Started = false;

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        Web3Service web3Service = context.getBean(Web3Service.class);
        UniswapTransactionsParser uniswapTransactionsParser = context.getBean(UniswapTransactionsParser.class);
        HarvestTransactionsParser harvestTransactionsParser = context.getBean(HarvestTransactionsParser.class);
        WsService ws = context.getBean(WsService.class);
        Web3Properties conf = context.getBean(Web3Properties.class);

        if (conf.isTestWs()) {
            startFakeDataForWebSocket(ws, conf.getTestWsRate());
        } else {
            if (conf.isParseTransactions()) {
                startParse(web3Service, uniswapTransactionsParser, ws, UNI_TRANSACTIONS_TOPIC_NAME);
            }

            if (conf.isParseHarvest()) {
                startParse(web3Service, harvestTransactionsParser, ws, HARVEST_TRANSACTIONS_TOPIC_NAME);
            }
        }
    }

    private static void startWeb3Subscribe(Web3Service web3Service) {
        if (!web3Started) {
            web3Service.subscribeTransactionFlowable();
            web3Started = true;
        }
    }

    public static void startParse(Web3Service web3Service, Web3Parser parser, WsService ws,
                                  String topicName) {
        startWeb3Subscribe(web3Service);
        parser.startParse();

        new Thread(() -> {
            while (true) {
                TransactionDTO transactionDTO = null;
                try {
                    transactionDTO = parser.getOutput().take();
                } catch (InterruptedException ignored) {
                }
                if (transactionDTO != null) {
                    ws.send(topicName, transactionDTO);
                }
            }
        }).start();

    }

    private static void startFakeDataForWebSocket(WsService ws, int rate) {
        int count = 0;
        while (true) {
            double currentCount = count * new Random().nextDouble();
            TransactionDTO dto = new TransactionDTO();
            dto.setAmount(currentCount);
            dto.setOtherAmount(currentCount);
            dto.setCoin("FARM");
            dto.setOtherCoin("USDC");
            dto.setHash("0x123123123asda2343121231sdad");
            dto.setType(new Random().nextBoolean() ?
                new Random().nextBoolean() ? "BUY" : "SELL" :
                new Random().nextBoolean() ? ADD_LIQ : REMOVE_LIQ);
            dto.setLastPrice(currentCount);
            dto.setConfirmed(new Random().nextBoolean());
            dto.setLastGas(currentCount / 6);
            dto.setBlockDate(Instant.now().plus(count, ChronoUnit.MINUTES).getEpochSecond());
            ws.send(UNI_TRANSACTIONS_TOPIC_NAME, dto);
            log.info("Msg sent " + currentCount);
            count++;
            try {
                Thread.sleep(rate);
            } catch (InterruptedException ignored) {
            }
        }
    }

    @Configuration
    @EnableConfigurationProperties({
        Web3Properties.class
    })
    public static class AppConfig {

    }

    @Configuration
    @EnableWebSocketMessageBroker
    public static class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

        @Override
        public void configureMessageBroker(MessageBrokerRegistry config) {
            config.enableSimpleBroker("/topic");
            config.setApplicationDestinationPrefixes("/app");
        }

        @Override
        public void registerStompEndpoints(StompEndpointRegistry registry) {
            registry.addEndpoint("/stomp")
                .setAllowedOrigins("*")
                .withSockJS();
        }
    }
}
