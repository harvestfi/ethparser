package pro.belbix.ethparser.utils;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.repositories.HarvestTvlRepository;

@SpringBootApplication
public class AppUtils {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        context.getBean(UtilsStarter.class).startUtils();
        context.close();
        System.exit(0);
    }
}
