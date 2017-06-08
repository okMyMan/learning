package com.infinity.codec;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLDecoder;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class ARC4Util {

    public static byte[] encode(byte[] key, byte[] data) {
        try {
            Cipher cipher = Cipher.getInstance("RC4");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "RC4");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            return cipher.update(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] decode(byte[] key, byte[] data) {
        try {
            Cipher cipher = Cipher.getInstance("RC4");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "RC4");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            return cipher.update(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param args
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     */
    public static void main(String[] args) throws Exception {
        String ss = URLDecoder.decode("\\xF0\\x9F\\x92\\x8B", "utf-8");

        List<Integer> userExtVOs = new ArrayList<>();
        for (int i = 1; i < 1031; i++) {
            userExtVOs.add(i);
        }

        int size = 100;

        // 计算出分几次处理
        int round = userExtVOs.size() / size + 1;

        for (int i = 0; i < round; i++) {
            int end = i * size + size;
            if (end>userExtVOs.size()) {
                end=userExtVOs.size();
            }
            for (int j = i * size; j<end;j++) {
                System.out.println(userExtVOs.get(j));
            }
        }

    }

}
