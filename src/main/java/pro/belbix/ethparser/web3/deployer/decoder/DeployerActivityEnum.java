package pro.belbix.ethparser.web3.deployer.decoder;

public enum DeployerActivityEnum {
  UNKNOWN("UNKNOWN"),
  CONTRACT_CREATION("CONTRACT_CREATION"),
  NO_INPUT_DATA("NO_INPUT_DATA"),
  DO_HARD_WORK("doHardWork"),
  DO_HARD_WORK_V2("doHardWork#V2"),
  SET_FEE_REWARD_FORWARDER("setFeeRewardForwarder"),
  SET_LIQUIDITY_LOAN_TARGET("setLiquidityLoanTarget"),
  SET_REWARD_DISTRIBUTION("setRewardDistribution"),
  SET_PATH("setPath"),
  SETTLE_LOAN("settleLoan"),
  PROVIDE_LOAN("provideLoan"),
  ADD_DEX("addDex"),
  SET_STRATEGY("setStrategy"),
  SET_CONTROLLER("setController"),
  ADD_VAULT_AND_STRATEGY("addVaultAndStrategy"),
  SET_TOKEN_POOL("setTokenPool"),
  ADD_MINTER("addMinter"),
  ADD_VAULT("addVault"),
  SET_HARD_REWARDS("setHardRewards"),
  SET_OPERATOR("setOperator"),
  TRANSFER("transfer"),
  MINT("mint"),
  SET_TEAM("setTeam"),
  NOTIFY_REWARD_AMOUNT("notifyRewardAmount"),
  RENOUNCE_MINTER("renounceMinter"),
  ADD_HARD_WORKER("addHardWorker"),
  SET_CONVERSION_PATH("setConversionPath"),
  EXECUTE_MINT("executeMint"),
  SET_VAULT_FRACTION_TO_INVEST("setVaultFractionToInvest"),
  APPROVE("approve"),
  WITHDRAW_ALL("withdrawAll"),
  SET_STORAGE("setStorage"),
  NOTIFY_PROFIT_SHARING("notifyProfitSharing"),
  NOTIFY_POOLS_INCLUDING_PROFIT_SHARE("notifyPoolsIncludingProfitShare"),
  WITHDRAW_ALL_TO_VAULT("withdrawAllToVault"),
  MIGRATE_IN_ONE_TX("migrateInOneTx"),
  NOTIFY_POOLS("notifyPools");

  private final String methodName;

  DeployerActivityEnum(String methodName) {
    this.methodName = methodName;
  }

  public String getMethodName() {
    return methodName;
  }

  public static DeployerActivityEnum getEnumByMethodName(String methodName) {
    DeployerActivityEnum deployerActivityEnum = DeployerActivityEnum.UNKNOWN;
    for (DeployerActivityEnum daEnum : DeployerActivityEnum.values()) {
      if (daEnum.getMethodName().equalsIgnoreCase(methodName)) {
        deployerActivityEnum = daEnum;
        break;
      }
    }
    return deployerActivityEnum;
  }
}
