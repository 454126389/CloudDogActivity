package com.rayee.monitor;

import android.app.Service;
import android.content.Context;
import android.demo.CloudDogActivity;
import android.demo.DemoApplication;
import android.demo.HandlerMessageCode;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.util.Log;

public final class Monitor implements SensorEventListener{
	
	private static final String TAG = "sensor";
	private SensorManager mSensorManager = null;
	private Sensor mSensor = null;
	private Handler mHandler = null;
	private float[] mLastRaw = null;
	private long mLastTime = 0;
	
	public Monitor(Context context) {
		mSensorManager = (SensorManager) context.getSystemService(Service.SENSOR_SERVICE);
		mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		
		if(mSensor == null) {
			Log.v(TAG, "not aviable the ACCELEROMETER sensor");
			return;
		}
		
		mHandler = ((CloudDogActivity)context).getHandler();
		mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	public void close() {
		mSensorManager.unregisterListener(this, mSensor);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		
		if(mLastRaw == null) {
			mLastRaw = event.values.clone();
		} else {
			if((Math.abs(mLastRaw[0] - event.values[0]) > 0.2)
					|| (Math.abs(mLastRaw[1] - event.values[1]) > 0.2) 
					|| (Math.abs(mLastRaw[2] - event.values[2]) > 0.15) ) {
				Log.v(TAG, "monitor move");
				long time = System.currentTimeMillis();
				
				if(mLastTime - time > 2000) {
					mHandler.sendEmptyMessage(HandlerMessageCode.HMC_MONITOR_ALARM);
					mLastTime = System.currentTimeMillis();
				}				
			}
			//Log.v(TAG, String.valueOf(Math.sqrt((mLastRaw[0] - event.values[0]) * (mLastRaw[0] - event.values[0]) 
			//		+ (mLastRaw[1] - event.values[1]) * (mLastRaw[1] - event.values[1])
			//		+ (mLastRaw[2] - event.values[2]) * (mLastRaw[2] - event.values[2]))));
			//Log.v(TAG, String.format("get raw data %f %f %f %f %f %f", event.values[0], event.values[1], event.values[2], mLastRaw[0], mLastRaw[1], mLastRaw[2]));
			mLastRaw = event.values.clone();
		}
	}
	
}
