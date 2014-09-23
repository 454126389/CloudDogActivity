package com.rayee.camera;

import android.content.Context;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.demo.DemoApplication;

public class FloatView extends ImageView {

	private static final String TAG = "FloatView";
	private static final boolean CAMERA_PREVIEW_ENABLE = false;
	private static final boolean DBG = false;
	private float mTouchX;
	private float mTouchY;
	private float x;
	private float y;
	private float mStartX;
	private float mStartY;
	private OnClickListener mClickListener;
    private CameraPreviewApp mCameraPreview = new CameraPreviewApp();

	private WindowManager windowManager = (WindowManager) getContext()
			.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
	// 此windowManagerParams變量為獲取的全局變量，用以保存懸浮窗口的屬性
	private WindowManager.LayoutParams windowManagerParams = ((DemoApplication) getContext().getApplicationContext()).getWindowParams();

	public FloatView(Context context) {
		super(context);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		//獲取到狀態欄的高度
		Rect frame =  new Rect();  
		getWindowVisibleDisplayFrame(frame);
		int  statusBarHeight = frame.top;
		if(DBG) Log.d(TAG,"statusBarHeight:"+statusBarHeight);
		// 獲取相對屏幕的坐標，即以屏幕左上角為原點
		x = event.getRawX();
		y = event.getRawY() - statusBarHeight; // statusBarHeight是系統狀態欄的高度
		if(DBG) Log.d(TAG, "currX" + x + "====currY" + y);
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN: // 捕獲手指觸摸按下動作
			// 獲取相對View的坐標，即以此View左上角為原點
			mTouchX = event.getX();
			mTouchY = event.getY();
			mStartX = x;
			mStartY = y;
			if(DBG) Log.d(TAG, "startX" + mTouchX + "====startY"
					+ mTouchY);
			break;

		case MotionEvent.ACTION_MOVE: // 捕獲手指觸摸移動動作
			updateViewPosition(statusBarHeight);
			break;

		case MotionEvent.ACTION_UP: // 捕獲手指觸摸離開動作
			updateViewPosition(statusBarHeight);
			mTouchX = mTouchY = 0;
			if ((x - mStartX) < 5 && (y - mStartY) < 5) {
				if(mClickListener!=null) {
					mClickListener.onClick(this);
				}
			}
			break;
		}
		return true;
	}
	@Override
	public void setOnClickListener(OnClickListener l) {
		this.mClickListener = l;
	}

	@Override
	public void onWindowVisibilityChanged(int visibility){
		if(DBG) Log.d(TAG, "visibility:" + visibility);
		if (CAMERA_PREVIEW_ENABLE) {
			if(visibility == View.VISIBLE)
				mCameraPreview.startPreview(windowManagerParams.x, windowManagerParams.y);
			else
				mCameraPreview.stopPreview();
		}
	}

	private void updateViewPosition(int offset) {
		// 更新浮動窗口位置參數
		windowManagerParams.x = (int) (x - mTouchX);
		windowManagerParams.y = (int) (y - mTouchY);
		windowManager.updateViewLayout(this, windowManagerParams); // 刷新顯示
		if (CAMERA_PREVIEW_ENABLE) {
			if(DBG) Log.d(TAG, "updateX" + windowManagerParams.x + "====updateY" + windowManagerParams.y);
			mCameraPreview.offsetPreview(windowManagerParams.x, windowManagerParams.y + offset);
		}
	}
}
