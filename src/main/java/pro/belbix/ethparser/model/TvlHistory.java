package pro.belbix.ethparser.model;

import lombok.Data;

@Data
public class TvlHistory {

    private Long calculateTime;
    private Double lastTvl;
    private Double sharePrice;
    private Integer lastOwnersCount;
    private Double lastPrice;
    private Double lastTvlNative;
}
