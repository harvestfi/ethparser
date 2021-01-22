package pro.belbix.ethparser.utils;

public class CommonUtils {

    public static long parseLong(String from, long def) {
        if (from != null) {
            return Long.parseLong(from);
        }
        return def;
    }

}
