package pro.belbix.ethparser;

import static pro.belbix.ethparser.ws.WsService.TOPIC_NAME;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import pro.belbix.ethparser.model.Printable;
import pro.belbix.ethparser.properties.Web3Properties;
import pro.belbix.ethparser.web3.Web3Service;
import pro.belbix.ethparser.web3.uniswap.UniswapTransactionsParser;
import pro.belbix.ethparser.ws.WsService;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        Web3Service web3Service = context.getBean(Web3Service.class);
        UniswapTransactionsParser parser = context.getBean(UniswapTransactionsParser.class);
        WsService ws = context.getBean(WsService.class);

        startParse(web3Service, parser, ws);
    }

    public static void startParse(Web3Service web3Service, UniswapTransactionsParser parser, WsService ws) {
        web3Service.subscribeTransactionFlowable();
        parser.startParse();

        while (true) {
            Printable printable = null;
            try {
                printable = parser.getOutput().take();
            } catch (InterruptedException ignored) {
            }
            if (printable != null) {
                ws.send(TOPIC_NAME, printable.print());
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
            registry.addEndpoint("/ws")
                .setAllowedOrigins("*")
                .withSockJS();
        }
    }
}
