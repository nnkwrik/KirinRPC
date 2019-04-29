package io.github.nnkwrik.kirinrpc.common.util;

import java.net.InetAddress;
import java.util.regex.Pattern;

/**
 * @author nnkwrik
 * @date 19/04/29 13:44
 */
public class NetUtils {

    public static final String LOCALHOST_KEY = "localhost";
    public static final String ANYHOST_VALUE = "0.0.0.0";

    private static final Pattern LOCAL_IP_PATTERN = Pattern.compile("127(\\.\\d{1,3}){3}$");

    public static boolean isInvalidLocalHost(String host) {
        return host == null
                || host.length() == 0
                || host.equalsIgnoreCase(LOCALHOST_KEY)
                || host.equals(ANYHOST_VALUE)
                || (LOCAL_IP_PATTERN.matcher(host).matches());
    }

    public static boolean isValidLocalHost(String host) {
        return !isInvalidLocalHost(host);
    }

}
