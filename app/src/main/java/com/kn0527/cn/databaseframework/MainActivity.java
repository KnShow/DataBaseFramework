package com.kn0527.cn.databaseframework;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.kn0527.cn.databaseframework.bean.User;
import com.kn0527.cn.databaseframework.db.BaseDao;
import com.kn0527.cn.databaseframework.db.BaseDaoFactory;
import com.kn0527.cn.databaseframework.db.BaseDapImpl;

import java.util.List;
//import com.kn0527.cn.databaseframework.db.BaseDaoFactory;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void insertObject(View view) {
        BaseDao<User> baseDao = BaseDaoFactory.getOurInstance().getBaseDao(BaseDapImpl.class,User.class);
        long result = baseDao.insert(new User(5, "kidstone", "234"));
        if (result != -1)
            Toast.makeText(MainActivity.this, "执行成功", Toast.LENGTH_SHORT).show();
    }

    public void update(View view) {
        BaseDao<User> baseDao = BaseDaoFactory.getOurInstance().getBaseDao(BaseDapImpl.class,User.class);
        User user = new User();
        user.setName("永动基");
        User where = new User();
        where.setId(5);
        long update = baseDao.update(user, where);
        if (update != -1)
            Toast.makeText(MainActivity.this, "执行成功", Toast.LENGTH_SHORT).show();
    }

    public void delete(View view) {
        BaseDao<User> baseDao = BaseDaoFactory.getOurInstance().getBaseDao(BaseDapImpl.class,User.class);
        User where = new User();
        where.setId(5);
        long delete = baseDao.delete(where);
        if (delete != -1)
            Toast.makeText(MainActivity.this, "执行成功", Toast.LENGTH_SHORT).show();
    }

    public void select(View view) {
        BaseDao<User> baseDao = BaseDaoFactory.getOurInstance().getBaseDao(BaseDapImpl.class,User.class);
        User where = new User();
        where.setName("kn");
        List<User> query = baseDao.query(where);
        Log.d("querySize", "     " + query.size());
        for (User user :
                query) {
            Log.d("query", "password :  " + user.getPassword());
        }
    }
}
