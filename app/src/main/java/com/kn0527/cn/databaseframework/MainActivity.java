package com.kn0527.cn.databaseframework;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.kn0527.cn.databaseframework.bean.User;
import com.kn0527.cn.databaseframework.db.BaseDao;
import com.kn0527.cn.databaseframework.db.BaseDaoFactory;
//import com.kn0527.cn.databaseframework.db.BaseDaoFactory;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void insertObject(View view) {
        BaseDao<User> baseDao = BaseDaoFactory.getOurInstance().getBaseDao(User.class);
        long result = baseDao.insert(new User(1, "kn", "123456"));
        if (result != -1)
            Toast.makeText(MainActivity.this, "执行成功", Toast.LENGTH_SHORT).show();
    }
}
