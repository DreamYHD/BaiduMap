package com.androidNewLab.baiduMap.Map;

import android.content.Context;
import android.graphics.Color;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.androidNewLab.baiduMap.R;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.trace.LBSTraceClient;

import com.baidu.trace.OnEntityListener;
import com.baidu.trace.OnStartTraceListener;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.trace.Trace;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class JustMap extends AppCompatActivity implements OnGetGeoCoderResultListener{

    int gatherInterval = 3;  //位置采集周期 (s)
    int packInterval = 10;  //打包周期 (s)
    String entityName = null;  // entity标识
    long serviceId = 122008;// 鹰眼服务ID
    int traceType = 2;  //轨迹服务类型
    private static OnStartTraceListener startTraceListener = null;  //开启轨迹服务监听器

    private static MapView mapView = null;
    private static BaiduMap baiduMap = null;
    private static OnEntityListener entityListener = null;
    private RefreshThread refreshThread = null;  //刷新地图线程以获取实时点
    private static MapStatusUpdate msUpdate = null;
    private static BitmapDescriptor realtimeBitmap;  //图标
    private static OverlayOptions overlay;  //覆盖物
    private static List<LatLng> pointList = new ArrayList<LatLng>();  //定位点的集合
    private static PolylineOptions polyline = null;  //路线覆盖物


    private Trace trace;  // 实例化轨迹服务
    private LBSTraceClient client;  // 实例化轨迹服务客户端
    public static int index=0;
    public static LatLng stLatLng=null;
    private GeoCoder mSearch = null;
    private static List<LatBean>mList=new ArrayList<LatBean>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        long time=System.currentTimeMillis();

        setContentView(R.layout.map);
        index=0;
        init();
        initOnEntityListener();
        initOnStartTraceListener();



        client.startTrace(trace, startTraceListener);  // 开启轨迹服务
        Date date=new Date(time);
        SimpleDateFormat format;
        format=new SimpleDateFormat("HH:mm:ss");

        Log.e("time","time1="+format.format(date));





    }






    private void init() {


        mSearch = GeoCoder.newInstance();
        mSearch.setOnGetGeoCodeResultListener(this);

        mapView = (MapView) findViewById(R.id.map);
        baiduMap = mapView.getMap();
        mapView.showZoomControls(false);

        entityName = getImei(getApplicationContext());  //手机Imei值的获取，用来充当实体名

        client = new LBSTraceClient(getApplicationContext());  //实例化轨迹服务客户端

        trace = new Trace(getApplicationContext(), serviceId, entityName, traceType);  //实例化轨迹服务

        client.setInterval(gatherInterval, packInterval);  //设置位置采集和打包周期
    }
    /**
     * 初始化设置实体状态监听器
     */
    private void initOnEntityListener(){
        Log.i("TGA","初始化成功");

        //实体状态监听器
        entityListener = new OnEntityListener(){

            @Override
            public void onRequestFailedCallback(String arg0) {
                Looper.prepare();
                Toast.makeText(
                        getApplicationContext(),
                        "entity请求失败的回调接口信息："+arg0,
                        Toast.LENGTH_SHORT)
                        .show();
                Looper.loop();
            }

            @Override
            public void onQueryEntityListCallback(String arg0) {
                /**
                 * 查询实体集合回调函数，此时调用实时轨迹方法
                 */
                showRealtimeTrack(arg0);
            }

        };
    }



    /** 追踪开始 */
    private void initOnStartTraceListener() {

        // 实例化开启轨迹服务回调接口
        startTraceListener = new OnStartTraceListener() {
            // 开启轨迹服务回调接口（arg0 : 消息编码，arg1 : 消息内容，详情查看类参考）
            @Override
            public void onTraceCallback(int arg0, String arg1) {
                Log.i("TAG", "onTraceCallback=" + arg1);
                if(arg0 == 0 || arg0 == 10006){
                    startRefreshThread(true);
                }
            }

            // 轨迹服务推送接口（用于接收服务端推送消息，arg0 : 消息类型，arg1 : 消息内容，详情查看类参考）
            @Override
            public void onTracePushCallback(byte arg0, String arg1) {
                Log.i("TAG", "onTracePushCallback=" + arg1);
            }
        };





    }


    /**
     * 轨迹刷新线程
     * @author Yanghaodong
     */
    private class RefreshThread extends Thread{

        protected boolean refresh = true;

        public void run(){

            while(refresh){
                queryRealtimeTrack();
                try{
                    Thread.sleep(packInterval * 1000);
                    Log.d("INDEX",index+""+stLatLng.toString());
                }catch(InterruptedException e){
                    System.out.println("线程休眠失败");
                }
            }

        }
    }

    /**
     * 查询实时线路
     */
    private void queryRealtimeTrack(){

        String entityName = this.entityName;
        String columnKey = "";
        int returnType = 0;
        int activeTime = 0;
        int pageSize = 10;
        int pageIndex = 1;

        this.client.queryEntityList(
                serviceId,
                entityName,
                columnKey,
                returnType,
                activeTime,
                pageSize,
                pageIndex,
                entityListener
        );

    }


    /**
     * 展示实时线路图
     * @param realtimeTrack
     */
    protected void showRealtimeTrack(String realtimeTrack){

        if(refreshThread == null || !refreshThread.refresh){
            return;
        }

        //数据以JSON形式存取
        RealtimeTrackData realtimeTrackData = GsonService.parseJson(realtimeTrack, RealtimeTrackData.class);

        if(realtimeTrackData != null && realtimeTrackData.getStatus() ==0){

            LatLng latLng = realtimeTrackData.getRealtimePoint();

            if(latLng != null){
                Log.i("TGA","当前有轨迹点");
                pointList.add(latLng);
                index++;
                if(index%3==0){
                    mSearch.reverseGeoCode(new ReverseGeoCodeOption()
                            .location(latLng));


                }


                if(index==1){
                    stLatLng=latLng;
                    Log.d("FFF",stLatLng.toString());



                }
                drawRealtimePoint(latLng);
            }
            else{

                Toast.makeText(getApplicationContext(), "当前无轨迹点", Toast.LENGTH_LONG).show();
            }

        }

    }

    /**
     * 画出实时线路点
     * @param point
     */
    private void drawRealtimePoint(LatLng point){
        Log.i("TGA","绘制成功");
        baiduMap.clear();
        MapStatus mapStatus = new MapStatus.Builder().target(point).zoom(25).build();
        msUpdate = MapStatusUpdateFactory.newMapStatus(mapStatus);
        realtimeBitmap = BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
        overlay = new MarkerOptions().position(point)
                .icon(realtimeBitmap).zIndex(9).draggable(true);


        if(pointList.size() >= 2  && pointList.size() <= 6000){
            polyline = new PolylineOptions().width(10).color(Color.GREEN).points(pointList);
        }

        addMarker();

    }


    private void addMarker(){
        if(stLatLng!=null){
            BitmapDescriptor bitmap =  BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
            OverlayOptions option = new MarkerOptions().position(stLatLng).icon(bitmap);
            baiduMap.addOverlay(option);
        }


        if(msUpdate != null){
            baiduMap.setMapStatus(msUpdate);
        }

        if(polyline != null){
            baiduMap.addOverlay(polyline);
        }

        if(overlay != null){
            baiduMap.addOverlay(overlay);
        }


    }


    /**
     * 启动刷新线程
     * @param isStart
     */
    private void startRefreshThread(boolean isStart){

        if(refreshThread == null){
            refreshThread = new RefreshThread();
        }

        refreshThread.refresh = isStart;

        if(isStart){
            if(!refreshThread.isAlive()){
                refreshThread.start();
            }
        }
        else{
            refreshThread = null;
        }


    }
    public void onGetGeoCodeResult(GeoCodeResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(JustMap.this, "抱歉，未能找到结果", Toast.LENGTH_LONG)
                    .show();
            return;
        }
        baiduMap.clear();
        //定位
        String strInfo = String.format("纬度：%f 经度：%f",
                result.getLocation().latitude, result.getLocation().longitude);
        Toast.makeText(JustMap.this, strInfo, Toast.LENGTH_LONG).show();
        //click()


        //result保存地理编码的结果 城市-->坐标
    }


    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(JustMap.this, "抱歉，未能找到结果", Toast.LENGTH_LONG)
                    .show();
            return;
        }
        Toast.makeText(JustMap.this, result.getAddress(),
                Toast.LENGTH_LONG).show();
        Log.i("MYWEIZHI",result.getAddress());

        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        SimpleDateFormat format;
        format=new SimpleDateFormat("HH:mm:ss");
        String str = format.format(curDate);
        Log.e("time","time2"+str);
        //将时间位置信息保存到list中
        LatBean a =new LatBean(str,result.getAddress().toString());
        mList.add(a);
        //result保存翻地理编码的结果 坐标-->城市
    }




    /**
     * 获取手机的Imei码，作为实体对象的标记值
     * @param context
     * @return
     */

    private String getImei(Context context){
        String mImei = "NULL";
        try {
            mImei = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
        } catch (Exception e) {
            System.out.println("获取IMEI码失败");
            mImei = "NULL";
        }
        return mImei;
    }






    @Override
    protected void onStart()
    {
        // 如果要显示位置图标,必须先开启图层定位
        baiduMap.setMyLocationEnabled(true);

        super.onStart();
    }
    @Override
    protected void onStop()
    {
        baiduMap.setMyLocationEnabled(false);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        if(mList!=null){
            Log.d("CHANTU",mList.size()+"   "+index);

        }
        mList.clear();
        index=0;


    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }


}
