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
        // ��ȡλ�ù������
        String serviceName = Context.LOCATION_SERVICE;
        locationManager = (LocationManager) mContext.getSystemService(serviceName);

        /*
        //����������ȡprovider
        // ���ҵ�������Ϣ
        Criteria criteria = new Criteria();
        //������ʾ����
        criteria.setAccuracy(Criteria.ACCURACY_FINE); // �߾���
        //�Ƿ��ú�������
        criteria.setAltitudeRequired(true);
        //�Ƿ��÷�������
        criteria.setBearingRequired(true);
        //�Ƿ�������Ӫ�̼Ʒ�
        criteria.setCostAllowed(false);
        //���úĵ�̶�
        criteria.setPowerRequirement(Criteria.POWER_LOW); // �͹���

        //����������÷���Ӧ��
        String provider = locationManager.getBestProvider(criteria, true); // ��ȡGPS��Ϣ
        */

        //Provider���Ƴ���  
        String provider_Network = LocationManager.NETWORK_PROVIDER;
        String provider_GPS = LocationManager.GPS_PROVIDER;
        //����Provider���ƻ��LocationProvider  
        //LocationProvider provider;  
        //provider = locationManager.getProvider(name);

        //if( ! (locationManager.isProviderEnabled(provider_GPS) 
        //    || locationManager.isProviderEnabled(provider_Network))) {
        //    return false;
        //}

        
        //��ȡ��һ����λ��
        /*
        Location location = locationManager.getLastKnownLocation(provider); // ͨ��GPS��ȡλ��

        if( location != null ) {
            //���gps��λ������Ϣ
            Double latitude = location.getLatitude();
            Double longitude = location.getLongitude();
        }
        */

        //����״̬
        locationManager.addGpsStatusListener(listener);
        //�󶨼�������4������    
        //����1���豸����GPS_PROVIDER��NETWORK_PROVIDER����
        //����2��λ����Ϣ�������ڣ���λ����    
        //����3��λ�ñ仯��С���룺��λ�þ���仯������ֵʱ��������λ����Ϣ    
        //����4������    
        //��ע������2��3���������3��Ϊ0�����Բ���3Ϊ׼������3Ϊ0����ͨ��ʱ������ʱ���£�����Ϊ0������ʱˢ��   
        
        // ���ü��������Զ����µ���Сʱ��Ϊ���N��(1��Ϊ1*1000������д��ҪΪ�˷���)����Сλ�Ʊ仯����N��
        // 1�����һ�Σ�����Сλ�Ʊ仯����1�׸���һ�Σ�
        //ע�⣺�˴�����׼ȷ�ȷǳ��ͣ��Ƽ���service��������һ��Thread����run��sleep(10000);Ȼ��ִ��handler.sendMessage(),����λ��
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

    //״̬����
    private GpsStatus.Listener listener  = new GpsStatus.Listener() {
        public void onGpsStatusChanged(int event) {
            switch (event) {
                //��һ�ζ�λ
                case GpsStatus.GPS_EVENT_FIRST_FIX: {
                    Log.i(TAG, "First Location");
                    /*
                    Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER); // ͨ��GPS��ȡλ��

                    if( location != null ) {
                        //���gps��λ������Ϣ
                    	double latitude = location.getLatitude();
                        double longtitude = location.getLongitude();
                        double altitude = location.getAltitude();       //����
                        double bearing = location.getBearing();         //����
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
                //����״̬�ı�
                case GpsStatus.GPS_EVENT_SATELLITE_STATUS: {
                    Log.i(TAG, "Satellite status changed");
                    //��ȡ��ǰ״̬
                    GpsStatus gpsStatus=locationManager.getGpsStatus(null);
                    //��ȡ���ǿ�����Ĭ�����ֵ
                    int maxSatellites = gpsStatus.getMaxSatellites();
                    //����һ�������������������� 
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
                    
                //��λ����
                case GpsStatus.GPS_EVENT_STARTED: {
                    Log.i(TAG, "Location started");
                    mbGpsFix = false;
                    Message msg = mHandler.obtainMessage(HandlerMessageCode.HMC_GPS_LOCATION_START);
                    mHandler.sendMessage(msg);
                    break;
                }
                    
                //��λ����
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
           // Provider��disableʱ�����˺���������GPS����  
           Log.v(TAG, provider + " Enable");
        }

        //@Override
        public void onProviderDisabled(String provider) {
           //  Provider��enableʱ�����˺���������GPS���ر�  
           Log.v(TAG, provider + " Disable");
        }

        //@Override
        public void onLocationChanged(Location location) {
            double latitude = location.getLatitude();
            double longtitude = location.getLongitude();
            double altitude = location.getAltitude();       //����
            double bearing = location.getBearing();         //����
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

