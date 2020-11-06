package pro.belbix.ethparser.model;

public class LpStat {
    private String coin1;
    private String coin2;
    private Double amount1;
    private Double amount2;

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
