package android.demo;

import java.util.ArrayList;

import com.baidu.lbsapi.auth.i;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapLoadedCallback;
import com.baidu.mapapi.map.BaiduMapOptions;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.DotOptions;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfigeration;
import com.baidu.mapapi.map.MyLocationConfigeration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.weifer.search.GpsSimulate;

import android.R.integer;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class BaiduMapActivity extends Activity {
	private static final String LTAG = "BaiduMap";
	private MapView mMapView;
	private static BaiduMap mBaiduMap;
	
	private Handler mHandler = null;
	private Handler mainHandler = null;
	public static final int GPS_DATA_RECEIVED				= 0x1000;
	public static final int GPS_POINT_STATUS				= 0x1001;

	/**
	 * ����㲥�����࣬���� SDK key ��֤�Լ������쳣�㲥
	 */
	public class SDKReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			String s = intent.getAction();
			Log.d(LTAG, "action: " + s);

			if (s.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR)) {
				Log.d(LTAG, "key ��֤����! ���� AndroidManifest.xml �ļ��м�� key ����");
			} else if (s
					.equals(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR)) {
				Log.d(LTAG, "�������");
			}
		}
	}

	private SDKReceiver mReceiver;
	private GpsSimulate mGpsSimulateThread = null;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.baidu);
		SDKInitializer.initialize(getApplicationContext());
		
		// ע�� SDK �㲥������
		IntentFilter iFilter = new IntentFilter();
		iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
		iFilter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);
		mReceiver = new SDKReceiver();
		registerReceiver(mReceiver, iFilter);
		
		// ��ͼ��ʼ��
		LatLng ll = new LatLng(24.6282, 118.1404);
		MapStatus status = new MapStatus.Builder().target(ll).zoom(17).build();
		BaiduMapOptions options = new BaiduMapOptions();
		options.mapStatus(status);
		mMapView = new MapView(this, options);
		setContentView(mMapView);
		mBaiduMap = mMapView.getMap();
		// ������λͼ��
		mBaiduMap.setMyLocationEnabled(true);
		mBaiduMap.setMyLocationConfigeration(new MyLocationConfigeration(
				LocationMode.FOLLOWING, true, null));
		mBaiduMap.setOnMapLoadedCallback(new OnMapLoadedCallback() {

			@Override
			public void onMapLoaded() {
				// TODO Auto-generated method stub
				Log.v(LTAG, "map loaded complete");
				mainHandler.sendEmptyMessage(HandlerMessageCode.HMC_MAP_LOADED_HANDLER);
			}
			
		});
		
		mHandler = new MapHandler();
		//mGpsSimulateThread = new GpsSimulate(mHandler);
		//mGpsSimulateThread.start();
	}
	
	public static class MapHandler extends Handler {

		private boolean isFirstLoc = true;

		@Override
		public void handleMessage(Message msg) {
			switch(msg.what)
			{
			case GPS_DATA_RECEIVED:
				IGpsData dat = (IGpsData)msg.obj;
				
				
				if(mBaiduMap == null) {
					break;
				}
				
				LatLng ll = new LatLng(dat.mLatitude,
						dat.mLongtitude);
				
				CoordinateConverter mConverter = new CoordinateConverter();
				mConverter.from(CoordinateConverter.CoordType.GPS);
				mConverter.coord(ll);
				LatLng newll = mConverter.convert();
				
				MyLocationData locData = new MyLocationData.Builder()
				.accuracy(50)
				// �˴����ÿ����߻�ȡ���ķ�����Ϣ��˳ʱ��0-360
				.direction((float)dat.mBearing)
				.latitude(newll.latitude)
				.longitude(newll.longitude)
				.speed(dat.mSpeed)
				.build();
				//MapStatusUpdateFactory.newLatLng(newll);
				//Log.v(LTAG, String.format("%f,%f,%f,%f", dat.mLatitude, dat.mLongtitude, newll.latitude, newll.longitude));
				mBaiduMap.setMyLocationData(locData);
				if (isFirstLoc) {
					isFirstLoc = false;
					MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(newll);
					mBaiduMap.animateMapStatus(u);
				}
				
				break;
				
			case GPS_POINT_STATUS:
				byte[] par = (byte[]) msg.obj;
				int cnt = par[0];
				int status = (msg.arg1 >> 24) & 0xFF;
				int color = 0x808080;
				double lat;
				double lon;
				int p;
				
				if(mBaiduMap == null) {
					break;
				}
				
				mBaiduMap.clear();
				for(int i = 0; i < cnt; i++) {
					p = 7 * i;
					status = par[p + 1];
					lat = (double)(((par[p + 2] << 16) & 0xFF0000) | ((par[p + 3] << 8) & 0xFF00) | (par[p + 4] & 0xFF)) / 10000;
					lon = (double)(((par[p + 5] << 16) & 0xFF0000) | ((par[p + 6] << 8) & 0xFF00) | (par[p + 7] & 0xFF)) / 10000;
					//Log.v(LTAG, String.format("show %f, %f point", lat, lon));
					
					LatLng old_pt = new LatLng(lat, lon);
					
					if(status == 0) {
						color = 0xFF000000;
					} else if(status == 1) {
						color = 0xFFFF0000;
					} else if(status == 2) {
						color = 0xFF0000FF;
					}
					
					CoordinateConverter convert = new CoordinateConverter();
					convert.from(CoordinateConverter.CoordType.GPS);
					convert.coord(old_pt);
					LatLng new_pt = convert.convert();

					// ��ӵ�
					OverlayOptions ooDot = new DotOptions().center(new_pt).radius(6)
							.color(color);
					mBaiduMap.addOverlay(ooDot);
				}
				
				break;
			}
		}
		
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		// activity ��ͣʱͬʱ��ͣ��ͼ�ؼ�
		Handler main = ((DemoApplication)getApplication()).getHandler();
		main.sendMessage(main.obtainMessage(HandlerMessageCode.HMC_MAP_REPORT_HANDLER, null));
		mMapView.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// activity �ָ�ʱͬʱ�ָ���ͼ�ؼ�
		mainHandler = ((DemoApplication)getApplication()).getHandler();
		mainHandler.sendMessage(mainHandler.obtainMessage(HandlerMessageCode.HMC_MAP_REPORT_HANDLER, mHandler));
		mMapView.onResume();
		
	}

	@Override
	protected void onDestroy() {
		// �رն�λͼ��
		mBaiduMap.setMyLocationEnabled(false);
		// activity ����ʱͬʱ���ٵ�ͼ�ؼ�
		mMapView.onDestroy();
				
		
		unregisterReceiver(mReceiver);
		super.onDestroy();
		
	}
	
}
