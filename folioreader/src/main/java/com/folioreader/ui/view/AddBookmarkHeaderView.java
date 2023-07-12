package com.folioreader.ui.view;

/**
 * @author： libaixing
 * time：2023/7/12 11:48
 * description：
 * updateUser：
 * updateDate：2023/7/12 11:48
 */

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.folioreader.R;
import com.scwang.smart.refresh.layout.api.RefreshHeader;
import com.scwang.smart.refresh.layout.api.RefreshKernel;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.constant.RefreshState;
import com.scwang.smart.refresh.layout.constant.SpinnerStyle;

/**
 * @author： libaixing
 * time：2023/7/12 10:41
 * description： 添加书签头部
 * updateUser：
 * updateDate：2023/7/12 10:41
 */
public class AddBookmarkHeaderView extends LinearLayout implements RefreshHeader {

    private final ImageView ivHeader;
    private final TextView headText;
    private Animation mAnimRefresh;

    /**
     * 1，构造方法
     */
    public AddBookmarkHeaderView(Context context) {
        this(context, null, 0);
    }

    public AddBookmarkHeaderView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AddBookmarkHeaderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View view = View.inflate(context, R.layout.widget_folio_bookmark, this);
        ivHeader = view.findViewById(R.id.iv_refresh_header);
        headText = view.findViewById(R.id.tv_header);
    }

    /**
     * 2，获取真实视图（必须返回，不能为null）一般就是返回当前自定义的view
     */
    @NonNull
    @Override
    public View getView() {
        return this;
    }

    /**
     * 3，获取变换方式（必须指定一个：平移、拉伸、固定、全屏）,Translate指平移，大多数都是平移
     */
    @NonNull
    @Override
    public SpinnerStyle getSpinnerStyle() {
        return SpinnerStyle.Translate;
    }

    /**
     * 4，一般可以理解为一下case中的三种状态，在达到相应状态时候开始改变
     * 注意：这三种状态都是初始化的状态
     */
    @Override
    public void onStateChanged(@NonNull RefreshLayout refreshLayout, @NonNull RefreshState oldState, @NonNull RefreshState newState) {
        switch (newState) {
            case None:
            case PullDownToRefresh:
                ivHeader.setImageResource(R.mipmap.ic_bookmark_up);
                headText.setText("松手添加标签");
                break;
            case Refreshing:
                headText.setText("正在添加标签...");
                ivHeader.setImageResource(R.mipmap.ic_bookmark_up);
                mAnimRefresh = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                mAnimRefresh.setFillAfter(!mAnimRefresh.getFillAfter());
                mAnimRefresh.setDuration(600);
                mAnimRefresh.setRepeatCount(Animation.INFINITE);
                ivHeader.startAnimation(mAnimRefresh);
                break;
            case ReleaseToRefresh:
                headText.setText("松手添加标签");
                ivHeader.setImageResource(R.mipmap.ic_bookmark_up);
                break;
            default:
        }
    }

    /**
     * 5，结束下拉刷新的时候需要关闭动画
     *
     * @param refreshLayout
     * @param success
     * @return
     */
    @Override
    public int onFinish(@NonNull RefreshLayout refreshLayout, boolean success) {
        if (mAnimRefresh != null && mAnimRefresh.hasStarted()) {
            mAnimRefresh.cancel();
        }
        if (success) {
            headText.setText("添加标签完成");
        } else {
            headText.setText("添加标签失败");
        }
        return 300;
    }

    @Override
    public void onStartAnimator(@NonNull RefreshLayout refreshLayout, int height, int maxDragHeight) {

    }

    @Override
    public void setPrimaryColors(int... colors) {

    }

    @Override
    public void onInitialized(@NonNull RefreshKernel kernel, int height, int maxDragHeight) {

    }

    @Override
    public void onMoving(boolean isDragging, float percent, int offset, int height, int maxDragHeight) {

    }

    @Override
    public void onReleased(@NonNull RefreshLayout refreshLayout, int height, int maxDragHeight) {

    }

    @Override
    public void onHorizontalDrag(float percentX, int offsetX, int offsetMax) {

    }

    @Override
    public boolean isSupportHorizontalDrag() {
        return false;
    }

    @Override
    public boolean autoOpen(int i, float v, boolean b) {
        return false;
    }

}

