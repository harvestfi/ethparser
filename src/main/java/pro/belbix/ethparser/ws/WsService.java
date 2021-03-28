package pro.belbix.ethparser.ws;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WsService {

  public final static String UNI_TRANSACTIONS_TOPIC_NAME = "/topic/transactions";
  public final static String HARVEST_TRANSACTIONS_TOPIC_NAME = "/topic/harvest";
  public final static String HARDWORK_TOPIC_NAME = "/topic/hardwork";
  public final static String IMPORTANT_EVENTS_TOPIC_NAME = "/topic/events";
  public final static String REWARDS_TOPIC_NAME = "/topic/rewards";
  public final static String TRANSFERS_TOPIC_NAME = "/topic/transfers";
  public final static String PRICES_TOPIC_NAME = "/topic/prices";
  public final static String DEPLOYER_TRANSACTIONS_TOPIC_NAME = "/topic/transactions/deployer";

  // ABC layers
  public final static String UNI_PRICES_TOPIC_NAME = "/topic/uni_prices";

  private final SimpMessagingTemplate messagingTemplate;

  public WsService(SimpMessagingTemplate messagingTemplate) {
    this.messagingTemplate = messagingTemplate;
  }

  public void send(String destination, Object o) {
    messagingTemplate.convertAndSend(destination, o);
  }
}
