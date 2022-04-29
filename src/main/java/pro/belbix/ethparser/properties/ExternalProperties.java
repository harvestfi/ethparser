package pro.belbix.ethparser.properties;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "external")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
public class ExternalProperties {
  ExternalApi covalenthq;
  ExternalApi harvest;

  @FieldDefaults(level = AccessLevel.PRIVATE)
  @Getter
  @Setter
  public static class ExternalApi {
    String url;
    String key;
  }

}
