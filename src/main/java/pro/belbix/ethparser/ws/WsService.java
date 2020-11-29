package pro.belbix.ethparser.ws;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WsService {
    public final static String UNI_TRANSACTIONS_TOPIC_NAME = "/topic/transactions";
    public final static String HARVEST_TRANSACTIONS_TOPIC_NAME = "/topic/harvest";
    public final static String HARDWORK_TOPIC_NAME = "/topic/hardwork";
    public final static String REWARDS_TOPIC_NAME = "/topic/rewards";

    private final SimpMessagingTemplate messagingTemplate;

    public WsService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void send(String destination, Object o) {
        messagingTemplate.convertAndSend(destination, o);
    }
}
