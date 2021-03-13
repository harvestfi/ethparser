package pro.belbix.ethparser.utils.recalculation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import pro.belbix.ethparser.Application;

@SpringBootApplication
public class AppUtils {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args).getBean(UtilsStarter.class).startUtils();
  }

}
