package com.androidNewLab.baiduMap;


import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.androidNewLab.baiduMap.Chat.Conf;
import com.androidNewLab.baiduMap.Chat.RegActivity;
import com.androidNewLab.baiduMap.Chat.User;
import com.androidNewLab.baiduMap.Fragment.ContentActivity;
import com.androidNewLab.baiduMap.Map.JustMap;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.listener.SaveListener;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText et_name,et_password;
    private Button btn_login,btn_reg;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bmob.initialize(MainActivity.this, Conf.APP_ID);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        et_name= (EditText) findViewById(R.id.et_name);
        et_password= (EditText) findViewById(R.id.et_password);
        btn_login= (Button) findViewById(R.id.btn_login);
        btn_reg= (Button) findViewById(R.id.btn_reg);
        btn_login.setOnClickListener(this);
        btn_reg.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_login:
                User user=new User();
                user.setUsername(et_name.getText().toString().trim());
                user.setPassword(et_password.getText().toString().trim());
                user.login(this, new SaveListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(MainActivity.this,"登陆Success",Toast.LENGTH_SHORT).show();
                        Intent intent=new Intent(MainActivity.this, ContentActivity.class);
                        startActivity(intent);

                    }

                    @Override
                    public void onFailure(int i, String s) {
                        Toast.makeText(MainActivity.this,"登陆failed",Toast.LENGTH_SHORT).show();
                    }
                });

                break;
            case R.id.btn_reg:
                Intent intent=new Intent(MainActivity.this, RegActivity.class);
                startActivity(intent);


                break;


        }

    }
}
