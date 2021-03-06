package com.wsqstar.mgisreporter4;
//目前的功能
/*
* 定位
* 罗盘、普通、跟随
* 切换定位
* POI检索（但是不能返回本地位置）
* 现在的bug有
* POI功能融合不完善 位置信息传输失误
* MapControl 保持闪退
* */
import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Toast;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

//首先是 布局界面并显示百度地图
//然后 实时获取定位信息中的经度和纬度
//最后 启用定位功能标记我的位置
public class MainActivity extends AppCompatActivity implements SensorEventListener {//MainActivity 最好是继承AppCompatActivity 这样的话，才有状态栏
    // 定位相关
    LocationClient mLocClient;
    public MyLocationListenerM myListener = new MyLocationListenerM();//为了区分demo与MainActivity 直接在函数名称后面加上M，表示来自MainActivity
    private MyLocationConfiguration.LocationMode mCurrentMode;
    BitmapDescriptor mCurrentMarker;
    private static final int accuracyCircleFillColor = 0xAAFFFF88;
    private static final int accuracyCircleStrokeColor = 0xAA00FF00;
    private SensorManager mSensorManager;//传感器相关
    private Double lastX = 0.0;
    private int mCurrentDirection = 0;
    private double mCurrentLat = 0.0;
    private double mCurrentLon = 0.0;
    private float mCurrentAccracy;
    private final int SDK_PERMISSION_REQUEST = 127;
    private ListView FunctionList;
    private String permissionInfo;

    /**
     * MapView 是地图主控件
     */
    private MapView mMapView;//声明地图组件
    private BaiduMap mBaiduMap;//定义百度地图对象


    //控制按钮
    private Button saveScreenButtonM;//保存截图

//    ///////////////////////////////////
    //来自个性化地图DEMO//BaseMapDemo
    private boolean mEnableCustomStyle = true;
    private static final int OPEN_ID = 0;
    private static final int CLOSE_ID = 1;
    //用于设置个性化地图的样式文件
    // 精简为1套样式模板:
    // "custom_config_dark.json"
    private static String PATH = "custom_config_dark.json";
    private static int icon_themeId = 1;
    //个性化地图DEMO模块结束
    ///////////////////////////////////


    //UI相关
    RadioGroup.OnCheckedChangeListener radioButtonListener;
    RadioGroup.OnCheckedChangeListener radioButtonListener2;
    Button requestLocButton;
    //    private TextView textView;//定义用于显示LocationProvider名称的TextView组件
//    private TextView text;
    private boolean isFirstLoc = true;//记录是否是第一次定位
    private MyLocationData locData;
    private float direction;
    //private MyLocationConfiguration.LocationMode locationMode;//当前定位模式

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //设置全屏显示
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏显示

        //textView = (TextView) findViewById(R.id.provider);
        SDKInitializer.initialize(getApplicationContext());//初始化地图SDK//放置在setContentView之前//demo里面好像没有？
        //来自BaseMapDemo 为了设置文件
        setMapCustomFile(this, PATH);

        setContentView(R.layout.activity_main);
        // after andrioid m,must request Permiision on runtime
        getPersimmions();

        //开关，设置自定义视图是否可见
        MapView.setMapCustomEnable(true);

       showForwardView(R.string.text_forward,true);

        requestLocButton = (Button) findViewById(R.id.button1_changeLocModeM);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);//获取传感器管理服务//Demo
        mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;//设置当前的定位模式

        //以下是有关切换定位模式的代码//LocationDemo
        requestLocButton.setText("普通");
        View.OnClickListener btnClickListener = new View.OnClickListener() {
            public void onClick(View v) {
                switch (mCurrentMode) {
                    case NORMAL:
                        requestLocButton.setText("跟随");
                        mCurrentMode = MyLocationConfiguration.LocationMode.FOLLOWING;
                        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(
                                mCurrentMode, true, mCurrentMarker));
                        MapStatus.Builder builder = new MapStatus.Builder();
                        builder.overlook(0);
                        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                        break;
                    case COMPASS:
                        requestLocButton.setText("普通");//如果点击现在显示普通
                        mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;//那么定位模式转换为普通
                        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(
                                mCurrentMode, true, mCurrentMarker));//使用默认的marker
                        MapStatus.Builder builder1 = new MapStatus.Builder();
                        builder1.overlook(0);
                        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder1.build()));
                        break;
                    case FOLLOWING:
                        requestLocButton.setText("罗盘");
                        mCurrentMode = MyLocationConfiguration.LocationMode.COMPASS;
                        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(
                                mCurrentMode, true, mCurrentMarker));
                        break;
                    default:
                        break;
                }
            }
        };
        requestLocButton.setOnClickListener(btnClickListener);
        //

        //接下来的代码修改图标格式
        RadioGroup group = (RadioGroup) this.findViewById(R.id.radioGroup);
        radioButtonListener = new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.defaulticon) {
                    // 传入null则，恢复默认图标
                    mCurrentMarker = null;
                    mBaiduMap
                            .setMyLocationConfigeration(new MyLocationConfiguration(
                                    mCurrentMode, true, null));
                }
                if (checkedId == R.id.customicon) {
                    // 修改为自定义marker
                    mCurrentMarker = BitmapDescriptorFactory
                            .fromResource(R.drawable.ic_icon_geo);
                    mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(
                            mCurrentMode, true, mCurrentMarker,
                            accuracyCircleFillColor, accuracyCircleStrokeColor));
                }
            }
        };
        group.setOnCheckedChangeListener(radioButtonListener);

                //这一小段代码更改个性化地图//但是好像实际上没有被实现功能，也许是调用顺序的问题
        RadioGroup group_custom_map = (RadioGroup) this.findViewById(R.id.radioGroup2);
        radioButtonListener2 = new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group_custom_map, int checkedId) {
                if (checkedId == R.id.default_map) {
                    MapView.setMapCustomEnable(true);
                    mBaiduMap.setCustomTrafficColor("#ffbb0101", "#fff33131", "#ffff9e19", "#00000000");
                    MapStatusUpdate u = MapStatusUpdateFactory.zoomTo(13);
                    mBaiduMap.animateMapStatus(u);
                } else if (checkedId == R.id.custom_map) {
                    MapView.setMapCustomEnable(false);
                }
            }
        };
        group_custom_map.setOnCheckedChangeListener(radioButtonListener2);


        //下面是对于截图功能的实现
        saveScreenButtonM = (Button) findViewById(R.id.savescreenM);
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.equals(saveScreenButtonM)) {
                    // 截图，在SnapshotReadyCallback中保存图片到 sd 卡
                    mBaiduMap.snapshot(new BaiduMap.SnapshotReadyCallback() {
                        public void onSnapshotReady(Bitmap snapshot) {
                            File file = new File("/mnt/sdcard/test.png");
                            FileOutputStream out;
                            try {
                                out = new FileOutputStream(file);
                                if (snapshot.compress(
                                        Bitmap.CompressFormat.PNG, 100, out)) {
                                    out.flush();
                                    out.close();
                                }
                                Toast.makeText(MainActivity.this,
                                        "屏幕截图成功，图片存在: " + file.toString(),
                                        Toast.LENGTH_SHORT).show();
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    Toast.makeText(MainActivity.this, "正在截取屏幕图片...",
                            Toast.LENGTH_SHORT).show();

                }
//                updateMapState();
            }

        };
        saveScreenButtonM.setOnClickListener(onClickListener);

        //





        //地图初始化//demo
        mMapView = findViewById(R.id.bmapView);//获取地图组件
        mBaiduMap = mMapView.getMap();//获取百度地图对象
        //开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        // 定位初始化
        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);
        mLocClient.setLocOption(option);
        mLocClient.start();
//      initListener();
    }

    @TargetApi(25)
    private void getPersimmions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ArrayList<String> permissions = new ArrayList<String>();
            /***
             * 定位权限为必须权限，用户如果禁止，则每次进入都会申请
             */
            // 定位精确位置
            if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
            if(checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            }
			/*
			 * 读写权限和电话状态权限非必要权限(建议授予)只会申请一次，用户同意或者禁止，只会弹一次
			 */
            // 读写权限
            if (addPermission(permissions, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                permissionInfo += "Manifest.permission.WRITE_EXTERNAL_STORAGE Deny \n";
            }
            // 读取电话状态权限
            if (addPermission(permissions, Manifest.permission.READ_PHONE_STATE)) {
                permissionInfo += "Manifest.permission.READ_PHONE_STATE Deny \n";
            }

            if (permissions.size() > 0) {
                requestPermissions(permissions.toArray(new String[permissions.size()]), SDK_PERMISSION_REQUEST);
            }
        }
    }

    @TargetApi(25)
    private boolean addPermission(ArrayList<String> permissionsList, String permission) {
        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) { // 如果应用没有获得对应权限,则添加到列表中,准备批量申请
            if (shouldShowRequestPermissionRationale(permission)){
                return true;
            }else{
                permissionsList.add(permission);
                return false;
            }

        }else{
            return true;
        }
    }

    @TargetApi(25)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // TODO Auto-generated method stub
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }


       

        @Override//LocationDemo 当传感器改变的时候
        public void onSensorChanged(SensorEvent sensorEvent) {
            double x = sensorEvent.values[SensorManager.DATA_X];
            if (Math.abs(x - lastX) > 1.0) {
             mCurrentDirection = (int) x;
             locData = new MyLocationData.Builder()
                      .accuracy(mCurrentAccracy)
                      // 此处设置开发者获取到的方向信息，顺时针0-360
                     .direction(mCurrentDirection).latitude(mCurrentLat)
                       .longitude(mCurrentLon).build();
             mBaiduMap.setMyLocationData(locData);
             }
             lastX = x;
        }

        @Override//当定位精度改变的时候
        public void onAccuracyChanged (Sensor sensor,int i){

        }

        /**
         * 定位SDK监听函数//demo
         */
        public class MyLocationListenerM implements BDLocationListener {

            @Override
            public void onReceiveLocation(BDLocation location) {
                // map view 销毁后不再处理新接收的位置
                if (location == null || mMapView == null) {
                    return;
                }
                mCurrentLat = location.getLatitude();
                mCurrentLon = location.getLongitude();
                mCurrentAccracy = location.getRadius();
                locData = new MyLocationData.Builder()
                        .accuracy(location.getRadius())
                        // 此处设置开发者获取到的方向信息，顺时针0-360
                        .direction(mCurrentDirection).latitude(location.getLatitude())
                        .longitude(location.getLongitude()).build();
                mBaiduMap.setMyLocationData(locData);
                if (isFirstLoc) {
                    isFirstLoc = false;
                    LatLng ll = new LatLng(location.getLatitude(),
                            location.getLongitude());
                    MapStatus.Builder builder = new MapStatus.Builder();
                    builder.target(ll).zoom(18.0f);//放大等级 18倍
                    mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));//animate 推动
                }
            }

            public void onReceivePoi(BDLocation poiLocation) {//似乎是POI接口
            }
        }
// 

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //个性化地图相关代码//BaseMapDemo
    // 设置个性化地图config文件路径
    private void setMapCustomFile(Context context, String PATH) {
        FileOutputStream out = null;
        InputStream inputStream = null;
        String moduleName = null;
        try {
            inputStream = context.getAssets()
                    .open("customConfigdir/" + PATH);
            byte[] b = new byte[inputStream.available()];
            inputStream.read(b);

            moduleName = context.getFilesDir().getAbsolutePath();
            File f = new File(moduleName + "/" + PATH);
            if (f.exists()) {
                f.delete();
            }
            f.createNewFile();
            out = new FileOutputStream(f);
            out.write(b);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        MapView.setCustomMapStylePath(moduleName + "/" + PATH);
        MapView.setMapCustomEnable(true);

    }

    /**
     * 设置个性化icon
     *
     * @param context
     * @param icon_themeId
     */
    private void setIconCustom(Context context, int icon_themeId){

        MapView.setIconCustom(icon_themeId);
    }
    //个性化地图 相关代码结束
    ////////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //菜单栏的设置
    @Override
    //首先是使用getMenuInflater（）方法获取到MenuInflater对象
    //然后调用它？的inflate（）方法创建当前活动菜单
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);//inflate（）方法接受两个参数，第一个用于指定我们通过哪一个资源文件来创建菜单，这里传入R.menu.main,第二个参数用于指定我们的菜单项目将要添加到哪一个Menu对象中
        return true;
    }

    //定义菜单相应事件，与上面的onCreateOptionsMenu(Menu menu) 互相配合
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_item_state:
                Toast.makeText(this, "You clicked Statelite，给你看看卫星图层", Toast.LENGTH_SHORT).show();
                //显示卫星图层
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.remove_item_normal:
                Toast.makeText(this, "You clicked Normal,返回正常地图", Toast.LENGTH_SHORT).show();
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                break;
            case R.id.item_traffic:
                Toast.makeText(this, "You clicked Traffic,前往交通路况地图", Toast.LENGTH_SHORT).show();
                mBaiduMap.setTrafficEnabled(true);
                mBaiduMap.setBaiduHeatMapEnabled(false);
                break;
            case R.id.item_heat:
                Toast.makeText(this, "You clicked Heat,前往热力地图", Toast.LENGTH_SHORT).show();
                mBaiduMap.setBaiduHeatMapEnabled(true);
                mBaiduMap.setTrafficEnabled(false);
                break;
            case R.id.Jump2LocDemo:
                Toast.makeText(this, "You clicked LocDemo,前往定位地图", Toast.LENGTH_SHORT).show();
                Intent intent_loc = new Intent(MainActivity.this, LocationDemo.class);
                startActivity(intent_loc);
                break;//每次选择结束都需要break
            case R.id.Jump2PoiSearchDemo:
                Toast.makeText(this, "You clicked PoiDemo,前往Poi检索地图", Toast.LENGTH_SHORT).show();
                Intent intent_poi = new Intent(MainActivity.this, PoiSearchDemo.class);
//                intent_poi.putExtra("mLocate_lat",mCurrentLat);
//                intent_poi.putExtra("mLocate_lng",mCurrentLon);
                Bundle bundle_lat = new Bundle();
                Bundle bundle_lon = new Bundle();
                bundle_lat.putDouble("mLocate_lat",mCurrentLat);
                bundle_lon.putDouble("mLocate_lng",mCurrentLon);
                intent_poi.putExtras(bundle_lat);
                intent_poi.putExtras(bundle_lon);

                startActivity(intent_poi);
                break;
            case R.id.Jump2MapCtDemo:
                Toast.makeText(this, "You clicked MapContral,前往控制地图实例", Toast.LENGTH_SHORT).show();
                Intent intent_control = new Intent(MainActivity.this, MapControlDemo.class);
                startActivity(intent_control);
                break;
            case R.id.Jump2Loc:
                Toast.makeText(this, "You clicked LocData,前往地理位置", Toast.LENGTH_SHORT).show();
                Intent intent_location = new Intent(MainActivity.this, LocationActivity.class);
                startActivity(intent_location);
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
        //为系统的方向传感器注册监听器
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        // 退出时销毁定位
        mLocClient.stop();
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        super.onDestroy();

    }

    @Override
    protected void onStart() {
        super.onStart();
        //在activity执行onStart时执行mMapView. onStart ()，实现地图生命周期管理
        mBaiduMap.setMyLocationEnabled(true);//开启定位图层//boolean类型参数
    }

    protected void onStop() {
        //在activity执行onStop时执行mMapView. onStop ()，实现地图生命周期管理
        //去电注册传感器监听
        mSensorManager.unregisterListener(this);
        mBaiduMap.setMyLocationEnabled(false);//停止定位图层
        super.onStop();//onStop需要在后面
    }
}


