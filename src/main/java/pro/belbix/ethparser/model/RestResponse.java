package pro.belbix.ethparser.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RestResponse {

    private String data;
    private String code;
    private String status;
    private Long block;

    public RestResponse(String data, String code, String status) {
        this.data = data;
        this.code = code;
        this.status = status;
    }

    public static RestResponse error(String status) {
        return new RestResponse(
            "{}",
            "500",
            status
        );
    }

    public static RestResponse ok(String data) {
        return new RestResponse(
            data,
            "200",
            "OK"
        );
    }

    public RestResponse addBlock(long block) {
        this.block = block;
        return this;
    }

}
