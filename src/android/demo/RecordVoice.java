package android.demo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.iflytek.tts.TtsService.AudioData;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

public class RecordVoice {
	private static AudioRecord mAudioRecord							= null;
	private static int bufferSizeInBytes							= 0;
	private static final int SAMPLE_RATE							= 8000;
	private static String filePrefix								= new String(android.os.Environment.getExternalStorageDirectory().getPath() + java.io.File.separator + "s4221r" + java.io.File.separator);
	private static String AudioName									= null;
	private static boolean isRecord									= false;
	private static boolean isPlay									= false;
	private static final String TAG									= "AudioRecord";
	private static int mRemainRecordSize							= 0;
	public recordCallback mrcb										= null;
	public playCallback mpcb										= null;
	private static String mplayFileName								= null;
	
	static {
		// 获得缓冲区字节大小  
        bufferSizeInBytes = AudioRecord.getMinBufferSize(SAMPLE_RATE,  
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
	}
	
	void setFilePrefix(String str) {
		filePrefix = str;
	}
	
	public interface recordCallback {
		public void recordStopCb();
	}
	
	public interface playCallback {
		public void playStopCb();
	}
	
	public void registerRecordListener(recordCallback rcb) {
		mrcb = rcb;
	}
	
	public void registerPlayListener(playCallback pcb) {
		mpcb = pcb;
	}
	
	void initRecord() {
		
	}
	
	void startRecord(int time, String fileName) {
		
		if(mAudioRecord != null) {
			return;
		}
		
		AudioName = filePrefix + fileName;
		mRemainRecordSize = (int) (((long)time * SAMPLE_RATE * 2) / 1000);
        
        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT
				,SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO 
				,AudioFormat.ENCODING_PCM_16BIT
				,bufferSizeInBytes);
        mAudioRecord.startRecording();
        isRecord = true;
        
        Log.v(TAG, "AUDIO record start");
        new Thread(new AudioRecordThread()).start();
	}
	
	void playRecord(String fileName) {
		mplayFileName = filePrefix + fileName;
		new Thread(new AudioPlayThread()).start();
	}
	
	void playFile() {
		byte[] audiodata = new byte[bufferSizeInBytes];  
        FileInputStream fos = null;  
        File file = new File(mplayFileName);  
        int len = 0;
        
        isPlay = true;
        
        if(file.exists()) {
        	try {
				fos = new FileInputStream(file);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}// 建立一个可存取字节的文件  
        	Log.v(TAG, "start play audio");
        	try {
				fos.skip(44);
				
				while(isPlay == true && (len = fos.read(audiodata)) > 0) {
					AudioData.onJniOutData(len, audiodata);
				}
				
				fos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	
        	Log.v(TAG, "stop play audio");
        }
	}
	
	void stopRecord() {
		isRecord = false;
		
		if(mAudioRecord != null) {
			mAudioRecord.stop();
	        mAudioRecord.release();
	        mAudioRecord = null;
		}
	}
	
	void stopPlay() {
		isPlay = false;
	}
	
	class AudioPlayThread implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			playFile();
			if(mpcb != null) {
				mpcb.playStopCb();
			}
		}
		
	}
	
	class AudioRecordThread implements Runnable {  
        @Override  
        public void run() {  
            writeDateTOFile();//往文件中写入裸数据  
            copyWaveFile(filePrefix + "tmp.raw", AudioName);//给裸数据加上头文件  
            if(mrcb != null) {
            	mrcb.recordStopCb();
            }
        }  
    }
	
	/** 
     * 这里将数据写入文件，但是并不能播放，因为AudioRecord获得的音频是原始的裸音频， 
     * 如果需要播放就必须加入一些格式或者编码的头信息。但是这样的好处就是你可以对音频的 裸数据进行处理，比如你要做一个爱说话的TOM 
     * 猫在这里就进行音频的处理，然后重新封装 所以说这样得到的音频比较容易做一些音频的处理。 
     */  
    private void writeDateTOFile() {  
        // new一个byte数组用来存一些字节数据，大小为缓冲区大小  
        byte[] audiodata = new byte[bufferSizeInBytes];  
        FileOutputStream fos = null;  
        int readsize = 0;
        String tmpFile = filePrefix + "tmp.raw";
        
        try {  
            File file = new File(tmpFile);  
            if (file.exists()) {  
                file.delete();  
            }  
            fos = new FileOutputStream(file);// 建立一个可存取字节的文件  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        while (isRecord == true) {  
            readsize = mAudioRecord.read(audiodata, 0, bufferSizeInBytes);
            mRemainRecordSize -= readsize;
            /*
            readsize /= 2;
            byte[] buf8bit = new byte[readsize];
            
            for(int i = 0; i < readsize; i++) {
            	buf8bit[i] = (byte) (((audiodata[i + 1] << 8) + audiodata[i]) * 256 / 65536);
            }
            */
            if (AudioRecord.ERROR_INVALID_OPERATION != readsize) {  
                try {  
                    fos.write(audiodata, 0, readsize);  
                } catch (IOException e) {  
                    e.printStackTrace();  
                }  
            }
            
            if(mRemainRecordSize <= 0) {
            	stopRecord();
            }
        }  
        try {
            fos.close();// 关闭写入流  
        } catch (IOException e) {  
            e.printStackTrace();  
        }
        
        copyWaveFile(tmpFile, AudioName);
        Log.v(TAG, "AUDIO record stop");
    }  
	
	// 这里得到可播放的音频文件  
    private void copyWaveFile(String inFilename, String outFilename) {  
        FileInputStream in = null;  
        FileOutputStream out = null;  
        long totalAudioLen = 0;  
        long totalDataLen = totalAudioLen + 36;  
        long longSampleRate = SAMPLE_RATE;  
        int channels = 1;  
        long byteRate = 16 * SAMPLE_RATE * channels / 8;  
        
        byte[] data = new byte[bufferSizeInBytes];  
        try {  
            in = new FileInputStream(inFilename);  
            out = new FileOutputStream(outFilename);  
            totalAudioLen = in.getChannel().size();  
            totalDataLen = totalAudioLen + 36;  
            WriteWaveFileHeader(out, totalAudioLen, totalDataLen,  
                    longSampleRate, channels, byteRate);  
            while (in.read(data) != -1) {  
                out.write(data);  
            }  
            in.close();  
            out.close();  
        } catch (FileNotFoundException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
    }
    
    /** 
     * 这里提供一个头信息。插入这些信息就可以得到可以播放的文件。 
     * 为我为啥插入这44个字节，这个还真没深入研究，不过你随便打开一个wav 
     * 音频的文件，可以发现前面的头文件可以说基本一样哦。每种格式的文件都有 
     * 自己特有的头文件。 
     */ 
    private void WriteWaveFileHeader(FileOutputStream out, long totalAudioLen,  
            long totalDataLen, long longSampleRate, int channels, long byteRate)  
            throws IOException {  
        byte[] header = new byte[44];  
        header[0] = 'R'; // RIFF/WAVE header  
        header[1] = 'I';  
        header[2] = 'F';  
        header[3] = 'F';  
        header[4] = (byte) (totalDataLen & 0xff);  
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);  
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);  
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);  
        header[8] = 'W';  
        header[9] = 'A';  
        header[10] = 'V';  
        header[11] = 'E';  
        header[12] = 'f'; // 'fmt ' chunk  
        header[13] = 'm';  
        header[14] = 't';  
        header[15] = ' ';  
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk  
        header[17] = 0;  
        header[18] = 0;  
        header[19] = 0;  
        header[20] = 1; // format = 1  
        header[21] = 0;  
        header[22] = (byte) channels;  
        header[23] = 0;  
        header[24] = (byte) (longSampleRate & 0xff);  
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);  
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);  
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);  
        header[28] = (byte) (byteRate & 0xff);  
        header[29] = (byte) ((byteRate >> 8) & 0xff);  
        header[30] = (byte) ((byteRate >> 16) & 0xff);  
        header[31] = (byte) ((byteRate >> 24) & 0xff);  
        header[32] = (byte) (2 * 16 / 8); // block align  
        header[33] = 0;  
        header[34] = 8; // bits per sample  
        header[35] = 0;  
        header[36] = 'd';  
        header[37] = 'a';  
        header[38] = 't';  
        header[39] = 'a';  
        header[40] = (byte) (totalAudioLen & 0xff);  
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);  
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);  
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);  
        out.write(header, 0, 44);  
    }  
}
