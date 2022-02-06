package pro.belbix.ethparser;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.properties.BscAppProperties;
import pro.belbix.ethparser.properties.EthAppProperties;
import pro.belbix.ethparser.properties.ExternalProperties;
import pro.belbix.ethparser.properties.MaticAppProperties;

@Configuration
@EnableConfigurationProperties({
    AppProperties.class,
    EthAppProperties.class,
    BscAppProperties.class,
    MaticAppProperties.class,
    ExternalProperties.class
})
@EnableScheduling
public class AppConfig {

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
                .withSockJS().setSupressCors(true);
        }
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
