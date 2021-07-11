package pro.belbix.ethparser.swaggerConfig;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfigSwagger {

  @Bean
  public OpenAPI customOpenApi(@Value("1.0.0")String appVersion) {
    return new OpenAPI().info(new Info().title("Ethparser Swagger API")
        .version(appVersion))
        .servers(List.of(new Server().url("https://ethparser-api.herokuapp.com")
                .description("Prod server"),
            new Server().url("https://ethparser-stage.harvestfi.builders")
                .description("Stage server"),
            new Server().url("http://localhost:8082")
                .description("Local server")));
  }

}
