package com.androidNewLab.baiduMap.Chat;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.androidNewLab.baiduMap.R;

import cn.bmob.v3.listener.SaveListener;

/**
 * Created by Administrator on 2016/8/10.
 */
public class RegActivity extends Activity  implements View.OnClickListener{
    private EditText et_name,et_password;
    private Button btn_reg;
    private EditText et_password2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reg);
        init();
    }

    private void init() {
        et_name= (EditText) findViewById(R.id.new_et_name);
        et_password= (EditText) findViewById(R.id.new_et_password);
        et_password2= (EditText) findViewById(R.id.new_et_password2);
        btn_reg= (Button) findViewById(R.id.new_btn_reg);
        btn_reg.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){


            case R.id.new_btn_reg:
                User user=new User();
                user.setUsername(et_name.getText().toString().trim());
                user.setPassword(et_password.getText().toString().trim());
                user.setInfo("畅途成员");
                if(et_password.getText().toString().equals(et_password2.getText().toString())){
                    user.signUp(this, new SaveListener() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(RegActivity.this,"注册成功",Toast.LENGTH_SHORT).show();
                            finish();

                        }

                        @Override
                        public void onFailure(int i, String s) {
                            Toast.makeText(RegActivity.this,"注册失败",Toast.LENGTH_SHORT).show();

                        }
                    });

                }else {
                    Toast.makeText(RegActivity.this,"请确认密码",Toast.LENGTH_SHORT).show();
                }




        }

    }
}
