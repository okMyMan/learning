package com.infinity.dubbo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * DemoServiceImpl
 *
 * @author Alvin Xu
 * @date 2016/9/20
 */
public class DemoServiceImpl implements DemoService {
    private static Logger logger = LoggerFactory.getLogger(DemoServiceImpl.class);
    @Override
    public String sayHello(String name) {
        logger.info("服务  sayHello  已经被调用,   ");

        return "Hello " + name;
    }
}
