package pro.belbix.ethparser.web3.erc20;

import static pro.belbix.ethparser.web3.ContractConstants.ZERO_ADDRESS;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import pro.belbix.ethparser.dto.TransferDTO;
import pro.belbix.ethparser.web3.MethodMapper;
import pro.belbix.ethparser.web3.contracts.StakeContracts;
import pro.belbix.ethparser.web3.contracts.Vaults;
import pro.belbix.ethparser.web3.contracts.LpContracts;

public enum TransferType {
    // all not specified transfers
    COMMON,

    //unknown contracts with high volume
    BOT,

    //exchanges
    ONE_INCH,
    ZERO_X,

    // friends
    BADGER,

    //uni/sushi lp
    LP_SEND, //unknown
    LP_RECEIVE, //unknown

    LP_BUY,
    LP_SELL,

    LP_ADD,
    LP_REM,

    // balancer
    BAL_TX, // balancer method decoder doesn't work, need to investigate

    // ps actions
    PS_STAKE,
    PS_EXIT,
    PS_INTERNAL,

    // harvests
    NOTIFY,
    MINT,
    REWARD,
    HARD_WORK;

    public static final Set<String> NOT_TRADE = new HashSet<>(Arrays.stream(TransferType.values())
        .filter(t -> t != LP_BUY && t != LP_SELL)
        .map(Enum::name)
        .collect(Collectors.toList()));
    public static final Set<String> KEEP_OWNERSHIP = new HashSet<>(Arrays.stream(TransferType.values())
        .filter(t ->
            t != PS_STAKE
                && t != PS_EXIT
                && t != LP_ADD
                && t != LP_REM
        )
        .map(Enum::name)
        .collect(Collectors.toList()));
    private static final String FEE_REWARD_FORWARDER = "0x9397bd6fB1EC46B7860C8073D2cb83BE34270D94".toLowerCase();

    public static TransferType getType(TransferDTO dto) {
        String recipient = dto.getRecipient().toLowerCase();
        String owner = dto.getOwner().toLowerCase();
        String methodName = dto.getMethodName();

        if (ZERO_ADDRESS.equals(owner)) {
            return MINT;
        }

        if (MethodMapper.isBadger(methodName)) {
            return BADGER;
        }

        if (MethodMapper.isOneInch(methodName)) {
            return ONE_INCH;
        }

        if (MethodMapper.is0x(methodName)) {
            return ZERO_X;
        }

        if (methodName.startsWith("doHardWork")) {
            return HARD_WORK;
        }

        if (Vaults.isPsHash(recipient)) {
            if (StakeContracts.isST_PS(owner)) {
                return PS_INTERNAL;
            } else {
                return PS_STAKE;
            }
        }

        if (Vaults.isPsHash(owner)) {
            // V0 reward
            if ("getReward".equalsIgnoreCase(methodName)) {
                return REWARD;
            }
            if (StakeContracts.isST_PS(recipient)) {
                return PS_INTERNAL;
            } else {
                return PS_EXIT;
            }
        }

        if (StakeContracts.hashToName.containsKey(recipient)) {
            return NOTIFY;
        }

        if (StakeContracts.hashToName.containsKey(owner) || MethodMapper.isReward(methodName)) {
            return REWARD;
        }

        if (LpContracts.lpHashToName.containsKey(owner)) {
            if (MethodMapper.isLpTrade(methodName)) {
                return LP_BUY;
            } else if (MethodMapper.isLpLiq(methodName)) {
                return LP_REM;
            } else {
                return LP_RECEIVE;
            }
        }

        if (LpContracts.lpHashToName.containsKey(recipient)) {
            if (MethodMapper.isLpTrade(methodName)) {
                return LP_SELL;
            } else if (MethodMapper.isLpLiq(methodName)) {
                return LP_ADD;
            } else {
                return LP_SEND;
            }
        }

        if (MethodMapper.isBalancerMethod(methodName)) {
            return BAL_TX;
        }

        if (FEE_REWARD_FORWARDER.equals(recipient) || FEE_REWARD_FORWARDER.equals(owner)) {
            return HARD_WORK;
        }

        if (MethodMapper.isBot(methodName)) {
            return BOT;
        }

        //we can have missed ST contracts
        if ("exit".equalsIgnoreCase(methodName)) {
            return REWARD;
        }

        return COMMON;
    }

    public boolean isUser() {
        return !(
            this == NOTIFY
                || this == MINT
                || this == HARD_WORK
                || this == PS_INTERNAL
                || this == BOT
        );
    }
}
