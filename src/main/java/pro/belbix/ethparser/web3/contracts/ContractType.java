package pro.belbix.ethparser.web3.contracts;

public enum ContractType {
  UNKNOWN(-1),
  VAULT(0),
  POOL(1),
  UNI_PAIR(2),
  INFRASTRUCTURE(3),
  TOKEN(4),
  STRATEGY(5);
  private final int id;

  ContractType(int id) {
    this.id = id;
  }

  public int getId() {
    return id;
  }

  public static ContractType valueOfId(int id) {
    for (ContractType type : values()) {
      if (type.id == id) {
        return type;
      }
    }
    throw new IllegalStateException("Id " + id + " not found");
  }
}
