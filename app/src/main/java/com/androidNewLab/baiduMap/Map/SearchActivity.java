package com.androidNewLab.baiduMap.Map;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import com.androidNewLab.baiduMap.Chat.RegActivity;
import com.androidNewLab.baiduMap.R;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;

import java.util.ArrayList;
import java.util.List;


public class SearchActivity extends AppCompatActivity {

    private MapView mapView;
    GeoCoder mSearch=null;
    private BaiduMap mBaiduMap=null;
    private Button mBtn;
    private Button mBtnLocation;
    private Button mBtnLine;
    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MyLocationListener();
    private  List<LatLng> points = new ArrayList<LatLng>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());
        mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
        mLocationClient.registerLocationListener(myListener);    //注册监听函数
        setContentView(R.layout.search);
        initDrawLine();
        startLocation();
        //获取地图控件引用
        mapView= (MapView) findViewById(R.id.bmapView);

        mBaiduMap=mapView.getMap();
        initSearchGeoPoint();
        mBtn= (Button) findViewById(R.id.button);
        mBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearch.geocode(new GeoCodeOption().city("太原").address("中北大学"));
            }
        });
    }

    private void initSearchGeoPoint() {
        mSearch= GeoCoder.newInstance();// 创建地理编码检索实例

        // 设置地理编码检索监听者
        mSearch.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {

            @Override
            public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {
                if (geoCodeResult == null || geoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
                    Toast.makeText(SearchActivity.this, "抱歉，未能找到结果", Toast.LENGTH_LONG)
                            .show();
                    return;
                }
//                mBaiduMap.clear();
                points.add(geoCodeResult.getLocation());//将当前位置放入到经纬度数组中
                //构建markerOption，用于在地图上添加marker ，先找到位置，在添加图标
                mBaiduMap.addOverlay(new MarkerOptions().position(geoCodeResult.getLocation())
                        .icon(BitmapDescriptorFactory
                                .fromResource(R.drawable.icon_st)));
                //地图位置移动到当前位置
                mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(geoCodeResult
                        .getLocation()));
                String strInfo = String.format("纬度：%f 经度：%f",
                        geoCodeResult.getLocation().latitude, geoCodeResult.getLocation().longitude);


                Toast.makeText(SearchActivity.this, strInfo, Toast.LENGTH_LONG).show();
            }

            //释放地理编码检索实例
            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {

                if (reverseGeoCodeResult == null || reverseGeoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
                    Toast.makeText(SearchActivity.this, "抱歉，未能找到结果", Toast.LENGTH_LONG)
                            .show();
                    return;
                }
                mBaiduMap.clear();
                mBaiduMap.addOverlay(new MarkerOptions().position(reverseGeoCodeResult.getLocation())
                        .icon(BitmapDescriptorFactory
                                .fromResource(R.drawable.icon_st)));
                //加上覆盖物
                mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(reverseGeoCodeResult
                        .getLocation()));
                //定位
                Toast.makeText(SearchActivity.this, reverseGeoCodeResult.getAddress(),
                        Toast.LENGTH_LONG).show();
                //result保存翻地理编码的结果 坐标-->城市
            }
        });

    }

    private void startLocation() {
        mBtnLocation= (Button) findViewById(R.id.button_location);
        mBtnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initLocation();//设置定位参数
                mLocationClient.start();

            }
        });
    }

    private void initDrawLine() {
        mBtnLine= (Button) findViewById(R.id.button_line);
        mBtnLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //设置连线的宽度和颜色
                OverlayOptions ooPolyline = new PolylineOptions().width(20)
                        .color(0xAA7CFC00).points(points);
                mBaiduMap.addOverlay(ooPolyline);
           }
       });
    }

    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy
        );//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span=1000;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认false，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        mLocationClient.setLocOption(option);
    }
   class MyLocationListener implements BDLocationListener {

           @Override
           public void onReceiveLocation(BDLocation location) {
               //得到当前所在位置的经纬度
               LatLng point= new LatLng(location.getLatitude(),location.getLongitude());
               points.add(point);
               mSearch.reverseGeoCode(new ReverseGeoCodeOption().location(point));
               mBaiduMap.addOverlay(new MarkerOptions().position(point)
                       .icon(BitmapDescriptorFactory
                               .fromResource(R.drawable.icon_st)));
               mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(point));
               mLocationClient.stop();
           }
       }
}