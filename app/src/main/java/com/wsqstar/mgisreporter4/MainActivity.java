package com.wsqstar.mgisreporter4;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;
//import android.view.WindowManager;
//import android.widget.TextView;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;

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

        //////////////////////////////////////////////////////////////////////////////////////////////
        //以下是权限检查 最好是先别动了 因为 不知道原因与结构 但是现在还是可以使用的 只需要打开设置，打开定位权限即可
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
        List<String> permissionList = new ArrayList<>();
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(!permissionList.isEmpty()){
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this,permissions,1);
        }else {
//            requestLocation();
            return;
        }
        //权限检查完毕
        ////////////////////////////////////////////////////////////////////////////////
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


//        //定义Maker坐标点
//        LatLng point = new LatLng(39.963175, 116.400244);
//
//        //构建Marker图标
//        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.ic_icon_geo);
//
//        //构建MarkerOption，用于在地图上添加Marker
//        OverlayOptions option = new MarkerOptions()
//                .position(point)
//                .icon(bitmap);
//
//        //在地图上添加Marker，并显示
//        mBaiduMap.addOverlay(option);

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
            locationMode = MyLocationConfiguration.LocationMode.NORMAL;//设置定位模式//locationMode是一个全局变量
            MyLocationConfiguration config = new MyLocationConfiguration(locationMode,true,bitmapDescriptor);//设置构造方式//有三个参数，定位模式，true，自定义的图标
            mBaiduMap.setMyLocationConfiguration(config);//显示定位图标

            //baidu lbs 范例 http://lbsyun.baidu.com/index.php?title=androidsdk/guide/create-map/location
//            // 设置定位图层的配置（定位模式，是否允许方向信息，用户自定义定位图标）
//            BitmapDescriptor mCurrentMarker = BitmapDescriptorFactory
//                    .fromResource(R.drawable.ic_icon_geo);
//            MyLocationConfiguration config = new MyLocationConfiguration(mCurrentMode, true, mCurrentMarker);
//            mBaiduMap.setMyLocationConfiguration();

        }else { //否则输出空信息
            Log.i("Location","没有获取到GPS信息");
        }
    }
////////////////////////////////////////////////////////////////////////////////////////////////
//菜单栏目的设置
    @Override
    //首先是使用getMenuInflater（）方法获取到MenuInflater对象
    //然后调用它？的inflate（）方法创建当前活动菜单
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);//inflate（）方法接受两个参数，第一个用于指定我们通过哪一个资源文件来创建菜单，这里传入R.menu.main,第二个参数用于指定我们的菜单项目将要添加到哪一个Menu对象中
        return true;
    }
    //定义菜单相应事件，与上面的onCreateOptionsMenu(Menu menu) 互相配合
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.add_item_state:
                Toast.makeText(this,"You clicked Statelite，给你看看卫星图层", Toast.LENGTH_SHORT).show();
                //显示卫星图层
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.remove_item_normal:
                Toast.makeText(this,"You clicked Normal,返回正常地图", Toast.LENGTH_SHORT).show();
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                break;
            default:
        }
        return true;
    }
//结束菜单栏目的设置以及功能管理
//////////////////////////////////////////////////////////////////////////////////////////////




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
        mMapView=null;
        mMapView.onDestroy();

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
//    @Override
//    Button mButton_report=(Button)findViewById(R.id.frame_button_report);



}
