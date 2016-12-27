package com.thrift.test;

/**
 * Created by xule on 2016/12/27.
 */
import org.apache.thrift.TException;

import com.thrift.test.Hello.Iface;

public class HelloImpl implements Iface {
    private static int count = 0;

    @Override
    public String helloString(String word) throws TException {
        // TODO Auto-generated method stub
        count += 1;
        System.out.println("get " + word + " " + count);
        return "hello " + word + " " + count;
    }
}