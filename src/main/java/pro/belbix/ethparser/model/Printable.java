package pro.belbix.ethparser.model;

import lombok.Data;

@Data
public class Printable {

    private String type;
    private String coin;
    private String amount;
    private String otherCoin;
    private String otherAmount;
    private String hash;

    public String print() {
        String result = type + " "
            + amount + " "
            + coin + " for "
            + otherCoin + " "
            + otherAmount
            + " " + hash;
        int amountInt = Integer.parseInt(amount);
        if (amountInt > 100 && amountInt < 1000) {
            result = "WARN" + result;
        } else if (amountInt > 1000) {
            result = "ERROR" + result;
        }
        return result;
    }

}
