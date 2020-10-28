package pro.belbix.ethparser.ws;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WsService {
    public final static String TOPIC_NAME = "/topic/transactions/last";

    private final SimpMessagingTemplate messagingTemplate;

    public WsService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void send(String destination, Object o) {
        messagingTemplate.convertAndSend(destination, o);
    }
}
