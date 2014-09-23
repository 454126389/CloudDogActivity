package com.rayee.camera;

import android.app.Application;
import android.hardware.Camera;
import android.util.Log;

/** A basic Camera preview class */
public class CameraPreviewApp extends Application {
    private Camera mCamera = null;
    private boolean mCameraPreviewStatus = false;
    private static final String TAG = "CameraPreview";

    public Camera getCamera() {
    	return mCamera;
    }

    public void startPreview(int x, int y){
    	if(mCamera == null)
            mCamera = getCameraInstance();
    	if (!mCameraPreviewStatus) {
    		Log.d("TAG","startPreview");
    	    mCamera.startPreview();
    	    offsetPreview(x,y);
    	    mCameraPreviewStatus = true;
    	}
    }

    public void stopPreview(){
    	if(mCamera == null)
    		return;
    	if (mCameraPreviewStatus) {
		    mCamera.stopPreview();
		    mCamera.release();
		    mCameraPreviewStatus = false;
    	}
    }

    public void offsetPreview(int x,int y){
    	if(mCamera == null)
    		return;
    	mCamera.sendCommand(0, x, y);
    }

	/** A safe way to get an instance of the Camera object. */
	public static Camera getCameraInstance(){
        Camera c = null;
		try {
    	    c = Camera.open(); // attempt to get a Camera instance
        }
		catch (Exception e){
		    // Camera is not available (in use or does not exist)
        }
	    return c; // returns null if camera is unavailable
	}
}
