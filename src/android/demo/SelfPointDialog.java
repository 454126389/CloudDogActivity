package android.demo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.iflytek.tts.TtsService.TTSControl;
import com.iflytek.tts.TtsService.TtsPacket;
import com.weifer.search.SearchPointClass;

import android.R.integer;
import android.content.Context;
import android.content.DialogInterface;
import android.demo.MediaRecord.playCallback;
import android.demo.MediaRecord.recordCallback;
import android.demo.SelfPointDataSample.OnSampleFinishListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public final class SelfPointDialog extends DogDialog implements OnClickListener, OnItemClickListener, 
				playCallback, recordCallback {
	
	public interface SelfpointFinishListener {
		void onFinished(byte[] data, String recFile);
	}

	private static Context context = null;
	public byte[] mSelfPointRecordBuf = new byte[16];
	private final static String filePrefix = new String(android.os.Environment.getExternalStorageDirectory().getPath() + 
													java.io.File.separator + "s4221r" + java.io.File.separator);
	/**
	 * 0: 简易点
	 * 1: 录音点
	 * 2: 专业点
	 */
	public int mSelfPointCategory = 0;
	
	/**
	 * 0: 开始录音
	 * 1：正在录音
	 * 2：录音完成
	 */
	public int mSelfPointRecordVoice = 0;
	
	/**
	 * 0: 开始播放录音
	 * 1：正在播放录音
	 * 2：录音播放完成
	 */
	public int mSelfPointRecordPlay = 0;
	protected TTSControl mTtsCtrl = null;
	private MediaRecord mRecordVoice = new MediaRecord();
	private Handler mHandler = null;
	private SelfPointDataSample mSample = SelfPointDataSample.getInstance();
	private static String mRecordFilePath = null;
	
	private SelfpointFinishListener mFinishListener = null;
	
	public SelfPointDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		SelfPointDialog.context = context;
	}

	public SelfPointDialog(Context context, int theme) {
		super(context, theme);
		// TODO Auto-generated constructor stub
		SelfPointDialog.context = context;
	}

	public SelfPointDialog(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		SelfPointDialog.context = context;
	}

	public ListView mListView = null;
	
	public void setFinishListener(SelfpointFinishListener listener) {
		mFinishListener = listener;
	}
	
	private void switchContent(DogTextAdapter adapter, int category) {
		adapter.clear();
		int id = R.array.SelfPointRecordModeMsg;
		
		String[] strTextStrings = context.getResources().getStringArray(id);
		adapter.add(strTextStrings[getValueFromArrayId(id)], id);
		if(category == 1) {
			// 1
			id = R.array.SelfPointItemRecordArratText;
			strTextStrings = context.getResources().getStringArray(id);
			adapter.add(strTextStrings[getValueFromArrayId(id)], id);
			
			// 2
			id = R.array.SelfPointItemPlayArrayText;
			strTextStrings = context.getResources().getStringArray(id);
			adapter.add(strTextStrings[getValueFromArrayId(id)], id);
		} else if(category == 2) {
			// 1
			id = R.array.SelfpointModeArray;
			strTextStrings = context.getResources().getStringArray(id);
			adapter.add(strTextStrings[getValueFromArrayId(id)], id);
			
			// 2
			id = R.array.SelfPointPictureArray;
			strTextStrings = context.getResources().getStringArray(id);
			adapter.add(strTextStrings[getValueFromArrayId(id)], id);
			
			// 3
			id = R.array.SelfPointSpeedArrayText;
			strTextStrings = context.getResources().getStringArray(id);
			adapter.add(strTextStrings[getValueFromArrayId(id)], id);
			
			// 4
			id = R.array.SelfPointDirectionArray;
			strTextStrings = context.getResources().getStringArray(id);
			adapter.add(strTextStrings[getValueFromArrayId(id)], id);
			
			// 5
			id = R.array.SelfPointItemRecordArratText;
			strTextStrings = context.getResources().getStringArray(id);
			adapter.add(strTextStrings[getValueFromArrayId(id)], id);
			
			// 6
			id = R.array.SelfPointItemPlayArrayText;
			strTextStrings = context.getResources().getStringArray(id);
			adapter.add(strTextStrings[getValueFromArrayId(id)], id);
		}
	}
	
	@Override
	protected final void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		mTtsCtrl = TTSControl.GetInstance();
		mHandler = new SelfPointHandler(this);
		
		findViewById(R.id.button_dlg_back).setOnClickListener(this);
		findViewById(R.id.button_dlg_save).setOnClickListener(this);
		
		mSelfPointRecordBuf[0] = 0x36;
		mSelfPointRecordBuf[1] = 0x1D;			//前方为自建点

		DogTextAdapter adapter = new DogTextAdapter(context);
		switchContent(adapter, mSelfPointCategory);
		adapter.setSelectedItem(0);
		
		mListView = (ListView) findViewById(R.id.listView_selfpoint_parameter);
		mListView.setAdapter(adapter);
		mListView.setSelection(0);
		mListView.setOnItemClickListener(this);
		
		mRecordVoice.registerPlayListener(this);
		mRecordVoice.registerRecordListener(this);
		
		SelfPointDataSample.setBackwardListener(mBackwardSampleFinishListener);
		SelfPointDataSample.setForwardListener(mForwardSampleFinishListener);
		Log.v(TAG, "ON create");
	}
	
	@Override
	public final void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()) {
		case R.id.button_dlg_back:
			closeDialog();
			break;
			
		case R.id.button_dlg_save:
			onKeyUp(KeyEvent.KEYCODE_BACK, null);
			break;
		}
	}
	
	private void SelfPointPlay(int id) {
		TtsPacket packet = new TtsPacket();
		String content = context.getResources().getString(R.string.DingDongMsg);
		packet.setContent(content);
		content = context.getResources().getString(id);
		packet.appendContent(content);
		packet.setPlayLevel(TtsPacket.TTS_PLAY_LEVEL_IMMEDIATELY);
		packet.play();
	}
	
	private void SelfPointPlay(int array, int position) {
		TtsPacket packet = new TtsPacket();
		String content = context.getResources().getString(R.string.DingDongMsg);
		packet.setContent(content);
		content = context.getResources().getStringArray(array)[position];
		packet.appendContent(content);
		packet.setPlayLevel(TtsPacket.TTS_PLAY_LEVEL_IMMEDIATELY);
		packet.play();
	}
	
	
	public final void saveDataToMemory() {
		Log.v(TAG, "save " + String.valueOf(nFreeIndex) + " data " + DebugOperate.ByteBufferConvertToString(mSelfPointRecordBuf, ','));
		SearchPointClass.SaveSelfPoint(nFreeIndex, mSelfPointRecordBuf);
		SelfPointPlay(R.string.SelfPointRecordFinish);
		
		if(mFinishListener != null) {
			byte[] buf = mSelfPointRecordBuf.clone();
			String rec = null;
			if(mRecordFilePath != null) {
				 rec = new String(filePrefix + mRecordFilePath);
			}
			mFinishListener.onFinished(buf, rec);
		}
	}
	
	public final int getMemoryFreeSpace() {
		return SearchPointClass.getSelfPointFreeIndex();
	}
	
	private final void getRecordFileName() {
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
		mRecordFilePath = String.format("%016d", UserPreference.getDeviceSn()) + "_" + format.format(new Date()) + "_" + String.format("%03d", nFreeIndex + 1) + ".amr";
	}
	
	private final void fillWavTail() {
		if(mRecordFilePath != null) {
			File wave = new File(filePrefix + mRecordFilePath);
			
			if(!wave.exists()) {
				Log.v(TAG, "wave file not found");
				return;
			}
			
			try {
				byte[] buffer = new byte[32];
				FileOutputStream fos = new FileOutputStream(wave, true);

				fos.write(String.format("%016d", UserPreference.getDeviceSn()).getBytes());
				fos.write(mSelfPointRecordBuf);
				buffer[0] = 0x11;	// flag bit[0-3]: data(0 upload over,1 wait upload) bit[4-7]: record(0 upload over,1 wait upload)
				for(int i = 1; i < 32; i++) {
					buffer[i] = (byte) 0xFF;
				}
				fos.write(buffer);
				fos.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public final void closeDialog() {
		hide();
	}
	
	public final void onGpsLost() {
			
		/*
		if(mbGetSampleData == false) {
			mTtsCtrl.ttsPendingContent(R.string.SelfPointSampleNotFinish);
			mTtsCtrl.ttsPendingContent(R.string.SelfPointGiveUp);
			closeDialog();
		}
		*/
		SelfPointDataSample.onGpsLost();
	}
	
	public final void resetSelectItem(int position) {
		if(mListView == null) {
			return;
		}
		
		DogTextAdapter adapter = (DogTextAdapter) mListView.getAdapter();
		mListView.setSelection(position);
		adapter.setSelectedItem(position);
		adapter.notifyDataSetInvalidated();
	}
	
	private final void playCategory(int arrayId, int position) {
		int resId = getResourceStringFromArray(arrayId);
		TtsPacket packet = new TtsPacket();
		String content = context.getResources().getString(R.string.DingDongMsg);
		packet.setContent(content);
		content = context.getResources().getString(resId);
		packet.appendContent(content);

		if(R.array.SelfPointItemPlayArrayText != arrayId 
				&& R.array.SelfPointItemRecordArratText != arrayId) {
			content = context.getResources().getStringArray(arrayId)[position];
			packet.appendContent(content);
		}
		packet.setPlayLevel(TtsPacket.TTS_PLAY_LEVEL_IMMEDIATELY);
		packet.play();
	}
	
	private final int getResourceStringFromArray(int arrayId) {
		int id = 0;
		switch(arrayId) {
		case R.array.SelfPointRecordModeMsg:
			id = R.string.SelfPointRecordModeText;
			break;
			
		case R.array.SelfpointModeArray:
			id = R.string.SelfpointModeMsg;
			break;
			
		case R.array.SelfPointPictureArray:
			id = R.string.SelfPointPictureMsg;
			break;
			
		case R.array.SelfPointSpeedArrayText:
			id = R.string.SelfPointSpeedText;
			break;
			
		case R.array.SelfPointDirectionArray:
			id = R.string.SelfPointDirectionMsg;
			break;
			
		case R.array.SelfPointItemPlayArrayText:
			id = R.string.SelfPointItemPlayText;
			break;
			
		case R.array.SelfPointItemRecordArratText:
			id = R.string.SelfPointItemRecordText;
			break;
		}
		
		return id;
	}
	
	private final void playParameter(int arrayId, int position) {
		SelfPointPlay(arrayId, position);
	}
	
	private final void DoCategory(boolean isInc) {
		DogTextAdapter adapter = (DogTextAdapter) mListView.getAdapter();
		int position = adapter.getSelectedItem();
		int maxItem = adapter.getCount();
		if(isInc) {
			position = position + 1;
		} else {
			position = position + maxItem - 1;
		}
		position %= maxItem;
		int id = adapter.getItemdata(position);
		playCategory(id, getValueFromArrayId(id));
		mListView.setSelection(position);
		adapter.setSelectedItem(position);
		adapter.notifyDataSetInvalidated();
	}
	
	private final void doItem(boolean isTouch, boolean isInc) {
		DogTextAdapter adapter = (DogTextAdapter) mListView.getAdapter();
		int position = adapter.getSelectedItem();
		int arrayId = adapter.getItemdata(position);
		String[] strText = context.getResources().getStringArray(arrayId);
		int value = getValueFromArrayId(arrayId);
		boolean isupdate = true;
		
		if(arrayId == R.array.SelfPointItemPlayArrayText) {
			if(value == 0 || value == 2) {
				mTtsCtrl.ttsStop();
				if(mRecordFilePath != null) {
					mRecordVoice.startPlayback(filePrefix + mRecordFilePath);
					value = 1;
				}
			} else if(value == 1){
				mRecordVoice.stopPlayback();
				isupdate = false;
			}
		} else if(arrayId == R.array.SelfPointItemRecordArratText) {
			if(value == 0 || value == 2) {
				mTtsCtrl.ttsStop();
				getRecordFileName();
				Log.v(TAG, "record file " + mRecordFilePath);
				mRecordVoice.startRecord(8000, filePrefix + mRecordFilePath);
				value = 1;
			} else if(value == 1) {
				mRecordVoice.stopRecording();
				isupdate = false;
			}
		} else {
			if(isInc) {
				value += 1;
			} else {
				value += strText.length - 1;
			}
			value %= strText.length;
			
			if(arrayId == R.array.SelfPointRecordModeMsg) {
				switchContent(adapter, value);
			}
			
			if(!isTouch) {
				playParameter(arrayId, value);
			}
		}
		
		if(isupdate) {
			setValueFromArrayId(arrayId, value);
			adapter.setItemText(position, strText[value]);
			adapter.notifyDataSetInvalidated();
		}
	}


	@Override
	public final boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		int key = KeyEvent.KEYCODE_UNKNOWN;
		
		key = TranslateKey.onKeyDown(keyCode, event);
		switch(key) {
		case KeyEvent.KEYCODE_DPAD_LEFT:
			break;
		}

		return false;
	}

	@Override
	public final boolean onKeyLongPress(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		int key = KeyEvent.KEYCODE_UNKNOWN;
		
		key = TranslateKey.onKeyLongPress(keyCode, event);
		switch(key) {
		case KeyEvent.KEYCODE_0:
			DoCategory(false);
			break;
			
		case KeyEvent.KEYCODE_1:
			break;
			
		case KeyEvent.KEYCODE_2:
			break;
			
		case KeyEvent.KEYCODE_3:
			break;
		}
		
		if(key != KeyEvent.KEYCODE_UNKNOWN) {
			return false;
		}
		
		return false;
	}
	
	@Override
	public final boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		int key = KeyEvent.KEYCODE_UNKNOWN;
		key = TranslateKey.onKeyUp(keyCode, event);

		switch(key){
		case KeyEvent.KEYCODE_DPAD_CENTER:
			DoCategory(true);
			break;
			
		case KeyEvent.KEYCODE_DPAD_UP:
			doItem(false, false);
			break;
			
		case KeyEvent.KEYCODE_DPAD_DOWN:
			doItem(false, true);
			break;
			
		case KeyEvent.KEYCODE_BACK:
			if(mbGetSampleData) {
				saveDataToMemory();
			} else {
				SelfPointDataSample.giveUpSample();
				SelfPointPlay(R.string.SelfPointGiveUp);
			}
			closeDialog();
			break;
		}
		
		return false;
	}

	private final int getValueFromArrayId(int resArrayId) {
		switch(resArrayId) {
		case R.array.SelfPointRecordModeMsg:
			return mSelfPointCategory;
		
		case R.array.SelfpointModeArray:
			return (mSelfPointRecordBuf[4] >> 4 & 0x0F);

		case R.array.SelfPointPictureArray:
			return (mSelfPointRecordBuf[4] & 0x0F);
			
		case R.array.SelfPointSpeedArrayText:
			return ((mSelfPointRecordBuf[0] & 0x0F) - 1);
			
		case R.array.SelfPointDirectionArray:
			return (mSelfPointRecordBuf[8] & 0x0F);
			
		case R.array.SelfPointItemPlayArrayText:
			return mSelfPointRecordPlay;
			
		case R.array.SelfPointItemRecordArratText:
			return mSelfPointRecordVoice;
		}
		return 0;
	}
	
	private final void setValueFromArrayId(int resArrayId, int value) {
		switch(resArrayId) {
		case R.array.SelfPointRecordModeMsg:
			mSelfPointCategory = value;
			break;
		
		case R.array.SelfpointModeArray:
			mSelfPointRecordBuf[4] = (byte) ((mSelfPointRecordBuf[4] & 0x0F) | (value << 4));
			break;

		case R.array.SelfPointPictureArray:
			mSelfPointRecordBuf[4] = (byte) ((mSelfPointRecordBuf[4] & 0xF0) | value);
			break;
			
		case R.array.SelfPointSpeedArrayText:
			mSelfPointRecordBuf[0] = (byte) ((mSelfPointRecordBuf[0] & 0xF0) | (value + 1));
			break;
			
		case R.array.SelfPointDirectionArray:
			mSelfPointRecordBuf[8] = (byte) ((mSelfPointRecordBuf[8] & 0xF0) | value);
			break;
			
		case R.array.SelfPointItemPlayArrayText:
			mSelfPointRecordPlay = value;
			break;
			
		case R.array.SelfPointItemRecordArratText:
			mSelfPointRecordVoice = value;
			break;
		}
	}

	@Override
	public final void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		DogTextAdapter adapter = (DogTextAdapter) parent.getAdapter();
		int index = adapter.getItemdata(position);
		
		adapter.setSelectedItem(position);
		adapter.notifyDataSetInvalidated();
		
		if(index == R.array.SelfPointItemPlayArrayText || index == R.array.SelfPointItemRecordArratText) {
			doItem(true, true);
		} else {
			if(index != R.array.SelfPointItemRecordArratText || index != R.array.SelfPointItemPlayArrayText) {
				DogDialog dlg = new DogDialog(context, R.style.MyDialog);
				dlg.setContentView(R.layout.selfpointparameter);
				dlg.setWindowSize(CloudDogActivity.mDisplayWidth / 2, 0, CloudDogActivity.mDisplayWidth / 2, CloudDogActivity.mDisplayHeight);
				ListView lv = (ListView) dlg.findViewById(R.id.listView_selfpoint_detail);
				adapter = new DogTextAdapter(context, index);
				lv.setAdapter(adapter);
				lv.setOnItemClickListener(dlg);
				adapter.setSelectedItem(getValueFromArrayId(index));
				lv.setSelection(getValueFromArrayId(index));
				dlg.setOnDismissListener(new OnDismissListener() {
					
					@Override
					public void onDismiss(DialogInterface dialog) {
						// TODO Auto-generated method stub
						ListView lv = (ListView) ((DogDialog)dialog).findViewById(R.id.listView_selfpoint_detail);
						DogTextAdapter adapter = (DogTextAdapter) lv.getAdapter();
						int position = adapter.getSelectedItem();
						adapter.clear();
						
						adapter = (DogTextAdapter) mListView.getAdapter();
						int itemdata = adapter.getItemdata(adapter.getSelectedItem());
						setValueFromArrayId(itemdata, position);
						adapter.setItemText(adapter.getSelectedItem(), itemdata, position);
						
						if(itemdata == R.array.SelfPointRecordModeMsg) {
							switchContent(adapter, position);
						}
						adapter.notifyDataSetInvalidated();
					}
				});
				
				dlg.show();
			}
		}
	}
	
	private static final int HANDLER_REFRESH_RECORD_TEXT			= 0;
	private static final int HANDLER_REFRESH_PLAY_TEXT				= 1;
	private static final int HANDLER_SAMPLE_FINISH					= 2;
	private static final String TAG = "SelfPointDialog";
	
	private class SelfPointHandler extends Handler {
		WeakReference<SelfPointDialog> mDialog = null;

		public SelfPointHandler(SelfPointDialog dlg) {
			// TODO Auto-generated constructor stub
			mDialog = new WeakReference<SelfPointDialog>(dlg);
		}
		
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			
			DogTextAdapter adapter = null;
			
			switch (msg.what) {
			case HANDLER_REFRESH_RECORD_TEXT:
				adapter = (DogTextAdapter) mDialog.get().mListView.getAdapter();
				adapter.setItemText(adapter.getSelectedItem(), adapter.getItemdata(adapter.getSelectedItem()), mDialog.get().mSelfPointRecordVoice);
				adapter.notifyDataSetInvalidated();
				mDialog.get().fillWavTail();
				mDialog.get().playParameter(adapter.getItemdata(adapter.getSelectedItem()), mDialog.get().mSelfPointRecordVoice);
				break;
				
			case HANDLER_REFRESH_PLAY_TEXT:
				adapter = (DogTextAdapter) mDialog.get().mListView.getAdapter();
				adapter.setItemText(adapter.getSelectedItem(), adapter.getItemdata(adapter.getSelectedItem()), mDialog.get().mSelfPointRecordPlay);
				adapter.notifyDataSetInvalidated();
				mDialog.get().playParameter(adapter.getItemdata(adapter.getSelectedItem()), mDialog.get().mSelfPointRecordPlay);
				break;
				
			case HANDLER_SAMPLE_FINISH:
				SelfPointDialog dlg = mDialog.get();
				TtsPacket packet = new TtsPacket();
				String content = context.getResources().getString(R.string.DingDongMsg);
				if(dlg != null) {
					packet.appendContent(content);
				}
				
				if(mDialog.get().mbStartCreateBackwardPoint) {
					content = context.getResources().getStringArray(R.array.SelfPointRecordDirectionTextArray)[1];
					packet.appendContent(content);
				}
				content = context.getResources().getString(R.string.SelfPointSampleFinish);
				packet.appendContent(content);
				packet.setPlayLevel(TtsPacket.TTS_PLAY_LEVEL_HIGH);
				packet.play();
				//int lat = (mDialog.get().mSelfPointRecordBuf[5] << 16) | (mDialog.get().mSelfPointRecordBuf[6] << 8) | (mDialog.get().mSelfPointRecordBuf[5]);
				//int lon = (mDialog.get().mSelfPointRecordBuf[9] << 16) | (mDialog.get().mSelfPointRecordBuf[10] << 8) | (mDialog.get().mSelfPointRecordBuf[11]);
				//Log.v(TAG, "add selfpoint " + String.valueOf(lat) + " " + String.valueOf(lon));
				break;

			default:
				break;
			}
		}
	};

	@Override
	public final void recordStopCb() {
		// TODO Auto-generated method stub
		mSelfPointRecordVoice = 2;
		//mRecordVoice.stopRecording();
		Message msg = mHandler.obtainMessage(HANDLER_REFRESH_RECORD_TEXT);
		mHandler.sendMessage(msg);
	}

	@Override
	public final void playStopCb() {
		// TODO Auto-generated method stub
		mSelfPointRecordPlay = 2;
		Message msg = mHandler.obtainMessage(HANDLER_REFRESH_PLAY_TEXT);
		mHandler.sendMessage(msg);
	}

	private boolean mbGetSampleData = false;
	private final OnSampleFinishListener mForwardSampleFinishListener = new OnSampleFinishListener() {

		@Override
		public void onSampleFinish(boolean status) {
			// TODO Auto-generated method stub
			if(status == false) {
				SelfPointPlay(R.string.SelfPointNotEnoughDistance);
				return;
			}
			if(mbGetSampleData == false) {
				mSample.getForwardSampleData(mSelfPointRecordBuf);
				mbGetSampleData = true;
				Message msg = mHandler.obtainMessage(HANDLER_SAMPLE_FINISH);
				mHandler.sendMessage(msg);
			}
		}
	};
	
	private final OnSampleFinishListener mBackwardSampleFinishListener = new OnSampleFinishListener() {

		@Override
		public void onSampleFinish(boolean status) {
			// TODO Auto-generated method stub
			if(mbGetSampleData == false) {
				mSample.getBackwardSampleData(mSelfPointRecordBuf);
				mbGetSampleData = true;
				Message msg = mHandler.obtainMessage(HANDLER_SAMPLE_FINISH);
				mHandler.sendMessage(msg);
			}
		}
	};

	private int nFreeIndex = -1;
	public final boolean StartCreateForwardPoint() {
		nFreeIndex = getMemoryFreeSpace();
		if(nFreeIndex == -1) {
			TtsPacket packet = new TtsPacket();
			String content = context.getResources().getString(R.string.DingDongMsg);
			packet.setContent(content);
			content = context.getResources().getString(R.string.SelfPointRecordNoMemory);
			packet.appendContent(content);
			packet.setPlayLevel(TtsPacket.TTS_PLAY_LEVEL_LOW);
			packet.play();
			return false;
		}
		mRecordFilePath = null;
		mbGetSampleData = false;
		SelfPointDataSample.StartSample(true);
		//mTtsCtrl.ttsSpeak(R.string.DingDongMsg);
		//mTtsCtrl.ttsPendingContent(R.string.SelfPointSampleStart);
		return true;
	}
	
	private boolean mbStartCreateBackwardPoint = false;
	public final boolean StartCreateBackwardPoint() {
		nFreeIndex = getMemoryFreeSpace();
		TtsPacket packet = new TtsPacket();
		if(nFreeIndex == -1) {
			String content = context.getResources().getString(R.string.DingDongMsg);
			packet.setContent(content);
			content = context.getResources().getString(R.string.SelfPointRecordNoMemory);
			packet.appendContent(content);
			packet.setPlayLevel(TtsPacket.TTS_PLAY_LEVEL_LOW);
			packet.play();
			return false;
		}
		mRecordFilePath = null;
		mbGetSampleData = false;
		SelfPointDataSample.StartSample(false);

		String content = context.getResources().getString(R.string.DingDongMsg);
		packet.setContent(content);
		content = context.getResources().getStringArray(R.array.SelfPointRecordDirectionTextArray)[1];
		packet.appendContent(content);
		content = context.getResources().getString(R.string.SelfPointSampleStart);
		packet.appendContent(content);
		packet.setPlayLevel(TtsPacket.TTS_PLAY_LEVEL_LOW);
		packet.play();
		
		return true;
	}
}
