package com.ooteco.operator;

import com.ooteco.bean.Account;
import org.apache.ibatis.session.SqlSession;

/**
 * Created by xule on 2017/2/1.
 */
public class AccountOperator extends BaseOperator {

    private static AccountOperator instance = new AccountOperator();

    private AccountOperator()
    {

    }

    public static AccountOperator getInstance()
    {
        return instance;
    }

    public Account selectAccountById(String id)
    {
        SqlSession ss = ssf.openSession();
        Account account = null;
        try
        {
            account = ss.selectOne("com.ooteco.dao.AccountMapper.selectByPrimaryKey", id);
        }
        finally
        {
            ss.close();
        }
        return account;
    }


//    public void insertOneStudent(String studentName, int studentAge, String studentPhone)
//    {
//        SqlSession ss = ssf.openSession();
//        try
//        {
//            ss.insert("com.xrq.StudentMapper.insertOneStudent",
//                    new Student(0, studentName, studentAge, studentPhone));
//            ss.commit();
//        }
//        catch (Exception e)
//        {
//            ss.rollback();
//        }
//        finally
//        {
//            ss.close();
//        }
//    }
}
