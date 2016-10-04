package com.androidNewLab.baiduMap.Map;

import com.baidu.mapapi.model.LatLng;

import java.util.List;

/**
 * Created by Haodong on 2016/10/4.
 */

public class LatBean {
    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getMyLat() {
        return myLat;
    }

    public void setMyLat(String myLat) {
        this.myLat = myLat;
    }

    private String time=null;
    private String myLat=null;
    public LatBean(String time ,String myLat){
          this.time=time;
        this.myLat=myLat;
    }

}
