package cn.cloudworkshop.miaoding.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.TextView;

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
import cn.cloudworkshop.miaoding.base.BaseActivity;
import cn.cloudworkshop.miaoding.bean.LogisticsBean;
import cn.cloudworkshop.miaoding.constant.Constant;
import cn.cloudworkshop.miaoding.utils.GsonUtils;
import okhttp3.Call;

/**
 * Author：binge on 2016/11/17 17:48
 * Email：1993911441@qq.com
 * Describe：物流查询
 */
public class LogisticsActivity extends BaseActivity {
    @BindView(R.id.img_header_back)
    ImageView imgHeaderBack;
    @BindView(R.id.tv_header_title)
    TextView tvHeaderTitle;
    @BindView(R.id.tv_logistics_num)
    TextView tvNumber;
    @BindView(R.id.tv_logistics_company)
    TextView tvCompany;
    @BindView(R.id.rv_logistics)
    RecyclerView rvLogistics;

    //快递单号
    private String number;
    //公司
    private String company;
    //公司名称
    private String companyName;
    //商品图片
    private String imgUrl;
    private List<LogisticsBean.DataBean> dataList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logistics);
        ButterKnife.bind(this);
        getData();
        initData();
    }

    /**
     * 加载数据
     */
    private void initData() {
        tvHeaderTitle.setText("物流追踪");
        tvNumber.setText(number);
        tvCompany.setText(companyName);
        OkHttpUtils.get()
                .url(Constant.LOGISTICS_TRACK)
                .addParams("ems_no", number)
                .addParams("ems_com", company)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {

                    }

                    @Override
                    public void onResponse(String response, int id) {
                        LogisticsBean entity = GsonUtils.jsonToBean(response, LogisticsBean.class);
                        if (entity.getData() != null && entity.getData().size() > 0) {
                            dataList.addAll(entity.getData());
                            initView();
                        }
                    }
                });
    }

    /**
     * 加载视图
     */
    private void initView() {

        rvLogistics.setLayoutManager(new LinearLayoutManager(this));
        CommonAdapter<LogisticsBean.DataBean> adapter = new CommonAdapter
                <LogisticsBean.DataBean>(this, R.layout.listitem_logistics, dataList) {
            @Override
            protected void convert(ViewHolder holder, LogisticsBean.DataBean dataBean, int position) {
                switch (position) {
                    case 0:
                        holder.setImageResource(R.id.img_logistics_address, R.mipmap.icon_logistics_current);
                        break;
                    case 1:
                        holder.setImageResource(R.id.img_logistics_address, R.mipmap.icon_logistics_last);
                        break;
                    default:
                        holder.setImageResource(R.id.img_logistics_address, R.mipmap.icon_logistics_pre);
                        break;
                }

                holder.setText(R.id.tv_logistics_time, dataBean.getTime());
                holder.setText(R.id.tv_logistics_details, dataBean.getContext());
            }
        };

        rvLogistics.setAdapter(adapter);
    }

    private void getData() {
        Intent intent = getIntent();
        number = intent.getStringExtra("number");
        company = intent.getStringExtra("company");
        companyName = intent.getStringExtra("company_name");
        imgUrl = intent.getStringExtra("img_url");

    }

    @OnClick(R.id.img_header_back)
    public void onClick() {
        finish();
    }
}
