package com.infinity.dao;

import com.infinity.entity.FundBase;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * TestDao
 *
 * @author Alvin Xu
 * @date 2016/9/12
 */
@Repository
public class TestDao {
    @Autowired
    private SqlSession sqlSession;

    public List<FundBase> getAllFundBase(){
        return sqlSession.selectList("CommonMapper.getAllFundBase");
    }
}
