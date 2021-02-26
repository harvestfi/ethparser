package pro.belbix.ethparser.web3.contracts;

public enum ContractType {
    VAULT(0),
    POOL(1),
    UNI_PAIR(2),
    INFRASTRUCTURE(3),
    TOKEN(4);
    private final int id;

    ContractType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
