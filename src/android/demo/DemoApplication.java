package android.demo;

import android.app.Application;
import android.os.Handler;
import android.view.WindowManager;

public class DemoApplication extends Application {
	// �������

	private Handler handler = null;

	// set����
	public void setHandler(Handler handler) {

	this.handler = handler;

	}

	// get����

	public Handler getHandler() {
		return handler;
	}
	
	private WindowManager.LayoutParams windowParams = new WindowManager.LayoutParams();

	public WindowManager.LayoutParams getWindowParams() {
		return windowParams;
	}
}
