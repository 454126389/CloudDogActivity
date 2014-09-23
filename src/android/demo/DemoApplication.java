package android.demo;

import android.app.Application;
import android.os.Handler;
import android.view.WindowManager;

public class DemoApplication extends Application {
	// 共享变量

	private Handler handler = null;

	// set方法
	public void setHandler(Handler handler) {

	this.handler = handler;

	}

	// get方法

	public Handler getHandler() {
		return handler;
	}
	
	private WindowManager.LayoutParams windowParams = new WindowManager.LayoutParams();

	public WindowManager.LayoutParams getWindowParams() {
		return windowParams;
	}
}
