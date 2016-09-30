package com.infinity.dubbo;

/**
 * HiGirlImpl
 *
 * @author Alvin Xu
 * @date 2016/9/22
 */
public class HiGirlImpl implements HiGirl {
    @Override
    public String hiGirl(String name) {
        return "Hi, beautiful girl " + name;
    }
}
