package pro.belbix.ethparser.entity;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@Table(name = "income", indexes = {
    @Index(name = "idx_income", columnList = "timestamp")
})
@Cacheable(false)
public class IncomeEntity {
    @Id
    private String id;
    private long timestamp;
    private double amount;
    private double amountUsd;
    private double amountSum;
    private double amountSumUsd;
    private double psTvl;
    private double psTvlUsd;
    private double perc;
    private double weekPerc;

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getAmountUsd() {
        return amountUsd;
    }

    public void setAmountUsd(double amountUsd) {
        this.amountUsd = amountUsd;
    }

    public double getAmountSum() {
        return amountSum;
    }

    public void setAmountSum(double amountSum) {
        this.amountSum = amountSum;
    }

    public double getAmountSumUsd() {
        return amountSumUsd;
    }

    public void setAmountSumUsd(double amountSumUsd) {
        this.amountSumUsd = amountSumUsd;
    }

    public double getPsTvl() {
        return psTvl;
    }

    public void setPsTvl(double psTvl) {
        this.psTvl = psTvl;
    }

    public double getPsTvlUsd() {
        return psTvlUsd;
    }

    public void setPsTvlUsd(double psTvlUsd) {
        this.psTvlUsd = psTvlUsd;
    }

    public double getPerc() {
        return perc;
    }

    public void setPerc(double perc) {
        this.perc = perc;
    }

    public double getWeekPerc() {
        return weekPerc;
    }

    public void setWeekPerc(double weekPerc) {
        this.weekPerc = weekPerc;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

}
