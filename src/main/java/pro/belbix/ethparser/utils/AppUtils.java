package pro.belbix.ethparser.utils;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import pro.belbix.ethparser.Application;

public class AppUtils {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        context.getBean(UtilsStarter.class).startUtils();
        context.close();
        try {
            Thread.sleep(10000); //todo gentle exit
        } catch (InterruptedException ignored) {}
        System.exit(0);
    }
}
