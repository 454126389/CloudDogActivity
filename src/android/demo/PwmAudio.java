package android.demo;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

public class PwmAudio {
	private final String TAG											= "PWM";
	private final static int mSampleFrequency							= 4000;			// 采样频率，单位HZ, 16K
	private static int mInterval										= 500;			// 播放间隔，单位毫秒
	private static int mSampleBit										= 8;			// 16bit
	private static byte[] mWaveBuf										= null;			//new byte[mSizePerGenerate];
	private static Thread mPlayThread											= null;
	private static boolean mThreadExit											= false;
	/** 正弦波的高度 **/  
    public final static int HEIGHT 										= 2 ^ (mSampleBit - 1) - 1;  
    /** 2PI **/  
    public final static double TWOPI 									= 2 * 3.1415926;
    
    private static AudioTrack mAudio 									= null;
    public static int mBufferSize										= 0;
    
    static {
    	mBufferSize = AudioTrack.getMinBufferSize(mSampleFrequency, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_8BIT);
    	mAudio = new AudioTrack(AudioManager.STREAM_MUSIC
				,mSampleFrequency, AudioFormat.CHANNEL_OUT_MONO 
				,AudioFormat.ENCODING_PCM_8BIT
				,mBufferSize, AudioTrack.MODE_STREAM );		
    }
    
    public class pwm_thread extends Thread {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				Thread.sleep(2500);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				return;
			}
			
			if (null == mAudio){
				Log.e(TAG," mAudio null");
				return;
			}
			if (mAudio.getState() != AudioTrack.STATE_INITIALIZED ){
				Log.e(TAG," mAudio STATE_INITIALIZED");
				return;
			}

			if(mAudio.getPlayState() == AudioTrack.PLAYSTATE_STOPPED) {
				mAudio.play();
			}

			do{
				try {
					mAudio.write(mWaveBuf, 0, mWaveBuf.length);
				}catch (Exception e){
					Log.e(TAG,e.toString());
				}

				try {
					Thread.sleep(mInterval);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}while(mThreadExit == false);
			
			mAudio.stop();
		}
    }

    /**
     * 播放一段PCM数据
     * @param wave PCM数据
     * @param interval 间隔多长时间重复播放
     */
    public void play(byte[] wave, int interval) {
    	if(mPlayThread != null) {
    		stop();
    		mPlayThread = null;
    	} 
    	
    	mWaveBuf = wave;
    	mInterval = interval;
		mThreadExit = false;
		mPlayThread = new pwm_thread();
    	mPlayThread.start();
    }
    
    /**
     * 创建固定频率声音PCM数据
     * @param freq 播放频率
     * @param oneshortime 一次播放时间(毫秒)
     */
    public static byte[] CreatePwm(int freq, int oneshorttime) {
    	int cntfor1T = mSampleFrequency / freq;		//1周期多少个点
		int i;
		int t1 = mSampleFrequency * oneshorttime / 1000;
		byte[] wavebuf = new byte[t1];
		byte[] wavetmp = new byte[cntfor1T];
		
		for (i = 0; i < cntfor1T; i++) {
			wavetmp[i] = (byte) ((HEIGHT * 10 / 20) * Math.sin(TWOPI * (i % cntfor1T) / cntfor1T));
		}
		
		for(i = 0; i < t1; i++) {
			wavebuf[i] = wavetmp[i % cntfor1T];
		}
		
		wavetmp = null;
		
    	return wavebuf;
    }
    
    public void stop() {
    	mThreadExit = true;
    	if(mPlayThread != null) {
    		mPlayThread.interrupt();
    	}
    	
    	try {
			mPlayThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
