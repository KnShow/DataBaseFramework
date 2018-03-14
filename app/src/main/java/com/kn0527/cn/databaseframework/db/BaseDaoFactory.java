package com.kn0527.cn.databaseframework.db;

import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

/**
 * auto：xkn on 2018/3/12 15:10
 */

public class BaseDaoFactory {
    private static final BaseDaoFactory ourInstance = new BaseDaoFactory();

    public static BaseDaoFactory getOurInstance() {
        return ourInstance;
    }

    private SQLiteDatabase sqLiteDatabase;
    //定义建数据数据的路径
    //建议写到SD卡中，好处，APP让删除了，下次再安装的时候，数据还在
    private String sqliteDatabasePath;

    private BaseDaoFactory() {
        //可以先判断有没有SD卡

//        sqliteDatabasePath = "data/data/com.kn0527.cn.databaseframework/kn.db";
        sqliteDatabasePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/1/kn.db";
        sqLiteDatabase = SQLiteDatabase.openOrCreateDatabase(sqliteDatabasePath, null);
    }

//    /**
//     * 用来生产basedao对象
//     */
//    public <T> BaseDao<T> getBaseDao(Class<T> entityClass) {
//        BaseDao baseDao = null;
//        try {
//            baseDao = BaseDao.class.newInstance();
//            //初始化数据库
//            baseDao.init(sqLiteDatabase, entityClass);
//        } catch (InstantiationException e) {
//            e.printStackTrace();
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        }
//        return baseDao;
//    }

    /**
     * 用来生产basedao对象
     * M  User
     * T  BaseDao
     */
    public <T extends BaseDao<M>, M> T getBaseDao(Class<T> daoClass, Class<M> entityClass) {
        BaseDao baseDao = null;
        try {
            baseDao = daoClass.newInstance();
            baseDao.insert(entityClass);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return (T) baseDao;
    }

}
