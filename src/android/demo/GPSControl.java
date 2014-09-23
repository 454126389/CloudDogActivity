package android.demo;

import java.util.List;
import java.util.Iterator;

import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import android.content.Context;

public class GPSControl {

    private Context mContext								= null;
    private final String TAG     							= "GPSControl";
    private LocationManager locationManager					= null;
    private Handler mHandler 								= null;
    private boolean mbGpsFix								= false;
    private GpxRecord mRecord								= null;
    
    GPSControl(Context context) {
        mContext = context;
        mHandler = ((CloudDogActivity)context).getHandler();
        mRecord = new GpxRecord(context);
    }

    public boolean open() {
        // 获取位置管理服务
        String serviceName = Context.LOCATION_SERVICE;
        locationManager = (LocationManager) mContext.getSystemService(serviceName);

        /*
        //根据条件获取provider
        // 查找到服务信息
        Criteria criteria = new Criteria();
        //设置显示精度
        criteria.setAccuracy(Criteria.ACCURACY_FINE); // 高精度
        //是否获得海拔数据
        criteria.setAltitudeRequired(true);
        //是否获得方向数据
        criteria.setBearingRequired(true);
        //是否允许运营商计费
        criteria.setCostAllowed(false);
        //设置耗电程度
        criteria.setPowerRequirement(Criteria.POWER_LOW); // 低功耗

        //根据条件获得服务供应商
        String provider = locationManager.getBestProvider(criteria, true); // 获取GPS信息
        */

        //Provider名称常量  
        String provider_Network = LocationManager.NETWORK_PROVIDER;
        String provider_GPS = LocationManager.GPS_PROVIDER;
        //根据Provider名称获得LocationProvider  
        //LocationProvider provider;  
        //provider = locationManager.getProvider(name);

        //if( ! (locationManager.isProviderEnabled(provider_GPS) 
        //    || locationManager.isProviderEnabled(provider_Network))) {
        //    return false;
        //}

        
        //获取上一个定位点
        /*
        Location location = locationManager.getLastKnownLocation(provider); // 通过GPS获取位置

        if( location != null ) {
            //获得gps定位坐标信息
            Double latitude = location.getLatitude();
            Double longitude = location.getLongitude();
        }
        */

        //监听状态
        locationManager.addGpsStatusListener(listener);
        //绑定监听，有4个参数    
        //参数1，设备：有GPS_PROVIDER和NETWORK_PROVIDER两种
        //参数2，位置信息更新周期，单位毫秒    
        //参数3，位置变化最小距离：当位置距离变化超过此值时，将更新位置信息    
        //参数4，监听    
        //备注：参数2和3，如果参数3不为0，则以参数3为准；参数3为0，则通过时间来定时更新；两者为0，则随时刷新   
        
        // 设置监听器，自动更新的最小时间为间隔N秒(1秒为1*1000，这样写主要为了方便)或最小位移变化超过N米
        // 1秒更新一次，或最小位移变化超过1米更新一次；
        //注意：此处更新准确度非常低，推荐在service里面启动一个Thread，在run中sleep(10000);然后执行handler.sendMessage(),更新位置
        locationManager.requestLocationUpdates(provider_GPS, 1 * 1000, 0, locationListener);
        locationManager.requestLocationUpdates(provider_Network, 10 * 1000, 0, locationListener);
        return true;
    }

    public List<String> GetEnabledProvider() {
        return locationManager.getProviders(true);
    }


    public void close() {
        locationManager.removeUpdates(locationListener);
        locationManager.removeGpsStatusListener(listener);
        
        mRecord.EndFile();
    }

    //状态监听
    private GpsStatus.Listener listener  = new GpsStatus.Listener() {
        public void onGpsStatusChanged(int event) {
            switch (event) {
                //第一次定位
                case GpsStatus.GPS_EVENT_FIRST_FIX: {
                    Log.i(TAG, "First Location");
                    /*
                    Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER); // 通过GPS获取位置

                    if( location != null ) {
                        //获得gps定位坐标信息
                    	double latitude = location.getLatitude();
                        double longtitude = location.getLongitude();
                        double altitude = location.getAltitude();       //海拔
                        double bearing = location.getBearing();         //方向
                        long time = location.getTime();
                        float speed = location.getSpeed();
                        
                        Log.v(TAG, location.toString());
                        
                        IGpsData gpsDat = new IGpsData(latitude, longtitude, altitude, bearing, time, speed);
                        Message msg = mHandler.obtainMessage(GPS_LOCATION_SUCCESS, gpsDat);
                        mHandler.sendMessage(msg);
                    }
                    */
                    mbGpsFix = true;
                    Message msg = mHandler.obtainMessage(HandlerMessageCode.HMC_GPS_LOCATION_SUCCESS, null);
                    mHandler.sendMessage(msg);
                    break;
                }
                //卫星状态改变
                case GpsStatus.GPS_EVENT_SATELLITE_STATUS: {
                    Log.i(TAG, "Satellite status changed");
                    //获取当前状态
                    GpsStatus gpsStatus=locationManager.getGpsStatus(null);
                    //获取卫星颗数的默认最大值
                    int maxSatellites = gpsStatus.getMaxSatellites();
                    //创建一个迭代器保存所有卫星 
                    Iterator<GpsSatellite> iters = gpsStatus.getSatellites().iterator();
                    int count = 0;
                    while (iters.hasNext() && count <= maxSatellites) {     
                        //GpsSatellite s = iters.next();
                        count++;     
                    }
                    Log.i(TAG, "Found "+count+" satellites");
                    Message msg = mHandler.obtainMessage(HandlerMessageCode.HMC_GPS_SATELLITE_NUMBER, count);
                    mHandler.sendMessage(msg);
                    break;
                }
                    
                //定位启动
                case GpsStatus.GPS_EVENT_STARTED: {
                    Log.i(TAG, "Location started");
                    mbGpsFix = false;
                    Message msg = mHandler.obtainMessage(HandlerMessageCode.HMC_GPS_LOCATION_START);
                    mHandler.sendMessage(msg);
                    break;
                }
                    
                //定位结束
                case GpsStatus.GPS_EVENT_STOPPED: {
                    Log.i(TAG, "Location stoped");
                    mbGpsFix = false;
                    Message msg = mHandler.obtainMessage(HandlerMessageCode.HMC_GPS_LOCATION_STOPPED);
                    mHandler.sendMessage(msg);
                    break;
                }
            }
        };
    };


    private LocationListener locationListener = new LocationListener() {
  
        //@Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
           
           Log.v(TAG, "GPS status changed to " + String.format("status:%d", status));
        }

        //@Override
        public void onProviderEnabled(String provider) {
           // Provider被disable时触发此函数，比如GPS被打开  
           Log.v(TAG, provider + " Enable");
        }

        //@Override
        public void onProviderDisabled(String provider) {
           //  Provider被enable时触发此函数，比如GPS被关闭  
           Log.v(TAG, provider + " Disable");
        }

        //@Override
        public void onLocationChanged(Location location) {
            double latitude = location.getLatitude();
            double longtitude = location.getLongitude();
            double altitude = location.getAltitude();       //海拔
            double bearing = location.getBearing();         //方向
            long time = System.currentTimeMillis();//location.getTime();
            float speed = location.getSpeed() * 3.6f;	//translate m/s to km/h
            
            //only send the fix data from gps when gps is fixed, send the fix data from network when gps is lost
            if(location.getProvider().equalsIgnoreCase("NETWORK") && mbGpsFix) {
            	return;
            }
            //Log.v(TAG, location.getProvider());
            
            IGpsData gpsDat = new IGpsData(latitude, longtitude, altitude, bearing, time, speed);
            Message msg = mHandler.obtainMessage(HandlerMessageCode.HMC_GPS_DATA_CHANGE, gpsDat);
            mHandler.sendMessage(msg);
            
            mRecord.AddPoint(gpsDat);
        }
    };
}

