package com.guangdian.aivideo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;

import com.guangdian.aivideo.models.AnalysisResultModel;
import com.guangdian.aivideo.models.CommendListModel;
import com.guangdian.aivideo.models.CommendModel;
import com.guangdian.aivideo.models.FacesModel;
import com.guangdian.aivideo.models.PersonModel;
import com.guangdian.aivideo.models.ProductModel;
import com.guangdian.aivideo.models.ProductResultModels;
import com.guangdian.aivideo.utils.NetWorkUtils;
import com.guangdian.aivideo.utils.ViewUtils;
import com.guangdian.aivideo.utils.YiPlusUtilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import wseemann.media.FFmpegMediaMetadataRetriever;

public class MainActivity extends AppCompatActivity {

    private ProgressBar mProgress;
    private VideoView mVideoView;
    private LinearLayout mContent;
    private RelativeLayout mContentParent;
    private Button mContentbg;

    private String mUrl;

    private boolean mIsPlaying = false;
    private int mVideoTotal = 0;
    private int mPlayPostion = 0;
    private Bitmap mBitmap;
    private View failView;

    private static int Product_Face = 0; // product and face back ansysics result
    private final Object synchronizedObject = new Object();
    private AnalysisResultModel mAnalysisResultModel;
    private CommendListModel models;
    private boolean PREPARE_ALL_DATA = false;
    private List<ProductModel> product_list = new ArrayList<>();  //product list data
    private List<CommendModel> mCurrentModels = new ArrayList<>();
    private boolean isContentShow = false;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.arg1 == 1) {

            } else if (msg.arg1 == 2) {
                //  set right data and show 识别 列表
                System.out.println("yangxinyu  selectRightResult  ");
                selectRightResult();
            } else if (msg.arg1 == 3) {
                // close the service
                if (isContentShow) {
                    isContentShow = false;
                    mContentParent.setVisibility(View.GONE);
                    mVideoView.setFocusable(true);
                    mVideoView.setFocusableInTouchMode(true);

                    if (!mIsPlaying) {
                        mVideoView.start();
                        mIsPlaying = true;
                    }
                }
            } else if (msg.arg1 == 4) {
                Bundle bundle = msg.getData();
                String tag = bundle.getString("source").trim();
                if (!YiPlusUtilities.isStringNullOrEmpty(tag)) {
                    String source[] = tag.split(" ");
                    String type = source[0];
                    String people = source[1];
                    Log.d("yangxinyu+", "淘宝   " + type + "   " + people);
                    int arg2 = 0;
                    int type2 = 0;
                    if (BAIDU.equals(type)) {
                        arg2 = 0;
                        type2 = 0;
                    } else if (VIDEO.equals(type)) {
                        arg2 = 1;
                        type2 = 1;
                    } else if (TAOBAO.equals(type)) {
                        arg2 = 4;
                        type2 = 2;
                    }

                    // show detail view
                    View view = ViewUtils.getDetailView(type2, MainActivity.this, mContent,
                            models.getModels(arg2), people, secondPageListener);
                    changeLayout(view);
                }
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        if (intent != null) {
            mUrl = intent.getStringExtra("video_url");
        }

        mVideoView = (VideoView) findViewById(R.id.main_video);
        mProgress = (ProgressBar) findViewById(R.id.main_load_progress);
        mContent = (LinearLayout) findViewById(R.id.main_content);
        mContentParent = (RelativeLayout) findViewById(R.id.main_content_parent);
        mContentbg = (Button) findViewById(R.id.main_content_bg);
        mProgress.setVisibility(View.VISIBLE);

        initVideoView();

        // load all data about the video
        PREPARE_ALL_DATA = false;
        initVideoData();
        failView = ViewUtils.getFailOrLoadingLayout(this, mContent, false, null);

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mVideoView != null && !mIsPlaying && !mVideoView.isPlaying()) {
            if (mPlayPostion > 0) {
                mVideoView.start();
                mIsPlaying = true;
                mVideoView.seekTo(mPlayPostion);
                mPlayPostion = 0;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mVideoView != null && mIsPlaying && mVideoView.isPlaying()) {
            mPlayPostion = mVideoView.getCurrentPosition();
            mVideoView.stopPlayback();
            mIsPlaying = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mVideoView != null) {
            mVideoView.destroyDrawingCache();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        System.out.println("yangxinyu    " + keyCode);

        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            if (mIsPlaying) {
                mVideoView.pause();
                mIsPlaying = false;
            } else {
                mVideoView.start();
                mIsPlaying = true;
            }
        } else if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (mBitmap != null) {
                mBitmap.recycle();
                mBitmap = null;
            }

            if (mIsPlaying) {
                mVideoView.pause();
                mIsPlaying = false;
            }

            screenCapNotRoot();
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!mIsPlaying) {
                mVideoView.start();
                mIsPlaying = true;
            }

            if (isContentShow) {
                isContentShow = false;
                mContentParent.setVisibility(View.GONE);
                mVideoView.setFocusable(true);
                mVideoView.setFocusableInTouchMode(true);
                return true;
            }
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            mContentbg.clearFocus();
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            mContentbg.requestFocus();
            mContentbg.setFocusableInTouchMode(true);
            mContentbg.setFocusable(true);
        }

        return super.onKeyDown(keyCode, event);
    }


    private void initVideoView() {
        mVideoView.setVideoURI(Uri.parse(mUrl));
        MediaController controller = new MediaController(this);
        mVideoView.setMediaController(controller);
        controller.requestFocus();
        controller.setEnabled(true);

        // 监听视频装载完成的事件
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                if (mediaPlayer != null) {
                    mVideoTotal = mediaPlayer.getDuration();
                    mVideoView.start();
                    mIsPlaying = true;
                    mProgress.setVisibility(View.GONE);
                }
            }
        });

        // 监听播放发生错误时候的事件
        mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                return false;
            }
        });

        mVideoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mediaPlayer, int i, int i1) {
                // 在有警告或错误信息时调用。例如：开始缓冲、缓冲结束、下载速度变化
                return false;
            }
        });

        // 监听播放完成的事件
        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                // TODO auto play next video
            }
        });
    }

    private void initVideoData() {
        String time = (System.currentTimeMillis() / 1000) + "";
        String data = YiPlusUtilities.getPostParams(time);
        NetWorkUtils.post(YiPlusUtilities.VIDEO_COMMEND_URL, data, null, new NetWorkCallback() {
            @Override
            public void onServerResponse(Bundle result) {
                try {
                    String res = (String) result.get("result");
                    if (!YiPlusUtilities.isStringNullOrEmpty(res)) {
                        JSONArray array = new JSONArray(res);
                        models = new CommendListModel(array);
                        PREPARE_ALL_DATA = true;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void screenCapNotRoot() {
        View loadingView = ViewUtils.getFailOrLoadingLayout(this, mContent, true, null);
        changeLayout(loadingView);

        FFmpegMediaMetadataRetriever mmr = new FFmpegMediaMetadataRetriever();
        try {
            int postion = mVideoView.getCurrentPosition();
            mmr.setDataSource(mUrl);
            mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_ALBUM);
            mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_ARTIST);
            mBitmap = mmr.getFrameAtTime(postion * 1000, FFmpegMediaMetadataRetriever.OPTION_CLOSEST); // frame at 2 seconds

            if (mBitmap == null) {
                // TODO  识别失败
                System.out.println("yangxinyu    识别失败 ");
                changeLayout(failView);
            } else {
                // TODO show shibie result
                if (PREPARE_ALL_DATA) {
                    System.out.println("yangxinyu    开始 分析截图数据 ");
                    analysisImage();
                } else {
                    Toast.makeText(this, "网络不给力呀...", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mmr.release();
        }
    }

    private void changeLayout(View view) {
        if (mContent != null) {
            mContent.removeAllViews();
            if (!isContentShow) {
                mContentParent.setVisibility(View.VISIBLE);
                mContentbg.setFocusable(true);
                mContentbg.setFocusableInTouchMode(true);
                mContentbg.requestFocus();
                isContentShow = true;
            }

            mContent.addView(view);
        }
    }

    // 包括 图片分析和 产品 分析
    private void analysisImage() {

        Product_Face = 0;

        String time = (System.currentTimeMillis() / 1000) + "";
        String data = YiPlusUtilities.getPostParams(time);
        String mBitmapBase64 = YiPlusUtilities.getBase64FromBitmap(mBitmap);

        String param = null;
        try {
            // base64 得到的 URL 在网络请求过程中 会出现 + 变 空格 的现象。 在 设置 base64 的字符串 之前 进行 格式化
            param = data + "&image=" + URLEncoder.encode(mBitmapBase64, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // analysis image for getting face
        NetWorkUtils.post(YiPlusUtilities.ANALYSIS_IMAGE_URL, param, null, new NetWorkCallback() {
            @Override
            public void onServerResponse(Bundle result) {
                try {
                    String res = (String) result.get("result");
                    System.out.println("yangxinyu  ANALYSIS_IMAGE_URL  " + res);
                    JSONObject object = new JSONObject(res);
                    mAnalysisResultModel = new AnalysisResultModel(object);

                    synchronized (synchronizedObject) {
                        Product_Face++;
                        if (Product_Face == 2) {
                            sendMessageForHandle(2, null);
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // analysis image for getting product
        NetWorkUtils.post(YiPlusUtilities.PRODUCT_URL, param, null, new NetWorkCallback() {
            @Override
            public void onServerResponse(Bundle result) {
                try {
                    String res = (String) result.get("result");
                    System.out.println("yangxinyu  Product_result   " + res);
                    JSONObject array = new JSONObject(res);

                    ProductResultModels productList = new ProductResultModels(array);
                    product_list.clear();
                    product_list = productList.getProductList();

                    synchronized (synchronizedObject) {
                        Product_Face++;
                        if (Product_Face == 2) {
                            sendMessageForHandle(2, null);
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void sendMessageForHandle(int arg, Bundle bundle) {
        synchronized (handler) {
            Message message = new Message();
            message.arg1 = arg;
            if (bundle != null) {
                message.setData(bundle);
            }

            handler.sendMessage(message);
        }
    }

    public static int mBaidu = 0;
    public static int mVideo = 0;
    public static int mTaobao = 0;

    private static final String BAIDU = "百度百科";
    private static final String VIDEO = "点播视频";
    private static final String TAOBAO = "商品";

    private void selectRightResult() {
        clearData();

        String name;
        // Show analysis result
        if (mAnalysisResultModel != null) {

            FacesModel faces = mAnalysisResultModel.getFaces();
            // get the first item as the person
            if (faces != null) {
                List<PersonModel> personModels = faces.getFace_attribute();
                if (personModels != null && !YiPlusUtilities.isListNullOrEmpty(personModels)) {
                    name = personModels.get(0).getStar_name();
                    Log.d("yangxinyu ", "识别出来的 " + name);
                    // match analysis result and all data collections
                    List<CommendModel> baidu = models.getModels(0);
                    List<CommendModel> dianbo = models.getModels(1);
                    List<CommendModel> taobao = models.getModels(4);

                    // get some data about the person
                    if (!YiPlusUtilities.isStringNullOrEmpty(name)) {
                        for (CommendModel m : baidu) {
                            if (name.equals(m.getTag_name()) && mBaidu == 0) {
                                mCurrentModels.add(m);
                                mBaidu++;
                                break;
                            }
                        }

                        List<CommendModel> dianboPerson = new ArrayList<>();
                        for (CommendModel m : dianbo) {
                            if (name.equals(m.getTag_name())) {
                                dianboPerson.add(m);
                            }
                        }

                        // 把 所有的 该人物的点播视频 识别出来。 在随机一个 放到 mCurrentModels
                        if (dianboPerson.size() > 0) {
                            randomOneData(dianboPerson);
                            Log.d("Yi+", "点播视频 数量 " + dianboPerson.size());
                            mVideo++;
                        }

                        List<CommendModel> taobaoPerson = new ArrayList<>();
                        for (CommendModel m : taobao) {
                            if (name.equals(m.getTag_name())) {
                                taobaoPerson.add(m);
                            }
                        }

                        if (taobaoPerson.size() > 0) {
                            randomOneData(taobaoPerson);
                            Log.d("Yi+", "淘宝 商品 数量 " + taobaoPerson.size());
                            mTaobao++;
                        }
                    }

                    //分析这个人的数据
                    if (mCurrentModels != null && mCurrentModels.size() > 0) {
                        //hasFace = true;
                        // show face list
                        if (mCurrentModels.size() > 1) {
                            // show some item data about the person
                            System.out.println("yangxinyu    展示 list 数据 ");
                            View view = ViewUtils.getPersonListView(this, mContent, mCurrentModels,
                                    mBitmap, listKeyListener);
                            changeLayout(view);
                            return;
                        } else if (mCurrentModels.size() == 1) {
                            // with only one item and is baidu , show detail
                            String type = mCurrentModels.get(0).getData_source();
                            int arg2 = 0;
                            if (BAIDU.equals(type)) {
                                arg2 = 0;
                                // show baidu baike data
                                System.out.println("yangxinyu    展示 百度百科");
                                View view = ViewUtils.getDetailView(0, this, mContent, models.getModels(arg2),
                                        name, null);
                                changeLayout(view);
                                return;
                            } else {
                                Toast.makeText(this, "别看他了， 他低调...", Toast.LENGTH_SHORT).show();
                                changeLayout(failView);
                                return;
                            }
                        }
                    }
                }
            }
        }

        // analysis face is null
        noAnalysisFaceResult();
    }

    private View.OnKeyListener secondPageListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
            // show last page
            if (YiPlusUtilities.DOUBLECLICK) {
                System.out.println("yangxinyu  secondPage click    " + keyCode);
                synchronized (YiPlusUtilities.class) {
                    YiPlusUtilities.DOUBLECLICK = false;
                }

                int id = view.getId();
                if (id == R.id.notifi_page_background) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        View v = ViewUtils.getPersonListView(MainActivity.this, mContent, mCurrentModels,
                                mBitmap, listKeyListener);
                        changeLayout(v);
                        return true;
                    } else if ((keyCode == KeyEvent.KEYCODE_DPAD_CENTER)) {
                        return true;
                    }
                }
            }

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    synchronized (YiPlusUtilities.class) {
                        YiPlusUtilities.DOUBLECLICK = true;
                    }
                }
            }, 1000);

            return false;
        }
    };

    private View.OnKeyListener listKeyListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
            if (YiPlusUtilities.DOUBLECLICK) {
                synchronized (synchronizedObject) {
                    YiPlusUtilities.DOUBLECLICK = false;
                }

                switch (keyCode) {
                    case KeyEvent.KEYCODE_BACK:
                        sendMessageForHandle(3, null);
                        break;
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                        Bundle bundle = new Bundle();
                        bundle.putString("source", (String) view.getTag());
                        sendMessageForHandle(4, bundle);
                        break;
                }
            }

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    synchronized (synchronizedObject) {
                        YiPlusUtilities.DOUBLECLICK = true;
                    }
                }
            }, 1000);

            return false;
        }
    };

    private void noAnalysisFaceResult() {
        if (product_list != null && product_list.size() > 0) {
            System.out.println("yangxinyu   展示 产品 界面 ");
            View view = ViewUtils.getProductDetailView(this, mContent, null, product_list);
            changeLayout(view);
        } else {
            // show error page
            View view = ViewUtils.getFailOrLoadingLayout(this, mContent, false, mBitmap);
            changeLayout(view);
        }
    }

    private void clearData() {
        if (mCurrentModels != null) {
            mCurrentModels.clear();
        }

        mBaidu = 0;
        mVideo = 0;
        mTaobao = 0;
    }

    private void randomOneData(List<CommendModel> datas) {
        Random baiduR = new Random();
        if (datas != null && datas.size() > 0) {
            int random = baiduR.nextInt(datas.size());
            mCurrentModels.add(datas.get(random));
        }
    }
}