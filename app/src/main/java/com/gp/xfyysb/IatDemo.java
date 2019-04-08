package com.gp.xfyysb;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import com.iflytek.cloud.ErrorCode;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class IatDemo extends Activity implements OnClickListener {
	// 语音听写对象
	// 用HashMap存储听写结果
	private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.iatdemo);
		requestPermissions();
		initLayout();
	}

	/**
	 * 初始化Layout。
	 */
	private void initLayout() {
		findViewById(R.id.iat_recognize).setOnClickListener(IatDemo.this);
		findViewById(R.id.iat_recognize_stream).setOnClickListener(IatDemo.this);
		findViewById(R.id.iat_stop).setOnClickListener(IatDemo.this);
		findViewById(R.id.iat_cancel).setOnClickListener(IatDemo.this);
		findViewById(R.id.image_iat_set).setOnClickListener(IatDemo.this);
	}

	int ret = 0; // 函数调用返回值

	@Override
	public void onClick(View view) {
		
		switch (view.getId()) {
		// 进入参数设置页面
		case R.id.image_iat_set:
			Intent intents = new Intent(IatDemo.this, IatSettings.class);
			startActivity(intents);
			break;
		// 开始听写
		// 如何判断一次听写结束：OnResult isLast=true 或者 onError
		case R.id.iat_recognize:
//			mResultText.setText(null);// 清空显示内容
//			mIatResults.clear();
			// 设置参数
//			setParam();
//			boolean isShowDialog = mSharedPreferences.getBoolean(getString(R.string.pref_key_iat_show), true);
//			if (isShowDialog) {
				// 显示听写对话框
//				mIatDialog.setListener(mRecognizerDialogListener);
//				mIatDialog.show();
//				showTip(getString(R.string.text_begin));
//			}else {
//				// 不显示听写对话框
			final RecordDialog recordDialog = new RecordDialog(this);
			recordDialog.show();
			recordDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					recordDialog.onDissmiss();
				}
			});
			break;
		// 音频流识别
		case R.id.iat_recognize_stream:
//			mResultText.setText(null);// 清空显示内容
			mIatResults.clear();
			// 设置参数
//			setParam();
			// 设置音频来源为外部文件
//			mIat.setParameter(SpeechConstant.AUDIO_SOURCE, "-1");
			// 也可以像以下这样直接设置音频文件路径识别（要求设置文件在sdcard上的全路径）：
			// mIat.setParameter(SpeechConstant.AUDIO_SOURCE, "-2");
			// mIat.setParameter(SpeechConstant.ASR_SOURCE_PATH, "sdcard/XXX/XXX.pcm");
//			ret = mIat.startListening(mRecognizerListener);
			if (ret != ErrorCode.SUCCESS) {
//				showTip("识别失败,错误码：" + ret);
			} else {
				byte[] audioData = FucUtil.readAudioFile(IatDemo.this, "iattest.wav");
				
				if (null != audioData) {
//					showTip(getString(R.string.text_begin_recognizer));
					// 一次（也可以分多次）写入音频文件数据，数据格式必须是采样率为8KHz或16KHz（本地识别只支持16K采样率，云端都支持），
					// 位长16bit，单声道的wav或者pcm
					// 写入8KHz采样的音频时，必须先调用setParameter(SpeechConstant.SAMPLE_RATE, "8000")设置正确的采样率
					// 注：当音频过长，静音部分时长超过VAD_EOS将导致静音后面部分不能识别。
					// 音频切分方法：FucUtil.splitBuffer(byte[] buffer,int length,int spsize);
//					mIat.writeAudio(audioData, 0, audioData.length);
//					mIat.stopListening();
				} else {
//					mIat.cancel();
//					showTip("读取音频流失败");
				}
			}
			break;

		default:
			break;
		}
	}

	private void requestPermissions(){
		try {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				int permission = ActivityCompat.checkSelfPermission(this,
						Manifest.permission.WRITE_EXTERNAL_STORAGE);
				if(permission!= PackageManager.PERMISSION_GRANTED) {
					ActivityCompat.requestPermissions(this,new String[]
							{Manifest.permission.WRITE_EXTERNAL_STORAGE,
									Manifest.permission.LOCATION_HARDWARE,Manifest.permission.READ_PHONE_STATE,
									Manifest.permission.WRITE_SETTINGS,Manifest.permission.READ_EXTERNAL_STORAGE,
									Manifest.permission.RECORD_AUDIO,Manifest.permission.READ_CONTACTS},0x0010);
				}

				if(permission != PackageManager.PERMISSION_GRANTED) {
					ActivityCompat.requestPermissions(this,new String[] {
							Manifest.permission.ACCESS_COARSE_LOCATION,
							Manifest.permission.ACCESS_FINE_LOCATION},0x0010);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
