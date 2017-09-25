package cn.cloudworkshop.miaoding.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;

import cn.cloudworkshop.miaoding.utils.LogUtils;

/**
 * Author：binge on 2017-06-05 17:35
 * Email：1993911441@qq.com
 * Describe：
 */
public class BallView extends ImageView {


    public BallView(Context context) {
        super(context);
    }

    public BallView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }


    public void moveTo(int x, int y) {
        super.setFrame(x, y, x + getWidth(), y + getHeight());
    }

}
