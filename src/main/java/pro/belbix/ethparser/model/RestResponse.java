package pro.belbix.ethparser.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonRawValue;

@Data
@NoArgsConstructor
public class RestResponse {

    @JsonRawValue
    private String data;
    private String code;
    private String status;
    @JsonInclude(Include.NON_NULL)
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
