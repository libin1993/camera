package cn.cloudworkshop.miaoding.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.wang.avi.AVLoadingIndicatorView;
import com.wang.avi.indicators.BallSpinFadeLoaderIndicator;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.cloudworkshop.miaoding.R;
import cn.cloudworkshop.miaoding.adapter.PhotoAdapter;
import cn.cloudworkshop.miaoding.application.MyApplication;
import cn.cloudworkshop.miaoding.base.BaseActivity;
import cn.cloudworkshop.miaoding.bean.OrderDetailsBean;
import cn.cloudworkshop.miaoding.constant.Constant;
import cn.cloudworkshop.miaoding.utils.GsonUtils;
import cn.cloudworkshop.miaoding.utils.ImageEncodeUtils;
import cn.cloudworkshop.miaoding.utils.PhoneNumberUtils;
import cn.cloudworkshop.miaoding.utils.PermissionUtils;
import cn.cloudworkshop.miaoding.utils.RecyclerItemClickListener;
import cn.cloudworkshop.miaoding.utils.SharedPreferencesUtils;
import me.iwf.photopicker.PhotoPicker;
import me.iwf.photopicker.PhotoPreview;
import okhttp3.Call;

/**
 * Author：binge on 2017/1/5 13:32
 * Email：1993911441@qq.com
 * Describe：售后订单修改
 */
public class ChangeOrderActivity extends BaseActivity {
    @BindView(R.id.tv_header_title)
    TextView tvHeaderTitle;
    @BindView(R.id.img_header_back)
    ImageView imgHeaderBack;
    @BindView(R.id.et_change_order)
    EditText etChangeOrder;
    @BindView(R.id.tv_input_count)
    TextView tvInputCount;
    @BindView(R.id.img_select_photo)
    ImageView imgSelectPhoto;
    @BindView(R.id.rv_select_photo)
    RecyclerView rvSelectPhoto;
    @BindView(R.id.et_input_name)
    EditText etInputName;
    @BindView(R.id.et_input_tel)
    EditText etInputTel;
    @BindView(R.id.tv_next_step)
    TextView tvNextStep;
    @BindView(R.id.sv_change_order)
    ScrollView svChangeOrder;
    @BindView(R.id.tv_consult_phone)
    TextView tvConsultPhone;
    @BindView(R.id.tv_back_sales)
    TextView tvBackSales;
    @BindView(R.id.ll_change_success)
    LinearLayout llChangeSuccess;
    @BindView(R.id.rv_select_goods)
    RecyclerView rvSelectGoods;
    @BindView(R.id.tv_first_next)
    TextView tvFirstNext;
    @BindView(R.id.ll_select_goods)
    LinearLayout llSelectGoods;
    @BindView(R.id.view_loading)
    AVLoadingIndicatorView loadingView;


    //字数限制
    private int num = 300;
    private PhotoAdapter photoAdapter;
    private ArrayList<String> selectedPhotos = new ArrayList<>();
    // 是否需要系统权限检测
    private boolean isRequireCheck = true;
    //危险权限（运行时权限）
    static final String[] permissionStr = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    PermissionUtils permissionUtils = new PermissionUtils(this);

    private String orderId;
    private OrderDetailsBean entity;
    //是否选中
    private boolean[] isSelected;
    private StringBuilder sb = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_order);
        ButterKnife.bind(this);

        getData();
        initData();

    }

    /**
     * 加载数据
     */
    private void initData() {
        tvHeaderTitle.setText("售后服务");
        OkHttpUtils.get()
                .url(Constant.ORDER_DETAIL)
                .addParams("token", SharedPreferencesUtils.getString(this, "token"))
                .addParams("id", orderId)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {

                    }

                    @Override
                    public void onResponse(String response, int id) {
                        entity = GsonUtils.jsonToBean(response, OrderDetailsBean.class);
                        if (entity.getData() != null) {
                            initView();
                        }
                    }
                });
    }

    private void getData() {
        Intent intent = getIntent();
        orderId = intent.getStringExtra("order_id");
    }


    /**
     * 加载视图
     */
    private void initView() {
        loadingView.setIndicator(new BallSpinFadeLoaderIndicator());
        loadingView.setIndicatorColor(Color.GRAY);
        isSelected = new boolean[entity.getData().getCar_list().size()];
        for (int i = 0; i < entity.getData().getCar_list().size(); i++) {
            isSelected[i] = false;
        }
        rvSelectGoods.setLayoutManager(new LinearLayoutManager(this));
        CommonAdapter<OrderDetailsBean.DataBean.CarListBean> adapter = new CommonAdapter<OrderDetailsBean.DataBean.CarListBean>(this,
                R.layout.listitem_shopping_cart, entity.getData().getCar_list()) {
            @Override
            protected void convert(ViewHolder holder, OrderDetailsBean.DataBean.CarListBean carListBean, final int position) {
                holder.setChecked(R.id.checkbox_goods_select, false);
                Glide.with(ChangeOrderActivity.this)
                        .load(Constant.HOST + entity.getData().getCar_list().get(position).getGoods_thumb())
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .into((ImageView) holder.getView(R.id.img_item_goods));
                holder.setText(R.id.tv_goods_name, entity.getData().getCar_list().get(position).getGoods_name());
                switch (entity.getData().getCar_list().get(position).getGoods_type()) {
                    case 2:
                        holder.setText(R.id.tv_goods_content, entity.getData().getCar_list().get(position).getSize_content());
                        break;
                    default:
                        holder.setText(R.id.tv_goods_content, "定制款");
                        break;
                }
                holder.setText(R.id.tv_goods_price, "¥" + entity.getData().getCar_list().get(position).getPrice());
                holder.setText(R.id.tv_goods_count, "x" + entity.getData().getCar_list().get(position).getNum());
                holder.setVisible(R.id.view_cart, true);

                ((CheckBox) holder.getView(R.id.checkbox_goods_select)).
                        setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                                isSelected[position] = b;
                            }
                        });
            }
        };
        rvSelectGoods.setAdapter(adapter);
        tvFirstNext.setVisibility(View.VISIBLE);


        etChangeOrder.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                svChangeOrder.requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

        etInputName.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                changeScrollView();
                return false;
            }
        });
        etInputTel.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                changeScrollView();
                return false;
            }
        });

        etChangeOrder.addTextChangedListener(new TextWatcher() {
            private CharSequence temp;
            private int selectionStart;
            private int selectionEnd;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                temp = charSequence;
            }

            @Override
            public void afterTextChanged(Editable editable) {
                int number = num - editable.length();
                tvInputCount.setText(num - number + "/" + num);
                selectionStart = etChangeOrder.getSelectionStart();
                selectionEnd = etChangeOrder.getSelectionEnd();
                if (temp.length() > num) {
                    editable.delete(selectionStart - 1, selectionEnd);
                    int tempSelection = selectionEnd;
                    etChangeOrder.setText(editable);
                    etChangeOrder.setSelection(tempSelection);//设置光标在最后
                }
            }
        });

        photoAdapter = new PhotoAdapter(this, selectedPhotos);
        rvSelectPhoto.setLayoutManager(new LinearLayoutManager(ChangeOrderActivity.this,
                LinearLayoutManager.HORIZONTAL, false));
        rvSelectPhoto.setAdapter(photoAdapter);
        rvSelectPhoto.addOnItemTouchListener(new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                PhotoPreview.builder()
                        .setPhotos(selectedPhotos)
                        .setCurrentItem(position)
                        .start(ChangeOrderActivity.this);
            }
        }));

        tvConsultPhone.setText("客服热线：" + MyApplication.serverPhone);
    }

    /**
     * 使ScrollView指向底部
     */
    private void changeScrollView() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                svChangeOrder.scrollTo(0, svChangeOrder.getHeight());
            }
        }, 300);
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

    @OnClick({R.id.img_header_back, R.id.img_select_photo, R.id.tv_next_step, R.id.tv_back_sales,
            R.id.tv_first_next})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_header_back:
                finish();
                break;
            case R.id.img_select_photo:
                if (isRequireCheck) {
                    //权限没有授权，进入授权界面
                    if (permissionUtils.judgePermissions(permissionStr)) {
                        if (Build.VERSION.SDK_INT >= 23) {
                            ActivityCompat.requestPermissions(this, permissionStr, 1);
                        } else {
                            permissionUtils.showPermissionDialog();
                        }
                    }
                }
                PhotoPicker.builder()
                        .setPhotoCount(4)
                        .setShowCamera(true)
                        .setSelected(selectedPhotos)
                        .start(this);
                break;
            case R.id.tv_next_step:
                submitData();
                break;
            case R.id.tv_back_sales:
                finish();
                break;
            case R.id.tv_first_next:
                nextStep();
                break;
        }
    }

    /**
     * 提交修改
     */
    private void submitData() {

        if (TextUtils.isEmpty(etChangeOrder.getText().toString().trim())
                || TextUtils.isEmpty(etInputName.getText().toString().trim())
                || !PhoneNumberUtils.judgePhoneNumber(etInputTel.getText().toString().trim())
                || selectedPhotos.size() == 0) {
            Toast.makeText(this, "请按要求填写所有信息", Toast.LENGTH_SHORT).show();
        } else {
            loadingView.smoothToShow();
            tvNextStep.setEnabled(false);
            OkHttpUtils.post()
                    .url(Constant.AFTER_SALE)
                    .addParams("token", SharedPreferencesUtils.getString(this, "token"))
                    .addParams("order_id", orderId)
                    .addParams("content", etChangeOrder.getText().toString().trim())
                    .addParams("name", etInputName.getText().toString().trim())
                    .addParams("phone", etInputTel.getText().toString().trim())
                    .addParams("img_list", ImageEncodeUtils.enCodeFile(selectedPhotos))
                    .addParams("car_id", sb.toString().trim())
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {

                        }

                        @Override
                        public void onResponse(String response, int id) {
                            loadingView.smoothToHide();
                            svChangeOrder.setVisibility(View.GONE);
                            llChangeSuccess.setVisibility(View.VISIBLE);
                        }
                    });
        }

    }

    /**
     * 下一步
     */
    private void nextStep() {
        boolean isChecked = false;
        for (int i = 0; i < isSelected.length; i++) {
            if (isSelected[i]) {
                isChecked = true;
                if (i == isSelected.length - 1) {
                    sb.append(entity.getData().getCar_list().get(i).getId());
                } else {
                    sb.append(entity.getData().getCar_list().get(i).getId()).append(",");
                }
            }
        }
        if (isChecked) {
            llSelectGoods.setVisibility(View.GONE);
            svChangeOrder.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, "请选择商品", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK &&
                (requestCode == PhotoPicker.REQUEST_CODE || requestCode == PhotoPreview.REQUEST_CODE)) {

            List<String> photos = null;
            if (data != null) {
                photos = data.getStringArrayListExtra(PhotoPicker.KEY_SELECTED_PHOTOS);
            }
            selectedPhotos.clear();

            if (photos != null) {
                selectedPhotos.addAll(photos);
                photoAdapter.notifyDataSetChanged();
            }

        }
    }

}
