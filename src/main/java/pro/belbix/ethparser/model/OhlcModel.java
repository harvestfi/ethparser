package pro.belbix.ethparser.model;

import lombok.Data;
import pro.belbix.ethparser.repositories.v0.UniswapRepository;

@Data
public class OhlcModel implements UniswapRepository.OhlcProjection {

    private long timestamp;
    private double open;
    private double high;
    private double low;
    private double close;
    private double volume;
}
