package pro.belbix.ethparser.web3.harvest;

import java.util.HashMap;
import java.util.Map;

public class HarvestTopics {

    //addVaultAndStrategy(address _vault, address _strategy)
    public static final String ADD_VAULT_AND_STRATEGY = "0x254c88e7a2ea123aeeb89b7cc413fb949188fefcdb7584c4f3d493294daf65c5";
    //exit()
    public static final String EXIT = "0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef";
    //stake(uint256 countryID)
    public static final String STAKE = "0x8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b925";
    //depositAll(uint256[] amounts, address[] vaultAddresses)
    public static final String DEPOSIT_ALL = "0xe1fffcc4923d04b559f4d29a8bfc6cda04eb5b0d3c460751c2402c5c5cc9109c";
    //migrateInOneTx(address _oldVault, address _newVault, address _migratorStrategy, address _newStrategy, address _poolAddress)
    public static final String MIGRATE_IN_ONE_TX = "0xa09b7ae452b7bffb9e204c3a016e80caeecf46f554d112644f36fa114dac6ffa";
    //withdraw(uint256 amount)
    public static final String WITHDRAW = "0x884edad9ce6fa2440d8a54cc123490eb96d2768479d49ff9c7366125a9424364";

    public static final Map<String, String> topicToMethodId = new HashMap<>();
    public static final Map<String, String> methodIdToTopic = new HashMap<>();

    static {
        topicToMethodId.put(ADD_VAULT_AND_STRATEGY, "0xed32fa33");
        topicToMethodId.put(EXIT, "0xe9fad8ee");
        topicToMethodId.put(STAKE, "0xa694fc3a");
//        topicToMethodId.put(STAKE, "0x095ea7b3");
        topicToMethodId.put(DEPOSIT_ALL, "0x3cb97237");
        topicToMethodId.put(MIGRATE_IN_ONE_TX, "0x5fe71f67");
        topicToMethodId.put(WITHDRAW, "0x2e1a7d4d");

        methodIdToTopic.put("0x3cb97237", DEPOSIT_ALL);
        methodIdToTopic.put("0x853828b6", "withdrawAll");
        methodIdToTopic.put("0xb6b55f25", "deposit");
        methodIdToTopic.put("0x2e1a7d4d", WITHDRAW);
        methodIdToTopic.put("0xa694fc3a", STAKE);
        methodIdToTopic.put("0xe9fad8ee", EXIT);
    }

}
