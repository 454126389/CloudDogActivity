package android.rayee.cloud;

import java.io.File;

import com.iflytek.tts.TtsService.TtsPacket;

import android.content.Context;
import android.demo.CloudDogActivity;
import android.demo.CloudDogPreference;
import android.demo.HandlerMessageCode;
import android.demo.IGpsData;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class NetService extends Thread {
	private Context mContext = null;
	private Handler mMainHandler = null;
	private Handler mHandler = null;
	private static final String TAG = "NetService";

	private boolean mbConnectedCloud = false;
	private boolean mbNetworkConnected = false;	
	private CloudOperate mOperate = null;
	private CloudUpdate mUpdate = null;
	private CloudSelfPointUpload mSpUpload = null;
	private CloudPositionReport mLocationReport = null;
	private CloudWeather mWeather = null;
	private CloudRequestPosition mCurPosition = null;
	private CloudRequestRoadInfo mCurRoadInfo = null;

	public final static int EVENT_SERVICE_TEST = 0x3001;
	public final static int EVENT_SERVICE_START = 0x0001;
	public final static int EVENT_SERVICE_RECVDAT = 0x0002;
	public final static int EVENT_SERVICE_NETWORK_CONNECT_STATUS = 0x0003;
	public final static int EVENT_SERVICE_RECVPACKET = 0x0005;
	public final static int EVENT_SERVICE_HEART = 0x0006;
	public final static int EVENT_LOGIN_STATUS = 0x0007;
	public final static int EVENT_BROAD_MESSAGE = 0x0009;
	public final static int EVENT_CUR_LOCATION = 0x000A;
	public final static int EVENT_SERVICE_NETWORK_CONNECTING = 0x000B;
	public final static int EVENT_SERVICE_UPDATE_FINISH = 0x000C;
	public final static int EVENT_SERVICE_UPDATE_KEY = 0x000D;
	public final static int EVENT_SERVICE_EXIT = 0x000E;
	public final static int EVENT_SERVICE_WEATHER_INFO = 0x000F;
	public final static int EVENT_SERVICE_ADDRESS_INFO = 0x0010;
	public final static int EVENT_SERVICE_ROAD_INFO = 0x0011;
	
	public NetService(Context context) {
		// TODO Auto-generated constructor stub
		this.start();
		mContext = context;
		mMainHandler = ((CloudDogActivity)context).getHandler();
	}
	
	public Context getContext() {
		return mContext;
	}
	
	public Handler getHandler() {
		return mHandler;
	}
	
	public Looper getLooper() {
		return Looper.myLooper();
	}

	private Runnable mStartRunnable = new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			if(mOperate != null) {
				mOperate.connect();
			}
		}
	};

	class NetServiceHandler extends Handler {
		
		NetServiceHandler(Looper loop) {
			super(loop);
		}
		
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case EVENT_SERVICE_TEST:
				Log.v(TAG, "received test message");
				break;
				
			case EVENT_SERVICE_START:
				mOperate = new CloudOperate(NetService.this);
				mSpUpload = new CloudSelfPointUpload(NetService.this);
				mLocationReport = new CloudPositionReport(NetService.this);
				mWeather = new CloudWeather(NetService.this);
				mCurPosition = new CloudRequestPosition(NetService.this);
				mCurRoadInfo = new CloudRequestRoadInfo(NetService.this);
				this.postDelayed(mStartRunnable, 1000);
				break;

			case EVENT_SERVICE_NETWORK_CONNECT_STATUS:
				Log.v(TAG, "connect to server status " + String.valueOf(msg.arg1));
				mMainHandler.sendMessage(mMainHandler.obtainMessage(HandlerMessageCode.HMC_CLOUD_NETWORK_STATUS, msg.arg1, msg.arg2));
				
				if (msg.arg1 == 1) {
					mbNetworkConnected = true;
					mOperate.Start();
				} else if (msg.arg1 == 2) {
					mbNetworkConnected = false;
					mbConnectedCloud = false;
					if (mOperate != null) {
						if(mUpdate != null) {
							mUpdate.close();
							mUpdate = null;
						}
						
						if(mSpUpload != null) {
							mSpUpload.close();
							mSpUpload = null;
						}
						
						mOperate.close();
						this.removeCallbacks(mStartRunnable);
						this.postDelayed(mStartRunnable, 3 * 60 * 1000);
						Log.v(TAG, "network disconnect, reconnect after 3 minutes");
					}
				}
				break;

			case EVENT_LOGIN_STATUS:
				mMainHandler.sendMessage(mMainHandler.obtainMessage(HandlerMessageCode.HMC_CLOUD_LOGIN_STATUS, msg.arg1, msg.arg2));
				if (msg.arg1 == 1) {
					Log.v(TAG, "Login server success");
					mbConnectedCloud = true;
					if(mSpUpload != null) {
						mSpUpload.start(new File(mContext.getFilesDir(), "self.xml"));
					}
				} else {
					Log.v(TAG, "Login server fail");
					mbConnectedCloud = false;
					break;
				}
				
				break;

			case EVENT_BROAD_MESSAGE:
				Log.v(TAG, "receive broadcast message " + (String) msg.obj);
				mMainHandler.sendMessage(mMainHandler.obtainMessage(HandlerMessageCode.HMC_CLOUD_BROADCAST_MSG, msg.obj));
				break;

			case EVENT_SERVICE_UPDATE_FINISH:
				if(msg.arg1 == 0) {
					//fail
				} else if(msg.arg1 == 1) {
					mMainHandler.sendMessage(mMainHandler.obtainMessage(HandlerMessageCode.HMC_UPDATE_UPDATE_DATBASE, msg.obj));
				} else if(msg.arg1 == 2) {
					mMainHandler.sendMessage(mMainHandler.obtainMessage(HandlerMessageCode.HMC_UPDATE_BASIC_DATABASE, msg.obj));
				} else if(msg.arg1 == 4) {
					mMainHandler.sendMessage(mMainHandler.obtainMessage(HandlerMessageCode.HMC_UPDATE_EXTRA_VOICE, msg.obj));
				}
				
				if(mUpdate != null) {
					mUpdate.close();
					mUpdate = null;
				}
				break;
				
			case EVENT_SERVICE_WEATHER_INFO:
				mMainHandler.sendMessage(mMainHandler.obtainMessage(HandlerMessageCode.HMC_CLOUD_WEATHER_INFO, msg.obj));
				break;
				
			case EVENT_SERVICE_ADDRESS_INFO:
				mMainHandler.sendMessage(mMainHandler.obtainMessage(HandlerMessageCode.HMC_CLOUD_ADDRESS_INFO, msg.obj));
				break;
				
			case EVENT_SERVICE_ROAD_INFO:
				Log.v(TAG, (String)msg.obj);
				mMainHandler.sendMessage(mMainHandler.obtainMessage(HandlerMessageCode.HMC_CLOUD_ADDRESS_INFO, msg.obj));
				break;
				
			case EVENT_SERVICE_EXIT:
				this.getLooper().quit();
				mHandler = null;
				break;
			}
		}
	};
	
	@Override
	public void run() {
		Looper.prepare();
		mHandler = new NetServiceHandler(Looper.myLooper());
		mHandler.sendEmptyMessage(EVENT_SERVICE_START);
		Looper.loop();
		Log.v(TAG, "net servie thread exit");
	}

	public void close() {
		if (mOperate != null) {
			mMainHandler.removeCallbacks(mStartRunnable);
			if(mUpdate != null) {
				mUpdate.close();
			}
			
			if(mSpUpload != null) {
				mSpUpload.close();
			}
			
			mOperate.close();
		}
		
		if(mHandler != null) {
			mHandler.sendEmptyMessage(EVENT_SERVICE_EXIT);
		}
		mOperate = null;
		mUpdate = null;
		mSpUpload = null;
	}
	
	public boolean startUpdate(int latitude, int longitude, byte[] key, int version) {
		// TODO Auto-generated method stub
		if (mbConnectedCloud) {
			if(mUpdate == null) {
				mUpdate = new CloudUpdate(this, mOperate);
				mUpdate.start(latitude,  longitude, CloudDogPreference.mUpdateRadius, key, version);
			}
		}
		
		return false;
	}
	
	public boolean reportLocation(IGpsData gps) {
		if(mbConnectedCloud) {
			mLocationReport.ReportPoint(gps);
		}
		return true;
	}
	
	public boolean getWeather(double latitude, double longitude) {
		if(mWeather == null) {
			return false;
		}
		
		mWeather.requestWeather(latitude, longitude);
		return true;
	}
	
	public boolean getCurAddressName(double latitude, double longitude) {
		if(mCurPosition == null) {
			return false;
		}
		
		mCurPosition.requestCurrentPosition(latitude, longitude);
		return true;
	}
	
	public boolean getCurLoadInfo(double latitude, double longitude) {
		if(mCurRoadInfo == null) {
			return false;
		}
		
		mCurRoadInfo.requestRoadStatus(latitude, longitude);
		return true;
	}
	
	public boolean setUpdateKey() {
		if(mOperate == null) {
			return false;
		}
		CloudDogPreference.mUpdateOldKey = CloudDogPreference.mUpdateNewKey;
		CloudDogPreference.mUpdateNewKey = CloudDogPreference.mUpdateDatabasePKey;
		mOperate.setUpdateKey(CloudDogPreference.mUpdateNewKey);
		CloudDogPreference.save();
		return true;
	}
	
	public boolean restart() {
		// TODO Auto-generated method stub
		if(mbNetworkConnected) {
			//close1();
			//start();
		}
		
		return false;
	}
	
	public boolean addSelfpoint(byte[] data, String recfile) {
		// TODO Auto-generated method stub
		//Log.e(TAG, DebugOperate.ByteBufferConvertToString(data, '-'));
		mSpUpload.addSelfPoint(data, recfile);
		if(mbConnectedCloud && mOperate != null) {
			mSpUpload.start();
		}
		return false;
	}
}
