package cn.cloudworkshop.miaoding.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.cloudworkshop.miaoding.R;
import cn.cloudworkshop.miaoding.base.BaseActivity;
import cn.cloudworkshop.miaoding.utils.LogUtils;
import cn.cloudworkshop.miaoding.utils.PermissionUtils;
import cn.qqtheme.framework.picker.DoublePicker;

/**
 * Author：binge on 2017-05-22 14:25
 * Email：1993911441@qq.com
 * Describe：
 */
public class MeasureFormActivity extends BaseActivity {
    @BindView(R.id.img_header_back)
    ImageView imgHeaderBack;
    @BindView(R.id.tv_header_title)
    TextView tvHeaderTitle;
    @BindView(R.id.tv_measure_next)
    TextView tvMeasureNext;
    @BindView(R.id.et_measure_height)
    EditText etMeasureHeight;

    // 是否需要系统权限检测
    private boolean isRequireCheck = true;
    static final String[] permissionStr = new String[]{Manifest.permission.CAMERA};
    //检测权限
    PermissionUtils permissionUtils = new PermissionUtils(this);
    //是否授权
    private boolean isGrant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measure_form);
        ButterKnife.bind(this);
        tvHeaderTitle.setText("用户信息");

    }

    @OnClick({R.id.img_header_back, R.id.tv_measure_next})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_header_back:
                finish();
                break;
            case R.id.tv_measure_next:
                String str = etMeasureHeight.getText().toString().trim();
                if (!TextUtils.isEmpty(str)) {
                    boolean isNumber = str.matches("-?[0-9]+.*[0-9]*");
                    if (isNumber && Float.parseFloat(str) > 140 && Float.parseFloat(str) < 250) {
                        judgePermission();
                        if (isGrant) {
                            Intent intent = new Intent(this, CameraActivity.class);
                            intent.putExtra("height", Float.parseFloat(str));
                            startActivity(intent);
                        }
                    } else {
                        Toast.makeText(this, "请输入正确的身高", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "请输入身高", Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }

    /**
     * 检测权限
     */
    private void judgePermission() {
        if (isRequireCheck) {
            //权限没有授权，进入授权界面
            if (permissionUtils.judgePermissions(permissionStr)) {
                isGrant = false;
                if (Build.VERSION.SDK_INT >= 23) {
                    ActivityCompat.requestPermissions(this, permissionStr, 1);
                } else {
                    permissionUtils.showPermissionDialog("读写内存");
                }
            } else {
                isGrant = true;
            }
        } else {
            isRequireCheck = true;
        }
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
            isGrant = true;
            judgePermission();
            if (isGrant) {
                Intent intent = new Intent(this, CameraActivity.class);
                intent.putExtra("height", Float.parseFloat(etMeasureHeight.getText().toString().trim()));
                startActivity(intent);
            }
        } else {
            isRequireCheck = true;
            permissionUtils.showPermissionDialog("读写内存");
            isGrant = false;
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

    @Override
    public boolean shouldShowRequestPermissionRationale(String permission) {
        return false;
    }
}
