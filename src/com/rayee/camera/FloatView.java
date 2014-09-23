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
	// ��windowManagerParams׃����@ȡ��ȫ��׃�������Ա���Ҹ����ڵČ���
	private WindowManager.LayoutParams windowManagerParams = ((DemoApplication) getContext().getApplicationContext()).getWindowParams();

	public FloatView(Context context) {
		super(context);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		//�@ȡ����B�ڵĸ߶�
		Rect frame =  new Rect();  
		getWindowVisibleDisplayFrame(frame);
		int  statusBarHeight = frame.top;
		if(DBG) Log.d(TAG,"statusBarHeight:"+statusBarHeight);
		// �@ȡ������Ļ�����ˣ�������Ļ���ϽǞ�ԭ�c
		x = event.getRawX();
		y = event.getRawY() - statusBarHeight; // statusBarHeight��ϵ�y��B�ڵĸ߶�
		if(DBG) Log.d(TAG, "currX" + x + "====currY" + y);
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN: // ���@��ָ�|��������
			// �@ȡ����View�����ˣ����Դ�View���ϽǞ�ԭ�c
			mTouchX = event.getX();
			mTouchY = event.getY();
			mStartX = x;
			mStartY = y;
			if(DBG) Log.d(TAG, "startX" + mTouchX + "====startY"
					+ mTouchY);
			break;

		case MotionEvent.ACTION_MOVE: // ���@��ָ�|���Ƅӄ���
			updateViewPosition(statusBarHeight);
			break;

		case MotionEvent.ACTION_UP: // ���@��ָ�|���x�_����
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
		// ���¸��Ӵ���λ�Å���
		windowManagerParams.x = (int) (x - mTouchX);
		windowManagerParams.y = (int) (y - mTouchY);
		windowManager.updateViewLayout(this, windowManagerParams); // ˢ���@ʾ
		if (CAMERA_PREVIEW_ENABLE) {
			if(DBG) Log.d(TAG, "updateX" + windowManagerParams.x + "====updateY" + windowManagerParams.y);
			mCameraPreview.offsetPreview(windowManagerParams.x, windowManagerParams.y + offset);
		}
	}
}
