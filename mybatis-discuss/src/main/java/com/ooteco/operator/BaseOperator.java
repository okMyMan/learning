package com.ooteco.operator;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.Reader;

/**
 * Created by xule on 2017/2/1.
 */
public class BaseOperator
{
    protected static SqlSessionFactory ssf;
    protected static Reader reader;

    static {
        try {
            reader = Resources.getResourceAsReader("config.xml");
            ssf = new SqlSessionFactoryBuilder().build(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}