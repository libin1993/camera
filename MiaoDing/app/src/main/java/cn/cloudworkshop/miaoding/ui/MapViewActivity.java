package cn.cloudworkshop.miaoding.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptor;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.MyLocationStyle;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.umeng.analytics.MobclickAgent;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.MultiItemTypeAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.cloudworkshop.miaoding.R;
import cn.cloudworkshop.miaoding.base.BaseActivity;
import cn.cloudworkshop.miaoding.bean.LocationBean;
import cn.cloudworkshop.miaoding.constant.Constant;
import cn.cloudworkshop.miaoding.utils.PermissionUtils;
import cn.cloudworkshop.miaoding.utils.SharedPreferencesUtils;
import okhttp3.Call;

/**
 * Author：Libin on 2016/11/3 14:46
 * Email：1993911441@qq.com
 * Describe：预约地图界面
 */
public class MapViewActivity extends BaseActivity implements LocationSource,
        AMapLocationListener, PoiSearch.OnPoiSearchListener, AMap.OnCameraChangeListener {
    @BindView(R.id.map_view)
    MapView mMapView;
    AMap aMap;
    @BindView(R.id.img_header_back)
    ImageView imgHeaderBack;
    @BindView(R.id.tv_header_title)
    TextView tvHeaderTitle;
    @BindView(R.id.et_current_address)
    EditText etCurrentAddress;
    @BindView(R.id.rv_search_address)
    RecyclerView rvSearch;
    @BindView(R.id.img_search_address)
    ImageView imgSearch;
    @BindView(R.id.tv_submit_appointment)
    TextView tvSubmit;

    //定位需要的声明
    private AMapLocationClient mLocationClient = null;//定位发起端
    private AMapLocationClientOption mLocationOption = null;//定位参数
    private OnLocationChangedListener mListener = null;//定位监听器

    //标识，用于判断是否只显示一次定位信息和用户重新定位
    private boolean isFirstLoc = true;
    //商品名称
    private String goodsName;
    //地址
    private String location = "";
    //搜索地图
    private boolean isSearch;
    //是否定位
    private boolean isLocation;
    // 是否需要系统权限检测
    private boolean isRequireCheck = true;
    //危险权限（运行时权限）
    static final String[] permissionStr = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION
    };

    PermissionUtils permissionUtils = new PermissionUtils(this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view);
        ButterKnife.bind(this);
        mMapView.onCreate(savedInstanceState);
        aMap = mMapView.getMap();
        tvHeaderTitle.setText("预约量体");
        getData();
        setUpMap();

        if (isRequireCheck) {
            //权限没有授权，进入授权界面
            if (Build.VERSION.SDK_INT >= 23) {
                if (permissionUtils.judgePermissions(permissionStr)) {
                    ActivityCompat.requestPermissions(this, permissionStr, 1);
                }
            } else {
                permissionUtils.showPermissionDialog();
            }
        }

        //开始定位
        initLocation();

    }


    /**
     * 自定义系统定位小蓝点
     */
    private void setUpMap() {

        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.mipmap.icon_mark1));// 设置小蓝点的图标
        myLocationStyle.strokeColor(Color.BLUE);// 设置圆形的边框颜色
        myLocationStyle.radiusFillColor(Color.argb(30, 0, 0, 40));// 设置圆形的填充颜色
        myLocationStyle.strokeWidth(0.5f);// 设置圆形的边框粗细
        aMap.setMyLocationStyle(myLocationStyle);

        //设置定位监听
        aMap.setLocationSource(this);
        // 是否显示定位按钮
        aMap.getUiSettings().setMyLocationButtonEnabled(true);
        // 是否可触发定位并显示定位层
        aMap.setMyLocationEnabled(true);
        aMap.setOnCameraChangeListener(this);

    }

    /**
     * 用户权限处理,
     * 如果全部获取, 则直接过.
     * 如果权限缺失, 则提示Dialog.
     *
     * @param requestCode  请求码
     * @param permissions  权限
     * @param grantResults 结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1 && hasAllPermissionsGranted(grantResults)) {
            isRequireCheck = false;
        } else {
            isRequireCheck = true;
            permissionUtils.showPermissionDialog();
        }
    }

    // 含有全部的权限
    private boolean hasAllPermissionsGranted(int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    private void getData() {
        goodsName = getIntent().getStringExtra("goods_name");
    }


    //定位
    private void initLocation() {
        //初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());
        //设置定位回调监听
        mLocationClient.setLocationListener(this);
        //初始化定位参数
        mLocationOption = new AMapLocationClientOption();
        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true);
        //设置是否只定位一次,默认为false
        mLocationOption.setOnceLocation(false);
        //设置是否强制刷新WIFI，默认为强制刷新
        mLocationOption.setWifiActiveScan(true);
        //设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.setMockEnable(false);
        //设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.setInterval(2000);
        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        //启动定位
        mLocationClient.startLocation();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，实现地图生命周期管理
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation != null) {
            if (aMapLocation.getErrorCode() == 0) {

                // 如果不设置标志位，此时再拖动地图时，它会不断将地图移动到当前的位置
                if (isFirstLoc) {
                    aMap.clear();
                    isLocation = true;
                    //设置缩放级别
                    aMap.moveCamera(CameraUpdateFactory.zoomTo(18));
                    //将地图移动到定位点
                    aMap.moveCamera(CameraUpdateFactory.changeLatLng(
                            new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude())));
                    //点击定位按钮 能够将地图的中心移动到定位点
                    mListener.onLocationChanged(aMapLocation);
                    setMark(aMapLocation);
                    //获取定位信息
                    StringBuilder sb = new StringBuilder();
                    sb.append(aMapLocation.getProvince()).append("")
                            .append(aMapLocation.getCity()).append("")
                            .append(aMapLocation.getDistrict()).append("")
                            .append(aMapLocation.getStreet()).append("")
                            .append(aMapLocation.getStreetNum());
                    location = sb.toString();
                    if (TextUtils.isEmpty(location)) {
                        ActivityCompat.requestPermissions(this, permissionStr, 1);
                    }
                    etCurrentAddress.setText(sb.toString());
                    etCurrentAddress.setSelection(etCurrentAddress.getText().length());
                    isFirstLoc = false;
                }
            }
        }

    }


    /**
     * @param aMapLocation
     * @return
     */
    private void setMark(AMapLocation aMapLocation) {

        ArrayList<BitmapDescriptor> gifList = new ArrayList<>();
        gifList.add(BitmapDescriptorFactory.fromResource(R.mipmap.icon_mark1));
        gifList.add(BitmapDescriptorFactory.fromResource(R.mipmap.icon_mark2));
        gifList.add(BitmapDescriptorFactory.fromResource(R.mipmap.icon_mark3));

        MarkerOptions markerOption1 = new MarkerOptions().anchor(0.5f, 0.5f)
                .position(new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude()))
                .icons(gifList)
                .period(4);

        aMap.addMarker(markerOption1);
    }


    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
    }

    @Override
    public void deactivate() {
        mListener = null;
    }


    @OnClick({R.id.img_search_address, R.id.tv_submit_appointment, R.id.img_header_back})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_search_address:
                //1.获得用户输入数据
                String address = etCurrentAddress.getText().toString().trim();
                //2.判断用户是否输入为空
                if (TextUtils.isEmpty(address)) {
                    Toast.makeText(this, "请输入查询条件", Toast.LENGTH_LONG).show();
                } else {
                    //3.不为空进行搜索
                    search(address);
                }
                break;
            case R.id.tv_submit_appointment:
                if (TextUtils.isEmpty(location) && TextUtils.isEmpty(etCurrentAddress.getText().toString().trim())) {
                    Toast.makeText(this, "请检查网络是否畅通或定位权限是否授予", Toast.LENGTH_SHORT).show();
                    ActivityCompat.requestPermissions(this, permissionStr, 1);
                } else {
                    if (TextUtils.isEmpty(etCurrentAddress.getText().toString().trim())) {
                        Toast.makeText(this, "请输入地址", Toast.LENGTH_LONG).show();
                    } else {
                        showPopupWindow();
                    }
                }
                break;
            case R.id.img_header_back:
                finish();
                break;
        }
    }


    /**
     * 预约成功
     */
    private void showPopupWindow() {
        tvSubmit.setEnabled(false);

        Map<String, String> map = new HashMap<>();
        map.put("token", SharedPreferencesUtils.getString(this, "token"));
        map.put("address", etCurrentAddress.getText().toString());
        if (goodsName != null) {
            map.put("goods_name", goodsName);
        }

        OkHttpUtils.post()
                .url(Constant.APPOINTMENT_ORDER)
                .params(map)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {

                    }

                    @Override
                    public void onResponse(String response, final int id) {
                        MobclickAgent.onEvent(MapViewActivity.this, "measure");
                        if (goodsName != null) {
                            setResult(1, getIntent());
                        }
                        tvSubmit.setEnabled(true);
                        Intent intent = new Intent(MapViewActivity.this, AppointmentActivity.class);
                        intent.putExtra("type", "appoint");
                        finish();
                        startActivity(intent);

                    }
                });

    }


    /**
     * @param keyword 查找地址
     */
    private void search(String keyword) {

        // 初始化查询条件
        PoiSearch.Query query = new PoiSearch.Query(keyword, "", "");
        query.setPageSize(10);
        query.setPageNum(1);

        // 查询兴趣点
        PoiSearch search = new PoiSearch(this, query);
        // 异步搜索
        search.searchPOIAsyn();
        search.setOnPoiSearchListener(this);
    }

    @Override
    public void onPoiSearched(PoiResult poiResult, int i) {
        final List<LocationBean> addressList = new ArrayList<LocationBean>();
        final List<PoiItem> items = poiResult.getPois();
        if (items != null && items.size() > 0) {
            for (int j = 0, count = items.size(); j < count; j++) {
                addressList.add(new LocationBean(items.get(j).getTitle(), items.get(j).getSnippet()));
            }

            rvSearch.setLayoutManager(new LinearLayoutManager(this));
            final CommonAdapter<LocationBean> adapter = new CommonAdapter<LocationBean>(this,
                    R.layout.listitem_search_address, addressList) {
                @Override
                protected void convert(ViewHolder holder, LocationBean addressBean, int position) {
                    holder.setText(R.id.tv_item_address, addressBean.getTitle());
                    holder.setText(R.id.tv_item_content, addressBean.getText());
                }

            };
            rvSearch.setAdapter(adapter);
            rvSearch.setVisibility(View.VISIBLE);

            adapter.setOnItemClickListener(new MultiItemTypeAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
                    etCurrentAddress.setText(addressList.get(position).getTitle());
                    etCurrentAddress.setSelection(etCurrentAddress.getText().length());
                    rvSearch.setVisibility(View.GONE);
                    isSearch = true;
                    aMap.moveCamera(CameraUpdateFactory.zoomTo(18));
                    //将地图移动到定位点
                    aMap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(items.get(position).
                            getLatLonPoint().getLatitude(), items.get(position).getLatLonPoint().getLongitude())));
                    //点击定位按钮 能够将地图的中心移动到定位点

                }

                @Override
                public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
                    return false;
                }
            });
        }
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

    }

    @Override
    public void onCameraChangeFinish(CameraPosition cameraPosition) {

        if (!isSearch || !isLocation) {
            LatLng target = cameraPosition.target;
            GeocodeSearch geocodeSearch = new GeocodeSearch(this);
            geocodeSearch.setOnGeocodeSearchListener(new GeocodeSearch.OnGeocodeSearchListener() {

                @Override
                public void onGeocodeSearched(GeocodeResult result, int rCode) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
                    String formatAddress = result.getRegeocodeAddress().getFormatAddress();
                    etCurrentAddress.setText(formatAddress);
                    etCurrentAddress.setSelection(etCurrentAddress.getText().length());
                }
            });
            LatLonPoint lp = new LatLonPoint(target.latitude, target.longitude);
            RegeocodeQuery query = new RegeocodeQuery(lp, 200, GeocodeSearch.AMAP);
            geocodeSearch.getFromLocationAsyn(query);
        } else {
            isSearch = false;
            isLocation = false;
        }

    }

}
