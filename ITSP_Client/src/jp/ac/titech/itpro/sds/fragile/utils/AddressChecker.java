package jp.ac.titech.itpro.sds.fragile.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class AddressChecker {
    public static boolean check(String address) {
        String ptnStr =
                "^[a-zA-Z0-9!#$%&'_`/=~\\*\\+\\-\\?\\^\\{\\|\\}]" +
                "+(\\.[a-zA-Z0-9!#$%&'_`/=~\\*\\+\\-\\?\\^\\{\\|\\}]+)*" +
                "@" +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]*(\\.[a-zA-Z0-9\\-]+)*$";
        Pattern ptn = Pattern.compile(ptnStr);
        Matcher mc = ptn.matcher(address); 
        return mc.matches();
    }
}
