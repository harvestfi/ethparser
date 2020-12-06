package pro.belbix.ethparser.utils;

import java.util.Optional;
import java.util.concurrent.Callable;

public class Caller {

    public static  <T> Optional<T> silentCall(Callable<T> callable) {
        try {
            T result = callable.call();
            if(result == null) {
                return Optional.empty();
            }
            return Optional.of(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

}
