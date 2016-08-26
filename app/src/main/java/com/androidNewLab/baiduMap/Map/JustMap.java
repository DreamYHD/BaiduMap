package com.androidNewLab.baiduMap.Map;

import android.graphics.Color;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ZoomControls;

import com.androidNewLab.baiduMap.Map.GsonService;
import com.androidNewLab.baiduMap.Map.HistoryTrackData;
import com.androidNewLab.baiduMap.R;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.trace.LBSTraceClient;

import com.baidu.trace.OnTrackListener;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;


public class JustMap extends AppCompatActivity {


    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private LocationClient locationClient;
    private boolean firstLocation;
    private BitmapDescriptor mCurrentMarker;
    private MyLocationConfiguration config;
    private Button btn;
    private OnTrackListener trackListener;
    private MapStatusUpdate msUpdate;
    protected static long serviceId=122008;
    protected static LBSTraceClient client = null;

    private int startTime = 0;
    private int endTime = 0;
    private BitmapDescriptor bmStart;
    private BitmapDescriptor bmEnd;

    // 起点图标覆盖物
    private static MarkerOptions startMarker = null;
    // 终点图标覆盖物
    private static MarkerOptions endMarker = null;
    // 路线覆盖物
    private static PolylineOptions polyline = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.map);
        client=new LBSTraceClient(JustMap.this);
        init();

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initOnTrackListener();
                queryHistoryTrack();



            }
        });




    }



    /**
     * 初始化OnTrackListener
     */
    private void initOnTrackListener() {
        Log.i("TGA","开始初始化历史轨迹");

        trackListener = new OnTrackListener() {

            // 请求失败回调接口
            @Override
            public void onRequestFailedCallback(String arg0) {
                // TODO Auto-generated method stub
                Looper.prepare();
                Toast.makeText(JustMap.this, "track请求失败回调接口消息 : " + arg0, Toast.LENGTH_SHORT).show();
                Looper.loop();
            }

            // 查询历史轨迹回调接口
            @Override
            public void onQueryHistoryTrackCallback(String arg0) {
                // TODO Auto-generated method stub
                super.onQueryHistoryTrackCallback(arg0);
                showHistoryTrack(arg0);
            }
        };
    }

    /**
     * 查询历史轨迹
     */
    public void queryHistoryTrack() {
        Log.i("TGA","开始查询历史轨迹");

        // entity标识
        String entityName ="myTrace";;
        // 是否返回精简的结果（0 : 否，1 : 是）
        int simpleReturn = 0;
        // 开始时间
        if (startTime == 0) {
            startTime = (int) (System.currentTimeMillis() / 1000 - 12 * 60 * 60);
        }
        if (endTime == 0) {
            endTime = (int) (System.currentTimeMillis() / 1000);
        }
        // 分页大小
        int pageSize = 1000;
        // 分页索引
        int pageIndex = 1;

        client.queryHistoryTrack(JustMap.serviceId, entityName, simpleReturn, startTime, endTime, pageSize, pageIndex, trackListener);
    }
    /**
     * 显示历史轨迹
     *
     * @param
     */
    public void showHistoryTrack(String historyTrack) {
        Log.i("TGA","开始显示历史轨迹");

        HistoryTrackData historyTrackData = GsonService.parseJson(historyTrack,
                HistoryTrackData.class);
        List<LatLng> latLngList = new ArrayList<LatLng>();
        drawHistoryTrack(latLngList);
        if (historyTrackData != null && historyTrackData.getStatus() == 0) {
            if (historyTrackData.getListPoints() != null) {
                latLngList.addAll(historyTrackData.getListPoints());
            }

            // 绘制历史轨迹
            drawHistoryTrack(latLngList);

        }

    }

    /**
     * 绘制历史轨迹
     *
     * @param points
     */
    public void drawHistoryTrack(final List<LatLng> points) {
        Log.i("TGA","开始绘制历史轨迹");
        // 绘制新覆盖物前，清空之前的覆盖物
        mBaiduMap.clear();

        if (points == null || points.size() == 0) {
            Looper.prepare();
            Toast.makeText(JustMap.this, "当前查询无轨迹点", Toast.LENGTH_LONG).show();
            Log.i("TGA","当前查询无轨迹点");

            Looper.loop();
            resetMarker();
        } else if (points.size() != 0&&points!=null) {
            Toast.makeText(JustMap.this, "当前查询有轨迹点", Toast.LENGTH_SHORT).show();

            LatLng llC = points.get(0);
            LatLng llD = points.get(points.size() - 1);
            LatLngBounds bounds = new LatLngBounds.Builder()
                    .include(llC).include(llD).build();

            msUpdate = MapStatusUpdateFactory.newLatLngBounds(bounds);

            bmStart = BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
            bmEnd = BitmapDescriptorFactory.fromResource(R.drawable.icon_st);

            // 添加起点图标
            startMarker = new MarkerOptions()
                    .position(points.get(points.size() - 1)).icon(bmStart)
                    .zIndex(9).draggable(true);

            // 添加终点图标
            endMarker = new MarkerOptions().position(points.get(0))
                    .icon(bmEnd).zIndex(9).draggable(true);

            // 添加路线（轨迹）
            polyline = new PolylineOptions().width(10)
                    .color(Color.RED).points(points);

            addMarker();

        }

    }

    private void resetMarker() {
        startMarker = null;
        endMarker = null;
        polyline = null;
    }

    /**
     * 添加覆盖物
     */
    public void addMarker() {

        if (null != msUpdate) {
            mBaiduMap.setMapStatus(msUpdate);
        }

        if (null != startMarker) {
            mBaiduMap.addOverlay(startMarker);
        }

        if (null != endMarker) {
            mBaiduMap.addOverlay(endMarker);
        }

        if (null != polyline) {
            mBaiduMap.addOverlay(polyline);
        }

    }
    private void init() {

        btn= (Button) findViewById(R.id.btn);
        mMapView= (MapView) findViewById(R.id.map);
        mBaiduMap=mMapView.getMap();
        // 隐藏logo
        View child = mMapView.getChildAt(1);
        if (child != null && (child instanceof ImageView || child instanceof ZoomControls)){
            child.setVisibility(View.INVISIBLE);
        }
        // 设置自定义图标
        BitmapDescriptor myMarker = BitmapDescriptorFactory
                .fromResource(R.drawable.icon_st);
        MyLocationConfiguration config = new MyLocationConfiguration(
                MyLocationConfiguration.LocationMode.FOLLOWING, true, myMarker);

        //地图上比例尺
        //mMapView.showScaleControl(false);
        // 隐藏缩放控件
        mMapView.showZoomControls(false);
        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(17.5f);
        mBaiduMap.setMapStatus(msu);
        //定位初始化
        locationClient = new LocationClient(this);
        firstLocation =true;
        // 设置定位的相关配置
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setOpenGps(true);
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);
        locationClient.setLocOption(option);
        locationClient.registerLocationListener(new BDLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation bdLocation) {
                if(bdLocation==null||mMapView==null)
                    return;
                //构造定位数据
                MyLocationData locData=new MyLocationData.Builder()
                        .accuracy(bdLocation.getRadius())
                        .direction(100).latitude(bdLocation.getLatitude())
                        .longitude(bdLocation.getLongitude()).build();
                mBaiduMap.setMyLocationData(locData);
                // 第一次定位时，将地图位置移动到当前位置
                if (firstLocation) {
                    firstLocation = false;
                    LatLng xy = new LatLng(bdLocation.getLatitude(),
                            bdLocation.getLongitude());
                    MapStatusUpdate status = MapStatusUpdateFactory.newLatLng(xy);
                    mBaiduMap.animateMapStatus(status);
                }

            }
        });

    }
    @Override
    protected void onStart()
    {
        // 如果要显示位置图标,必须先开启图层定位
        mBaiduMap.setMyLocationEnabled(true);
        if (!locationClient.isStarted())
        {
            locationClient.start();
        }
        super.onStart();
    }
    @Override
    protected void onStop()
    {
        mBaiduMap.setMyLocationEnabled(false);
        locationClient.stop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }


}
