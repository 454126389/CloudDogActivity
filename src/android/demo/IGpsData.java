package android.demo;

import java.util.Calendar;
import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 保存GPS数据，用于各个模块之间传递
 * @author Leven.lai
 *
 */
public final class IGpsData {
	/**
	 * 纬度 
	 */
	public double mLatitude 						= 0;
	
	/**
	 * 经度 
	 */
	public double mLongtitude 						= 0;
	
	/**
	 * 海拔
	 */
	public double mAltitude 						= 0;
	
	/**
	 * 方向
	 */
	public double mBearing 						= 0;
	
	/**
	 * 时间，the UTC time of this fix, in milliseconds since January 1, 1970.
	 */
	public long mTime							= 0;
	
	/**
	 * 速度
	 */
	public float mSpeed							= 0;
	
	/**
	 * 
	 */
	private Calendar mCalendar					= null;
	
	public IGpsData() {
		mCalendar = Calendar.getInstance();
	}
	
	public void setDate(Date date) {
		mCalendar.setTime(date);
	}
	
	/**
	 * 
	 * @param lat 纬度，单位degress
	 * @param longt 经度，单位degress
	 * @param alt 海拔，单位meters
	 * @param bear 方向，单位degress，东偏真北角度(east of true north)
	 * @param time 时间，the UTC time 单位milliseconds.
	 * @param speed 速度，单位meters/second
	 */
	public IGpsData(double lat, double longt, double alt, double bear, long time, float speed) {
		mLatitude = lat;
		mLongtitude = longt;
		mAltitude = alt;
		mBearing = bear;
		mTime = time;
		mSpeed = speed;
		//mDate = new Date(mTime);
		mCalendar = Calendar.getInstance();
		mCalendar.setTimeInMillis(time);
	}
	
	/**
	 * 返回GPS时间
	 * @return the year - 1900.
	 */
	
	public int GetYear() {
		return mCalendar.get(Calendar.YEAR);
	}
	/**
	 * 返回GPS时间
	 * @return the month
	 */
	public int GetMonth() {
		return (mCalendar.get(Calendar.MONTH) + 1);
	}
	
	/**
	 * 返回GPS时间
	 * @return the day
	 */
	public int GetDay() {
		return mCalendar.get(Calendar.DAY_OF_MONTH);
	}
	
	/**
	 * 返回GPS时间
	 * @return the hour
	 */
	public int GetHour() {
		return mCalendar.get(Calendar.HOUR_OF_DAY);
	}
	
	public int GetMinute() {
		return mCalendar.get(Calendar.MINUTE);
	}
	
	public int GetSecond() {
		return mCalendar.get(Calendar.SECOND);
	}
	
	public int[] toArray() {
		int[] array = new int[11];
		array[0] = (int) (mLatitude * 10000);
		array[1] = (int) (mLongtitude * 10000);
		array[2] = (int) (mSpeed);		//translate m/s to km/h
		array[3] = (int) (mBearing);
		array[4] = (int) GetYear();
		array[5] = (int) GetMonth();
		array[6] = (int) GetDay();
		array[7] = (int) GetHour();
		array[8] = (int) GetMinute();
		array[9] = (int) GetSecond();
		array[10] = (int) (mAltitude * 10);
		
		return array;
	}
}
