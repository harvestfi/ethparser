package pro.belbix.ethparser.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.web3j.tuples.generated.Tuple2;
import pro.belbix.ethparser.web3.uniswap.contracts.LpContracts;

public class LpStat {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private String coin1;
    private String coin2;
    private Double amount1;
    private Double amount2;

    public static String createJson(String lpHash, double firstCoinAmount, double secondCoinAmount) {
        try {
            Tuple2<String, String> coinNames = LpContracts.lpHashToCoinNames.get(lpHash);
            LpStat lpStat = new LpStat();
            lpStat.setCoin1(coinNames.component1());
            lpStat.setCoin2(coinNames.component2());
            lpStat.setAmount1(firstCoinAmount);
            lpStat.setAmount2(secondCoinAmount);
            return OBJECT_MAPPER.writeValueAsString(lpStat);
        } catch (JsonProcessingException ignored) {
        }
        return null;
    }

    public String getCoin1() {
        return coin1;
    }

    public void setCoin1(String coin1) {
        this.coin1 = coin1;
    }

    public String getCoin2() {
        return coin2;
    }

    public void setCoin2(String coin2) {
        this.coin2 = coin2;
    }

    public Double getAmount1() {
        return amount1;
    }

    public void setAmount1(Double amount1) {
        this.amount1 = amount1;
    }

    public Double getAmount2() {
        return amount2;
    }

    public void setAmount2(Double amount2) {
        this.amount2 = amount2;
    }
}
