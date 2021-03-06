package com.sinotopia.fundamental.database;

import java.util.List;

/**
 * 默认Dao的基类
 * Created by Administrator on 2015/12/14.
 */
public interface DaoBase<T, K> {
    /**
     * 添加
     *
     * @param t
     */
    void add(T t);

    /**
     * 更新
     *
     * @param t
     * @return
     */
    int update(T t);

    /**
     * 条件查询
     *
     * @param t
     * @return
     */
    T get(T t);

    /**
     * 根据id查询
     *
     * @param id
     * @return
     */
    T getById(K id);

    /**
     * 条件查询列表
     *
     * @param t
     * @return
     */
    List<T> query(T t);

    /**
     * 根据id删除记录
     *
     * @param id
     * @return
     */
    int delete(K id);

    /**
     * 查询记录数
     *
     * @param t
     * @return
     */
    int count(T t);
}
