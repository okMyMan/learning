package com.infinity.codec;

import com.alibaba.fastjson.JSON;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;

/**
 * md5生成
 * 
 * @author M
 */
public class MD5Utils {
    public static String getMD5Str(String str) {

        return getMD5Str(str, "UTF-8");
    }

    public static String getMD5Str(Object object){
        try {
            String json = JSON.toJSONString(object);
            return getMD5Str(json, "UTF-8");
        } catch (Exception e) {
            throw e;
        }
    }
    
    public static String getMD5Str(String str, String encoding) {

        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(str.getBytes(encoding));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        byte[] byteArray = messageDigest.digest();

        StringBuffer md5StrBuff = new StringBuffer();

        for (int i = 0; i < byteArray.length; i++) {
            if (Integer.toHexString(0xFF & byteArray[i]).length() == 1) {
                md5StrBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
            } else {
                md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
            }
        }

        return md5StrBuff.toString();
    }

    public static void main(String[] sdg) throws ParseException {

        System.out.println(MD5Utils.getMD5Str("真知棒"));
    }
}
