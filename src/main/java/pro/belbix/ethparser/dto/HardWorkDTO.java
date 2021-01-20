package pro.belbix.ethparser.dto;

import java.time.Instant;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "hard_work", indexes = {
    @Index(name = "idx_hard_work", columnList = "blockDate")
})
@Data
public class HardWorkDTO implements DtoI {

    @Id
    private String id;
    private String vault;
    private long block;
    private long blockDate;
    private double shareChange;
    private double shareChangeUsd;
    private double shareUsdTotal;
    private double tvl;
    private double allProfit;
    private long periodOfWork;
    private long psPeriodOfWork;
    private double perc;
    private double apr;
    private double weeklyProfit;
    private double weeklyAllProfit;
    private double psTvlUsd;
    private double psApr;
    private double farmBuyback;
    private double farmBuybackSum;
    private int callsQuantity;
    private int poolUsers;
    private double savedGasFees;
    private double savedGasFeesSum;
    private double fee;
    private Double weeklyAverageTvl;

    public String print() {
        return Instant.ofEpochSecond(blockDate) + " "
            + vault + " "
            + shareChangeUsd + " "
            + shareUsdTotal + " "
            + id;

    }
}
