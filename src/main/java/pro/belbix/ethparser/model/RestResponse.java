package pro.belbix.ethparser.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RestResponse {

    private String data;
    private String code;
    private String status;

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

}
