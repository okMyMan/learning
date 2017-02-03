package com.ooteco.utils;

import com.ooteco.keyspace.space1.KeyValueSpace;
/**
 * Created by xule on 2017/1/13.
 * 模拟redis功能
 */
public class CacheUtils {
    //
    private static KeyValueSpace keyValueSpace;

    private CacheUtils(){}

    private CacheUtils(KeyValueSpace keyValueSpace) {
        this.keyValueSpace = keyValueSpace;
    }

    public static void setString(String key, String value) {
        keyValueSpace.setStr(key, value);
    }

    public static String getString(String key) {
        return keyValueSpace.getStr(key);
    }

}
