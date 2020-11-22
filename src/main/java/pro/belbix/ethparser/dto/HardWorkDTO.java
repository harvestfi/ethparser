package pro.belbix.ethparser.dto;

import java.time.Instant;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@Table(name = "hard_work", indexes = {
    @Index(name = "idx_hard_work", columnList = "blockDate")
})
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

    public String print() {
        return Instant.ofEpochSecond(blockDate) + " "
            + vault + " "
            + shareChangeUsd + " "
            + shareUsdTotal + " "
            + id;

    }

    public double getTvl() {
        return tvl;
    }

    public void setTvl(double tvl) {
        this.tvl = tvl;
    }

    public double getShareUsdTotal() {
        return shareUsdTotal;
    }

    public void setShareUsdTotal(double shareUsdTotal) {
        this.shareUsdTotal = shareUsdTotal;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVault() {
        return vault;
    }

    public void setVault(String vault) {
        this.vault = vault;
    }

    public long getBlock() {
        return block;
    }

    public void setBlock(long block) {
        this.block = block;
    }

    public long getBlockDate() {
        return blockDate;
    }

    public void setBlockDate(long blockDate) {
        this.blockDate = blockDate;
    }

    public double getShareChange() {
        return shareChange;
    }

    public void setShareChange(double shareChange) {
        this.shareChange = shareChange;
    }

    public double getShareChangeUsd() {
        return shareChangeUsd;
    }

    public void setShareChangeUsd(double shareChangeUsd) {
        this.shareChangeUsd = shareChangeUsd;
    }
}
