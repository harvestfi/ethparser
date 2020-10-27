package pro.belbix.ethparser.model;

import lombok.Data;

@Data
public class Printable {

    private String type;
    private String coin;
    private double amount;
    private String otherCoin;
    private double otherAmount;
    private String hash;
    private boolean uncomfirmed = true;

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
