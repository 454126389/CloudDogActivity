package com.iflytek.tts.TtsService;

import java.util.ArrayList;

import android.R.anim;
import android.content.Context;
import android.util.Log;

/**
 * 先实例化TTSControl对象，对象会调用ttsInit函数初始化Tts，后续不需要再调用ttsInit，除非对象被释放。然后只需要
 * 通过对象调用{@link ttsSpeak}(String content)播放内容。
 * @author Leven.lai
 *
 */

public final class TTSControl{

	public static final int ivTTS_ERR_OK							= 0x0000;					/* success */
	public static final int ivTTS_ERR_FAILED						= 0xFFFF;					/* failed */

	public static final int ivTTS_ERR_END_OF_INPUT					= 0x0001;					/* end of input stream */
	public static final int ivTTS_ERR_EXIT							= 0x0002;					/* exit TTS */
	public static final int ivTTS_STATE_BASE						= 0x0100;					/* state base */
	public static final int ivTTS_STATE_INVALID_DATA				= ivTTS_STATE_BASE+2;		/* invalid data */
	public static final int ivTTS_STATE_TTS_STOP					= ivTTS_STATE_BASE+3;		/* TTS stop */

	public static final int ivTTS_ERR_BASE							= 0x8000;					/* error number base */

	public static final int ivTTS_ERR_UNIMPEMENTED					= ivTTS_ERR_BASE+0;			/* unimplemented function */
	public static final int ivTTS_ERR_UNSUPPORTED					= ivTTS_ERR_BASE+1;			/* unsupported on this platform */
	public static final int ivTTS_ERR_INVALID_HANDLE				= ivTTS_ERR_BASE+2;			/* invalid handle */
	public static final int ivTTS_ERR_INVALID_PARAMETER				= ivTTS_ERR_BASE+3;			/* invalid parameter(s) */
	public static final int ivTTS_ERR_INSUFFICIENT_HEAP				= ivTTS_ERR_BASE+4;			/* insufficient heap size  */
	public static final int ivTTS_ERR_STATE_REFUSE					= ivTTS_ERR_BASE+5;			/* refuse to do in current state  */
	public static final int ivTTS_ERR_INVALID_PARAM_ID				= ivTTS_ERR_BASE+6;			/* invalid parameter ID */
	public static final int ivTTS_ERR_INVALID_PARAM_VALUE			= ivTTS_ERR_BASE+7;			/* invalid parameter value */
	public static final int ivTTS_ERR_RESOURCE						= ivTTS_ERR_BASE+8;			/* Resource is error */
	public static final int ivTTS_ERR_RESOURCE_READ					= ivTTS_ERR_BASE+9;			/* read resource error */
	public static final int ivTTS_ERR_LBENDIAN						= ivTTS_ERR_BASE+10;			/* the Endian of SDK  is error */
	public static final int ivTTS_ERR_HEADFILE						= ivTTS_ERR_BASE+11;			/* the HeadFile is different of the SDK */
	public static final int ivTTS_ERR_SIZE_EXCEED_BUFFER			= ivTTS_ERR_BASE+12;			/* get data size exceed the data buffer */
	public static final int ivTTS_ERR_RESOURCE_LICENSE				= ivTTS_ERR_BASE+13;			/* some Resources haven't license  */


	/*
	 *	INSTANCE PARAMETERS
	 */

	/* constants for values of field nParamID */
	public static final int ivTTS_PARAM_PARAMCH_CALLBACK			= 0x00000000;	/* parameter change callback entry */
	public static final int ivTTS_PARAM_LANGUAGE					= 0x00000100;	/* language, e.g. Chinese */
	public static final int ivTTS_PARAM_INPUT_CODEPAGE				= 0x00000101;	/* input code page, e.g. GBK */
	public static final int ivTTS_PARAM_TEXT_MARK					= 0x00000102;	/* text mark, e.g. CSSML */
	public static final int ivTTS_PARAM_USE_PROMPTS					= 0x00000104;	/* whether use prompts */
	public static final int ivTTS_PARAM_RECOGNIZE_PHONEME			= 0x00000105;	/* how to recognize phoneme input */
	public static final int ivTTS_PARAM_INPUT_MODE					= 0x00000200;	/* input mode, e.g. from fixed buffer, from callback */
	public static final int ivTTS_PARAM_INPUT_TEXT_BUFFER			= 0x00000201;	/* input text buffer */
	public static final int ivTTS_PARAM_INPUT_TEXT_SIZE				= 0x00000202;	/* input text size */
	public static final int ivTTS_PARAM_INPUT_CALLBACK				= 0x00000203;	/* input callback entry */
	public static final int ivTTS_PARAM_PROGRESS_BEGIN				= 0x00000204;	/* current processing position */
	public static final int ivTTS_PARAM_PROGRESS_LENGTH				= 0x00000205;	/* current processing length */
	public static final int ivTTS_PARAM_PROGRESS_CALLBACK			= 0x00000206;	/* progress callback entry */
	public static final int ivTTS_PARAM_READ_AS_NAME				= 0x00000301;	/* whether read as name */
	public static final int ivTTS_PARAM_READ_DIGIT					= 0x00000302;	/* how to read digit, e.g. read as number, read as value  */
	public static final int ivTTS_PARAM_CHINESE_NUMBER_1			= 0x00000303;	/* how to read number "1" in Chinese */
	public static final int ivTTS_PARAM_MANUAL_PROSODY				= 0x00000304;	/* whether use manual prosody */
	public static final int ivTTS_PARAM_ENGLISH_NUMBER_0			= 0x00000305;	/* how to read number "0" in Englsih */
	public static final int ivTTS_PARAM_READ_WORD           		= 0x00000306;	/* how to read word in Englsih,  e.g. read by word, read as alpha  */
	public static final int ivTTS_PARAM_OUTPUT_CALLBACK				= 0x00000401;	/* output callback entry */
	public static final int ivTTS_PARAM_ROLE						= 0x00000500;	/* speaker role */
	public static final int ivTTS_PARAM_SPEAK_STYLE					= 0x00000501;	/* speak style */
	public static final int ivTTS_PARAM_VOICE_SPEED					= 0x00000502;	/* voice speed */
	public static final int ivTTS_PARAM_VOICE_PITCH					= 0x00000503;	/* voice tone */
	public static final int ivTTS_PARAM_VOLUME						= 0x00000504;	/* volume value */
	public static final int ivTTS_PARAM_CHINESE_ROLE        		= 0x00000510;	/* Chinese speaker role */
	public static final int ivTTS_PARAM_ENGLISH_ROLE        		= 0x00000511;	/* English speaker role */
	public static final int ivTTS_PARAM_VEMODE						= 0x00000600;	/* voice effect - predefined mode */
	public static final int ivTTS_PARAM_USERMODE					= 0x00000701;	/* user's mode */
	public static final int ivTTS_PARAM_NAVIGATION_MODE				= 0x00000701;	/* Navigation Version*/

	public static final int ivTTS_PARAM_EVENT_CALLBACK				= 0x00001001;	/* sleep callback entry */
	public static final int ivTTS_PARAM_OUTPUT_BUF					= 0x00001002;	/* output buffer */
	public static final int ivTTS_PARAM_OUTPUT_BUFSIZE				= 0x00001003;	/* output buffer size */
	public static final int ivTTS_PARAM_DELAYTIME					= 0x00001004;	/* delay time */


	/* constants for values of parameter ivTTS_PARAM_LANGUAGE */
	public static final int ivTTS_LANGUAGE_AUTO             		= 0;           /* Detect language automatically */
	public static final int ivTTS_LANGUAGE_CHINESE					= 1;			/* Chinese (with English) */
	public static final int ivTTS_LANGUAGE_ENGLISH					= 2;			/* English */

	/* constants for values of parameter ivTTS_PARAM_INPUT_CODEPAGE */
	public static final int ivTTS_CODEPAGE_ASCII					= 437;			/* ASCII */
	public static final int ivTTS_CODEPAGE_GBK						= 936;			/* GBK (default) */
	public static final int ivTTS_CODEPAGE_BIG5						= 950;			/* Big5 */
	public static final int ivTTS_CODEPAGE_UTF16LE					= 1200;		/* UTF-16 little-endian */
	public static final int ivTTS_CODEPAGE_UTF16BE					= 1201;		/* UTF-16 big-endian */
	public static final int ivTTS_CODEPAGE_UTF8						= 65001;		/* UTF-8 */
	public static final int ivTTS_CODEPAGE_GB2312					= ivTTS_CODEPAGE_GBK;
	public static final int ivTTS_CODEPAGE_GB18030					= ivTTS_CODEPAGE_GBK;
	public static final int ivTTS_CODEPAGE_UTF16					= ivTTS_CODEPAGE_UTF16LE;
	public static final int ivTTS_CODEPAGE_UNICODE					= ivTTS_CODEPAGE_UTF16;
	public static final int ivTTS_CODEPAGE_PHONETIC_PLAIN			= 23456;		/* Kingsoft Phonetic Plain */

	/* constants for values of parameter ivTTS_PARAM_TEXT_MARK */
	public static final int ivTTS_TEXTMARK_NONE						= 0;			/* none */
	public static final int ivTTS_TEXTMARK_SIMPLE_TAGS				= 1;			/* simple tags (default) */

	/* constants for values of parameter ivTTS_PARAM_INPUT_MODE */
	public static final int ivTTS_INPUT_FIXED_BUFFER				= 0;			/* from fixed buffer */
	public static final int ivTTS_INPUT_CALLBACK					= 1;			/* from callback */

	/* constants for values of parameter ivTTS_PARAM_READ_DIGIT */
	public static final int ivTTS_READDIGIT_AUTO					= 0;			/* decide automatically (default) */
	public static final int ivTTS_READDIGIT_AS_NUMBER				= 1;			/* say digit as number */
	public static final int ivTTS_READDIGIT_AS_VALUE				= 2;			/* say digit as value */

	/* constants for values of parameter ivTTS_PARAM_CHINESE_NUMBER_1 */
	public static final int ivTTS_CHNUM1_READ_YAO					= 0;			/* read number "1" [yao1] in chinese (default) */
	public static final int ivTTS_CHNUM1_READ_YI					= 1;			/* read number "1" [yi1] in chinese */

	/* constants for values of parameter ivTTS_PARAM_ENGLISH_NUMBER_0 */
	public static final int ivTTS_ENNUM0_READ_ZERO					= 0;			/* read number "0" [zero] in english (default) */
	public static final int ivTTS_ENNUM0_READ_O						= 1;			/* read number "0" [o] in englsih */

	/* constants for values of parameter ivTTS_PARAM_SPEAKER */
	public static final int ivTTS_ROLE_TIANCHANG					= 1;			/* Tianchang (female, Chinese) */
	public static final int ivTTS_ROLE_WENJING						= 2;			/* Wenjing (female, Chinese) */
	public static final int ivTTS_ROLE_XIAOYAN						= 3;			/* Xiaoyan (female, Chinese) */
	public static final int ivTTS_ROLE_YANPING						= 3;			/* Xiaoyan (female, Chinese) */
	public static final int ivTTS_ROLE_XIAOFENG						= 4;			/* Xiaofeng (male, Chinese) */
	public static final int ivTTS_ROLE_YUFENG						= 4;			/* Xiaofeng (male, Chinese) */
	public static final int ivTTS_ROLE_SHERRI						= 5;			/* Sherri (female, US English) */
	public static final int ivTTS_ROLE_XIAOJIN						= 6;			/* Xiaojin (female, Chinese) */
	public static final int ivTTS_ROLE_NANNAN						= 7;			/* Nannan (child, Chinese) */
	public static final int ivTTS_ROLE_JINGER						= 8;			/* Jinger (female, Chinese) */
	public static final int ivTTS_ROLE_JIAJIA						= 9;			/* Jiajia (girl, Chinese) */
	public static final int ivTTS_ROLE_YUER							= 10;			/* Yuer (female, Chinese) */
	public static final int ivTTS_ROLE_XIAOQIAN						= 11;			/* Xiaoqian (female, Chinese Northeast) */
	public static final int ivTTS_ROLE_LAOMA						= 12;			/* Laoma (male, Chinese) */
	public static final int ivTTS_ROLE_BUSH							= 13;			/* Bush (male, US English) */
	public static final int ivTTS_ROLE_XIAORONG						= 14;			/* Xiaorong (female, Chinese Szechwan) */
	public static final int ivTTS_ROLE_XIAOMEI						= 15;			/* Xiaomei (female, Cantonese) */
	public static final int ivTTS_ROLE_ANNI							= 16;			/* Anni (female, Chinese) */
	public static final int ivTTS_ROLE_JOHN							= 17;			/* John (male, US English) */
	public static final int ivTTS_ROLE_ANITA						= 18;			/* Anita (female, British English) */
	public static final int ivTTS_ROLE_TERRY						= 19;			/* Terry (female, US English) */
	public static final int ivTTS_ROLE_CATHERINE					= 20;			/* Catherine (female, US English) */
	public static final int ivTTS_ROLE_TERRYW						= 21;			/* Terry (female, US English Word) */
	public static final int ivTTS_ROLE_XIAOLIN						= 22;			/* Xiaolin (female, Chinese) */
	public static final int ivTTS_ROLE_XIAOMENG						= 23;			/* Xiaomeng (female, Chinese) */
	public static final int ivTTS_ROLE_XIAOQIANG					= 24;			/* Xiaoqiang (male, Chinese) */
	public static final int ivTTS_ROLE_XIAOKUN						= 25;			/* XiaoKun (male, Chinese) */
	public static final int ivTTS_ROLE_JIUXU						= 51;			/* Jiu Xu (male, Chinese) */
	public static final int ivTTS_ROLE_DUOXU						= 52;			/* Duo Xu (male, Chinese) */
	public static final int ivTTS_ROLE_XIAOPING						= 53;			/* Xiaoping (female, Chinese) */
	public static final int ivTTS_ROLE_DONALDDUCK					= 54;			/* Donald Duck (male, Chinese) */
	public static final int ivTTS_ROLE_BABYXU						= 55;			/* Baby Xu (child, Chinese) */
	public static final int ivTTS_ROLE_DALONG						= 56;			/* Dalong (male, Cantonese) */
	public static final int ivTTS_ROLE_TOM							= 57;			/* Tom (male, US English) */
	public static final int ivTTS_ROLE_USER							= 99;			/* user defined */

	/* constants for values of parameter ivTTS_PARAM_SPEAK_STYLE */
	public static final int ivTTS_STYLE_PLAIN						= 0;			/* plain speak style */
	public static final int ivTTS_STYLE_NORMAL						= 1;			/* normal speak style (default) */

	/* constants for values of parameter ivTTS_PARAM_VOICE_SPEED */
	/* the range of voice speed value is from -32768 to +32767 */
	public static final int ivTTS_SPEED_MIN							= -32768;		/* slowest voice speed */
	public static final int ivTTS_SPEED_NORMAL						= 0;			/* normal voice speed (default) */
	public static final int ivTTS_SPEED_MAX							= +32767;		/* fastest voice speed */

	/* constants for values of parameter ivTTS_PARAM_VOICE_PITCH */
	/* the range of voice tone value is from -32768 to +32767 */
	public static final int ivTTS_PITCH_MIN							= -32768;		/* lowest voice tone */
	public static final int ivTTS_PITCH_NORMAL						= 0;			/* normal voice tone (default) */
	public static final int ivTTS_PITCH_MAX							= +32767;		/* highest voice tone */

	/* constants for values of parameter ivTTS_PARAM_VOLUME */
	/* the range of volume value is from -32768 to +32767 */
	public static final int ivTTS_VOLUME_MIN						= -32768;		/* minimized volume */
	public static final int ivTTS_VOLUME_NORMAL						= 0;			/* normal volume */
	public static final int ivTTS_VOLUME_MAX						= +32767;		/* maximized volume (default) */

	/* constants for values of parameter ivTTS_PARAM_VEMODE */
	public static final int ivTTS_VEMODE_NONE						= 0;			/* none */
	public static final int ivTTS_VEMODE_WANDER						= 1;			/* wander */
	public static final int ivTTS_VEMODE_ECHO						= 2;			/* echo */
	public static final int ivTTS_VEMODE_ROBERT						= 3;			/* robert */
	public static final int ivTTS_VEMODE_CHROUS						= 4;			/* chorus */
	public static final int ivTTS_VEMODE_UNDERWATER					= 5;			/* underwater */
	public static final int ivTTS_VEMODE_REVERB						= 6;			/* reverb */
	public static final int ivTTS_VEMODE_ECCENTRIC					= 7;			/* eccentric */

	/* constants for values of parameter ivTTS_PARAM_USERMODE(ivTTS_PARAM_NAVIGATION_MODE) */
	public static final int ivTTS_USE_NORMAL						= 0;			/* synthesize in the Mode of Normal */
	public static final int ivTTS_USE_NAVIGATION					= 1;			/* synthesize in the Mode of Navigation */
	public static final int ivTTS_USE_MOBILE						= 2;			/* synthesize in the Mode of Mobile */
	public static final int ivTTS_USE_EDUCATION						= 3;			/* synthesize in the Mode of Education */

	/* constants for values of parameter ivTTS_PARAM_READ_WORD */
	public static final int ivTTS_READWORD_BY_WORD					= 2;			/* say words by the way of word */
	public static final int ivTTS_READWORD_BY_ALPHA					= 1;			/* say words by the way of alpha */
	public static final int ivTTS_READWORD_BY_AUTO					= 0;			/* say words by the way of auto */

	/* constants for values of parameter nEventID */
	public static final int ivTTS_EVENT_SLEEP						= 0x0100;		/* sleep */
	public static final int ivTTS_EVENT_PLAYSTART					= 0x0101;		/* start playing */
	public static final int ivTTS_EVENT_SWITCHCONTEXT				= 0x0102;		/* context switch */

	/* constants for values of parameter wCode */
	public static final int ivTTS_CODE_PCM8K16B						= 0x0208;		/* PCM 8K 16bit */
	public static final int ivTTS_CODE_PCM11K16B					= 0x020B;		/* PCM 11K 16bit */
	public static final int ivTTS_CODE_PCM16K16B					= 0x0210;		/* PCM 16K 16bit */

	private static String TAG										= "TTSControl";
	private static String mResourceFile								= android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/ttsResource.irf";

	public int mTtsRole												= ivTTS_ROLE_XIAOYAN;
	public int mTtsVolume											= ivTTS_VOLUME_NORMAL;
	public int mTtsSpeed											= ivTTS_SPEED_NORMAL;
	public int mTtsPitch											= ivTTS_PITCH_NORMAL;
	public int mTtsLanguage											= ivTTS_LANGUAGE_CHINESE;
	public int mTtsCodePage											= ivTTS_CODEPAGE_UNICODE;
	
	private Thread mTtsSpeechThread									= null;
	private ArrayList<TtsPacket> mTtsPacketList						= new ArrayList<TtsPacket>();
	private boolean mTtsThreadManualExit							= false;
	public Context mContext											= null;
	private static TTSControl ttsCtrl								= null;
	private PlayFinishCallback mListener							= null;
	private TtsPacket mCurPacket									= null;
	
	public interface PlayFinishCallback {
		/**
		 * TTS播放完成回调函数
		 * @param status 0: normal play stop 1:stop by other operation
		 */
		void playFinish(int status);
	}
	
	public TTSControl(Context context) {
		mContext = context;
		ttsInit();
		mTtsThreadManualExit = false;
		ttsCtrl = this;
		mTtsSpeechThread = new Thread(mTtsThread);
		mTtsSpeechThread.start();
	}
	
	public static TTSControl GetInstance() {
		return ttsCtrl;
	}
	
	public final int getTtsVolume() {
		return mTtsVolume;
	}

	public final void setTtsVolume(int Volume) {
		mTtsVolume = Volume;
		ttsSetParameter(ivTTS_PARAM_VOLUME, mTtsVolume);
	}
	
	public final int getTtsRole() {
		return mTtsRole;
	}

	public final void setTtsRole(int Role) {
		mTtsRole = Role;
		ttsSetParameter(ivTTS_PARAM_ROLE, Role);
	}

	public final int getTtsSpeed() {
		return mTtsSpeed;
	}

	public final void setTtsSpeed(int Speed) {
		mTtsSpeed = Speed;
		ttsSetParameter(ivTTS_PARAM_VOICE_SPEED, Speed);
	}

	public final String getResourceFile() {
		return mResourceFile;
	}
	
	/**
	 * 设置TTS的resource文件，设置完成后需要重新创建。
	 * @param File resource文件
	 */
	public final void setResourceFile(String File) {
		mResourceFile = File;
		JniStop();
		JniDestory();
		ttsInit();
	}
	
	/**
	 * 初始化Tts服务，对各个参数进行设置
	 * @return 错误类型
	 */
	public int ttsInit() {
		JniCreate(mResourceFile);
		JniSetParam(ivTTS_PARAM_LANGUAGE, mTtsLanguage);
		JniSetParam(ivTTS_PARAM_INPUT_CODEPAGE, mTtsCodePage);
		JniSetParam(ivTTS_PARAM_ROLE, mTtsRole);
		JniSetParam(ivTTS_PARAM_VOICE_SPEED, mTtsSpeed);
		//JniSetParam(ivTTS_PARAM_VOICE_PITCH, mTtsPitch);
		JniSetParam(ivTTS_PARAM_VOLUME, mTtsVolume);
		return 0;
	}
	
	/**
	 * 对Tts各个参数设置接口。
	 * @param paramId 参数名字
	 * @param value 参数值
	 * @return 设定过程中的错误类型
	 */
	public int ttsSetParameter(int paramId,int value) {
		return JniSetParam(paramId, value);
	}
	
	public int ttsGetParameter(int paramId) {
		return JniGetParam(paramId);
	}
	
	/**
	 * 
	 * @return 返回当前Tts的版本
	 */
	public int ttsGetVersion() {
		return JniGetVersion();
	}
	
	/**
	 * ，播放完后查询等待播放列表，若有数据接着播放。
	 */
	private Runnable mTtsThread = new Runnable() {
		
		private TtsPacket getHighestLevelPacket() {
			
			if(mTtsPacketList.isEmpty()) {
				return null;
			}
			
			TtsPacket packet = null;
			TtsPacket tmpPacket = null;
			int h = 0;
			packet = mTtsPacketList.get(0);
			for(int i = 1; i < mTtsPacketList.size(); i++) {
				tmpPacket = mTtsPacketList.get(i);
				if(packet.playLevel > tmpPacket.playLevel) {
					packet = tmpPacket;
					h = i;
				}
			}
			
			return packet;
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			while(true) {
				if(mTtsThreadManualExit) {
					break;
				}
				
				synchronized (mTtsPacketList) {
					if(mTtsPacketList.isEmpty()) {
						try {
							mTtsPacketList.wait();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							break;
						}
					}
					
					mCurPacket = getHighestLevelPacket();
				}
				
				if(mCurPacket == null) {
					continue;
				}
				
				JniSpeak(mCurPacket.ttsContent);

				if(mCurPacket.callback != null) {
					mCurPacket.callback.playFinish(0);
				}
				synchronized (mTtsPacketList) {
					mTtsPacketList.remove(mCurPacket);
					mCurPacket = null;
				}				
			}
			Log.v(TAG, "tts thread exit");
		}
	};
	
	public int ttsSpeak(TtsPacket packet) {
		synchronized (mTtsPacketList) {
			if((mCurPacket != null && mCurPacket.playLevel > packet.playLevel)
					|| (packet.playLevel == TtsPacket.TTS_PLAY_LEVEL_IMMEDIATELY)) {
				JniStop();
			}
		}
		
		synchronized (mTtsPacketList) {
			
			if(packet.playLevel == TtsPacket.TTS_PLAY_LEVEL_IGNORE) {
				if(!mTtsPacketList.isEmpty()) {
					return 0;
				}
			}
			
			mTtsPacketList.add(packet);
			mTtsPacketList.notify();
		}

		return 0;
	}
	
	/**
	 * 停止当前播放
	 * @return
	 */
	public int ttsStop() {
		synchronized (mTtsPacketList) {
			mTtsPacketList.clear();
		}
		return JniStop();
	}
	
	/**
	 * 释放Tts内存
	 * @return
	 */
	public int ttsDestroy() {
		mTtsThreadManualExit = true;
		ttsStop();
		synchronized (mTtsPacketList) {
			mTtsPacketList.notify();
		}
		
		return JniDestory();
	}

	public static native int JniGetVersion();
	public static native int JniCreate(String resFilename);	
	public static native int JniDestory();	
	public static native int JniStop(); 
	public static native int JniSpeak(String text); 	
	public static native int JniSetParam(int paramId,int value);
	public static native int JniGetParam(int paramId);
	public static native int JniIsPlaying();
	public static native boolean JniIsCreated();
	
	static {
		System.loadLibrary("Aisound");	
	}
}
