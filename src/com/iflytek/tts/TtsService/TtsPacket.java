package com.iflytek.tts.TtsService;

import com.iflytek.tts.TtsService.TTSControl.PlayFinishCallback;

public class TtsPacket {
	
	public static final int TTS_PLAY_LEVEL_IMMEDIATELY				= 0;
	public static final int TTS_PLAY_LEVEL_HIGH						= 1;
	public static final int TTS_PLAY_LEVEL_MID						= 2;
	public static final int TTS_PLAY_LEVEL_LOW						= 3;
	public static final int TTS_PLAY_LEVEL_IGNORE					= 4;
	
	public String ttsContent = null;
	public int playLevel = TTS_PLAY_LEVEL_IMMEDIATELY;
	public PlayFinishCallback callback = null;
	
	public TtsPacket() {
		// TODO Auto-generated constructor stub
	}
	
	public TtsPacket(String content, int level, PlayFinishCallback callback) {
		ttsContent = content;
		playLevel = level;
		this.callback = callback;
	}
	
	public void setCallback(PlayFinishCallback callback) {
		this.callback = callback;
	}
	
	public boolean setPlayLevel(int level) {
		if(level > TTS_PLAY_LEVEL_LOW || level < TTS_PLAY_LEVEL_IMMEDIATELY) {
			return false;
		}
		
		this.playLevel = level;
		return true;
	}
	
	public void setContent(String content) {
		this.ttsContent = content;
	}
	
	public void appendContent(String content) {
		ttsContent += content;
	}
	
	public void play() {
		TTSControl.GetInstance().ttsSpeak(this);
	}
}