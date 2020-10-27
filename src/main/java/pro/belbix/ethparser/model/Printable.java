package pro.belbix.ethparser.model;

public class Printable {

    private String type;
    private String coin;
    private double amount;
    private String otherCoin;
    private double otherAmount;
    private String hash;
    private boolean confirmed = false;

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

}
