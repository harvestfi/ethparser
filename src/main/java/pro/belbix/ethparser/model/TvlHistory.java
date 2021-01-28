package pro.belbix.ethparser.model;

import lombok.Data;

@Data
public class TvlHistory {

    private long calculateTime;
    private double lastTvl;
    private double sharePrice;
    private int lastOwnersCount;
    private double lastPrice;
    private double lastTvlNative;
}
