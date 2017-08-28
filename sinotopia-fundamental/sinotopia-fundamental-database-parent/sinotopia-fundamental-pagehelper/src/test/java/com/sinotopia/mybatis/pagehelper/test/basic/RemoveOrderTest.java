/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2017 abel533@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.sinotopia.mybatis.pagehelper.test.basic;

import com.sinotopia.mybatis.pagehelper.Page;
import com.sinotopia.mybatis.pagehelper.PageHelper;
import com.sinotopia.mybatis.pagehelper.mapper.CountryMapper;
import com.sinotopia.mybatis.pagehelper.model.Country;
import com.sinotopia.mybatis.pagehelper.util.MybatisHelper;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Created by Administrator on 14-8-24.
 */
public class RemoveOrderTest {

    @Test
    public void simpleOrderTest() {
        SqlSession sqlSession = MybatisHelper.getSqlSession();
        CountryMapper countryMapper = sqlSession.getMapper(CountryMapper.class);
        //主要观看查询执行count查询的sql
        try {
            PageHelper.startPage(1, 50);
            List<Country> list = countryMapper.selectAllOrderby();
            //总数183
            Assert.assertEquals(183, ((Page<?>) list).getTotal());
        } finally {
            sqlSession.close();
        }
    }


    @Test
    public void paramsOrderTest() {
        SqlSession sqlSession = MybatisHelper.getSqlSession();
        CountryMapper countryMapper = sqlSession.getMapper(CountryMapper.class);
        //主要观看查询执行count查询的sql
        try {
            PageHelper.startPage(1, 50);
            List<Country> list = countryMapper.selectAllOrderByParams("countryname", "countrycode");
            //总数183
            Assert.assertEquals(183, ((Page<?>) list).getTotal());
        } finally {
            sqlSession.close();
        }
    }


}