package com.wsqstar.mgisreporter4;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
//import android.view.WindowManager;
//import android.widget.TextView;
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

//首先是 布局界面并显示百度地图
//然后 实时获取定位信息中的经度和纬度
//最后 启用定位功能标记我的位置
public class MainActivity extends AppCompatActivity {

    private MapView mMapView;//声明地图组件
    private BaiduMap mBaiduMap;//定义百度地图对象
//    private TextView textView;//定义用于显示LocationProvider名称的TextView组件
//    private TextView text;
    private boolean isFirstLoc=true;//记录是否是第一次定位
    private MyLocationConfiguration.LocationMode locationMode;//当前定位模式

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏显示
        //textView = (TextView) findViewById(R.id.provider);
        SDKInitializer.initialize(getApplicationContext());//初始化地图SDK//放置在setContentView之前
        setContentView(R.layout.activity_main);
        mMapView = findViewById(R.id.bmapView);//获取地图组件
        mBaiduMap = mMapView.getMap();//获取百度地图对象
        //获取系统的LocationManager对象
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        //添加权限检查
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
//        locationManager.getProvider(LocationManager.GPS_PROVIDER);//huoqu
//        textView//文本显示location provider

        //注册一个监听器
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,//指定GPS定位的提供者
                1000,//间隔时间1000毫秒
                1,//位置间隔1米
                new LocationListener() {//监听GPS定位信息是否改变
                    @Override
                    public void onLocationChanged(Location location) {//GPS信息发生改变时回调

                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {//GPS状态发生改变时回调

                    }

                    @Override
                    public void onProviderEnabled(String provider) {//定位提供者启动时回调

                    }

                    @Override
                    public void onProviderDisabled(String provider) {//定位提供者关闭时回调

                    }
                }

        );
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);//从GPS获取最新的定位信息
        locationUpdates(location);//将最新的定位信息 传递 给location//这样就可以获取定位信息了
    }

    //字符信息
    public void locationUpdates(Location location){//获取指定的查询信息
        if(location!=null){
//            StringBuilder stringBuilder=new StringBuilder();//创建一个字符串构建器，用于记录定位信息
//            stringBuilder.append("your location is: \n");
//            stringBuilder.append("经度：");
//            stringBuilder.append(location.getAltitude());
//            text.setText(stringBuilder.toString());//显示到页面上
            LatLng ll = new LatLng(location.getLatitude(),location.getLongitude());//获取用户当前经纬度
            Log.i("Location","纬度："+location.getLatitude()+"| 经度："+location.getLongitude());//通过日志输出经纬度的值
            if(isFirstLoc){
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);//更新坐标位置
                mBaiduMap.animateMapStatus(u);//设置地图位置
                isFirstLoc=false;//取消第一次定位
            }
            //构造定位数据//方向 纬度 经度
            MyLocationData locData=new MyLocationData.Builder().accuracy(location.getAccuracy())//accuray设置经度
                    .direction(100)//设置方向信息
            .latitude(location.getLatitude())//设置纬度坐标
            .longitude(location.getLongitude())//设置经度坐标
            .build();
            mBaiduMap.setMyLocationData(locData);//设置定位数据
            BitmapDescriptor bitmapDescriptor= BitmapDescriptorFactory.fromResource(R.drawable.ic_icon_geo);//设置自定义定位图标
            locationMode=MyLocationConfiguration.LocationMode.NORMAL;//设置定位模式//locationMode是一个全局变量
            MyLocationConfiguration config=new MyLocationConfiguration(locationMode,true,bitmapDescriptor);//设置构造方式//有三个参数，定位模式，true，自定义的图标
            mBaiduMap.setMyLocationConfiguration(config);//显示定位图标

        }else { //否则输出空信息
            Log.i("Location","没有获取到GPS信息");
        }
    }

    //实现地图生命周期管理
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
        mMapView=null;
    }
    @Override
    protected void onStart() {
        super.onStart();
        //在activity执行onStart时执行mMapView. onStart ()，实现地图生命周期管理
        mBaiduMap.setMyLocationEnabled(true);//开启定位图层//boolean类型参数
    }
    protected void onStop() {
        super.onStop();
        //在activity执行onStop时执行mMapView. onStop ()，实现地图生命周期管理
        mBaiduMap.setMyLocationEnabled(false);//停止定位图层
    }
}
