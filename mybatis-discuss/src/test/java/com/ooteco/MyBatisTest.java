package com.ooteco;

import com.ooteco.bean.Account;
import com.ooteco.operator.AccountOperator;

/**
 * Created by xule on 2017/2/1.
 */
public class MyBatisTest {
    public static void main(String[] args) {
        Account account = AccountOperator.getInstance().selectAccountById("000c62260d484d4c95b8414ec6c1072d");

        System.currentTimeMillis();
    }
}
