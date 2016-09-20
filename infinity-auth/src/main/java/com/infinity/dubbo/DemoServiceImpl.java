package com.infinity.dubbo;

import java.util.ArrayList;
import java.util.List;

/**
 * DemoServiceImpl
 *
 * @author Alvin Xu
 * @date 2016/9/20
 */
public class DemoServiceImpl implements DemoService {
    @Override
    public String sayHello(String name) {
        return "Hello " + name;
    }
}
