package com.guangdian.aivideo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.guangdian.aivideo.models.VideoListModel;
import com.guangdian.aivideo.models.VideoModel;
import com.guangdian.aivideo.utils.NetWorkUtils;
import com.guangdian.aivideo.utils.YiPlusUtilities;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import wseemann.media.FFmpegMediaMetadataRetriever;

public class ContentActivity extends FragmentActivity implements View.OnClickListener, View.OnFocusChangeListener {

    private List<VideoModel> mListVideos;

    private LinearLayout mLayoutContainer;

    private int screenWidth;
    private int screenHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);
        screenWidth = YiPlusUtilities.getScreenWidth(this);
        screenHeight = YiPlusUtilities.getScreenHeight(this);
        mLayoutContainer = findViewById(R.id.content_grid);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mLayoutContainer.getLayoutParams();
        params.width = (int) (screenWidth * 0.9);
        params.height = (int) (screenHeight * 0.7);
        mLayoutContainer.setLayoutParams(params);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mListVideos == null || mListVideos.size() == 0) {
            requestData();
        }
    }

    private void requestData() {
        String time = (System.currentTimeMillis() / 1000) + "";
        String data = YiPlusUtilities.getPostParams(time);
        NetWorkUtils.post(YiPlusUtilities.LIST_URL, data, null, new NetWorkCallback() {
            @Override
            public void onServerResponse(Bundle result) {
                try {
                    String res = (String) result.get("result");
                    System.out.println("Yi Plus  videolist " + res);
                    if (!YiPlusUtilities.isStringNullOrEmpty(res)) {
                        JSONArray array = new JSONArray(res);
                        VideoListModel model = new VideoListModel(array);
                        mListVideos = model.getVideoList();
                        if (mListVideos != null) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    for (int i= 0;i<mListVideos.size();++i) {
                                        View view = LayoutInflater.from(ContentActivity.this).inflate(R.layout.item_grid_layout, mLayoutContainer, false);
                                        setDataAboutVideo(mListVideos.get(i), view);
                                        mLayoutContainer.addView(view);
                                    }
                                }
                            });
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void setDataAboutVideo(VideoModel model, View parent) {
        RelativeLayout mContainer = parent.findViewById(R.id.item_grid_container);
        ImageButton mBackground = parent.findViewById(R.id.item_grid_background);
        TextView mTitle = parent.findViewById(R.id.item_grid_title);
        TextView mContent = parent.findViewById(R.id.item_grid_content);

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mContainer.getLayoutParams();
        params.width = (int) (screenWidth * 0.27);
        mContainer.setLayoutParams(params);

        mTitle.setText(model.getVideo_name());
        Date date = new Date(model.getCreate_time() * 1000);
        mContent.setText(DateFormat.getDateInstance().format(date));

        // 咪咕 的 盒子 ，无法 取帧 ，只适合天猫的盒子
        FFmpegMediaMetadataRetriever mm=new FFmpegMediaMetadataRetriever();
        try{
            //获取视频文件数据
            mm.setDataSource(model.getVideo_url());
            //获取文件缩略图
            Bitmap bitmap=mm.getFrameAtTime(5000*1000);
            if (bitmap != null) {

                mBackground.setImageBitmap(bitmap);
            }
        }catch (Exception e){
            e.printStackTrace();
        } finally {
            mm.release();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBackground.setStateListAnimator(null);
            mBackground.setElevation(4.0f);
        }

        mBackground.setTag(model.getVideo_url());
        mBackground.setOnClickListener(this);
        mBackground.setOnFocusChangeListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view != null) {
            String url = (String) view.getTag();
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("video_url", url);
            startActivity(intent);
        }
    }

    private void showOnFocusAnimation(View v) {
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.button_big);
        v.startAnimation(anim);
        v.bringToFront();
    }

    private void showLoseFocusAnimation(View v) {
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.button_small);
        v.startAnimation(anim);
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (hasFocus) {
            showOnFocusAnimation(view);
        } else {
            showLoseFocusAnimation(view);
        }
    }
}
