package com.kn0527.cn.databaseframework.bean;

import com.kn0527.cn.databaseframework.annotation.DbField;
import com.kn0527.cn.databaseframework.annotation.DbTable;

/**
 * auto：xkn on 2018/3/12 13:57
 */
@DbTable("tb_user")
public class User {
    @DbField("_id")
    private Integer id;
    private String name;
    private String password;

    public User(Integer id, String name, String password) {
        this.id = id;
        this.name = name;
        this.password = password;
    }

    public User() {

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
