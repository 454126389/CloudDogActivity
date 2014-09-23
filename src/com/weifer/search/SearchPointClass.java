package com.weifer.search;

import java.io.UnsupportedEncodingException;

import android.R.integer;
import android.demo.CloudDogActivity;
import android.demo.DebugOperate;
import android.demo.HandlerMessageCode;
import android.demo.PwmAudio;
import android.demo.UserPreference;
import android.os.Handler;
import android.os.Message;
import android.util.*;

public final class SearchPointClass {
	private static final String TAG = "SearchPoint";
	private static final int POINT_BUFFER_LEN = 18;
	private static final int PASSWORD_LEN = 16;
	private static final int SELF_POINT_MODE = 3;
	private static final String DATABASE_DIRECTORY = new String(android.os.Environment.getExternalStorageDirectory().getPath()
			+ java.io.File.separator);
	private static final String BASE_FILE_NAME = DATABASE_DIRECTORY + "i6011s"; // new
	// String();
	private static final String UPDATE_FILE_NAME = DATABASE_DIRECTORY + "u7333u"; // new
	// String("u7333u");
	private static final String EXTRA_BASE_FILE_NAME = DATABASE_DIRECTORY + "e3227e"; // new
	// String("e3227e");
	private static final String EXTRA_UPDATE_FILE_NAME = DATABASE_DIRECTORY + "e3228u"; // new
	// String("e3228u");
	private static final String SELF_POINT_FILE_NAME = DATABASE_DIRECTORY + "r9555b"; // new
	// String("r9555b");
	private static final int RADAR_AVG_MOD = 0;
	public static final int TTS_SPEAK_CONTENT = 0x6001;
	public static final int RADAR_WARNNING_DISPLAY = 0x6002;
	public static final int UPDATE_BASIC_DATABASE = 0x6003;
	public static final int UPDATE_UPDATE_DATBASE = 0x6004;
	public static final int UPDATE_EXTRA_VOICE = 0x6005;

	private Handler mHandler = null;

	private static PwmAudio mRadarPwm = new PwmAudio();
	private final static int RADAR_X_BAND = 0;
	private final static int RADAR_KU_BAND = 1;
	private final static int RADAR_K_BAND = 2;
	private final static int RADAR_KA_BAND = 3;
	private final static int RADAR_LASER = 4;
	private final static int RADAR_LEVEL_HIGH = 1;
	private final static int RADAR_LEVEL_MIDDLE = 2;
	private final static int RADAR_LEVEL_LOW = 3;
	private boolean mbRadarHwDoing = false;
	private boolean mbRadarSmDoing = false;
	private static Object mRadarObject = new Object(); 
	
	private final static int RADAR_ALARM_BY_SIMULATE = 1;
	private final static int RADAR_ALARM_BY_HARDWARE = 0;

	private static byte[] mRadarXBandWave = PwmAudio.CreatePwm(1000, 250);
	private static byte[] mRadarKBandWave = PwmAudio.CreatePwm(500, 250);
	private static byte[] mRadarKaBandWave = PwmAudio.CreatePwm(416, 250);
	private static byte[] mRadarKuBandWave = PwmAudio.CreatePwm(625, 250);

	private static final int NOTIFY_UI_CODE_TTS_PLAY = 0x0001;
	private static final int NOTIFY_UI_CODE_RADAR_WARNNING = 0x0002;
	private static final int NOTIFY_UI_CODE_ALARM_DISTANCE = 0x0005;
	private static final int NOTIFY_UI_CODE_BASIC_DATABASE_UPDATE_FINISH = 0x0006;
	private static final int NOTIFY_UI_CODE_ALARM_POINT_STATUS = 0x0007;

	public SearchPointClass(Handler handler) {// Initial function
		mHandler = handler;
	}

	private static final int GPS_DB_POINT_TYPE_PICTURE_MODE = 0;
	private static final int GPS_DB_POINT_TYPE_SAFE_MODE = 1;
	private static final int GPS_DB_POINT_TYPE_SELF_POINT_MODE = 3;
	private static final int GPS_DB_POINT_TYPE_ONEPC_PICTURE_MODE = 4;
	private static final int GPS_DB_POINT_TYPE_ONEPC_SAFE_MODE = 5;
	private static final int GPS_DB_POINT_TYPE_ONEPNC_PICTURE_MODE = 8;
	private static final int GPS_DB_POINT_TYPE_ONEPNC_SAFE_MODE = 9;
	private static final int GPS_DB_POINT_TYPE_SPECIAL_VOICE_MODE = 15;

	public final int Initial() {
		Setting(UserPreference.tobyteArray());
		mDogThread.start();
		return 0;
	}

	private DogThread mDogThread = new DogThread();
	public final int StartSearch(int[] array) {
		mDogThread.arg = array;
		synchronized (mDogThread) {
			mDogThread.notify();
		}

		return 0;
	}

	public final void SearchPointTtsSpeak(byte[] strBuf) {
		String str;
		int level = strBuf[0];
		try {
			str = new String(strBuf, 1, strBuf.length - 1, "GBK");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

		// str = new String(strBuf);
		Log.v(TAG, "tts play " + str);
		Message msg = mHandler.obtainMessage(HandlerMessageCode.HMC_TTS_SPEAK_CONTENT, level, 0, str);
		mHandler.sendMessage(msg);
	}
	
	private Runnable mRadarStopRunnable = new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			synchronized (mRadarObject) {
				if(mbRadarHwDoing || mbRadarSmDoing) {
					return;
				}
				
				mRadarPwm.stop();
				String strRadarType = "";
				Message msg = mHandler.obtainMessage(HandlerMessageCode.HMC_RADAR_WARNNING_DISPLAY, 0, 0, strRadarType);
				mHandler.sendMessage(msg);
			}
		}
	};

	public int JniCallBack(int code, byte[] parameter) {
		switch (code) {
		case NOTIFY_UI_CODE_TTS_PLAY:
			SearchPointTtsSpeak(parameter);
			break;
			
		case NOTIFY_UI_CODE_ALARM_DISTANCE:
			mHandler.sendMessage(mHandler.obtainMessage(HandlerMessageCode.HMC_NOTIFY_ALARM_DISTANCE, parameter));
			break;
			
		case NOTIFY_UI_CODE_BASIC_DATABASE_UPDATE_FINISH:
			mHandler.sendEmptyMessage(HandlerMessageCode.HMC_DATABASE_UPDATE_FINISH);
			break;
			
		case NOTIFY_UI_CODE_ALARM_POINT_STATUS:
			mHandler.sendMessage(mHandler.obtainMessage(HandlerMessageCode.HMC_POINT_ALARM_STATUS, parameter));
			break;

		case NOTIFY_UI_CODE_RADAR_WARNNING:
			byte[] radar_wave = null;
			int level = parameter[0];
			String strRadarType = null;
			int dev = parameter[2];
			
			if (level == -1) {
				synchronized (mRadarObject) {
					if(dev == RADAR_ALARM_BY_SIMULATE) {
						mbRadarSmDoing = false;
					} else if(dev == RADAR_ALARM_BY_HARDWARE) {
						mbRadarHwDoing = false;
					}
					
					if(mbRadarHwDoing || mbRadarSmDoing) {
						break;
					}
				}

				mHandler.removeCallbacks(mRadarStopRunnable);
				mHandler.postDelayed(mRadarStopRunnable, 5000);
				break;
			} else {
				synchronized (mRadarObject) {
					if(dev == RADAR_ALARM_BY_SIMULATE) {
						if(mbRadarSmDoing) {
							break;
						}
						
						mbRadarSmDoing = true;
						
						if(mbRadarHwDoing) {
							break;
						}
					} else if(dev == RADAR_ALARM_BY_HARDWARE) {
						if(mbRadarHwDoing) {
							break;
						}
						
						mbRadarHwDoing = true;
						
						if(mbRadarSmDoing) {
							break;
						}
					}
				}
				
				mHandler.removeCallbacks(mRadarStopRunnable);
				
				switch (parameter[1]) {
				case RADAR_X_BAND:
					radar_wave = mRadarXBandWave;
					strRadarType = "X Band";
					break;

				case RADAR_K_BAND:
					radar_wave = mRadarKBandWave;
					strRadarType = "K Band";
					break;

				case RADAR_KU_BAND:
					radar_wave = mRadarKuBandWave;
					strRadarType = "KU Band";
					break;

				case RADAR_KA_BAND:
					radar_wave = mRadarKaBandWave;
					strRadarType = "KA Band";
					break;

				case RADAR_LASER:
					strRadarType = "Laser";
					break;
				}

				switch (parameter[0] & 0x0F) {
				case RADAR_LEVEL_HIGH:
					level = 100;
					break;

				case RADAR_LEVEL_MIDDLE:
					level = 200;
					break;

				case RADAR_LEVEL_LOW:
					level = 300;
					break;
				}

				if (radar_wave != null) {
					mRadarPwm.play(radar_wave, level);
				}
			}

			Message msg = mHandler.obtainMessage(HandlerMessageCode.HMC_RADAR_WARNNING_DISPLAY, parameter[0] & 0x0F, 0, strRadarType);
			mHandler.sendMessage(msg);
			break;
		}
		return 0;
	}
	
	class DogThread extends Thread {
		int[] arg = null;
		
		public DogThread() {

		}
		
		@Override
		public void run() {
			Init();
			while(true) {
				try {
					synchronized (this) {
						this.wait();
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					break;
				}
				Run(arg);
			}
			DeInit();
		}
		
	}

	public byte[] GetUserSetting() {
		return UserPreference.tobyteArray();
	}
	
	public void Exit() {
		mDogThread.interrupt();
		try {
			mDogThread.join(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public native int DeInit();

	public native int Init();

	public native int Run(int[] array);

	public native int Setting(byte[] array);

	public native static int SaveSelfPoint(int index, byte[] array);

	public native static int getSelfPointFreeIndex();
	
	public native int Test();
	
	public native int updateBase(String path);
	public native int updateUpdate(String path);
	public native int updateExtra(String path);

	static {
		System.loadLibrary("dog");
	}
}
