package pro.belbix.ethparser.web3.erc20;

import pro.belbix.ethparser.dto.TransferDTO;
import pro.belbix.ethparser.web3.harvest.contracts.StakeContracts;
import pro.belbix.ethparser.web3.harvest.contracts.Vaults;
import pro.belbix.ethparser.web3.uniswap.MethodMapper;
import pro.belbix.ethparser.web3.uniswap.contracts.LpContracts;

public enum TransferType {
    // unknown actions and transfers
    COMMON,

    //uni/sushi lp
    LP_SEND,
    LP_RECEIVE,

    LP_BUY,
    LP_SELL,

    LP_ADD,
    LP_REM,

    // balancer
    BAL_ADD,
    BAL_REM,
    BAL_TX, //unknown

    // ps actions
    PS_STAKE,
    PS_EXIT,
    PS_INTERNAL,

    // harvests
    NOTIFY,
    MINT,
    REWARD,
    HARD_WORK;

    private static final String FEE_REWARD_FORWARDER = "0x9397bd6fB1EC46B7860C8073D2cb83BE34270D94".toLowerCase();

    public static String mapType(TransferDTO dto) {
        String recipient = dto.getRecipient().toLowerCase();
        String owner = dto.getOwner().toLowerCase();
        String methodName = dto.getMethodName();

        if (Vaults.isPsHash(recipient)) {
            if (StakeContracts.isST_PS(owner)) {
                return PS_INTERNAL.name();
            } else {
                return PS_STAKE.name();
            }
        }

        if (Vaults.isPsHash(owner)) {
            if (StakeContracts.isST_PS(recipient)) {
                return PS_INTERNAL.name();
            } else {
                return PS_EXIT.name();
            }
        }

        if (StakeContracts.hashToName.containsKey(recipient)) {
            return NOTIFY.name();
        }

        if (StakeContracts.hashToName.containsKey(owner)) {
            return REWARD.name();
        }

        if (LpContracts.lpHashToName.containsKey(owner)) {
            if (MethodMapper.isLpTrade(methodName)) {
                return LP_BUY.name();
            } else if (MethodMapper.isLpLiq(methodName)) {
                return LP_REM.name();
            } else {
                return LP_RECEIVE.name();
            }
        }

        if (LpContracts.lpHashToName.containsKey(recipient)) {
            if (MethodMapper.isLpTrade(methodName)) {
                return LP_SELL.name();
            } else if (MethodMapper.isLpLiq(methodName)) {
                return LP_ADD.name();
            } else {
                return LP_SEND.name();
            }
        }

        if(MethodMapper.isBalancerMethod(methodName)) {
            return BAL_TX.name();
        }

        if (FEE_REWARD_FORWARDER.equals(recipient) || FEE_REWARD_FORWARDER.equals(owner)) {
            return HARD_WORK.name();
        }

        if ("mint".equals(methodName)) {
            return MINT.name();
        }

        return COMMON.name();

    }
}
