package android.demo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class CloudDogPreference {
	private static Context mContext = null;
	//private static String mCloudInfoFile = Environment.getExternalStorageDirectory().getAbsolutePath() + java.io.File.separator;
	
	public static String ServerIP 				= "183.251.83.25";
	public static int ServerTCPPort 			= 8124;//8124;//8001;
	public static int ServerUDPPort 			= 8123;//8123;//8000;
	
	/* FKey(first) PKey(last) */
	public static byte[] mBaseDatabaseFKey = { (byte) 0xE2, 0x00, 0x54, 0x5F, (byte) 0xCF, (byte) 0xB9, (byte) 0x84, 0x6A, 0x7A, (byte) 0xB4, 0x4B, (byte) 0xCD, 0x4D, (byte) 0xBA, (byte) 0xF6, 0x68 };

	public static byte[] mBaseDatabasePKey = { (byte) 0xAF, (byte) 0xA0, (byte) 0xAC, 0x75, 0x44, 0x19, (byte) 0xE8, 0x77, 0x2A, 0x73, (byte) 0xF7, (byte) 0xE8, 0x35, 0x01, (byte) 0xE7, 0x1B };

	public static byte[] mUpdateDatabaseFKey = { 0x17, 0x30, (byte) 0xc1, (byte) 0xa2, 0x19, (byte) 0xef, 0x0, 0x26, 0x31, 0x34, 0x41,
			(byte) 0xee, (byte) 0xfa, (byte) 0x85, (byte) 0x97, 0x1f };

	public static byte[] mUpdateDatabasePKey = { 0x28, 0x58, 0x7f, (byte) 0x94, 0x12, 0x15, 0x44, 0x3b, 0x3e, (byte) 0xba, 0x63,
			(byte) 0xf6, 0x14, 0x7d, 0x68, 0x56 };

	public static byte[] mUpdateOldKey = { 0x14, 0x7d, 0x0d, 0x13, 0x1b, (byte) 0xc2, 0x70, 0x06, 0x4c, (byte) 0xe7, (byte) 0xb5, 0x1f, 0x48,
		(byte) 0xc7, (byte) 0xc2, 0x05 };
	
	public static byte[] mUpdateNewKey = { 0x14, 0x7d, 0x0d, 0x13, 0x1b, (byte) 0xc2, 0x70, 0x06, 0x4c, (byte) 0xe7, (byte) 0xb5, 0x1f, 0x48,
			(byte) 0xc7, (byte) 0xc2, 0x05 };

	public static volatile int mBaseDatabaseVersion = 0;
	public static volatile int mUpdateDabaBaseVersion = 0;

	public static double mLastUpdateLatitude = 0;
	public static double mLastUpdateLongitude = 0;
	public static long mLastUpdateTime = 0;
	
	public static int mNormalPositionReportInterval = 30;
	public static int mParkPositionReportInterval = 30;
	public static int mRoadStatusReportInterval = 30;
	public static boolean mbUploadLocation = false;
	public static boolean mListenInPark = false;
	public static String mMasterPhoneNumber = "13319403411";
	public static String mSecondPhoneNumber = "00000000000";
	public static int mDefendLevel = 0;
	public static boolean mbEnableUploadSelfPointRecord = true;

	public static int mUpdateRadius	= 5;

	public final static String PERFERENCE_FULL_NAME = "FULL";
	// 两个号码设置
	public final static String PERFERENCE_FSNU_NAME = "FSNU";
	// GPS数据上传参数
	public final static String PERFERENCE_SGPS_NAME = "SGPS";
	// 数据更新半径
	public final static String PERFERENCE_RMAP_NAME = "RMAP";
	public final static String PERFERENCE_TR_T_NAME = "TR_T";
	public final static String PERFERENCE_DEFE_NAME = "DEFE";
	// 服务器TCP IP地址
	public final static String PERFERENCE_IP_T_NAME = "IP_T";
	// 服务器UDP IP地址
	public final static String PERFERENCE_IP_U_NAME = "IP_U";
	// 更新KEY参数
	public final static String PERFERENCE_PKEY_NAME = "PKEY";
	// 自建点上传开关
	public final static String PERFERENCE_SPRU_NAME = "SPRU";

	private static final String TAG = "DogPreference";

	public static final void setUpdateDatabasePKey(byte[] key) {
		CloudDogPreference.mUpdateDatabasePKey = key;
	}

	public static final void setUpdateDatabaseFKey(byte[] key) {
		CloudDogPreference.mUpdateDatabaseFKey = key;
	}
	
	public static void setContext(Context context) {
		mContext = context;
	}
	
	public static void save() {
		SharedPreferences settings = mContext.getSharedPreferences("server", 0);
		Editor editor = settings.edit();
		editor.putString("ip", ServerIP);
		editor.putInt("tcp", ServerTCPPort);
		editor.putInt("udp", ServerUDPPort);
		editor.putLong("time", mLastUpdateTime);
		editor.putFloat("latitude", (float)mLastUpdateLatitude);
		editor.putFloat("longitude", (float)mLastUpdateLongitude);
		editor.putString("BFKey", DebugOperate.ByteBufferConvertToString(mBaseDatabaseFKey, '-'));
		editor.putString("BPKey", DebugOperate.ByteBufferConvertToString(mBaseDatabasePKey, '-'));
		editor.putString("UFKey", DebugOperate.ByteBufferConvertToString(mUpdateDatabaseFKey, '-'));
		editor.putString("UPKey", DebugOperate.ByteBufferConvertToString(mUpdateDatabasePKey, '-'));
		editor.putString("UNewKey", DebugOperate.ByteBufferConvertToString(mUpdateNewKey, '-'));
		editor.putString("UOldKey", DebugOperate.ByteBufferConvertToString(mUpdateOldKey, '-'));
		
		editor.commit();
	}
	
	public static void read() {
		SharedPreferences settings = mContext.getSharedPreferences("server", 0);
		
		if(settings.contains("ip")) {
			ServerIP = settings.getString("ip", "183.251.83.25");
			ServerTCPPort = settings.getInt("tcp", 8124);
			ServerUDPPort = settings.getInt("udp", 8123);
			mLastUpdateTime = settings.getLong("time", System.currentTimeMillis());
			mLastUpdateLatitude = settings.getFloat("latitude", (float) 0.0);
			mLastUpdateLongitude = settings.getFloat("longitude", (float) 0.0);
			mBaseDatabaseFKey = DebugOperate.StringToByteArray(settings.getString("BFKey", ""), '-');
			mBaseDatabasePKey = DebugOperate.StringToByteArray(settings.getString("BPKey", ""), '-');
			mUpdateDatabaseFKey = DebugOperate.StringToByteArray(settings.getString("UFKey", ""), '-');
			mUpdateDatabasePKey = DebugOperate.StringToByteArray(settings.getString("UPKey", ""), '-');
			mUpdateNewKey = DebugOperate.StringToByteArray(settings.getString("UNewKey", ""), '-');
			mUpdateOldKey = DebugOperate.StringToByteArray(settings.getString("UOldKey", ""), '-');
		} else {
			save();
		}
	}
}
