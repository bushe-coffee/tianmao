package com.guangdian.aivideo.utils;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterViewFlipper;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.guangdian.aivideo.MainActivity;
import com.guangdian.aivideo.R;
import com.guangdian.aivideo.adapters.FilperAdapter;
import com.guangdian.aivideo.adapters.ProductFilperAdapter;
import com.guangdian.aivideo.models.CommendModel;
import com.guangdian.aivideo.models.ProductModel;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

public class ViewUtils {

    public static View getFailOrLoadingLayout(MainActivity mainActivity, LinearLayout mContent, boolean isOK, Bitmap mBitmap) {

        View view = LayoutInflater.from(mainActivity).inflate(R.layout.view_welcome_page, mContent, false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.setElevation(8);
        }

        ImageButton image = view.findViewById(R.id.view_welcome_image);
        TextView text = view.findViewById(R.id.view_welcome_text);
        if (isOK) {
            image.setImageResource(R.drawable.yiplus_logo);
            text.setText("智能识别请稍后...");
            Animation animation = AnimationUtils.loadAnimation(mainActivity, R.anim.animation_scale_big);
            animation.setDuration(1000);
            animation.setRepeatMode(Animation.RESTART);
            animation.setRepeatCount(5);
            image.startAnimation(animation);
        } else {
            if (mBitmap != null) {
                image.setImageBitmap(mBitmap);
            } else {
                image.setImageResource(R.drawable.result_error);
            }

            text.setText("识别失败，请返回重试...");
        }

        return view;
    }

    private RelativeLayout baiduContainer;
    private TextView baiduTitle;
    private TextView baiduContent;
    private ImageView baiduImage;
    private Button baiduBg;

    private RelativeLayout dianboContainer;
    private TextView dianboTitle;
    private TextView dianboContent;
    private ImageView dianboImage;
    private Button dianboBg;

    private RelativeLayout weiboContainer;
    private TextView weiboTitle;
    private TextView weiboContent;

    private RelativeLayout taobaoContainer;
    private ImageView taobaoImage;
    private Button taobaoBg;

    private static final String BAIDU = "百度百科";
    private static final String VIDEO = "点播视频";
    private static final String TAOBAO = "商品";

    // 一定 识别出了 人脸
    public static View getPersonListView(MainActivity mainActivity, LinearLayout mContent,
                                  List<CommendModel> mCurrentModels,
                                  Bitmap mBitmap,
                                  View.OnKeyListener callback) {

        View view = LayoutInflater.from(mainActivity).inflate(R.layout.view_ansync_list, mContent, false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.setElevation(8);
        }

        RelativeLayout baiduContainer;
        TextView baiduTitle;
        TextView baiduContent;
        ImageView baiduImage;
        ImageButton baiduBg;

        RelativeLayout dianboContainer;
        TextView dianboTitle;
        TextView dianboContent;
        ImageView dianboImage;
        ImageButton dianboBg;

        RelativeLayout taobaoContainer;
        ImageView taobaoImage;
        ImageButton taobaoBg;

        baiduContainer = view.findViewById(R.id.view_baidu_container);
        baiduTitle = view.findViewById(R.id.view_baidu_title);
        baiduContent = view.findViewById(R.id.view_baidu_content);
        baiduImage = view.findViewById(R.id.view_baidu_image);
        baiduBg = view.findViewById(R.id.view_baidu_button);

        dianboContainer = view.findViewById(R.id.view_dianbo_container);
        dianboTitle = view.findViewById(R.id.view_dianbo_title);
        dianboContent = view.findViewById(R.id.view_dianbo_content);
        dianboImage = view.findViewById(R.id.view_dianbo_image);
        dianboBg = view.findViewById(R.id.view_dianbo_button);

        taobaoContainer = view.findViewById(R.id.view_taobao_container);
        taobaoImage = view.findViewById(R.id.view_taobao_image);
        taobaoBg = view.findViewById(R.id.view_taobao_button);

        baiduContainer.setVisibility(MainActivity.mBaidu == 0 ? View.GONE : View.VISIBLE);
        dianboContainer.setVisibility(MainActivity.mVideo == 0 ? View.GONE : View.VISIBLE);
        taobaoContainer.setVisibility(MainActivity.mTaobao == 0 ? View.GONE : View.VISIBLE);

        baiduBg.setOnKeyListener(callback);
        dianboBg.setOnKeyListener(callback);
        taobaoBg.setOnKeyListener(callback);

        FoucusChange change = new FoucusChange(mainActivity);
        baiduBg.setOnFocusChangeListener(change);
        dianboBg.setOnFocusChangeListener(change);
        taobaoBg.setOnFocusChangeListener(change);

        ImageButton image = view.findViewById(R.id.view_screen_cap);

        baiduBg.setFocusable(true);
        baiduBg.requestFocus();
        baiduBg.setFocusableInTouchMode(true);

        if (mBitmap != null) {
            image.setImageBitmap(mBitmap);
        }

        for (CommendModel model : mCurrentModels) {
            if (BAIDU.equals(model.getData_source())) {
                baiduTitle.setText(model.getDisplay_title());
                baiduContent.setText(model.getDisplay_brief());
                ImageLoader.getInstance().displayImage(model.getDetailed_image_url(), baiduImage);
                baiduBg.setTag(model.getData_source() + " " + model.getDisplay_title());
            } else if (VIDEO.equals(model.getData_source())) {
                dianboTitle.setText(model.getDisplay_title());
                dianboContent.setText(model.getDisplay_brief());
                ImageLoader.getInstance().displayImage(model.getDetailed_image_url(), dianboImage);
                dianboBg.setTag(model.getData_source() + " " + model.getDisplay_title());
            } else if (TAOBAO.equals(model.getData_source())) {
                Log.d("Yi+", "image URL  " + model.getDetailed_image_url());
                ImageLoader.getInstance().displayImage(model.getDetailed_image_url(), taobaoImage);
                taobaoBg.setTag(model.getData_source() + " " + model.getTag_name());
            }
        }

        return view;
    }

    // type :0 baidu baike 1:dianbo detial 2 taobao detail
    public static View getDetailView(int type, MainActivity mainActivity, LinearLayout mContent,
                                     List<CommendModel> models, String name,
                                     View.OnKeyListener callback) {
        View detailView = null;
        switch (type) {
            case 0: {
                for (CommendModel model : models) {
                    if (name.equals(model.getDisplay_title())) {
                        detailView = showBaiduDetail(mainActivity, mContent, model, callback);
                        break;
                    }
                }

                break;
            }
            case 1: {
                List<CommendModel> showOther = new ArrayList<>();
                for (int i = 0; i < models.size(); ++i) {
                    CommendModel model = models.get(i);
                    if (name.equals(model.getDisplay_title())) {
                        showOther.add(model);
                        break;
                    }
                }

                detailView = showDianboDetail(mainActivity, mContent, showOther, callback);

                break;
            }
            case 2: {
                List<CommendModel> showOther = new ArrayList<>();
                for (int i = 0; i < models.size(); ++i) {
                    CommendModel model = models.get(i);
                    if (name.equals(model.getTag_name())) {
                        showOther.add(model);
                        break;
                    }
                }

                detailView = getProductDetailView(mainActivity, mContent, showOther, null);
                break;
            }
        }

        return detailView;
    }

    public static View getProductDetailView(MainActivity mainActivity, LinearLayout mContent,
                                            List<CommendModel> showOther,
                                            List<ProductModel> product_list) {
        View view = LayoutInflater.from(mainActivity).inflate(R.layout.notifi_detail_layout, mContent, false);

        TextView title = view.findViewById(R.id.notifi_page_title);
        AdapterViewFlipper mFliper = view.findViewById(R.id.notifi_page_content);
        Button background = view.findViewById(R.id.notifi_page_background);
        if (showOther != null) {
            title.setText("商品详情");
            FilperAdapter adapter = new FilperAdapter(mainActivity);
            adapter.setDatas(showOther, 0);
            mFliper.setAdapter(adapter);
        } else {
            title.setText("商品推荐");
            ProductFilperAdapter adapter = new ProductFilperAdapter(mainActivity);
            adapter.setDatas(product_list);
            mFliper.setAdapter(adapter);
        }

        return view;
    }

    private static View showDianboDetail(MainActivity mainActivity, LinearLayout mContent,
                                         List<CommendModel> showOther, View.OnKeyListener callback) {
        View view = LayoutInflater.from(mainActivity).inflate(R.layout.notifi_detail_layout, mContent, false);

        TextView title = view.findViewById(R.id.notifi_page_title);
        AdapterViewFlipper mFliper = view.findViewById(R.id.notifi_page_content);
        Button background = view.findViewById(R.id.notifi_page_background);

        if (callback != null) {
            background.setOnKeyListener(callback);
        }

        background.setFocusable(true);
        background.requestFocus();
        background.setFocusableInTouchMode(true);

        title.setText("点播视频");
        FilperAdapter adapter = new FilperAdapter(mainActivity);
        adapter.setDatas(showOther, 0);
        mFliper.setAdapter(adapter);

        return view;
    }

    private static View showBaiduDetail(MainActivity mainActivity, LinearLayout mContent,
                                        CommendModel model, View.OnKeyListener callback) {
        View view = LayoutInflater.from(mainActivity).inflate(R.layout.notifi_detail_layout, mContent, false);

        TextView title = view.findViewById(R.id.notifi_page_title);
        AdapterViewFlipper mFliper = view.findViewById(R.id.notifi_page_content);
        Button background = view.findViewById(R.id.notifi_page_background);

        if (callback != null) {
            background.setOnKeyListener(callback);
        }

        background.setFocusable(true);
        background.requestFocus();
        background.setFocusableInTouchMode(true);

        title.setText("百度百科");
        FilperAdapter adapter = new FilperAdapter(mainActivity);
        List<CommendModel> showOther = new ArrayList<>();
        showOther.add(model);
        adapter.setDatas(showOther, 0);
        mFliper.setAdapter(adapter);

        return view;
    }

    private static void showOnFocusAnimation(Context context, View v) {
        Animation anim = AnimationUtils.loadAnimation(context, R.anim.button_big);
        v.startAnimation(anim);
        v.bringToFront();
    }

    private static void showLoseFocusAnimation(Context context,View v) {
        Animation anim = AnimationUtils.loadAnimation(context, R.anim.button_small);
        v.startAnimation(anim);
    }

    private static class FoucusChange implements View.OnFocusChangeListener {

        private Context activity;

        public FoucusChange(Activity activity){
            this.activity = activity.getApplicationContext();
        }

        @Override
        public void onFocusChange(View view, boolean hasFocus) {
            if (hasFocus) {
                showOnFocusAnimation(activity, view);
            } else {
                showLoseFocusAnimation(activity, view);
            }
        }
    }
}
