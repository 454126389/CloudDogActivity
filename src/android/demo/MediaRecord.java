package android.demo;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnInfoListener;
import android.util.Log;

public class MediaRecord implements OnCompletionListener, OnErrorListener, OnInfoListener {
	private static final String TAG 								= "MediaRecord";
	public recordCallback mrcb										= null;
	public playCallback mpcb										= null;
	
	private MediaRecorder mRecorder									= null;
	private static MediaPlayer mPlayer								= null;
	private String mSampleFile										= null;
	private static Timer mTimer										= null;
	private int mRecrodFileTime										= 8000;			//8s
	
	private TimerTask recordStopTask 								= null;
	private Object mRecordSyncObject								= new Object();
	
	public interface recordCallback {
		public void recordStopCb();
	}
	
	public interface playCallback {
		public void playStopCb();
	}
	
	private class RecordStopTask extends TimerTask {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if(mrcb != null) {
				stopRecording();
				mrcb.recordStopCb();
			}
		}
		
	}
	
	public void registerRecordListener(recordCallback rcb) {
		mrcb = rcb;
	}
	
	public void registerPlayListener(playCallback pcb) {
		mpcb = pcb;
	}
	
	void startRecord(int time, String fileName) {
		mSampleFile = fileName;
		
		if(mRecorder == null) {
			mRecorder = new MediaRecorder();
		}
		
		mRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);  
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);  
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);  
        mRecorder.setOutputFile(fileName);
        mRecorder.setOnInfoListener(this);
        try {
        	mTimer = new Timer();
            mRecorder.prepare();
            mRecorder.start();
            mTimer.schedule(new RecordStopTask(), time);
        } catch(IOException exception) {  
            mRecorder.reset();  
            mRecorder.release();  
            mRecorder = null;
            exception.printStackTrace();
            return;
        }
	}
	
	public synchronized void stopRecording() {
		if (mRecorder == null)  
            return;
        
        mRecorder.stop();  
        mRecorder.release();
        mTimer.cancel();
        mTimer = null;
        mRecorder = null;
    } 
	
	public void startPlayback(String file) {  
		if(mPlayer == null) {
			mPlayer = new MediaPlayer();
		} else {
			mPlayer.reset();
		}
		
        try {  
            mPlayer.setDataSource(file);  
            mPlayer.setOnCompletionListener(this);  
            mPlayer.setOnErrorListener(this);  
            mPlayer.prepare();
            mPlayer.start();
        } catch (IllegalArgumentException e) {   
            e.printStackTrace();
        } catch (IOException e) {  
            e.printStackTrace();
        }
    }  
      
    public void stopPlayback() {  
        if (mPlayer == null) // we were not in playback  
            return;  
  
        mPlayer.stop();  
    }
      
    public void stop() {  
        stopRecording();  
        stopPlayback();  
    }

	@Override
	public void onCompletion(MediaPlayer mp) {
		// TODO Auto-generated method stub
		if(mpcb != null) {
			mpcb.playStopCb();
		}
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		// TODO Auto-generated method stub
		Log.e(TAG, "playback error");
		return false;
	}

	@Override
	public void onInfo(MediaRecorder mr, int what, int extra) {
		// TODO Auto-generated method stub
		Log.e(TAG, "recorder error");
	}
}
