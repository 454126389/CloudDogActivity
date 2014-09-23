package android.demo;

import java.lang.reflect.Method;
import java.util.Calendar;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.telephony.TelephonyManager;
import android.util.Log;

public class UserPreference {
	private static final String TAG								= "preference";
	private static Context mContext 							= null;
	private static final String PREFERENCE_FILE_NAME			= "user";
	
	//private static Object waitObject							= new Object();
	
	public static long mDeviceSn								= 0x0000000011111114;
	public static final long getDeviceSn() {
		return mDeviceSn;
	}

	public static final void setDeviceSn(long sn) {
		UserPreference.mDeviceSn = sn;
	}

	public static int mSoftwareVersion							= 0x12345678;
	public static String mSimIMSI								= "460023803225511";

	private TelephonyManager mTM								= null;
	
	public static byte mUserPicSafeMode							= 0;	// 0->safe spd, 1->camera spd, 2->fix camera spd, 3->safe, 4->camera, 5->fix camera
	public static byte mUserSpeedUnit							= 0;	// 0->����, 1->Ӣ��, 2->����
	public static byte mUserCityMode							= 0;	// 0->ȫ��, 1->����, 2->����
	public static byte mUserTimeZoneHour						= 0;	// ʱ��--Сʱ
	public static byte mUserTimeZoneMinute						= 0;	// ʱ��--��
	public static byte mUserSpeedRebuild						= 0;	// �ٶ��ؽ�
	public static byte mUserVoiceLong							= 0;	// 0->�����, 1->�����
	public static byte mUserTimeFormat							= 0;	// 0->24��, 1->12��
	public static byte mUserCruiseSpeed							= 0;	// Ѳ���ٶ�(�����ռ�����ʹ��20->80m, 30->150m, 40~60->300m, 70~80->500m, 90->700m, 100~120->1000m)
	public static byte mUserVoiceMode							= 0;	// 0->����ģʽ, 1->����ģʽ, 2->����ģʽ
	public static byte mUserRDAutoMuteBelowSpeed				= 0;	// 0->OFF(����),1~15->10~150����/ʱ�����״ﾲ��
	public static byte mUserVolume								= 0;	// ����
	public static byte mUserRDSensitivity						= 0;	// �״�ж� 0->����, 1->�� , 2->��, 3->��
	public static byte mUserAutoLightTime						= 0;	// �Զ�����ʱ��
	public static byte mUserAutoDimTime							= 0;	// �Զ�΢��ʱ��
	public static byte mUserRDSwitch							= 0;	// �״￪�� 0->on & offline alarm, 1->on no offline alarm, 2->off
	public static byte mUserSpecialVoiceSwitch					= 0;	// special voice 0->off 1->on
	public static byte mUserXBandSwitch							= 0;	// 0->off, other->on
	public static byte mUserKUBandSwitch						= 0;	// 0->off, other->on
	public static byte mUserKABandSwitch						= 0;	// 0->off, other->on
	public static byte mUserKBandSwitch							= 0;	// 0->off, other->on
	public static byte mUserLaserSwitch							= 0;	// 0->off, other->on
	public static byte mUserRadarLightTime						= 0;	// fix radar start work time
	public static byte mUserRadarDimTime						= 0;	// fix radar not work time
	public static byte mUserCameraSwitch						= 0;	// inter camera control 0->off, 1->on
	public static byte mUserRDAutoMuteSwitch					= 0;	// rd mute auto 0->on, other->off
	
	public static int mTotalDistance							= 0;	// �ܼ��ó�
	public static int mTripDistance								= 0;	// �ۼ��ó�
	public static int mMaxSpeed									= 0;	// ��߳���//20090216change
	public static Calendar mMaxSpeedDate						= Calendar.getInstance();	// ��߳�������

	public static String getSerialNumber(){
	    String serial = null;
	    try {
	    Class<?> c =Class.forName("android.os.SystemProperties");
	       Method get =c.getMethod("get", String.class);
	       serial = (String)get.invoke(c, "ro.serialno");
	    } catch (Exception e) {
	       e.printStackTrace();
	    }
	    return serial;
	}
	
	UserPreference( Context context ) {
		mContext = context;
		mTM = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
		UserPreference.mSimIMSI = mTM.getSubscriberId();
		/*
		Log.i(TAG, "SoftWare Version: " + mTM.getDeviceSoftwareVersion());
		Log.i(TAG, "IMSI: " + mTM.getSubscriberId());
		Log.i(TAG, "IMEI: " + mTM.getDeviceId());
		Log.i(TAG, "Phone number: " + mTM.getLine1Number());
		Log.i(TAG, "Serial: " + getSerialNumber());
		*/
	}
	
	public boolean ReadPreference() {
		SharedPreferences settings = mContext.getSharedPreferences(PREFERENCE_FILE_NAME, 0);
		
		if(settings.contains("sn")) {
			mDeviceSn = settings.getLong("sn", 0x0000000011111119);
			mSimIMSI = settings.getString("IMSI", "460023803225511");
			mSoftwareVersion = settings.getInt("version", 0x12345678);
			mBroadcastSwitch = settings.getInt("Broadcast", 0xFFFFFFFF);
			mUserPicSafeMode = (byte)settings.getInt("PicSafeMode", 0);
			mUserSpeedUnit = (byte)settings.getInt("SpeedUnit", 0);
			mUserCityMode = (byte)settings.getInt("CityMode", 0);
			mUserTimeZoneHour = (byte)settings.getInt("TimeZoneHour", 0);
			mUserTimeZoneMinute = (byte)settings.getInt("TimeZoneMin", 0);
			mUserSpeedRebuild = (byte)settings.getInt("SpeedRebuild", 0);
			mUserVoiceLong = (byte)settings.getInt("VoiceLong", 0);
			mUserTimeFormat = (byte)settings.getInt("TimeFormat", 0);
			mUserCruiseSpeed = (byte)settings.getInt("CruiseSpeed", 0);
			mUserVoiceMode = (byte)settings.getInt("VoiceMode", 0);
			mUserRDAutoMuteBelowSpeed = (byte)settings.getInt("RDSpeed", 0);
			mUserVolume = (byte)settings.getInt("volume", 0);
			mUserRDSensitivity = (byte)settings.getInt("RDSens", 0);
			mUserAutoLightTime = (byte)settings.getInt("LEDLTime", 0);
			mUserAutoDimTime = (byte)settings.getInt("LEDDTime", 0);
			mUserRDSwitch = (byte)settings.getInt("RDSwitch", 0);
			mUserSpecialVoiceSwitch = (byte)settings.getInt("SVswitch", 0);
			mUserXBandSwitch = (byte)settings.getInt("XBand", 0);
			mUserKUBandSwitch = (byte)settings.getInt("KUBand", 0);
			mUserKABandSwitch = (byte)settings.getInt("KABand", 0);
			mUserLaserSwitch = (byte)settings.getInt("Laser", 0);
			mUserRadarLightTime = (byte)settings.getInt("RDLTime", 0);
			mUserRadarDimTime = (byte)settings.getInt("RDDTime", 0);
			mUserCameraSwitch = (byte)settings.getInt("Camera", 0);
			mTotalDistance = settings.getInt("TotalDis", 0);
			mTripDistance = settings.getInt("TripDis", 0);
			mMaxSpeed = settings.getInt("MaxSpeed", 0);
			mMaxSpeedDate.setTimeInMillis(settings.getLong("MaxSpeedTime", System.currentTimeMillis()));
		}
		else {
			SavePreference();
		}

		return true;
	}
	
	public boolean SavePreference() {
		SharedPreferences settings = mContext.getSharedPreferences(PREFERENCE_FILE_NAME, 0);
		Editor editor = settings.edit();
		//editor.putString("autostart", "off");
		editor.putLong("sn", mDeviceSn);
		editor.putString("IMSI", mSimIMSI);
		editor.putInt("version", mSoftwareVersion);
		editor.putInt("Broadcast", mBroadcastSwitch);
		editor.putInt("PicSafeMode", mUserPicSafeMode);
		editor.putInt("SpeedUnit", mUserSpeedUnit);
		editor.putInt("CityMode", mUserCityMode);
		editor.putInt("TimeZoneHour", mUserTimeZoneHour);
		editor.putInt("TimeZoneMin", mUserTimeZoneMinute);
		editor.putInt("SpeedRebuild", mUserSpeedRebuild);
		editor.putInt("VoiceLong", mUserVoiceLong);
		editor.putInt("TimeFormat", mUserTimeFormat);
		editor.putInt("CruiseSpeed", mUserCruiseSpeed);
		editor.putInt("VoiceMode", mUserVoiceMode);
		editor.putInt("RDSpeed", mUserRDAutoMuteBelowSpeed);
		editor.putInt("volume", mUserVolume);
		editor.putInt("RDSens", mUserRDSensitivity);
		editor.putInt("LEDLTime", mUserAutoLightTime);
		editor.putInt("LEDDTime", mUserAutoDimTime);
		editor.putInt("RDSwitch", mUserRDSwitch);
		editor.putInt("SVswitch", mUserSpecialVoiceSwitch);
		editor.putInt("XBand", mUserXBandSwitch);
		editor.putInt("KUBand", mUserKUBandSwitch);
		editor.putInt("KABand", mUserKABandSwitch);
		editor.putInt("Laser", mUserLaserSwitch);
		editor.putInt("RDLTime", mUserRadarLightTime);
		editor.putInt("RDDTime", mUserRadarDimTime);
		editor.putInt("Camera", mUserCameraSwitch);
		editor.putInt("TotalDis", mTotalDistance);
		editor.putInt("TripDis", mTripDistance);
		editor.putInt("MaxSpeed", mMaxSpeed);
		editor.putLong("MaxSpeedTime", mMaxSpeedDate.getTimeInMillis());
		editor.commit();
		return true;
	}
	
	public static byte[] tobyteArray() {
		byte[] array = new byte[41];
		int i = 0;
		
		array[i++] = 1;			//user flag
		array[i++] = mUserPicSafeMode;
		array[i++] = mUserSpeedUnit;
		array[i++] = mUserCityMode;
		array[i++] = mUserTimeZoneHour;
		array[i++] = mUserTimeZoneMinute;
		array[i++] = mUserSpeedRebuild;
		array[i++] = mUserVoiceLong;
		array[i++] = mUserTimeFormat;
		array[i++] = mUserCruiseSpeed;
		array[i++] = mUserVoiceMode;
		array[i++] = mUserRDAutoMuteBelowSpeed;
		array[i++] = mUserVolume;
		array[i++] = mUserRDSensitivity;
		array[i++] = mUserAutoDimTime;
		array[i++] = mUserRDSwitch;
		
		array[i++] = mUserSpecialVoiceSwitch;
		array[i++] = mUserXBandSwitch;
		array[i++] = mUserKUBandSwitch;
		array[i++] = mUserKABandSwitch;
		
		array[i++] = mUserLaserSwitch;
		array[i++] = mUserRadarLightTime;
		
		array[i++] = mUserRadarDimTime;
		array[i++] = mUserCameraSwitch;
		
		array[i++] = (byte)(mTotalDistance >> 24);
		array[i++] = (byte)(mTotalDistance >> 16);
		array[i++] = (byte)(mTotalDistance >> 8);
		array[i++] = (byte)(mTotalDistance);
		
		array[i++] = (byte)(mTripDistance >> 24);
		array[i++] = (byte)(mTripDistance >> 16);
		array[i++] = (byte)(mTripDistance >> 8);
		array[i++] = (byte)(mTripDistance);
		
		array[i++] = (byte)(mMaxSpeed >> 8);
		array[i++] = (byte)(mMaxSpeed);
		
		array[i++] = (byte)(mMaxSpeedDate.get(Calendar.YEAR) >> 8);
		array[i++] = (byte)(mMaxSpeedDate.get(Calendar.YEAR));
		array[i++] = (byte)(mMaxSpeedDate.get(Calendar.MONTH));
		array[i++] = (byte)(mMaxSpeedDate.get(Calendar.DAY_OF_MONTH));
		array[i++] = (byte)(mMaxSpeedDate.get(Calendar.HOUR));
		array[i++] = (byte)(mMaxSpeedDate.get(Calendar.MINUTE));
		array[i++] = (byte)(mMaxSpeedDate.get(Calendar.SECOND));
		
		return array;
	}
	
	//�����Ϣ���أ��ܹ�32bit�����Բ���32����Ϣ��0��ʾ�أ������������ͣ���1��ʾ�������������Ͷ�Ӧ��Ϣ��
	private static int mBroadcastSwitch								= 0xFFFFFFFF;
	
	public final static int getBroadcastSwitch() {
		return mBroadcastSwitch;
	}

	public final static void setBroadcastSwitch(int BroadcastSwitch) {
		mBroadcastSwitch = BroadcastSwitch;
	}
}
