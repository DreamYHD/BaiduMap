package com.androidNewLab.baiduMap.Fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidNewLab.baiduMap.R;

/**
 * Created by Administrator on 2016/8/21.
 */
public class BottomBarFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {



        return inflater.inflate(R.layout.bottombar,container,false);
    }
}
