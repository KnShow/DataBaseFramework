package com.kn0527.cn.databaseframework.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.kn0527.cn.databaseframework.annotation.DbField;
import com.kn0527.cn.databaseframework.annotation.DbTable;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * auto：xkn on 2018/3/12 13:51
 */

public class BaseDao<T> implements IBaseDao<T> {

    private SQLiteDatabase sqLiteDatabase;
    private Class<T> entityClass;
    /**
     * 用来标识是否做过初始化操作
     */
    private boolean isInit = false;
    private String tableName;
    //定义一个缓存空间(key-字段名    value-成员变量)
    private HashMap<String, Field> cacheMap;

    protected boolean init(SQLiteDatabase sqLiteDatabase, Class<T> entityClass) {
        this.sqLiteDatabase = sqLiteDatabase;
        this.entityClass = entityClass;
        if (!isInit) {
            //自动建表
            //取到表名
            if (entityClass.getAnnotation(DbTable.class) == null) {
                //反射获取类名作为表名
                tableName = entityClass.getSimpleName();
            } else {
                tableName = entityClass.getAnnotation(DbTable.class).value();
            }
            //判断数据库是否打开
            if (!sqLiteDatabase.isOpen())
                return false;
            String createTabSql = getCreateTabSql();
            sqLiteDatabase.execSQL(createTabSql);
            cacheMap = new HashMap<>();
            initCacheMap();
        }
        return false;
    }

    private void initCacheMap() {
        //1、取到所有列名
        String sql = "select * from " + tableName + " limit 1,0";//空表
        Cursor cursor = sqLiteDatabase.rawQuery(sql, null);
        String[] columnNames = cursor.getColumnNames();
        //2、取所有的成员变量
        Field[] columnFields = entityClass.getDeclaredFields();
        //把所有字段的访问权限打开 作用就是让我们在用反射时访问私有变量
        for (Field field : columnFields) {
            field.setAccessible(true);
        }
        //对1和2进行映射
        for (String columnName : columnNames) {
            Field columnField = null;
            for (Field field : columnFields) {
                String fieldName = null;
                if (field.getAnnotation(DbField.class) != null) {
                    fieldName = field.getAnnotation(DbField.class).value();
                } else {
                    fieldName = field.getName();
                }
                if (columnName.equals(fieldName)) {
                    columnField = field;
                }
                if (columnField != null)
                    cacheMap.put(fieldName, columnField);
            }
        }
    }

    /**
     * 获取建表字符串
     *
     * @return
     */
    private String getCreateTabSql() {
        //create table if not exists tb_user(_id INTEGER,name TEXT,password TEXT)
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("CREATE TABLE IF NOT EXISTS " + tableName + "(");
        //反射获取所有字段
        Field[] fields = entityClass.getDeclaredFields();
        for (Field field : fields) {
            Class type = field.getType();
            if (field.getAnnotation(DbField.class) != null) {
                if (type == String.class)
                    stringBuffer.append(field.getAnnotation(DbField.class).value() + "TEXT,");
                else if (type == Integer.class)
                    stringBuffer.append(field.getAnnotation(DbField.class).value() + "INTEGER,");
                else if (type == Long.class)
                    stringBuffer.append(field.getAnnotation(DbField.class).value() + "BIGINT,");
                else if (type == Double.class)
                    stringBuffer.append(field.getAnnotation(DbField.class).value() + "DOUBLE,");
                else if (type == Byte[].class)
                    stringBuffer.append(field.getAnnotation(DbField.class).value() + "BLOB,");
                else
                    //不支持的型号
                    continue;
            } else {
                if (type == String.class)
                    stringBuffer.append(field.getName() + "TEXT,");
                else if (type == Integer.class)
                    stringBuffer.append(field.getName() + "INTEGER,");
                else if (type == Long.class)
                    stringBuffer.append(field.getName() + "BIGINT,");
                else if (type == Double.class)
                    stringBuffer.append(field.getName() + "DOUBLE,");
                else if (type == Byte[].class)
                    stringBuffer.append(field.getName() + "BLOB,");
                else
                    //不支持的型号
                    continue;
            }
        }
        if (stringBuffer.charAt(stringBuffer.length() - 1) == ',') {
            stringBuffer.deleteCharAt(stringBuffer.length() - 1);
        }
        stringBuffer.append(")");
        return stringBuffer.toString();
    }

    @Override
    public long insert(T entity) {
        //        sqLiteDatabase.insert(tableName,null, contentValues);
        Map<String, String> map = getValues(entity);
        ContentValues contentValues = getContentValues(map);
        long result = sqLiteDatabase.insert(tableName, null, contentValues);
        return result;
    }

    private Map<String, String> getValues(T entity) {
        HashMap<String, String> map = new HashMap<>();
        Iterator<Field> fieldIterator = cacheMap.values().iterator();
        while (fieldIterator.hasNext()) {
            Field field = fieldIterator.next();
            field.setAccessible(true);
            //获取成员变量的值
            try {
                Object object = field.get(entity);
                if (object == null)
                    continue;
                String value = object.toString();
                //获取列名
                String key = null;
                if (field.getAnnotation(DbField.class) != null) {
                    key = field.getAnnotation(DbField.class).value();
                } else {
                    key = field.getName();
                }
                if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value))
                    map.put(key, value);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }
        return map;
    }

    private ContentValues getContentValues(Map<String, String> map) {
        ContentValues contentValues = new ContentValues();
        Set keys = map.keySet();
        Iterator<String> iterator = keys.iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            String value = map.get(key);
            if (value != null)
                contentValues.put(key, value);
        }
        return contentValues;
    }
}
