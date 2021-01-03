package pro.belbix.ethparser.web3.uniswap;

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
        "bind"
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

}
