package pro.belbix.ethparser;

import static pro.belbix.ethparser.ws.WsService.TOPIC_NAME;

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
import pro.belbix.ethparser.web3.Web3Service;
import pro.belbix.ethparser.web3.uniswap.UniswapTransactionsParser;
import pro.belbix.ethparser.ws.WsService;

@SpringBootApplication
public class Application {

    private final static Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        Web3Service web3Service = context.getBean(Web3Service.class);
        UniswapTransactionsParser parser = context.getBean(UniswapTransactionsParser.class);
        WsService ws = context.getBean(WsService.class);

        startParse(web3Service, parser, ws);
//        startFakeDataForWebSocket(ws);
    }

    public static void startParse(Web3Service web3Service, UniswapTransactionsParser parser, WsService ws) {
        web3Service.subscribeTransactionFlowable();
        parser.startParse();

        while (true) {
            TransactionDTO transactionDTO = null;
            try {
                transactionDTO = parser.getOutput().take();
            } catch (InterruptedException ignored) {
            }
            if (transactionDTO != null) {
                ws.send(TOPIC_NAME, transactionDTO);
                log.info("sent to ws");
            }
        }
    }

    private static void startFakeDataForWebSocket(WsService ws) {
        int count = 0;
        while (true) {
            double currentCount = count * new Random().nextDouble();
            TransactionDTO dto = new TransactionDTO();
            dto.setAmount(currentCount);
            dto.setOtherAmount(currentCount);
            dto.setCoin("FARM");
            dto.setOtherCoin("USDC");
            dto.setHash("0x123123123asda2343121231sdad");
            dto.setType("BUY");
            dto.setLastPrice(currentCount);
            ws.send(TOPIC_NAME, dto);
            log.info("Msg sent " + currentCount);
            count++;
            try {
                Thread.sleep(100);
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
