package android.demo;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Instrumentation;
import android.view.KeyEvent;

public class TranslateKey {

	public TranslateKey() {
		// TODO Auto-generated constructor stub
	}
	
	static class KeyTimerTask extends TimerTask {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if(isDoubleKey == true) {
				Instrumentation inst = new Instrumentation();
				inst.sendKeySync(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_RIGHT));
			}
			
			isDoubleKey = false;
			if(mKeyTimer != null) {
				mKeyTimer.cancel();
			}
			//ShowSelfPointActivity(0);
		}
	};
	
	private static boolean isDoubleKey = false;
	private static KeyTimerTask mKeyTimerTask = null;
	private static Timer mKeyTimer = null;//new Timer();
	private static boolean isLongPressKey = false;
	
	private static boolean checkDoubleKey(KeyEvent event) {
		if(event.getRepeatCount() != 0) {
			return false;
		}
		
		if(isDoubleKey == false) {
			isDoubleKey = true;
			
			if(mKeyTimer != null) {
				mKeyTimer.cancel();
			}
			
			mKeyTimer = new Timer();
			mKeyTimer.schedule(new KeyTimerTask(), 500);
		} else {
			isDoubleKey = false;

			if(mKeyTimer != null) {
				mKeyTimer.cancel();
			}
			return true;
		}
		
		return false;
	}
	
	public static int onKeyDown(int keyCode, KeyEvent event) {
		if (event.getRepeatCount() == 0) {
			event.startTracking();
		}
		
		if (keyCode == KeyEvent.KEYCODE_CAMERA) {
			if(checkDoubleKey(event)) {
				return KeyEvent.KEYCODE_DPAD_LEFT;
			}
		}
		
		return KeyEvent.KEYCODE_UNKNOWN;
	}
	
	public static int onKeyUp(int keyCode, KeyEvent event) {
		if (isLongPressKey) {
			isLongPressKey = false;
			return KeyEvent.KEYCODE_UNKNOWN;
		}
		
		return getKeyCode(keyCode, event);
	}
	
	public static int onKeyLongPress(int keyCode, KeyEvent event) {
		isLongPressKey = true;
		
		if (keyCode == KeyEvent.KEYCODE_CAMERA) {
			isDoubleKey = false;
			
			if(mKeyTimer != null) {
				mKeyTimer.cancel();
			}
		}
		
		switch(keyCode) {
		case KeyEvent.KEYCODE_CAMERA:
			return KeyEvent.KEYCODE_0;
		
		case KeyEvent.KEYCODE_MENU:
			return KeyEvent.KEYCODE_1;
			
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			return KeyEvent.KEYCODE_2;
			
		case KeyEvent.KEYCODE_BACK:
			return KeyEvent.KEYCODE_3;
		}
		
		return KeyEvent.KEYCODE_UNKNOWN;
	}
	
	public static int getKeyCode(int KeyCode, KeyEvent event) {
		switch(KeyCode) {
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			return KeyEvent.KEYCODE_DPAD_CENTER;
			
		case KeyEvent.KEYCODE_MENU:
			return KeyEvent.KEYCODE_DPAD_DOWN;
			
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			return KeyEvent.KEYCODE_DPAD_UP;
			
		case KeyEvent.KEYCODE_BACK:
			return KeyEvent.KEYCODE_BACK;
		}
		
		return KeyEvent.KEYCODE_UNKNOWN;
	}
}
