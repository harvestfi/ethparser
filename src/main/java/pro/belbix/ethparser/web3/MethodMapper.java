package pro.belbix.ethparser.web3;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MethodMapper {

    private static final Set<String> lpTradeMethods = new HashSet<>(Arrays.asList(
        "swapExactTokensForTokens",
        "swapExactETHForTokens",
        "swapTokensForExactTokens",
        "swapETHForExactTokens",
        "swapTokensForExactETH",
        "swapExactTokensForTokensSupportingFeeOnTransferTokens",
        "swapExactTokensForETHSupportingFeeOnTransferTokens",
        "swapExactETHForTokensSupportingFeeOnTransferTokens",
        "swapExactTokensForETH"
    ));

    private static final Set<String> lpLiqMethods = new HashSet<>(Arrays.asList(
        "addLiquidity",
        "removeLiquidityETHWithPermit",
        "addLiquidityETH",
        "removeLiquidity",
        "removeLiquidityETH",
        "removeLiquidityWithPermit"
    ));

    private static final Set<String> balancerMethods = new HashSet<>(Arrays.asList(
        "batchEthOutSwapExactOut",
        "batchEthInSwapExactOut",
        "batchEthOutSwapExactIn",
        "batchEthInSwapExactIn",
        "batchSwapExactOut",
        "batchSwapExactIn",
        "exitswapExternAmountOut",
        "exitswapPoolAmountIn",
        "joinswapPoolAmountOut",
        "joinswapExternAmountIn",
        "swapExactAmountOut",
        "swapExactAmountIn",
        "gulp",
        "unbind",
        "rebind",
        "bind",
        "execute",
        "0xb02f0b73",
        "0x86b2ecc4",
        "0xe2b39746"
    ));

    private static final Set<String> bots = new HashSet<>(Arrays.asList(
        "0x375e243b",
        "0xe8a21393",
        "0xc6dd594d"
    ));

    private static final Set<String> oneInch = new HashSet<>(Arrays.asList(
        "0x90411a32"
    ));

    private static final Set<String> zeroX = new HashSet<>(Arrays.asList(
        "0x5f575529",
        "sellToUniswap"
    ));

    private static final Set<String> rewards = new HashSet<>(Arrays.asList(
        "getReward"
    ));

    private static final Set<String> badger = new HashSet<>(Arrays.asList(
        "tend",
        "harvest"
    ));

    public static boolean isLpTrade(String methodName) {
        return lpTradeMethods.contains(methodName);
    }

    public static boolean isLpLiq(String methodName) {
        return lpLiqMethods.contains(methodName);
    }

    public static boolean isBalancerMethod(String methodName) {
        return balancerMethods.contains(methodName);
    }

    public static boolean isBot(String methodName) {
        return bots.contains(methodName);
    }

    public static boolean isOneInch(String methodName) {
        return oneInch.contains(methodName);
    }

    public static boolean is0x(String methodName) {
        return zeroX.contains(methodName);
    }

    public static boolean isReward(String methodName) {
        return rewards.contains(methodName);
    }

    public static boolean isBadger(String methodName) {
        return badger.contains(methodName);
    }
}
