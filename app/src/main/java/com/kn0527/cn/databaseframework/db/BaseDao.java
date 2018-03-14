package com.kn0527.cn.databaseframework.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.kn0527.cn.databaseframework.annotation.DbField;
import com.kn0527.cn.databaseframework.annotation.DbTable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
            isInit = true;
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
        stringBuffer.append("create table if not exists ");
        stringBuffer.append(tableName + "(");
        //反射获取所有字段
        Field[] fields = entityClass.getDeclaredFields();
        for (Field field : fields) {
            Class type = field.getType();
            if (field.getAnnotation(DbField.class) != null) {
                if (type == String.class)
                    stringBuffer.append(field.getAnnotation(DbField.class).value() + " TEXT,");
                else if (type == Integer.class)
                    stringBuffer.append(field.getAnnotation(DbField.class).value() + " INTEGER,");
                else if (type == Long.class)
                    stringBuffer.append(field.getAnnotation(DbField.class).value() + " BIGINT,");
                else if (type == Double.class)
                    stringBuffer.append(field.getAnnotation(DbField.class).value() + " DOUBLE,");
                else if (type == Byte[].class)
                    stringBuffer.append(field.getAnnotation(DbField.class).value() + " BLOB,");
                else
                    //不支持的类型
                    continue;
            } else {
                if (type == String.class)
                    stringBuffer.append(field.getName() + " TEXT,");
                else if (type == Integer.class)
                    stringBuffer.append(field.getName() + " INTEGER,");
                else if (type == Long.class)
                    stringBuffer.append(field.getName() + " BIGINT,");
                else if (type == Double.class)
                    stringBuffer.append(field.getName() + " DOUBLE,");
                else if (type == Byte[].class)
                    stringBuffer.append(field.getName() + " BLOB,");
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

    @Override
    public long update(T entity, T where) {
//        sqLiteDatabase.update(tableName,contentValue,"name=?,",new String[]{"kn"});
        int result = -1;
        Map<String, String> values = getValues(entity);
        ContentValues contentValues = getContentValues(values);
        Map<String, String> whereCause = getValues(where);
        //获取where和whereArgs
        Condition condition = new Condition(whereCause);
        result = sqLiteDatabase.update(tableName, contentValues, condition.whereCasue, condition.whereAres);
        return result;
    }

    @Override
    public long delete(T where) {
//        sqLiteDatabase.delete(tableName,whereClause,whereArgs);
        long result = -1;
        Map<String, String> whereClause = getValues(where);
        Condition condition = new Condition(whereClause);
        result = sqLiteDatabase.delete(tableName, condition.whereCasue, condition.whereAres);
        return result;
    }

    @Override
    public List<T> query(T where) {
//        sqLiteDatabase.query(tableName,null,"id=?",new String[],null,null,orderBy,"1,5");
        return query(where, null, null, null);
    }

    public List<T> query(T where, String orderBy, Integer startIndex, Integer limit) {
        Map<String, String> values = getValues(where);
        String limitString = null;
        if (startIndex != null && limit != null)
            limitString = startIndex + "," + limit;
        Condition condition = new Condition(values);
        Cursor cursor = sqLiteDatabase.query(tableName, null, condition.whereCasue, condition.whereAres, null, null, orderBy, limitString);
        List<T> result = getResult(cursor, where);
        return result;
    }

    //obj是用来表示User类的结构
    private List<T> getResult(Cursor cursor, T obj) {
        ArrayList result = new ArrayList<>();
        Object item = null;
        while (cursor.moveToNext()) {
            try {
                item = obj.getClass().newInstance();//new User();
                Iterator iterator = cacheMap.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry entry = (Map.Entry) iterator.next();
                    String columnName = (String) entry.getKey();
                    int columnIndex = cursor.getColumnIndex(columnName);
                    Field field = (Field) entry.getValue();
                    if (columnIndex != -1) {
                        Class type = field.getType();
                        if (type == String.class) {
                            field.set(item, cursor.getString(columnIndex));
                        } else if (type == Integer.class) {
                            field.set(item, cursor.getInt(columnIndex));
                        } else if (type == Double.class) {
                            field.set(item, cursor.getDouble(columnIndex));
                        } else if (type == Byte[].class) {
                            field.set(item, cursor.getBlob(columnIndex));
                        } else if (type == Long.class) {
                            field.set(item, cursor.getLong(columnIndex));
                        } else {
                            continue;
                        }
                    }
                }
                result.add(item);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    private class Condition {
        private String whereCasue;//"name = ?"
        private String[] whereAres;//new String[]{"kn"}

        private Condition(Map<String, String> whereValues) {
            ArrayList<String> list = new ArrayList();//whereArgs里的内容存入list
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("1=1");
            Set keys = whereValues.keySet();
            Iterator iterator = keys.iterator();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                String value = whereValues.get(key);
                if (value != null) {
                    stringBuilder.append(" and " + key + "=?");
                    list.add(value);
                }
            }
            this.whereCasue = stringBuilder.toString();
            this.whereAres = (String[]) list.toArray(new String[list.size()]);
        }
    }

    private Map<String, String> getValues(T entity) {//User
        HashMap<String, String> map = new HashMap<>();
        Iterator<Field> fieldIterator = cacheMap.values().iterator();//获取所有的values
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
