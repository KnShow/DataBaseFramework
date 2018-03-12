package com.kn0527.cn.databaseframework.db;

/**
 * auto：xkn on 2018/3/12 14:57
 *  规范所有的数据库操作
 */
public interface IBaseDao<T> {
    long insert(T entity);
}
