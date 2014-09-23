package android.rayee.cloud;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import android.demo.CloudDogPreference;
import android.demo.DebugOperate;
import android.demo.UserPreference;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.rayee.cloud.CloudPacket.PacketListener;
import android.util.Log;

public final class CloudOperate implements PacketListener{
	private static final String TAG				= "CloudOpearte";
	private Handler mMainHandler				= null;
	private Handler mHandler					= null;

	private CloudTranslate mTranslate			= null;
	private static final int PACKET_TIMEOUT		= 15;//数据包发送超时时间，单位秒
	private static CloudOperate hInstance		= null;
	
	private static int mHeartTime				= 30;		//心跳频率，单位秒
	private boolean mbNetworkConnected			= false;	
	
	private static HashMap<Short, CloudPacket> mNeedAckMap = new HashMap<Short, CloudPacket>(16); 
	
	public static final int OPERATE_CONNECT_STATUS					= 1;
	public static final int OPERATE_RECEIVE_PACKET					= 2;

	CloudOperate(NetService net) {
		mMainHandler = net.getHandler();

		mHandler = new DataParseHandler(net.getLooper());
		mTranslate = new CloudTranslate(this);
		hInstance = this;
	}
	
	public static CloudOperate getInstance() {
		return hInstance;
	}
	public Handler getHandler() {
		return mHandler;
	}
	
	class DataParseHandler extends Handler {

		DataParseHandler(Looper looper) {
			super(looper);
		}
		
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case OPERATE_CONNECT_STATUS:
				mMainHandler.sendMessage(mMainHandler.obtainMessage(NetService.EVENT_SERVICE_NETWORK_CONNECT_STATUS, msg.arg1, msg.arg2));
				break;
				
			case OPERATE_RECEIVE_PACKET:
				//Log.v(TAG, "receive packet");
				TerminalParseReceivedPacket((CloudPacket)msg.obj);
				break;
			}
		}
		
	}
	
	public boolean connect() {
		if(mTranslate == null) {
			mTranslate = new CloudTranslate(this);
		}

		return mTranslate.start();
	}
	
	/**
	 * 停止网络操作，并把相关资源释放
	 * @throws IOException
	 */
	void close() {
		if(mTranslate != null) {
			mTranslate.close();
			mTranslate = null;
		}
		Stop();
	}
	
	/**
	 * 发送数据包给服务器
	 * @param packet 需要发送的数据包
	 * @param bRetry 如果发送失败，是否重发。如果该调用返回false，则重发机制无效
	 * @return true表示已经添加到发送队列，false表示发送失败或者网络未连接
	 */
	public boolean SendPacket(CloudPacket packet, boolean bRetry){
		boolean ret = false;
		if(!mbNetworkConnected) {
			return false;
		}
		
		if(mTranslate == null) {
			return false;
		}
		
		try {
			ret = mTranslate.ClientSendPacket(packet);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		if(ret && bRetry) {
			
			if(packet.GetPacketCmd() == CloudPacket.ACK_CMD) {
				return ret;
			}
			
			packet.mPacketWaitTime = PACKET_TIMEOUT;
			
			synchronized(mPacketTimeOut) {
				if(mbNetworkConnected && mNeedAckMap.isEmpty()) {
					mMainHandler.postDelayed(mPacketTimeOut, 1000);
				}
				mNeedAckMap.put(packet.GetPacketId(), packet);
			}
		}
		return ret;
	}
	
	private Runnable mPacketTimeOut = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			synchronized(mNeedAckMap) {
				//Log.v(TAG, "total " + mNeedAckMap.size());
				ArrayList<Object> listDel = new ArrayList<Object>();
				for(Iterator<Short> it =  mNeedAckMap.keySet().iterator();it.hasNext();){
					Object key = it.next();
					CloudPacket packet = mNeedAckMap.get(key);
					//Log.v(TAG, String.valueOf(packet.mPacketWaitTime));
					if((--packet.mPacketWaitTime) == 0) {
						packet.mPacketWaitTime = PACKET_TIMEOUT;
						if((--packet.mPacketRetryTimer) == 0) {
							//Log.v(TAG, new String().valueOf(key) + " fail");
							if(packet.getListener() != null) {
								packet.getListener().onReceive(packet);
							}
							//mNeedAckMap.remove(key);
							listDel.add(key);
						} else {
							//不要再次添加到监控队列
							Log.v(TAG, "retry send commnd " + Integer.toHexString(packet.GetPacketCmd()) + ":" + Integer.toHexString((int)(Short)key) + ","+ packet.mPacketRetryTimer);
							SendPacket(packet, false);
						}
					}
				}
				
				for(int i = 0; i < listDel.size(); i++) {
					mNeedAckMap.remove(listDel.get(i));
				}
			
				if(mbNetworkConnected && !mNeedAckMap.isEmpty()) {
					//mHandler.removeCallbacks(this);
					mMainHandler.postDelayed(this, 1000);
				}
			}
			//Log.v(TAG, "check time out thread exit");
		}
		
	};
	
	private Runnable mHeartRunnable = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			synchronized(mHeartRunnable) {
				if(mbNetworkConnected) {
					CloudPacket packet = CloudPacket.TerminalHeartPacket();
					packet.setListener(CloudOperate.this);
					SendPacket(packet, true);
					//mMainHandler.postDelayed(mHeartRunnable, mHeartTime*1000);
				}
			}
		}
	};
	
	private Runnable mLoginRunnable = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			synchronized (mLoginRunnable) {
				if(mbNetworkConnected) {
					sendLoginCommand();
				}
			}
		}
	};
	
	private PacketListener mListener = new PacketListener() {

		@Override
		public void onReceive(CloudPacket packet) {
			// TODO Auto-generated method stub
			short cmd, idCode;
			cmd = packet.GetPacketCmd();
			idCode = packet.GetPacketId();
			
			if(packet.getPacketDirection() == CloudPacket.PACKET_DEVICE_TO_CLOUD) {
				//send packet fail
				switch(cmd) {
				case CloudPacket.LOGIN_CMD:
					//continue login after 5minutes
					Log.v(TAG, "login command send fail, retry after 5 minutes");
					mMainHandler.postDelayed(mLoginRunnable, 5 * 60000);
					break;
				}
				
				return;
			}
			
			switch(cmd) {
			case CloudPacket.ACK_CMD:
				switch(packet.getShort()) {
				case CloudPacket.LOGIN_CMD:
					byte status = packet.getByte();
					NotifyToService(NetService.EVENT_LOGIN_STATUS, status);
					if(status == 1) {
						CloudPacket new_packet = CloudPacket.TerminalReportMessageSwitch(UserPreference.getBroadcastSwitch());
						new_packet.setListener(this);
						SendPacket(new_packet, true);
						StringBuilder str = new StringBuilder();
						str.append(CloudDogPreference.PERFERENCE_SGPS_NAME + ";");
						str.append(CloudDogPreference.PERFERENCE_FSNU_NAME + ";");
						str.append(CloudDogPreference.PERFERENCE_RMAP_NAME + ";");
						str.append(CloudDogPreference.PERFERENCE_TR_T_NAME + ";");
						str.append(CloudDogPreference.PERFERENCE_DEFE_NAME + ";");
						str.append(CloudDogPreference.PERFERENCE_SPRU_NAME + ";");
						//str.append(CloudDogPreference.PERFERENCE_IP_T_NAME + ";");
						//str.append(CloudDogPreference.PERFERENCE_IP_U_NAME + ";");
						str.append(CloudDogPreference.PERFERENCE_PKEY_NAME + ";");
						//str.append(CloudDogPreference.PERFERENCE_FULL_NAME + ";");
						new_packet = CloudPacket.TerminalRequestParameterPacket(str.toString());
						new_packet.setListener(this);
						SendPacket(new_packet, true);
					}
					break;
				}
				break;
				
			case CloudPacket.TERMINAL_PARA_REQ_CMD:
				String parString = new String(packet.getRemain());
				Log.i(TAG, parString);
				setDeviceParameter(parString);
				SendPacket(CloudPacket.TerminalAnswerPacket(cmd, idCode), false);
				break;
			}
		}
		
	};
	
	private String[] parameterParse(String par) {
		int index = par.indexOf(":");
		String sub = par.substring(index + 1);
		String[] val = sub.split(",");
		
		return val;
	}
	
	private void setDeviceParameter(String value) {
		String[] values = value.split(";");
		String[] par_value = null;
		
		for(String par : values) {
			par_value = parameterParse(par);
			if(par.indexOf(CloudDogPreference.PERFERENCE_SGPS_NAME) >= 0) {
				CloudDogPreference.mbUploadLocation  = (par_value[0].equals("1")) ? true : false;
				CloudDogPreference.mNormalPositionReportInterval = Integer.parseInt(par_value[1]);
				CloudDogPreference.mParkPositionReportInterval = Integer.parseInt(par_value[2]);
			} else if(par.indexOf(CloudDogPreference.PERFERENCE_FSNU_NAME) >= 0) {
				CloudDogPreference.mMasterPhoneNumber = par_value[0];
				CloudDogPreference.mSecondPhoneNumber = par_value[1];
			} else if(par.indexOf(CloudDogPreference.PERFERENCE_RMAP_NAME) >= 0) {
				CloudDogPreference.mUpdateRadius = Integer.parseInt(par_value[0]);
			} else if(par.indexOf(CloudDogPreference.PERFERENCE_TR_T_NAME) >= 0) {
				CloudDogPreference.mRoadStatusReportInterval = Integer.parseInt(par_value[0]) * 60;
			} else if(par.indexOf(CloudDogPreference.PERFERENCE_DEFE_NAME) >= 0) {
				CloudDogPreference.mDefendLevel = Integer.parseInt(par_value[0]);
			} else if(par.indexOf(CloudDogPreference.PERFERENCE_SPRU_NAME) >= 0) {
				CloudDogPreference.mbEnableUploadSelfPointRecord = (par_value[0].equals("1")) ? true : false;
			} else if(par.indexOf(CloudDogPreference.PERFERENCE_IP_T_NAME) >= 0) {
				String[] ip_port = par_value[0].split(":");
				CloudDogPreference.ServerIP = ip_port[0];
				CloudDogPreference.ServerTCPPort = Integer.parseInt(ip_port[1]);
			} else if(par.indexOf(CloudDogPreference.PERFERENCE_IP_U_NAME) >= 0) {
				String[] ip_port = par_value[0].split(":");
				CloudDogPreference.ServerIP = ip_port[0];
				CloudDogPreference.ServerUDPPort = Integer.parseInt(ip_port[1]);
			} else if(par.indexOf(CloudDogPreference.PERFERENCE_PKEY_NAME) >= 0) {
				int i = 0;
				byte[] p = par_value[0].getBytes();
				String hex = "0123456789ABCDEF";
				for(i = 0; i < p.length; i++) {
					CloudDogPreference.mUpdateNewKey[i / 2] = (byte) hex.indexOf(p[i]);
					CloudDogPreference.mUpdateNewKey[i / 2] = (byte) ((CloudDogPreference.mUpdateNewKey[i / 2] << 4) | (hex.indexOf(p[i + 1]) & 0xFF));
					i++;
				}
				Log.v(TAG, "receive update key " + DebugOperate.ByteBufferConvertToString(CloudDogPreference.mUpdateNewKey, ','));
			}
		}
	}
	
	public final void Start() {
		//delay 10s start heart thread
		mbNetworkConnected = true;
		mMainHandler.postDelayed(mHeartRunnable, 10 * 1000);

		//mtParseThread = new Thread(mParseReadData);
		//mtParseThread.start();
		
		sendLoginCommand();

		Log.v(TAG, "Operate Start");
	}

	private void sendLoginCommand() {
		CloudPacket packet = CloudPacket.TerminalLoginPacket();
		packet.setListener(mListener);
		SendPacket(packet, true);
	}
	
	public final void Stop() {
		
		if(mbNetworkConnected == false) {
			return;
		}
		
		mbNetworkConnected = false;
		synchronized(mHeartRunnable) {
			mMainHandler.removeCallbacks(mHeartRunnable);
		}
		synchronized(mNeedAckMap){
			mMainHandler.removeCallbacks(mPacketTimeOut);
		}
		
		synchronized (mLoginRunnable) {
			mMainHandler.removeCallbacks(mLoginRunnable);
		}
		
		synchronized(mNeedAckMap){
			mNeedAckMap.clear();
		}
		
		Log.v(TAG, "Operate Stop");
	}

	
	/**
	 * 解析服务器发送过来的包
	 * @param buf 完整的数据包
	 */
	public final void TerminalParseReceivedPacket(CloudPacket packet) {
		short cmd = packet.GetPacketCmd();
		String strMsg = null;
		boolean bNeedAnswer = true;
		short ackId = packet.GetPacketId();
		
		Log.v(TAG, "receive packet cmd " + Integer.toHexString((int)(cmd & 0xFFFF)).toUpperCase() + ", " + Integer.toHexString(ackId));
		
		synchronized(mNeedAckMap) {
			if(mNeedAckMap.containsKey(ackId)) {
				CloudPacket oldCloudPacket = mNeedAckMap.remove(ackId);
				if(mNeedAckMap.isEmpty()) {
					mMainHandler.removeCallbacks(mPacketTimeOut);
				}
				if(oldCloudPacket.getListener() != null) {
					oldCloudPacket.getListener().onReceive(packet);
					return;
				}
			}
		}
		
		
		
		switch(cmd) {
			case CloudPacket.ACK_CMD: {
				short ackCmd = packet.getShort();
				Log.v(TAG, "received ack, cmd " + Integer.toHexString(ackCmd));
				bNeedAnswer = false;
				break;
			}
			
			case CloudPacket.BROADCAST_CMD:
				strMsg = packet.getString();
				NotifyToService(NetService.EVENT_BROAD_MESSAGE, strMsg);
				break;
			
			case CloudPacket.LOCATION_CMD:
				strMsg = packet.getString();
				NotifyToService(NetService.EVENT_BROAD_MESSAGE, strMsg);
				break;
				
			case CloudPacket.TERMINAL_PARA_SET_CMD:
				strMsg = packet.getString();
				Log.v(TAG, strMsg);
				setDeviceParameter(strMsg);
				break;
			
			default:
				break;
		}
		
		if( bNeedAnswer ) {
			SendPacket(CloudPacket.TerminalAnswerPacket(cmd, ackId), false);
		}
	}
	
	/**
	 * 发送消息到Service进程
	 * @param what
	 * @param arg1
	 * @param arg2
	 * @param obj
	 */
	public final void NotifyToService(int what, int arg1, int arg2, Object obj){
		Message msg = mMainHandler.obtainMessage(what, arg1, arg2, obj);
		mMainHandler.sendMessage(msg);
	}
	
	public final void NotifyToService(int what) {
		Message msg = mMainHandler.obtainMessage(what);
		mMainHandler.sendMessage(msg);
	}
	
	public final void NotifyToService(int what, int arg1, int arg2) {
		Message msg = mMainHandler.obtainMessage(what, arg1, arg2);
		mMainHandler.sendMessage(msg);
	}
	
	public final void NotifyToService(int what, int arg1) {
		Message msg = mMainHandler.obtainMessage(what, arg1, 0);
		mMainHandler.sendMessage(msg);
	}
	
	public final void NotifyToService(int what, Object obj) {
		Message msg = mMainHandler.obtainMessage(what, obj);
		mMainHandler.sendMessage(msg);
	}
	
	public final void setUpdateKey(byte[] key) {
		CloudPacket packet = CloudPacket.TerminalReportUpdateKey(CloudDogPreference.mUpdateOldKey, key);
		packet.setListener(this);
		packet.send(true);
	}

	@Override
	public void onReceive(CloudPacket packet) {
		// TODO Auto-generated method stub
		if(packet.mPacketDirection == CloudPacket.PACKET_DEVICE_TO_CLOUD) {
			if(packet.GetPacketCmd() == CloudPacket.HEART_CMD) {
				Log.e(TAG, "Heart command send fail, retry 10s later");
				mHandler.postDelayed(mHeartRunnable, 10 * 1000);
			}
			return;
		}
		
		if(packet.GetPacketCmd() == CloudPacket.ACK_CMD) {
			short cmd = packet.getShort();
			if(cmd ==  CloudPacket.KEY_MODIFY_REPORT_CMD) {
				Log.v(TAG, "update key set success");
			} else if(cmd == CloudPacket.HEART_CMD) {
				mHandler.postDelayed(mHeartRunnable, mHeartTime * 1000);
			}
		}
	}
}
