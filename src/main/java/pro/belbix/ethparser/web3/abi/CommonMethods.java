package pro.belbix.ethparser.web3.abi;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Type;
import pro.belbix.ethparser.web3.abi.DynamicStructures;
import pro.belbix.ethparser.web3.deployer.decoder.DeployerActivityEnum;

// todo refactoring
@SuppressWarnings({"unchecked", "rawtypes"})
public class CommonMethods {

    public static Map<String, List<TypeReference<Type>>> getMethods() throws ClassNotFoundException {
        Map<String, List<TypeReference<Type>>> parameters = new HashMap<>();

        parameters.put("SmartContractRecorded",
            Arrays.asList(
                TypeReference.makeTypeReference("address", true, false),
                TypeReference.makeTypeReference("address", true, false)
            ));
        parameters.put("addVaultAndStrategy",
            Arrays.asList(
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("address")

            ));
        parameters.put("exit", Collections.emptyList());
        parameters.put("stake",
            Collections.singletonList(
                TypeReference.makeTypeReference("uint256")

            ));
        parameters.put("migrateInOneTx",
            Arrays.asList(
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("address")

            ));
      parameters.put("Conversion",
          Arrays.asList(
              TypeReference.makeTypeReference("address", true, false),
              TypeReference.makeTypeReference("address", true, false),
              TypeReference.makeTypeReference("address", true, false),
              TypeReference.makeTypeReference("uint256"),
              TypeReference.makeTypeReference("uint256"),
              TypeReference.makeTypeReference("address")

          ));
        parameters.put("Withdraw",
            Arrays.asList(
                TypeReference.makeTypeReference("address", true, false),
                TypeReference.makeTypeReference("uint256")
            ));
        parameters.put("Deposit",
            Arrays.asList(
                TypeReference.makeTypeReference("address", true, false),
                TypeReference.makeTypeReference("uint256")
            ));
        parameters.put("Invest",
            Collections.singletonList(
                TypeReference.makeTypeReference("uint256")
            ));
        parameters.put("StrategyAnnounced",
            Arrays.asList(
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("uint256")
            ));
        parameters.put("StrategyChanged",
            Arrays.asList(
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("address")
            ));
        parameters.put("Staked",
            Arrays.asList(
                TypeReference.makeTypeReference("address", true, false),
                TypeReference.makeTypeReference("uint256")
            ));
        parameters.put("Withdrawn",
            Arrays.asList(
                TypeReference.makeTypeReference("address", true, false),
                TypeReference.makeTypeReference("uint256")
            ));
        parameters.put("RewardPaid",
            Arrays.asList(
                TypeReference.makeTypeReference("address", true, false),
                TypeReference.makeTypeReference("uint256")
            ));
        parameters.put("RewardPaid#V2",
            Arrays.asList(
                TypeReference.makeTypeReference("address", true, false),
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("uint256")
            ));
        parameters.put("RewardAdded",
            Collections.singletonList(
                TypeReference.makeTypeReference("uint256")
            ));
        parameters.put("RewardAdded#V2",
            Arrays.asList(
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("uint256")
            ));
        parameters.put("Migrated",
            Arrays.asList(
                TypeReference.makeTypeReference("address", true, false),
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("uint256")
            ));
        parameters.put("OwnershipTransferred",
            Arrays.asList(
                TypeReference.makeTypeReference("address", true, false),
                TypeReference.makeTypeReference("address", true, false)
            ));
        parameters.put("Staked#V2",
            Arrays.asList(
                TypeReference.makeTypeReference("address", true, false),
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("uint256")
            ));
        parameters.put("Withdraw#V2",
            Arrays.asList(
                TypeReference.makeTypeReference("address", true, false),
                TypeReference.makeTypeReference("uint256", true, false),
                TypeReference.makeTypeReference("uint256")
            ));
        parameters.put("ProfitLogInReward",
            Arrays.asList(
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("uint256")
            ));
        parameters.put("SharePriceChangeLog",
            Arrays.asList(
                TypeReference.makeTypeReference("address", true, false),
                TypeReference.makeTypeReference("address", true, false),
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("uint256")
            ));
        parameters.put("Deposit#V2",
            Arrays.asList(
                TypeReference.makeTypeReference("address", true, false),
                TypeReference.makeTypeReference("uint256", true, false),
                TypeReference.makeTypeReference("uint256")
            ));
        parameters.put("Rewarded",
            Arrays.asList(
                TypeReference.makeTypeReference("address", true, false),
                TypeReference.makeTypeReference("address", true, false),
                TypeReference.makeTypeReference("uint256")
            ));
        parameters.put("underlyingBalanceInVault", Collections.emptyList());
        parameters.put("underlyingBalanceWithInvestment", Collections.emptyList());
        parameters.put("governance", Collections.emptyList());
        parameters.put("controller", Collections.emptyList());
        parameters.put("underlying", Collections.emptyList());
        parameters.put("strategy", Collections.emptyList());
        parameters.put("withdrawAll", Collections.emptyList());
        parameters.put("getPricePerFullShare", Collections.emptyList());
        parameters.put("doHardWork", Collections.emptyList());
        parameters.put("rebalance", Collections.emptyList());
        parameters.put("setStrategy",
            Collections.singletonList(
                TypeReference.makeTypeReference("address")
            ));
        parameters.put("setVaultFractionToInvest",
            Arrays.asList(
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("uint256")

            ));
        parameters.put("deposit",
            Collections.singletonList(
                TypeReference.makeTypeReference("uint256")

            ));
        parameters.put("depositFor",
            Arrays.asList(
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("address")

            ));
        parameters.put("withdraw",
            Collections.singletonList(
                TypeReference.makeTypeReference("uint256")

            ));
        parameters.put("underlyingBalanceWithInvestmentForHolder",
            Collections.singletonList(
                TypeReference.makeTypeReference("address")

            ));
        parameters.put("depositAll",
            Arrays.asList(
                TypeReference.makeTypeReference("uint256[]"),
                TypeReference.makeTypeReference("address[]")

            ));
        parameters.put("approve",
            Arrays.asList(
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("uint256")

            ));
        parameters.put("Swap",
            Arrays.asList(
                TypeReference.makeTypeReference("address", true, false),
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("address", true, false)
            ));
        parameters.put("Mint",
            Arrays.asList(
                TypeReference.makeTypeReference("address", true, false),
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("uint256")
            ));
        parameters.put("Burn",
            Arrays.asList(
                TypeReference.makeTypeReference("address", true, false),
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("address", true, false)
            ));
        parameters.put("Sync",
            Arrays.asList(
                TypeReference.makeTypeReference("uint112"),
                TypeReference.makeTypeReference("uint112")
            ));
        parameters.put("Approval",
            Arrays.asList(
                TypeReference.makeTypeReference("address", true, false),
                TypeReference.makeTypeReference("address", true, false),
                TypeReference.makeTypeReference("uint256")
            ));
        parameters.put("Transfer",
            Arrays.asList(
                TypeReference.makeTypeReference("address", true, false),
                TypeReference.makeTypeReference("address", true, false),
                TypeReference.makeTypeReference("uint256")
            ));
        parameters.put("addLiquidity",
            Arrays.asList(
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("uint256")

            ));
        parameters.put("addLiquidityETH",
            Arrays.asList(
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("uint256")

            ));
        parameters.put("removeLiquidity",
            Arrays.asList(
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("uint256")

            ));
        parameters.put("removeLiquidityETH",
            Arrays.asList(
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("uint256")

            ));
        parameters.put("removeLiquidityWithPermit",
            Arrays.asList(
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("bool"),
                TypeReference.makeTypeReference("uint8"),
                TypeReference.makeTypeReference("bytes32"),
                TypeReference.makeTypeReference("bytes32")
            ));
        parameters.put("removeLiquidityETHWithPermit",
            Arrays.asList(
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("bool"),
                TypeReference.makeTypeReference("uint8"),
                TypeReference.makeTypeReference("bytes32"),
                TypeReference.makeTypeReference("bytes32")
            ));
        parameters.put("removeLiquidityETHSupportingFeeOnTransferTokens",
            Arrays.asList(
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("uint256")
            ));
        parameters.put("removeLiquidityETHWithPermitSupportingFeeOnTransferTokens",
            Arrays.asList(
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("bool"),
                TypeReference.makeTypeReference("uint8"),
                TypeReference.makeTypeReference("bytes32"),
                TypeReference.makeTypeReference("bytes32")
            ));
        parameters.put("swapExactTokensForTokens",
            Arrays.asList(
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("address[]"),
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("uint256")
            ));
        parameters.put("swapTokensForExactTokens",
            Arrays.asList(
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("address[]"),
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("uint256")
            ));
        parameters.put("swapExactETHForTokens",
            Arrays.asList(
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("address[]"),
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("uint256")
            ));
        parameters.put("swapTokensForExactETH",
            Arrays.asList(
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("address[]"),
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("uint256")
            ));
        parameters.put("swapExactTokensForETH",
            Arrays.asList(
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("address[]"),
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("uint256")
            ));
        parameters.put("swapETHForExactTokens",
            Arrays.asList(
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("address[]"),
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("uint256")
            ));
        parameters.put("swapExactTokensForTokensSupportingFeeOnTransferTokens",
            Arrays.asList(
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("address[]"),
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("uint256")
            ));
        parameters.put("swapExactETHForTokensSupportingFeeOnTransferTokens",
            Arrays.asList(
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("address[]"),
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("uint256")
            ));
        parameters.put("swapExactTokensForETHSupportingFeeOnTransferTokens",
            Arrays.asList(
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("address[]"),
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("uint256")
            ));
        parameters.put("RewardDenied",
            Arrays.asList(
                TypeReference.makeTypeReference("address", true, false),
                TypeReference.makeTypeReference("uint256")
            ));
        parameters.put("UpdateLiquidityLimit",
            Arrays.asList(
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("uint256")
            ));
        parameters.put("transfer",
            Arrays.asList(
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("uint256")
            ));
        parameters.put("mint",
            Arrays.asList(
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("uint256")
            ));
        parameters.put("execute",
            Arrays.asList(
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("bytes")
            ));
        parameters.put("addMinter",
            Collections.singletonList(
                TypeReference.makeTypeReference("address")
            ));
        parameters.put("allowance",
            Arrays.asList(
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("address")
            ));
        parameters.put("transferFrom",
            Arrays.asList(
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("uint256")
            ));
        parameters.put("increaseAllowance",
            Arrays.asList(
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("uint256")
            ));
        parameters.put("decreaseAllowance",
            Arrays.asList(
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("uint256")
            ));
        parameters.put("setStorage",
            Collections.singletonList(
                TypeReference.makeTypeReference("address")
            ));
        parameters.put("getReward", Collections.emptyList());
        parameters.put("delegate", Collections.emptyList());
        parameters.put("swap",
            Arrays.asList(
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("address[]"),
                TypeReference.makeTypeReference("bytes"),
                TypeReference.makeTypeReference("uint256[]"),
                TypeReference.makeTypeReference("uint256[]")
            ));
        parameters.put("ZapIn",
            Arrays.asList(
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("uint256")
            ));
        parameters.put("bind",
            Arrays.asList(
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("uint"),
                TypeReference.makeTypeReference("uint")
            ));
        parameters.put("rebind",
            Arrays.asList(
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("uint"),
                TypeReference.makeTypeReference("uint")
            ));
        parameters.put("unbind",
            Collections.singletonList(
                TypeReference.makeTypeReference("address")
            ));
        parameters.put("gulp",
            Collections.singletonList(
                TypeReference.makeTypeReference("address")
            ));
        parameters.put("swapExactAmountIn",
            Arrays.asList(
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("uint"),
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("uint"),
                TypeReference.makeTypeReference("uint")
            ));
        parameters.put("swapExactAmountOut",
            Arrays.asList(
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("uint"),
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("uint"),
                TypeReference.makeTypeReference("uint")
            ));
        parameters.put("joinswapExternAmountIn",
            Arrays.asList(
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("uint"),
                TypeReference.makeTypeReference("uint")
            ));
        parameters.put("joinswapPoolAmountOut",
            Arrays.asList(
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("uint"),
                TypeReference.makeTypeReference("uint")
            ));
        parameters.put("exitswapPoolAmountIn",
            Arrays.asList(
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("uint"),
                TypeReference.makeTypeReference("uint")
            ));
        parameters.put("exitswapExternAmountOut",
            Arrays.asList(
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("uint"),
                TypeReference.makeTypeReference("uint")
            ));
        parameters.put("batchSwapExactIn",
            Arrays.asList(
                DynamicStructures.swapTypeReference(),
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("uint"),
                TypeReference.makeTypeReference("uint")
            ));
        parameters.put("batchSwapExactOut",
            Arrays.asList(
                DynamicStructures.swapTypeReference(),
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("uint")
            ));
        parameters.put("batchEthInSwapExactIn",
            Arrays.asList(
                DynamicStructures.swapTypeReference(),
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("uint")
            ));
        parameters.put("batchEthOutSwapExactIn",
            Arrays.asList(
                DynamicStructures.swapTypeReference(),
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("uint"),
                TypeReference.makeTypeReference("uint")
            ));
        parameters.put("batchEthInSwapExactOut",
            Arrays.asList(
                DynamicStructures.swapTypeReference(),
                TypeReference.makeTypeReference("address")
            ));
        parameters.put("batchEthOutSwapExactOut",
            Arrays.asList(
                DynamicStructures.swapTypeReference(),
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("uint")
            ));
        parameters.put("doHardWork#V2",
            Collections.singletonList(
                TypeReference.makeTypeReference("address")
            ));
        parameters.put("viewSplitExactOut",
            Arrays.asList(
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("uint"),
                TypeReference.makeTypeReference("uint")
            ));
        parameters.put("viewSplitExactIn",
            Arrays.asList(
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("uint"),
                TypeReference.makeTypeReference("uint")
            ));
        parameters.put("smartSwapExactOut",
            Arrays.asList(
                TypeReference.makeTypeReference("address"), //TokenInterface
                TypeReference.makeTypeReference("address"), //TokenInterface
                TypeReference.makeTypeReference("uint"),
                TypeReference.makeTypeReference("uint"),
                TypeReference.makeTypeReference("uint")
            ));
        parameters.put("smartSwapExactIn",
            Arrays.asList(
                TypeReference.makeTypeReference("address"), //TokenInterface
                TypeReference.makeTypeReference("address"), //TokenInterface
                TypeReference.makeTypeReference("uint"),
                TypeReference.makeTypeReference("uint"),
                TypeReference.makeTypeReference("uint")
            ));
        parameters.put("multihopBatchSwapExactOut",
            Arrays.asList(
                DynamicStructures.swapTypeReferenceDoubleArray(),
                TypeReference.makeTypeReference("address"), //TokenInterface
                TypeReference.makeTypeReference("address"), //TokenInterface
                TypeReference.makeTypeReference("uint")
            ));
        parameters.put("multihopBatchSwapExactIn",
            Arrays.asList(
                DynamicStructures.swapTypeReferenceDoubleArray(),
                TypeReference.makeTypeReference("address"), //TokenInterface
                TypeReference.makeTypeReference("address"), //TokenInterface
                TypeReference.makeTypeReference("uint"),
                TypeReference.makeTypeReference("uint")
            ));
        parameters.put("notifyPoolsIncludingProfitShare",
            Arrays.asList(
                TypeReference.makeTypeReference("uint256[]"),
                TypeReference.makeTypeReference("address[]"),
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("uint256")
            ));
        parameters.put("notifyProfitSharing", Collections.emptyList());
        parameters.put("provideLoan", Collections.emptyList());
        parameters.put("withdrawAllToVault", Collections.emptyList());
        parameters.put("tend", Collections.emptyList());
        parameters.put("harvest", Collections.emptyList());
        parameters.put("notifyPools",
            Arrays.asList(
                TypeReference.makeTypeReference("uint256[]"),
                TypeReference.makeTypeReference("address[]"),
                TypeReference.makeTypeReference("uint256")
            ));
        parameters.put("poolNotifyFixedTarget",
            Arrays.asList(
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("uint256")
            ));
        parameters.put("sellToUniswap",
            Arrays.asList(
                TypeReference.makeTypeReference("address[]"),
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("uint256"),
                TypeReference.makeTypeReference("bool")
            ));
        parameters.put("executeMint",
            Collections.singletonList(
                TypeReference.makeTypeReference("uint256")
            ));
        parameters.put(DeployerActivityEnum.SET_FEE_REWARD_FORWARDER.getMethodName(),
            Collections.singletonList(
                TypeReference.makeTypeReference("address")
            ));
        parameters.put(DeployerActivityEnum.SET_REWARD_DISTRIBUTION.getMethodName(),
            Collections.singletonList(
                TypeReference.makeTypeReference("address")
            ));
        parameters.put(DeployerActivityEnum.SET_PATH.getMethodName(),
            Arrays.asList(
                TypeReference.makeTypeReference("bytes32"),
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("address[]")
            ));
        parameters.put(DeployerActivityEnum.SET_LIQUIDITY_LOAN_TARGET.getMethodName(),
            Collections.singletonList(
                TypeReference.makeTypeReference("uint256")
            ));
        parameters.put(DeployerActivityEnum.SETTLE_LOAN.getMethodName(),
            Collections.singletonList(
                TypeReference.makeTypeReference("uint256")
            ));
        parameters.put(DeployerActivityEnum.ADD_DEX.getMethodName(),
            Arrays.asList(
                TypeReference.makeTypeReference("bytes32"),
                TypeReference.makeTypeReference("address")
            ));
        parameters.put(DeployerActivityEnum.SET_CONTROLLER.getMethodName(),
            Collections.singletonList(
                TypeReference.makeTypeReference("address")
            ));
        parameters.put(DeployerActivityEnum.SET_HARD_REWARDS.getMethodName(),
            Collections.singletonList(
                TypeReference.makeTypeReference("address")
            ));
        parameters.put(DeployerActivityEnum.ADD_VAULT.getMethodName(),
            Collections.singletonList(
                TypeReference.makeTypeReference("address")
            ));
        parameters.put(DeployerActivityEnum.SET_TOKEN_POOL.getMethodName(),
            Collections.singletonList(
                TypeReference.makeTypeReference("address")
            ));
        parameters.put(DeployerActivityEnum.SET_OPERATOR.getMethodName(),
            Collections.singletonList(
                TypeReference.makeTypeReference("address")
            ));
        parameters.put(DeployerActivityEnum.SET_TEAM.getMethodName(),
            Collections.singletonList(
                TypeReference.makeTypeReference("address")
            ));
        parameters.put(DeployerActivityEnum.NOTIFY_REWARD_AMOUNT.getMethodName(),
            Collections.singletonList(
                TypeReference.makeTypeReference("uint256")
            ));
        parameters.put(DeployerActivityEnum.RENOUNCE_MINTER.getMethodName(),
            Collections.emptyList());
        parameters.put(DeployerActivityEnum.ADD_HARD_WORKER.getMethodName(),
            Collections.singletonList(
                TypeReference.makeTypeReference("address")
            ));
        parameters.put(DeployerActivityEnum.SET_CONVERSION_PATH.getMethodName(),
            Arrays.asList(
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("address[]")
            ));
        parameters.put("Minted",
            Arrays.asList(
                TypeReference.makeTypeReference("address", true, false),
                TypeReference.makeTypeReference("address"),
                TypeReference.makeTypeReference("uint256")
            ));
        parameters.put("DistributedSupplierComp",
            Arrays.asList(
                TypeReference.makeTypeReference("address", true, false),
                TypeReference.makeTypeReference("address", true, false),
                TypeReference.makeTypeReference("uint"),
                TypeReference.makeTypeReference("uint")
            ));
        parameters.put("Claimed",
            Arrays.asList(
                TypeReference.makeTypeReference("address", true, false),
                TypeReference.makeTypeReference("uint256")
            ));
        return parameters;
    }

}
