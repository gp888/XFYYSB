package com.gp.xfyysb;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;


public class RecordDialog extends Dialog {

    private static String TAG = RecordDialog.class.getSimpleName();
    private Context context;
    private Button record;
    private TextView tv_time;
    private EditText mResultText;
    private Toast mToast;

    // 引擎类型
    private String mEngineType = SpeechConstant.TYPE_CLOUD;
    // 语音听写对象
    private SpeechRecognizer mIat;
    private boolean isRecording;
    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();
    private int ret = 0;
    //当前触摸相对于屏幕的Y轴坐标
    private int mCurrentInScreenY;

    private int mDownInScreenY;
    private int mUpInScreenY;
    /**
     * 当前点击时间
     */
    private long mCurrentClickTime;


    CountDownTimer mCountDownTimer = new CountDownTimer(11000, 1000) {

        @Override
        public void onTick(long millisUntilFinished) {
            int second = (int) (millisUntilFinished / 1000);
            tv_time.setText(second + "秒");
        }

        @Override
        public void onFinish() {
            stopRecord();
        }

    };

    public RecordDialog(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_record);

        tv_time = findViewById(R.id.tv_time);

        mResultText = findViewById(R.id.et_result);

        record = findViewById(R.id.btn_record);


        // 初始化识别无UI识别对象
        // 使用SpeechRecognizer对象，可根据回调消息自定义界面；
        mIat = SpeechRecognizer.createRecognizer(context, mInitListener);

        mToast = Toast.makeText(context, "", Toast.LENGTH_SHORT);

        record.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mResultText.setText(null);// 清空显示内容
                mIatResults.clear();
                setParam();
                if( null != mIat ){
                    ret = mIat.startListening(mRecognizerListener);
                    if (ret != ErrorCode.SUCCESS) {
                        showTip("听写失败,错误码：" + ret);
                    } else {
                        showTip(context.getString(R.string.text_begin));
                    }
                    mCountDownTimer.start();

                    isRecording = true;
                } else {
                    showTip("语音听写初始化失败");
                }
                return false;
            }
        });

        record.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mCurrentInScreenY = (int) event.getRawY();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        //记录Down下时的坐标
                        mDownInScreenY = (int) event.getRawY();
                        //记录当前点击的时间
                        mCurrentClickTime = Calendar.getInstance().getTimeInMillis();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int mCurrentInScreenY = (int) event.getRawY();
                        if (mDownInScreenY > mCurrentInScreenY && mDownInScreenY - mCurrentInScreenY > 200) {
                            tv_time.setText("松开取消");
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        mUpInScreenY = (int) event.getRawY();

                        int result = mDownInScreenY - mUpInScreenY;
                        Log.d(TAG, "yyy" + mDownInScreenY + "," + mUpInScreenY  + ",," + result);

                        if (mDownInScreenY > mUpInScreenY && mDownInScreenY - mUpInScreenY > 200) {
                            cancelRecord();
                        } else {
                            stopRecord();
                        }
                        break;
                }
                return false;
            }
        });
    }

    private void stopRecord() {
        mIat.stopListening();
        showTip("停止听写");
        mCountDownTimer.cancel();
        tv_time.setText("");
    }

    private void cancelRecord() {
        mIat.cancel();
        showTip("取消听写");
        mCountDownTimer.cancel();
        tv_time.setText("");
    }

    /**
     * 初始化监听器。
     */
    private InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            Log.d(TAG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                showTip("初始化失败，错误码：" + code);
            }
        }
    };

    private void showTip(final String str) {
        mToast.setText(str);
        mToast.show();
    }
    /**
     * 听写监听器。
     */
    private RecognizerListener mRecognizerListener = new RecognizerListener() {

        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            showTip("开始说话");
        }

        @Override
        public void onError(SpeechError error) {
            // 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
            showTip(error.getPlainDescription(true));
        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            showTip("结束说话");
        }

        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            Log.d(TAG, results.getResultString());
            printResult(results);
        }

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
//            showTip("当前正在说话，音量大小：" + volume);
            Log.d(TAG, "返回音频数据："+data.length);
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }
    };


    private void printResult(RecognizerResult results) {
        String text = JsonParser.parseIatResult(results.getResultString());

        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mIatResults.put(sn, text);

        StringBuffer resultBuffer = new StringBuffer();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }

        mResultText.setText(resultBuffer.toString());
        mResultText.setSelection(mResultText.length());
    }

    /**
     * 参数设置
     *
     * @return
     */
    public void setParam() {
        // 清空参数
        mIat.setParameter(SpeechConstant.PARAMS, null);

        // 设置听写引擎
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        // 设置返回结果格式
        mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");

        //cantonese,mandarin,en_us
        String lag = "mandarin";
        // 设置语言
        mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        // 设置语言区域
        mIat.setParameter(SpeechConstant.ACCENT, lag);
        //此处用于设置dialog中不显示错误码信息
        //mIat.setParameter("view_tips_plain","false");

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS, "4000");

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS, "1000");

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT, "1");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mIat.setParameter(SpeechConstant.AUDIO_FORMAT,"wav");
        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/msc/iat.wav");
    }

    public void onDissmiss(){
        if( null != mIat ){
            // 退出时释放连接
            mIat.cancel();
            mIat.destroy();
        }
    }
}

