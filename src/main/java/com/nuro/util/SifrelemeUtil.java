package com.nuro.util;
import org.mindrot.jbcrypt.BCrypt;
public class SifrelemeUtil {
    public static String hashSifre(String sifre) {
        return BCrypt.hashpw(sifre, BCrypt.gensalt());
    }

    public static boolean dogrula(String girilen, String hashli) {
        return BCrypt.checkpw(girilen, hashli);
    }
}
