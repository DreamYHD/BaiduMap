package com.androidNewLab.baiduMap.Fragment;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.androidNewLab.baiduMap.R;

/**
 * Created by Administrator on 2016/8/21.
 */
public class ContentActivity extends Activity  implements View.OnClickListener {
    private Button mMap,mXieYi,mOk;
    private MapFragment mMapFragment;
    private XieyiFragment mXieyiFragment;
    private OkFragment mOkFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.second_main);
        init();
        setDefaultFragment();

    }

    private void setDefaultFragment() {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        mMapFragment = new MapFragment();
        transaction.replace(R.id.id_content,mMapFragment);
        transaction.commit();
    }

    private void init() {
        mMap= (Button) findViewById(R.id.bot_map);
        mXieYi= (Button) findViewById(R.id.bot_xieyi);
        mOk= (Button) findViewById(R.id.bot_ok);
        mMap.setOnClickListener(this);
        mXieYi.setOnClickListener(this);
        mOk.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        FragmentManager fm=getFragmentManager();
        FragmentTransaction transation=fm.beginTransaction();
        switch (v.getId()){
            case R.id.bot_map:
                if(mMapFragment==null){
                    mMapFragment=new MapFragment();
                    transation.replace(R.id.id_content,mMapFragment);
                    mOkFragment=null;
                    mXieyiFragment=null;


                }
                break;
            case R.id.bot_xieyi:
                if(mXieyiFragment==null){
                    mXieyiFragment=new XieyiFragment();
                    transation.replace(R.id.id_content,mXieyiFragment);
                    mMapFragment=null;
                    mOkFragment=null;
                }

                break;
            case R.id.bot_ok:
                if(mOkFragment==null){
                    mOkFragment=new OkFragment();
                    transation.replace(R.id.id_content,mOkFragment);
                    mMapFragment=null;
                    mXieyiFragment=null;
                }
                break;


        }
        transation.commit();


    }
}
