package pro.belbix.ethparser.model;

public class Printable {

    private String type;
    private String coin;
    private double amount;
    private String otherCoin;
    private double otherAmount;
    private double ethAmount;
    private String hash;
    private boolean confirmed = false;
    private double lastPrice;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCoin() {
        return coin;
    }

    public void setCoin(String coin) {
        this.coin = coin;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getOtherCoin() {
        return otherCoin;
    }

    public void setOtherCoin(String otherCoin) {
        this.otherCoin = otherCoin;
    }

    public double getOtherAmount() {
        return otherAmount;
    }

    public void setOtherAmount(double otherAmount) {
        this.otherAmount = otherAmount;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public double getEthAmount() {
        return ethAmount;
    }

    public void setEthAmount(double ethAmount) {
        this.ethAmount = ethAmount;
    }

    public double getLastPrice() {
        return lastPrice;
    }

    public void setLastPrice(double lastPrice) {
        this.lastPrice = lastPrice;
    }

    public String print() {
        String result = type + " "
            + String.format("%.2f", amount) + " "
            + coin + " for "
            + otherCoin + " "
            + String.format("%.2f", otherAmount)
            + " " + hash;

        if (amount > 1000.0) {
            result = "WARN " + result;
        }
        return result;
    }

    @Override
    public String toString() {
        return "Printable{" +
            "type='" + type + '\'' +
            ", coin='" + coin + '\'' +
            ", amount=" + amount +
            ", otherCoin='" + otherCoin + '\'' +
            ", otherAmount=" + otherAmount +
            ", ethAmount=" + ethAmount +
            ", hash='" + hash + '\'' +
            ", confirmed=" + confirmed +
            '}';
    }
}
