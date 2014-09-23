package android.demo;

import com.iflytek.tts.TtsService.TTSControl;

import android.os.Handler;
import android.view.KeyEvent;

public class EDogMenu {
	public String TAG 									= "Menu";
	public Handler mMainHandler							= null;
	
	public static final int NORMALKEY_FLAG				= 0;
	public static final int LONGKEY_FLAG				= 2;
	
	public TTSControl mTtsCtrl							= null;
	
	EDogMenu(Handler mainHandler) {
		mMainHandler = mainHandler;
		mTtsCtrl = TTSControl.GetInstance();
	}
	
	public void Display() {
		
	}
	
	public int KeyProcess(int keyCode, boolean isLongKeyPress) {
		switch(keyCode) {
		case KeyEvent.KEYCODE_ENTER:
			break;
			
		case KeyEvent.KEYCODE_PLUS:
			break;
			
		case KeyEvent.KEYCODE_MINUS:
			break;
			
		//key mode
		case KeyEvent.KEYCODE_A:
			break;
		
		//key set
		case KeyEvent.KEYCODE_B:
			break;
			
		//key double mode
		case KeyEvent.KEYCODE_C:
			break;
		}
		
		
		return 0;
	}
}
