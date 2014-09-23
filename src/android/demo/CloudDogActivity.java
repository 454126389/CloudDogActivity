/**
 * 
 */
package android.demo;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.demo.SelfPointDialog.SelfpointFinishListener;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.rayee.cloud.NetService;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.iflytek.tts.TtsService.TTSControl;
import com.iflytek.tts.TtsService.TtsPacket;
import com.other.RoundProgressBar;
import com.rayee.camera.FloatView;
import com.rayee.monitor.Monitor;
import com.weifer.search.GpsSimulate;
import com.weifer.search.SearchPointClass;

/**
 * @author Leven
 * 
 */
public final class CloudDogActivity extends FragmentActivity implements
		OnItemClickListener, OnTouchListener, OnGestureListener,
		OnClickListener, SelfpointFinishListener {
	private final static String TAG = "CloudDog";

	private GPSControl mGPSCtrl = null;
	private TextView mNetStatusTextView;
	private TextView mGpsView;
	private TextView mLoginView;
	private TextView mRadarStatus;
	private TextView mRadarLevel;
	private TextView mSpeedView;
	private TextView mCurPosView;
	private TextView mDirectionView;
	// private TextView[] mAlarmPointInfo = new TextView[2];

	private LinearLayout[] event_happen_ly = new LinearLayout[2];
	private TextView[] event_distance = new TextView[2];
	// private TextView[] event_speed = new TextView[2];
	private ImageView[] event_image = new ImageView[2];

	// 距离进度条
	private RoundProgressBar[] mRoundProgressBar = new RoundProgressBar[2];;
	private int[] progress = { 0, 0 };

	private FloatView floatView = null;
	private WindowManager windowManager = null;
	private WindowManager.LayoutParams windowManagerParams = null;

	/* net service interface */
	private NetService mNetService = null;
	private TTSControl mTtsCtrl = null;
	private SearchPointClass mPointSearch = null;
	private UserPreference mPreference = null;
	// private PowerManager powerManager = null;
	private WakeLock wakeLock = null;
	public static int mDisplayWidth = 0;
	public static int mDisplayHeight = 0;
	private SelfPointDialog mSelfPointDialog = null;
	private static MainActivityHanlder mActivityHandler = null;
	private static final String NETWORK_BROADCAST_CODE_STRING = "android.net.conn.CONNECTIVITY_CHANGE";

	private long mLastReportLocationTime = 0;
	private boolean mIsParking = false;
	private int mParkCnt = 0;
	private IGpsData mGpsData = null;
	private GpsSimulate mGpsSimulateThread = null;
	private Handler mMapHandler = null;
	private Monitor mMonitor = null;

	private boolean mbBackground = false;

	private ImageView speed_pt_iv;
	private ImageView derection_pt_iv;

	private Boolean toast_flag = false;

	private LinearLayout left_pand;
	private RelativeLayout right_pand, speed_pand;

	private ImageView imageView_Network_status, imageView_Login_status,
			imageView_Radar_status, imageView_GPS_status;

	private LinearLayout btn_panel;
	private LinearLayout left_status_ly;
	private TextView scroll_text;

	static final int POINT_TYPE_PICTURE_START = 0x00;
	static final int POINT_TYPE_PICTURE_END = 0x1E;
	static final int POINT_TYPE_SAFE_START = 0x20;
	static final int POINT_TYPE_SAFE_END = 0xFF;

	static final int POINT_TYPE_NULL = 0x00;
	static final int POINT_TYPE_CAMERA = 0x01; // 照相点
	static final int POINT_TYPE_VIDEO = 0x02; // 录影点
	static final int POINT_TYPE_REGION = 0x03; // 区间测速点
	static final int POINT_TYPE_RADAR = 0x04; // 雷达补偿点
	// resver (0x05~0x1E)

	static final int POINT_TYPE_SELFPOINT = 0x1F; // 自建点

	static final int POINT_TYPE_HOSPITAL = 0x20; // 医院
	static final int POINT_TYPE_SCHOOL = 0x21; // 学校
	static final int POINT_TYPE_ENTER_PORT = 0x22; // 交流道路口
	static final int POINT_TYPE_EXIT_PORT = 0x23; // 交流道出口
	static final int POINT_TYPE_GAS_STATION = 0x24; // 加油站
	static final int POINT_TYPE_TOLL_STATION = 0x25; // 收费站
	static final int POINT_TYPE_REST_STATION = 0x26; // 休息站
	static final int POINT_TYPE_BUS_STATION = 0x27; // 客运站
	static final int POINT_TYPE_TUNNEL = 0x28; // 隧道
	static final int POINT_TYPE_CAR_PARK = 0x29; // 停车场

	private android.widget.RelativeLayout.LayoutParams visablelv; // 底部菜单可视布局
	private android.widget.RelativeLayout.LayoutParams gonelv; // 底部菜单不可视布局

	private PopupWindow menu;
	private LayoutInflater inflater;
	private View layout;

	private PopupWindow menu_left, menu_right;
	private View layout_left, slid_menu_left, slid_menu_right;

	private Boolean isLeft;

	/**
	 * 双向滑动菜单布局
	 */

	private ArrayList<String> list = new ArrayList<String>();
	private ArrayList<String> list_right = new ArrayList<String>();

	private ListView mylistview, mylistview_right;

	private GestureDetector detector = new GestureDetector(
			CloudDogActivity.this);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main5);
		// 检测sd卡
		if (!CheckSDIsInsert()) {
			Toast.makeText(this, "sdcard need!", Toast.LENGTH_SHORT).show();
			return;
		}

		// 初始化界面
		InitActivity();

		
		initMenu();

		mActivityHandler = new MainActivityHanlder(this);

		// Application将mActivityHandler设为全局
		((DemoApplication) getApplication()).setHandler(mActivityHandler);

		// 连接
		InitPreference();

		// 自建坐标
		InitSelfPointDlg();

		InitDisplayNotLock();
		InitGpsCtrl();
		InitNetwork();
		// 打印信息
		DumpInfo();

	}

	private void initMenu() {
		// TODO Auto-generated method stub

		// 监听这个ImageView组件上的触摸屏时间
		findViewById(R.id.left_pand).setOnTouchListener(CloudDogActivity.this);
		findViewById(R.id.left_pand).setLongClickable(true);

		findViewById(R.id.right_pand).setOnTouchListener(CloudDogActivity.this);
		findViewById(R.id.right_pand).setLongClickable(true);

		detector.setIsLongpressEnabled(true);

		// 获取LayoutInflater实例
		inflater = (LayoutInflater) this
				.getSystemService(LAYOUT_INFLATER_SERVICE);
		// 获取弹出菜单的布局
		layout = inflater.inflate(R.layout.pop_menu, null);
		// 设置popupWindow的布局
		menu = new PopupWindow(layout, WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.WRAP_CONTENT);

		menu.setBackgroundDrawable(new BitmapDrawable());// 点击窗口外消失
		menu.setOutsideTouchable(true);

		// 自建菜单
		layout_left = inflater.inflate(R.layout.pop_menu_left, null);

		slid_menu_left = inflater.inflate(R.layout.slid_menu_left, null);
		slid_menu_right = inflater.inflate(R.layout.slid_menu_right, null);

		// 设置popupWindow的布局
		// menu_left = new PopupWindow(layout_left,
		// WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.WRAP_CONTENT);
		// menu_left.setBackgroundDrawable(new BitmapDrawable());//点击窗口外消失
		// menu_left.setOutsideTouchable(true);

		menu_left = new PopupWindow(slid_menu_left, 400,
				LayoutParams.MATCH_PARENT, true);
		// 设置动画效果
		menu_left.setAnimationStyle(R.style.AnimationFade);

		menu_left.setBackgroundDrawable(new BitmapDrawable());// 点击窗口外消失
		menu_left.setOutsideTouchable(true);

		menu_right = new PopupWindow(slid_menu_right, 400,
				LayoutParams.MATCH_PARENT, true);
		// 设置动画效果
		menu_right.setAnimationStyle(R.style.AnimationFade2);

		menu_right.setBackgroundDrawable(new BitmapDrawable());// 点击窗口外消失
		menu_right.setOutsideTouchable(true);

		((Button) layout.findViewById(R.id.button_test))
				.setOnClickListener(this);
		((Button) layout.findViewById(R.id.button_position))
				.setOnClickListener(this);
		((Button) layout.findViewById(R.id.button_roadstatus))
				.setOnClickListener(this);
		((Button) layout.findViewById(R.id.button_simulate))
				.setOnClickListener(this);
		((Button) layout.findViewById(R.id.button_map))
				.setOnClickListener(this);

		((Button) layout_left.findViewById(R.id.a)).setOnClickListener(this);
		((Button) layout_left.findViewById(R.id.a2)).setOnClickListener(this);

		mylistview = (ListView) slid_menu_left
				.findViewById(R.id.slid_menu_list_left);

		mylistview_right = (ListView) slid_menu_right
				.findViewById(R.id.slid_menu_list_right);

		// list.add("模拟轨迹");
		// list.add("天气信息");
		// list.add("当前位置");
		// list.add("当前路况");
		list.add("查询菜单");
		list.add(getString(R.string.Simulate));
		list.add(getString(R.string.WeatherInfo));
		list.add(getString(R.string.CurrentPosition));
		list.add(getString(R.string.RoadStatus));
		list.add(getString(R.string.BaiduMap));
		list.add("查询预留");
		list.add("查询预留");
		list.add("查询预留");
		list.add("查询预留");
		list.add("查询预留");

		list_right.add("设置菜单");
		list_right.add("设置预留");
		list_right.add("设置预留");
		list_right.add("设置预留");
		list_right.add("设置预留");
		list_right.add("设置预留");
		list_right.add("设置预留");
		list_right.add("设置预留");
		list_right.add("设置预留");
		list_right.add("设置预留");
		list_right.add("设置预留");
		list_right.add("设置预留");
		// list_right.add("正向自建");
		// list_right.add("反向自建");
//		 list_right.add("测试A");
//		 list_right.add("测试B");

		ArrayAdapter<String> myArrayAdapter_left = new ArrayAdapter<String>(
				this, android.R.layout.simple_list_item_1, list_right);

		ArrayAdapter<String> myArrayAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, list);
		
		
		mylistview.setAdapter(myArrayAdapter);
		mylistview.setOnItemClickListener(this);

		mylistview_right.setAdapter(myArrayAdapter_left);
		mylistview_right.setOnItemClickListener(this);

	}

	private void InitPreference() {
		CloudDogPreference.setContext(this);
		CloudDogPreference.read();

		mPreference = new UserPreference(this);
		mPreference.ReadPreference();
	}

	void DumpInfo() {
		Log.i(TAG,
				"basic database version "
						+ String.valueOf(CloudDogPreference.mBaseDatabaseVersion));
		Log.i(TAG,
				"update database version "
						+ String.valueOf(CloudDogPreference.mUpdateDabaBaseVersion));
		Log.i(TAG, "Server IP " + CloudDogPreference.ServerIP);
		Log.i(TAG,
				"TCP port " + String.valueOf(CloudDogPreference.ServerTCPPort));
		Log.i(TAG,
				"UDP port " + String.valueOf(CloudDogPreference.ServerUDPPort));
		Log.i(TAG,
				"software version "
						+ String.valueOf(UserPreference.mSoftwareVersion));
		Log.i(TAG, "IMSI " + UserPreference.mSimIMSI);
		Log.i(TAG, "device SN " + String.valueOf(UserPreference.mDeviceSn));
	}

	private void InitNetwork() {
		IntentFilter filter = new IntentFilter(NETWORK_BROADCAST_CODE_STRING);
		registerReceiver(mBroadcastReceiver, filter);
	}

	private void InitActivity() {
		WindowManager m = getWindowManager();
		Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
		mDisplayWidth = d.getWidth();
		mDisplayHeight = d.getHeight();

		mSpeedView = (TextView) findViewById(R.id.textView_speed_status);
		mNetStatusTextView = (TextView) findViewById(R.id.textView_Network_status);
		mGpsView = (TextView) findViewById(R.id.textView_GPS_status);
		mLoginView = (TextView) findViewById(R.id.textView_Login_status);
		mRadarStatus = (TextView) findViewById(R.id.textView_Radar_status);
		mRadarLevel = (TextView) findViewById(R.id.textView_Radar_level);
		mCurPosView = (TextView) findViewById(R.id.textView_current_position);
		mSpeedView = (TextView) findViewById(R.id.textView_speed_status);
		// mAlarmPointInfo[0] = (TextView)
		// findViewById(R.id.textView_AlarmPoint_Info0);
		// mAlarmPointInfo[1] = (TextView)
		// findViewById(R.id.textView_AlarmPoint_Info1);

		event_happen_ly[0] = (LinearLayout) findViewById(R.id.event_happen_00);
		event_happen_ly[1] = (LinearLayout) findViewById(R.id.event_happen_01);

		event_distance[0] = (TextView) findViewById(R.id.event_distance_00);
		event_distance[1] = (TextView) findViewById(R.id.event_distance_01);

		// event_speed[0] = (TextView) findViewById(R.id.event_speed_00);
		// event_speed[1] = (TextView) findViewById(R.id.event_speed_01);

		event_image[0] = (ImageView) findViewById(R.id.event_image_00);
		event_image[1] = (ImageView) findViewById(R.id.event_image_01);

		mRoundProgressBar[0] = (RoundProgressBar) findViewById(R.id.roundProgressBar_00);
		mRoundProgressBar[1] = (RoundProgressBar) findViewById(R.id.roundProgressBar_01);

		mDirectionView = (TextView) findViewById(R.id.textView_direction);

		// 滚动文字
		scroll_text = (TextView) findViewById(R.id.scroll_text);

		// btnBind.setOnClickListener(this);
		// btnUnBind.setOnClickListener(this);
		// findViewById(R.id.button_testsearch).setOnClickListener(this);

		((Button) findViewById(R.id.button_search)).setOnClickListener(this);
		((Button) findViewById(R.id.button_setting)).setOnClickListener(this);

		imageView_Network_status = (ImageView) findViewById(R.id.imageView_Network_status);
		imageView_Login_status = (ImageView) findViewById(R.id.imageView_Login_status);
		imageView_Radar_status = (ImageView) findViewById(R.id.imageView_Radar_status);
		imageView_GPS_status = (ImageView) findViewById(R.id.imageView_GPS_status);

		speed_pt_iv = (ImageView) findViewById(R.id.speed_pt_iv);
		derection_pt_iv = (ImageView) findViewById(R.id.derection_pt_iv);

		speed_pand = (RelativeLayout) findViewById(R.id.speed_pand);
		left_pand = (LinearLayout) findViewById(R.id.left_pand);
		right_pand = (RelativeLayout) findViewById(R.id.right_pand);

		btn_panel = (LinearLayout) findViewById(R.id.btn_panel);
		left_status_ly = (LinearLayout) findViewById(R.id.left_status_ly);

		visablelv = new RelativeLayout.LayoutParams(mDisplayHeight - 150,
				mDisplayHeight);
		visablelv.addRule(RelativeLayout.CENTER_HORIZONTAL);

		// 缩放visablelv
		gonelv = new RelativeLayout.LayoutParams(mDisplayHeight - 100,
				mDisplayHeight);
		gonelv.addRule(RelativeLayout.CENTER_HORIZONTAL);

		// // 速度面板居中
		// android.widget.RelativeLayout.LayoutParams lp = new
		// RelativeLayout.LayoutParams(
		// mDisplayHeight - 100, mDisplayHeight - 100);
		// lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
		speed_pand.setLayoutParams(gonelv);

		android.widget.RelativeLayout.LayoutParams lp_left = new RelativeLayout.LayoutParams(
				mDisplayWidth / 2 - (mDisplayHeight - 100) / 4+50,
				LayoutParams.FILL_PARENT);
		left_pand.setLayoutParams(lp_left);

		android.widget.RelativeLayout.LayoutParams lp_right = new RelativeLayout.LayoutParams(
				mDisplayWidth / 2 - (mDisplayHeight - 100) / 4+50,
				LayoutParams.FILL_PARENT);
		lp_right.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		right_pand.setLayoutParams(lp_right);

		scroll_text.setText("2014年8月29日 17:29:47   晴,无风");

		// android.widget.RelativeLayout.LayoutParams lp = new
		// RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT);
		// lp.addRule(RelativeLayout.LEFT_OF,R.id.speed_pand);
		// lp.rightMargin=-(mDisplayHeight-100)/3;
		// msg_ly.setLayoutParams(lp);

		// 126

		// speed_pand.setLayoutParams(new RelativeLayout.LayoutParams(
		// mDisplayHeight - 100, mDisplayHeight - 100));

		// android.widget.RelativeLayout.LayoutParams lp = new
		// RelativeLayout.LayoutParams(mDisplayHeight-100, mDisplayHeight-100);
		// lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		// speed_pand.setLayoutParams(lp);

		// speed_pand.addv(new
		// RelativeLayout.LayoutParams(mDisplayHeight-100,mDisplayHeight-100).addRule(RelativeLayout.ALIGN_PARENT_RIGHT));

		// panel_blue.setLayoutParams(new
		// LinearLayout.LayoutParams(mDisplayWidth-mDisplayHeight+100+(mDisplayHeight-100)/3,
		// LayoutParams.FILL_PARENT));
		// panel_black.setLayoutParams(new
		// LinearLayout.LayoutParams(mDisplayWidth-mDisplayHeight+100+(mDisplayHeight-100)/3,
		// LayoutParams.FILL_PARENT));
		//

	}

	private void InitSelfPointDlg() {
		mSelfPointDialog = new SelfPointDialog(this, R.style.MyDialog);
		mSelfPointDialog.setFinishListener(this);
		mSelfPointDialog.setContentView(R.layout.selfpointdlg);
		mSelfPointDialog.setWindowSize(mDisplayWidth / 2, 0, mDisplayWidth / 2,
				mDisplayHeight);
	}

	private boolean CheckSDIsInsert() {
		if (!Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {// 已经插入了sd卡，并且可以读写
			new AlertDialog.Builder(this)
					.setTitle("错误")
					.setMessage("没有检测到SD卡")
					.setPositiveButton("退出",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									dialog.dismiss();
									System.exit(0);
								}

							}).show();
			return false;
		}

		return true;
	}

	private void InitDisplayNotLock() {
		PowerManager powerManager = (PowerManager) this
				.getSystemService(Service.POWER_SERVICE);
		wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,
				"My Lock");
	}

	private void InitGpsCtrl() {
		mGPSCtrl = new GPSControl(this);
		mGPSCtrl.open();
		mTtsCtrl = new TTSControl(this);
		mPointSearch = new SearchPointClass(mActivityHandler);
		mPointSearch.Initial();

		// GpsSimulate simulate = new GpsSimulate(mActivityHandler);
		// simulate.start();
	}

	private void createView() {
		floatView = new FloatView(getApplicationContext());
		floatView.setOnClickListener(this);
		floatView.setImageResource(R.drawable.black_block); // 這裡簡單的用自帶的icon來做演示
		// 獲取WindowManager
		windowManager = (WindowManager) getApplicationContext()
				.getSystemService(Context.WINDOW_SERVICE);
		// 設置LayoutParams(全局變量）相關參數
		windowManagerParams = ((DemoApplication) getApplication())
				.getWindowParams();

		windowManagerParams.type = LayoutParams.TYPE_SYSTEM_ERROR; // 設置window
																	// type
		windowManagerParams.format = PixelFormat.RGBA_8888; // 設置圖片格式，效果為背景透明
		// 設置Window flag
		windowManagerParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
				| LayoutParams.FLAG_NOT_FOCUSABLE;
		/*
		 * 注意，flag的值可以為： 下面的flags屬性的效果形同“鎖定”。 懸浮窗不可觸摸，不接受任何事件,同時不影響後面的事件響應。
		 * LayoutParams.FLAG_NOT_TOUCH_MODAL 不影響後面的事件
		 * LayoutParams.FLAG_NOT_FOCUSABLE 不可聚焦 LayoutParams.FLAG_NOT_TOUCHABLE
		 * 不可觸摸
		 */
		// 調整懸浮窗口至左上角，便於調整坐標
		windowManagerParams.gravity = Gravity.LEFT | Gravity.TOP;
		// 以屏幕左上角為原點，設置x、y初始值
		windowManagerParams.x = 0;
		windowManagerParams.y = 0;
		// 設置懸浮窗口長寬數據
		windowManagerParams.width = LayoutParams.WRAP_CONTENT;
		windowManagerParams.height = LayoutParams.WRAP_CONTENT;
		// 顯示myFloatView圖像
		windowManager.addView(floatView, windowManagerParams);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

		if (v == floatView) {
			Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT).show();
			return;
		}

		switch (v.getId()) {
		case R.id.button_test:
			// int[] t = {245062 ,1181438 ,0 ,270 ,114 ,3 ,5 ,22 ,3 ,19 ,67};
			// mPointSearch.Test();

			if (mNetService != null && mGpsData != null) {
				mNetService
						.getWeather(mGpsData.mLatitude, mGpsData.mLongtitude);
			}
			// mMonitor = new Monitor(this);

			btn_panel.setVisibility(View.GONE);
			speed_pand.setLayoutParams(gonelv);
			menu.dismiss();
			break;
		// 位置
		case R.id.button_position:
			if (mNetService != null && mGpsData != null) {
				mNetService.getCurAddressName(mGpsData.mLatitude,
						mGpsData.mLongtitude);
			}

			btn_panel.setVisibility(View.GONE);
			speed_pand.setLayoutParams(gonelv);
			menu.dismiss();
			break;
		// 路况
		case R.id.button_roadstatus:
			if (mNetService != null && mGpsData != null) {
				mNetService.getCurLoadInfo(mGpsData.mLatitude,
						mGpsData.mLongtitude);
			}

			btn_panel.setVisibility(View.GONE);
			speed_pand.setLayoutParams(gonelv);
			menu.dismiss();
			break;

		case R.id.button_simulate:
			/*
			 * if(floatView == null) { createView(); } else {
			 * if(floatView.getVisibility() == View.INVISIBLE) {
			 * floatView.setVisibility(View.VISIBLE); } else {
			 * floatView.setVisibility(View.INVISIBLE); } }
			 */

			if (mGpsSimulateThread != null) {
				mGpsSimulateThread.Exit();
				mGpsSimulateThread = null;
				((Button) v).setText(R.string.Simulate);
			} else {
				mGpsSimulateThread = new GpsSimulate(mActivityHandler);
				mGpsSimulateThread.start();
				((Button) v).setText(R.string.CancleSimulate);
			}

			btn_panel.setVisibility(View.GONE);
			speed_pand.setLayoutParams(gonelv);
			menu.dismiss();
			break;

		case R.id.button_map:
			/*
			 * float ret[] = new float[3]; Location.distanceBetween(24.6484,
			 * 118.1572, 24.6518, 118.1523, ret); Log.v(TAG,
			 * String.format("d:%f,c:%f,f:%f", ret[0], ret[1], ret[2]));
			 */

			PackageManager manager = this.getPackageManager();
			Intent intent = new Intent();

			try {
				PackageInfo info = manager.getPackageInfo("com.baidu.BaiduMap",
						PackageManager.GET_ACTIVITIES);
				intent = manager
						.getLaunchIntentForPackage("com.baidu.BaiduMap");
				startActivity(intent);
			} catch (NameNotFoundException e) {
				new AlertDialog.Builder(this)
						.setTitle("错误")
						.setMessage("没有安装百度地图")
						.setPositiveButton("退出",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
										dialog.dismiss();
									}

								}).show();
			}

			btn_panel.setVisibility(View.GONE);
			speed_pand.setLayoutParams(gonelv);
			menu.dismiss();
			/*
			 * Intent intent = new Intent(this, BaiduMapActivity.class); Bundle
			 * bundle = new Bundle(); intent.putExtra("parent", bundle);
			 */

			break;
		case R.id.a:
			// if (mSelfPointDialog.StartCreateBackwardPoint()) {
			mSelfPointDialog.resetSelectItem(0);
			mSelfPointDialog.show();
			// }
			break;
		case R.id.a2:
			// if (mSelfPointDialog.StartCreateForwardPoint()) {
			mSelfPointDialog.resetSelectItem(0);
			mSelfPointDialog.show();
			break;
		// }
		case R.id.button_search:

			// .setText((msg.arg1 == 1) ? R.string.LoginOK
			// : R.string.LoginFail);

			// btn_panel
			// .setVisibility((btn_panel.getVisibility() == View.VISIBLE) ?
			// View.GONE
			// : View.VISIBLE);
			//
			// if(btn_panel.getVisibility()==View.GONE)
			// speed_pand.setLayoutParams(gonelv);
			//
			// else
			// speed_pand.setLayoutParams(visablelv);
			//

			// 设置位置
			// menu_left.showAtLocation(this.findViewById(R.id.a),
			// Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL,0,0); //设置在屏幕中的显示位置
			// menu_left.showAtLocation(this.findViewById(R.id.a), Gravity.LEFT,
			// 0, 0);
			Toast.makeText(CloudDogActivity.this, "功能键预留", Toast.LENGTH_SHORT)
					.show();
			break;

		// setContentView(R.layout.main);

		case R.id.button_setting:

			// btn_panel
			// .setVisibility((btn_panel.getVisibility() == View.VISIBLE) ?
			// View.GONE
			// : View.VISIBLE);
			//
			// if(btn_panel.getVisibility()==View.GONE)
			// speed_pand.setLayoutParams(gonelv);
			//
			// else
			// speed_pand.setLayoutParams(visablelv);

			// 设置位置
			// menu.showAtLocation(this.findViewById(R.id.a), Gravity.BOTTOM
			// | Gravity.CENTER_HORIZONTAL, 0, 0); // 设置在屏幕中的显示位置
			Toast.makeText(CloudDogActivity.this, "功能键预留", Toast.LENGTH_SHORT)
					.show();

			break;

		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		Log.v(TAG, "Activity destroy");
		unregisterReceiver(mBroadcastReceiver);

		if (floatView != null) {
			windowManager.removeView(floatView);
		}

		mGPSCtrl.close();

		mTtsCtrl.ttsDestroy();
		mTtsCtrl = null;

		mPointSearch.Exit();
		mPointSearch = null;

		mSelfPointDialog.dismiss();

		super.onDestroy();
		System.exit(0);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		Log.v(TAG, "onPause Called");
		wakeLock.release();
		mbBackground = true;

		if (mMonitor != null) {
			mMonitor.close();
		}

		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		Log.v(TAG, "onResume Called");
		wakeLock.acquire();
		mbBackground = false;
		super.onResume();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		int key = KeyEvent.KEYCODE_UNKNOWN;

		key = TranslateKey.onKeyDown(keyCode, event);
		switch (key) {
		case KeyEvent.KEYCODE_DPAD_LEFT:
			if (mSelfPointDialog.StartCreateBackwardPoint()) {
				mSelfPointDialog.resetSelectItem(0);
				mSelfPointDialog.show();
			}
			break;
		}

		if (key != KeyEvent.KEYCODE_UNKNOWN) {
			return false;
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		int key = KeyEvent.KEYCODE_UNKNOWN;

		key = TranslateKey.onKeyLongPress(keyCode, event);
		switch (key) {
		case KeyEvent.KEYCODE_0:
			break;

		case KeyEvent.KEYCODE_1:
			break;

		case KeyEvent.KEYCODE_2:
			break;

		case KeyEvent.KEYCODE_3:
			break;
		}

		if (key != KeyEvent.KEYCODE_UNKNOWN) {
			return false;
		}

		return super.onKeyLongPress(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		int key = KeyEvent.KEYCODE_UNKNOWN;

		key = TranslateKey.onKeyUp(keyCode, event);

		switch (key) {
		case KeyEvent.KEYCODE_DPAD_CENTER:
			if (mSelfPointDialog.StartCreateForwardPoint()) {
				mSelfPointDialog.resetSelectItem(0);
				mSelfPointDialog.show();
			}
			break;

		case KeyEvent.KEYCODE_DPAD_UP:
			break;

		case KeyEvent.KEYCODE_DPAD_DOWN:
			break;

		case KeyEvent.KEYCODE_BACK:
			if (mNetService != null) {
				mNetService.close();
				try {
					mNetService.join(5000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			return super.onKeyUp(keyCode, event);
		}

		return false;
	}

	private final boolean checkUpdate(double latitude, double longitude) {
		float[] results = new float[1];

		Location.distanceBetween(latitude, longitude,
				CloudDogPreference.mLastUpdateLatitude,
				CloudDogPreference.mLastUpdateLongitude, results);

		if (results[0] > 30000) {
			return true;
		}

		return false;
	}

	private final boolean checkReportLocation() {
		long time = System.currentTimeMillis();
		int interval = 0;

		interval = (mIsParking) ? CloudDogPreference.mParkPositionReportInterval
				: CloudDogPreference.mNormalPositionReportInterval;
		interval *= 1000;
		if (time - mLastReportLocationTime > interval) {
			mLastReportLocationTime = time;
			return true;
		}

		return false;
	}

	private final void checkParking(IGpsData gps) {
		if (mIsParking && (gps.mSpeed >= 5)) {
			if ((++mParkCnt) > 3) {
				mParkCnt = 0;
				mIsParking = false;
			}
		} else if (!mIsParking && (gps.mSpeed < 5)) {
			if ((++mParkCnt) > 3) {
				mParkCnt = 0;
				mIsParking = true;
			}
		}
	}

	public Handler getHandler() {
		return mActivityHandler;
	}

	private static class MainActivityHanlder extends Handler {

		private RotateAnimation speed_pt_ani;
		private RotateAnimation derection_pt_ani;
		// 开始角度
		private float startangle = 0;
		private float startangle_derection = 0;

		private WeakReference<CloudDogActivity> mActivity = null;
		private boolean isMapLoaded = false;

		public MainActivityHanlder(CloudDogActivity activity) {
			// TODO Auto-generated constructor stub
			mActivity = new WeakReference<CloudDogActivity>(activity);

		}

		@Override
		public void handleMessage(Message msg) {
			CloudDogActivity activity = mActivity.get();
			switch (msg.what) {
			case HandlerMessageCode.HMC_GPS_DATA_CHANGE: {
				IGpsData gps = (IGpsData) msg.obj;
				activity.mGpsData = gps;
				int[] gpsDataArray = gps.toArray();

				if (activity.mPointSearch != null) {
					long time = System.currentTimeMillis();
					activity.mPointSearch.StartSearch(gpsDataArray);
					TestForInfomax test = new TestForInfomax();
					test.process();
					Log.v(TAG, String.format("point(%f,%f) process use %d ms",
							gps.mLatitude, gps.mLongtitude,
							System.currentTimeMillis() - time));
				}

				if (activity.mNetService != null) {
					if (activity.checkUpdate(gps.mLatitude, gps.mLongtitude)) {
						activity.mNetService.startUpdate(gpsDataArray[0],
								gpsDataArray[1],
								CloudDogPreference.mUpdateNewKey,
								CloudDogPreference.mBaseDatabaseVersion);
					}

					if (CloudDogPreference.mbUploadLocation) {
						activity.checkParking(gps);
						if (activity.checkReportLocation()) {
							activity.mNetService.reportLocation(gps);
						}
					}
				}

				SelfPointDataSample.getInstance().AddGpsPoint(gps);
				if (!activity.mbBackground) {

					speed_pt_ani = new RotateAnimation(startangle,
							gpsDataArray[2], Animation.RELATIVE_TO_SELF, 0.5f,
							Animation.RELATIVE_TO_SELF, 0.5f);

					startangle = gpsDataArray[2];

					derection_pt_ani = new RotateAnimation(
							startangle_derection, (int) gps.mBearing,
							Animation.RELATIVE_TO_SELF, 0.5f,
							Animation.RELATIVE_TO_SELF, 0.5f);

					startangle_derection = (int) gps.mBearing;

					speed_pt_ani.setDuration(1000);
					derection_pt_ani.setDuration(1000);

					activity.speed_pt_iv.startAnimation(speed_pt_ani);
					activity.derection_pt_iv.startAnimation(derection_pt_ani);

					// 经纬度
					activity.mCurPosView.setText(String.valueOf(gps.mLatitude)
							+ "\n" + String.valueOf(gps.mLongtitude));
					activity.mCurPosView.setText("经度"
							+ String.format("%.2f", gps.mLatitude) + "\n"
							+ "纬度" + String.format("%.2f", gps.mLongtitude));
					activity.mSpeedView
							.setText(String.valueOf(gpsDataArray[2]));
					// 方向
					activity.mDirectionView.setText("正北"
							+ Float.toString((float) gps.mBearing));
					activity.mDirectionView.setText("正北"
							+ Float.toString((float) gps.mBearing).substring(
									0,
									Float.toString((float) gps.mBearing)
											.indexOf(".")));

				}

				if (activity.mMapHandler != null && isMapLoaded) {
					activity.mMapHandler.sendMessage(activity.mMapHandler
							.obtainMessage(BaiduMapActivity.GPS_DATA_RECEIVED,
									gps));
				}
				break;
			}

			case HandlerMessageCode.HMC_MAP_LOADED_HANDLER:
				isMapLoaded = true;
				break;

			case HandlerMessageCode.HMC_GPS_SATELLITE_NUMBER:
				break;

			case HandlerMessageCode.HMC_GPS_LOCATION_START: {
				if (!activity.mbBackground) {
					mActivity.get().mGpsView.setText(R.string.LocationStart);
					activity.imageView_GPS_status
							.setImageResource(R.drawable.icon_gps_status_false);
				}
				break;
			}

			case HandlerMessageCode.HMC_GPS_LOCATION_SUCCESS: {
				if (!activity.mbBackground) {
					activity.mGpsView.setText(R.string.LocationSuccess);
					activity.imageView_GPS_status
							.setImageResource(R.drawable.icon_gps_status_true);
				}
				break;
			}

			case HandlerMessageCode.HMC_GPS_LOCATION_STOPPED: {
				if (!activity.mbBackground) {
					activity.mGpsView.setText(R.string.LocationStop);
					activity.imageView_GPS_status
							.setImageResource(R.drawable.icon_gps_status_true);
				}
				break;
			}

			// 播报中心
			case HandlerMessageCode.HMC_TTS_SPEAK_CONTENT: {
				String str = (String) msg.obj;
				TtsPacket packet = new TtsPacket(str, msg.arg1, null);
				// //判定是否存在
				// if(str.contains("请依速限行驶"))
				// {
				// String distance=str.substring(str.indexOf("前方") + 2,
				// str.lastIndexOf("米为区间测速路段结束点"));
				// String speedLimits=str.substring(str.indexOf("限速") + 2,
				// str.lastIndexOf("公里"));
				//
				//
				//
				// }
				//
				// //距离
				activity.scroll_text.setText(str);
				packet.play();
				break;
			}
			// radar
			case HandlerMessageCode.HMC_RADAR_WARNNING_DISPLAY: {
				if (activity.mbBackground) {
					break;
				}
				String str = (String) msg.obj;
				if (str.length() == 0) {
					activity.mRadarStatus.setText(R.string.RadarNotValid);
					activity.mRadarLevel.setText(R.string.Empty);

					activity.imageView_Radar_status
							.setImageResource(R.drawable.icon_radar_status_false);

				} else {
					activity.mRadarStatus.setText(str);
					activity.imageView_Radar_status
							.setImageResource(R.drawable.icon_radar_status_true);
					if (msg.arg1 == 1) {
						activity.mRadarLevel.setText(R.string.RadarLevelHigh);
					} else if (msg.arg1 == 2) {
						activity.mRadarLevel.setText(R.string.RadarLevelMid);
					} else if (msg.arg1 == 3) {
						activity.mRadarLevel.setText(R.string.RadarLevelLow);
					} else {
						activity.mRadarLevel.setText(R.string.Empty);
					}
				}
				break;
			}

			// 出现限速
			case HandlerMessageCode.HMC_NOTIFY_ALARM_DISTANCE:
				if (activity.mbBackground) {
					break;
				}

				byte[] arg = (byte[]) msg.obj;

				// [1, 0, 60, 1, 110]
				int cnt = arg[0];
				int index = 0;

				int type = 0;
				int speed;
				int distance = 0;

				if (cnt > 2) {
					cnt = 2;
				}

				for (; index < cnt; index++) {
					type = (int) (arg[5 * index + 1] & 0xFF);
					speed = (int) (arg[5 * index + 2] & 0xFF);
					distance = (int) ((((arg[5 * index + 3] << 8) & 0xFF00) | (arg[5 * index + 4] & 0xFF)) & 0xFFFF);
					// voice_index = (int)(arg[5 * index + 5]);

					// type=3，distance=

					// 如果未弹过则提示
					if (activity.toast_flag == false && distance != 0) {
						activity.toast_flag = true;

						activity.progress[index] = distance;

						// 弹出框
						Toast warn_toast = Toast.makeText(
								activity.getApplicationContext(), "注意警告",
								Toast.LENGTH_LONG);
						ImageView imageCodeProject = new ImageView(
								activity.getApplicationContext());

						// warn_toast.setGravity(Gravity.TOP | Gravity.LEFT, 30,
						// 50);

						warn_toast.setGravity(Gravity.CENTER, 0, 0);
						LinearLayout toastView = (LinearLayout) warn_toast
								.getView();

						imageCodeProject.setImageResource(activity
								.getResources().getIdentifier("icon_spd_" + 60,
										"drawable", activity.getPackageName()));
						toastView.addView(imageCodeProject, 0);
						warn_toast.show();
					}

					if (type >= POINT_TYPE_PICTURE_START
							&& type <= POINT_TYPE_PICTURE_END) {
						// activity.mAlarmPointInfo[index].setText(String.format("限速%d   %d",
						// speed, distance));

						System.out.println("activity.progress[index]="
								+ activity.progress[index] + "---distance="
								+ distance);

						activity.event_happen_ly[index]
								.setVisibility(View.VISIBLE);

						// 两个通知时隐藏速度面板
						if (index == 1)
							activity.left_status_ly.setVisibility(View.GONE);

						activity.event_distance[index].setText("前方" + distance
								+ "米");
						activity.event_image[index].setImageResource(activity
								.getResources().getIdentifier(
										"icon_spd_" + speed, "drawable",
										activity.getPackageName()));

						if (activity.progress[index] < distance)
							activity.progress[index] = distance;

						activity.mRoundProgressBar[index]
								.setMax(activity.progress[index]);

						activity.mRoundProgressBar[index]
								.setProgress(activity.progress[index]
										- distance);

						// activity.event_speed[index].setText("限速" + speed);

						// 自建坐标？
					} else if (type == POINT_TYPE_SELFPOINT) {
						// activity.mAlarmPointInfo[index].setText(String.format("限速%d   %d",
						// speed, distance));

						activity.event_happen_ly[index]
								.setVisibility(View.VISIBLE);

						if (index == 1)
							activity.left_status_ly.setVisibility(View.GONE);

						activity.event_distance[index].setText("前方" + distance
								+ "米");
						// activity.event_speed[index].setText("限速" + speed);
						activity.event_image[index].setImageResource(activity
								.getResources().getIdentifier(
										"icon_spd_" + speed, "drawable",
										activity.getPackageName()));

						// 安全点
					} else if (type >= POINT_TYPE_SAFE_START
							&& type <= POINT_TYPE_SAFE_END) {

						activity.event_happen_ly[index]
								.setVisibility(View.VISIBLE);

						if (index == 1)
							activity.left_status_ly.setVisibility(View.GONE);

						activity.event_distance[index]
								.setVisibility(View.INVISIBLE);
						// activity.event_speed[index].setText("");
						activity.event_image[index].setImageResource(activity
								.getResources().getIdentifier("icon_flow_spd",
										"drawable", activity.getPackageName()));

					}
				}

				if (distance == 0)

					if (type == 3) {

						activity.progress[index] = distance;

						activity.event_image[index]
								.setImageResource(R.drawable.icon_flow_spd);
						activity.event_distance[index].setVisibility(View.GONE);
						activity.mRoundProgressBar[index].setMax(0);
						activity.mRoundProgressBar[index].setProgress(0);
					} else {
						for (; index < 2; index++) {
							activity.event_happen_ly[index]
									.setVisibility(View.GONE);
							activity.toast_flag = false;

							if (index == 1)
								activity.left_status_ly
										.setVisibility(View.VISIBLE);

						}

					}

				break;

			case HandlerMessageCode.HMC_UPDATE_BASIC_DATABASE: {
				String path = (String) msg.obj;
				Log.v(TAG, "will update basic database " + path);
				activity.mPointSearch.updateBase(path);
				break;
			}

			case HandlerMessageCode.HMC_UPDATE_UPDATE_DATBASE: {
				String path = (String) msg.obj;
				Log.v(TAG, "will update update database " + path);
				if (activity.mPointSearch.updateUpdate(path) == 0) {
					CloudDogPreference.save();
				}
				break;
			}

			case HandlerMessageCode.HMC_UPDATE_EXTRA_VOICE: {
				String path = (String) msg.obj;
				Log.v(TAG, "will update voice database " + path);
				activity.mPointSearch.updateExtra(path);
				break;
			}

			case HandlerMessageCode.HMC_CLOUD_LOGIN_STATUS:
				if (!activity.mbBackground) {
					activity.mLoginView
							.setText((msg.arg1 == 1) ? R.string.LoginOK
									: R.string.LoginFail);
					activity.imageView_Login_status
							.setImageResource((msg.arg1 == 1) ? R.drawable.icon_login_status_true
									: R.drawable.icon_login_status_false);

				}
				break;

			case HandlerMessageCode.HMC_CLOUD_NETWORK_STATUS:
				if (!activity.mbBackground) {
					activity.mNetStatusTextView
							.setText((msg.arg1 == 1) ? R.string.NetworkConnected
									: ((msg.arg1 == 2) ? R.string.NetworkDisConnected
											: R.string.NetworkConnecting));
					activity.imageView_Network_status
							.setImageResource((msg.arg1 == 1) ? R.drawable.icon_network_status_true
									: ((msg.arg1 == 2) ? R.drawable.icon_network_status_false
											: R.drawable.icon_network_status_false));
				}
				break;

			case HandlerMessageCode.HMC_CLOUD_BROADCAST_MSG:
				this.sendMessage(this.obtainMessage(
						HandlerMessageCode.HMC_TTS_SPEAK_CONTENT,
						TtsPacket.TTS_PLAY_LEVEL_LOW, 0, msg.obj));
				break;

			case HandlerMessageCode.HMC_CLOUD_ADDRESS_INFO:
				this.sendMessage(this.obtainMessage(
						HandlerMessageCode.HMC_TTS_SPEAK_CONTENT,
						TtsPacket.TTS_PLAY_LEVEL_LOW, 0, msg.obj));
				break;

			case HandlerMessageCode.HMC_CLOUD_WEATHER_INFO:
				this.sendMessage(this.obtainMessage(
						HandlerMessageCode.HMC_TTS_SPEAK_CONTENT,
						TtsPacket.TTS_PLAY_LEVEL_LOW, 0, msg.obj));
				break;

			case HandlerMessageCode.HMC_DATABASE_UPDATE_FINISH:
				// activity.mNetService.setUpdateKey();
				break;

			case HandlerMessageCode.HMC_POINT_ALARM_STATUS:
				if (activity.mMapHandler != null && isMapLoaded) {
					activity.mMapHandler.sendMessage(activity.mMapHandler
							.obtainMessage(BaiduMapActivity.GPS_POINT_STATUS,
									msg.obj));
				}
				break;

			case HandlerMessageCode.HMC_MAP_REPORT_HANDLER:
				activity.mMapHandler = (Handler) msg.obj;
				if (null == msg.obj) {
					isMapLoaded = false;
				}
				break;

			case HandlerMessageCode.HMC_MONITOR_ALARM:

				break;
			}
		}
	}

	private static final boolean isNetworkAvailable(Context context) {
		ConnectivityManager mgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo[] info = mgr.getAllNetworkInfo();
		if (info != null) {
			for (int i = 0; i < info.length; i++) {
				if (info[i].getState() == NetworkInfo.State.CONNECTED) {
					return true;
				}
			}
		}

		return false;
	}

	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (intent.getAction().equals(NETWORK_BROADCAST_CODE_STRING)) {
				if (!isNetworkAvailable(context)) {
					Log.v(TAG, "Network is disable");
					if (mNetService != null) {
						mNetService.close();
						mNetService = null;
					}
				} else {
					Log.v(TAG, "Network is enable");
					if (mNetService == null) {
						mNetService = new NetService(CloudDogActivity.this);
					}
				}
			}
		}
	};

	@Override
	public void onFinished(byte[] data, String recFile) {
		// TODO Auto-generated method stub
		if (mNetService != null) {
			mNetService.addSelfpoint(data, recFile);
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onDown(MotionEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2,
			float arg3) {
		// TODO Auto-generated method stub

		if (arg2 < 0 && isLeft == false) {
			// menu.showAtLocation(this.findViewById(R.id.a), Gravity.BOTTOM
			// | Gravity.CENTER_HORIZONTAL, 0, 0); // 设置在屏幕中的显示位置
			menu_right.showAtLocation(this.findViewById(R.id.a), Gravity.RIGHT,
					0, 0);

			System.out.println("1");

		} else if (arg2 > 0 && isLeft == true) {

			menu_left.showAtLocation(this.findViewById(R.id.a), Gravity.LEFT,
					0, 0);

			System.out.println("2");
		}

		return false;
	}

	@Override
	public void onLongPress(MotionEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2,
			float arg3) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onSingleTapUp(MotionEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
		// TODO Auto-generated method stub
		if (arg0.getId() == left_pand.getId())
			isLeft = true;
		else
			isLeft = false;
		detector.onTouchEvent(arg1);
		return false;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub

		if (arg0.getId() == mylistview.getId()) {
			if (list.get(arg2).equals(getString(R.string.Simulate))
					|| list.get(arg2)
							.equals(getString(R.string.CancleSimulate))) {

				if (mGpsSimulateThread != null) {
					mGpsSimulateThread.Exit();
					mGpsSimulateThread = null;
					list.set(arg2, getString(R.string.Simulate));
				} else {
					mGpsSimulateThread = new GpsSimulate(mActivityHandler);
					mGpsSimulateThread.start();
					list.set(arg2, getString(R.string.CancleSimulate));
				}

				// speed_pand.setLayoutParams(gonelv);

			} else if (list.get(arg2)
					.equals(getString(R.string.CancleSimulate))) {
				mGpsSimulateThread = new GpsSimulate(mActivityHandler);
				mGpsSimulateThread.start();
				// speed_pand.setLayoutParams(gonelv);
			}

			else if (list.get(arg2).equals(getString(R.string.WeatherInfo))) {
				if (mNetService != null && mGpsData != null) {
					mNetService.getWeather(mGpsData.mLatitude,
							mGpsData.mLongtitude);
				}
				btn_panel.setVisibility(View.GONE);
				// speed_pand.setLayoutParams(gonelv);
			} else if (list.get(arg2).equals(
					getString(R.string.CurrentPosition))) {
				if (mNetService != null && mGpsData != null) {
					mNetService.getCurAddressName(mGpsData.mLatitude,
							mGpsData.mLongtitude);
				}

				// btn_panel.setVisibility(View.GONE);
				// speed_pand.setLayoutParams(gonelv);
			} else if (list.get(arg2).equals(getString(R.string.RoadStatus))) {
				if (mNetService != null && mGpsData != null) {
					mNetService.getCurLoadInfo(mGpsData.mLatitude,
							mGpsData.mLongtitude);
				}

				// btn_panel.setVisibility(View.GONE);
				// speed_pand.setLayoutParams(gonelv);
			} else if (list.get(arg2).equals(getString(R.string.BaiduMap))) {
				PackageManager manager = this.getPackageManager();
				Intent intent = new Intent();

				try {
					PackageInfo info = manager
							.getPackageInfo("com.baidu.BaiduMap",
									PackageManager.GET_ACTIVITIES);
					intent = manager
							.getLaunchIntentForPackage("com.baidu.BaiduMap");
					startActivity(intent);
				} catch (NameNotFoundException e) {
					new AlertDialog.Builder(this)
							.setTitle("错误")
							.setMessage("没有安装百度地图")
							.setPositiveButton("退出",
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											// TODO Auto-generated method stub
											dialog.dismiss();
										}

									}).show();
				}

				// speed_pand.setLayoutParams(gonelv);

			}
			if(list.get(arg2).equals("查询菜单"))
					Toast.makeText(CloudDogActivity.this, "查询菜单", Toast.LENGTH_SHORT).show();
			else
				menu_left.dismiss();
			
			
		} else if (arg0.getId() == mylistview_right.getId()) {
			if (list_right.get(arg2).equals("正向自建")) {
				mSelfPointDialog.resetSelectItem(0);
				mSelfPointDialog.show();

			} else if (list_right.get(arg2).equals("反向自建")) {
				mSelfPointDialog.resetSelectItem(0);
				mSelfPointDialog.show();
			} else if (list_right.get(arg2).equals("测试A")) {
				event_happen_ly[0].setVisibility(View.VISIBLE);
				event_image[0]
						.setImageResource(R.drawable.icon_flow_spd);
				progress[0] = 0;

				new Thread(new Runnable() {

					@Override
					public void run() {
						while (progress[0] <= 100) {
							progress[0] += 3;

							
					

							mRoundProgressBar[0].setProgress(progress[0]);

							try {
								Thread.sleep(100);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}

					}
				}).start();

			} else if (list_right.get(arg2).equals("测试B")) {
//				event_happen_ly[0].setVisibility(View.GONE);
				event_image[0]
						.setImageResource(R.drawable.icon_interval_spd_60);
			}
			
			if(list_right.get(arg2).equals("设置菜单"))
				Toast.makeText(CloudDogActivity.this, "设置菜单", Toast.LENGTH_SHORT).show();
		else
			menu_right.dismiss();
			
		
		}

	}
}
