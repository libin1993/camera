package cn.cloudworkshop.miaoding.bean;

import com.flyco.tablayout.listener.CustomTabEntity;

/**
 * Author：binge on 2017/2/15 17:03
 * Email：1993911441@qq.com
 * Describe：
 */
public class TabBean implements CustomTabEntity {
    public String title;



    public TabBean(String title) {
        this.title = title;

    }

    @Override
    public String getTabTitle() {
        return title;
    }

    @Override
    public String getTabSelectedIcon() {
        return null;
    }

    @Override
    public String getTabUnselectedIcon() {
        return null;
    }


}
