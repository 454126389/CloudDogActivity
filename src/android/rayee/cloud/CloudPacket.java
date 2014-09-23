package android.rayee.cloud;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import android.demo.CloudDogPreference;
import android.demo.IGpsData;
import android.demo.UserPreference;

/**
 * 通常数据包由{@link CloudPacket}来产生，调用{@link CloudTranslate}去传输到服务器。
 * @author Leven.lai
 * @version 0.1
 * 
 */
public final class CloudPacket {
	
	public interface PacketListener {
		void onReceive(CloudPacket packet);
	}

	@SuppressWarnings("unused")
	private static final String TAG					= "CloudPacket";
	public static final int PACKET_UDP_FLAG			= 0x00000;
	public static final int PACKET_TCP_FLAG			= 0x10000;
	public static final int PACKET_CMD_MASK			= 0xFFFF;
	
	public static final int PACKET_LENGTH_POS		= 0x05;
	
	
	public int mPacketWaitTime						= 20;		//默认20秒超时
	public int mPacketRetryTimer					= 4;		//默认只发4次
	
	public int mPacketTranslateFlag					= PACKET_UDP_FLAG;
	
	public static final int PACKET_DEVICE_TO_CLOUD	= 0;
	public static final int PACKET_CLOUD_TO_DEVICE	= 1;
	public int mPacketDirection						= PACKET_DEVICE_TO_CLOUD;
	
	private PacketListener mListener				= null;
	
	public final void setListener(PacketListener listener) {
		mListener = listener;
	}
	
	public final PacketListener getListener() {
		return mListener;
	}
	
	public int getPacketTranslateFlag() {
		return mPacketTranslateFlag;
	}

	public int getPacketDirection() {
		return mPacketDirection;
	}

	public void setPacketTranslateFlag(int flag) {
		this.mPacketTranslateFlag = flag;
	}

	public void setPacketDirection(int direction) {
		this.mPacketDirection = direction;
	}

	/**
	 * 服务器发送数据后，终端返回该指令
	 */
	public static final short ACK_CMD					= (short) 0xA001;
	/**
	 * 心跳指令，{@link HeartPacket}
	 */
	public static final short HEART_CMD 				= 0x1001;
	/**
	 * 登录指令，{@link LoginPacket}
	 */
	public static final short LOGIN_CMD 				= 0x1002;
	/**
	 * GPS数据发送指令
	 */
	public static final short GPS_DATA_CMD				= 0x1003;
	public static final short SMS0_CMD					= 0x1004;
	public static final short SMS1_CMD					= 0x1005;
	public static final short SERVER_REQUEST_PARA_CMD	= 0x1006;
	public static final short TERMINAL_PARA_REQ_CMD		= 0x1007;
	public static final short TERMINAL_PARA_SET_CMD		= 0x1008;
	public static final short WEATHER_CMD				= 0x1009;
	public static final short WARNING_CMD				= 0x100A;
	public static final short SERVER_GET_IMSI_CMD		= 0x100B;
	public static final short ROAD_INFO_CMD				= 0x100C;
	public static final short LOCATION_CMD				= 0x100D;
	public static final short SIM_INFO_CMD				= 0x100E;
	public static final short MESSAGE_SWITCH_CMD		= 0x100F;
	public static final short SIM_CHANGE_CMD			= 0x1010;
	public static final short SELFPOINT_REC_REQUEST_CMD	= 0x1011;
	public static final short SELFPOINT_REC_UPLOAD_CMD	= 0x1012;
	public static final short PASSWORD_SET_CMD			= 0x1013;
	
	public static final short BROADCAST_CMD				= 0x2001;
	public static final short SELFPOINT_UPLOAD_CMD		= 0x2002;
	public static final short UPDATE_REQUEST_CMD		= 0x2003;
	public static final short UPDATE_REQUEST_DATA_CMD	= 0x2004;
	public static final short KEY_MODIFY_REPORT_CMD		= 0x2005;
	
	//allocate the bytebuffer capacity
	private static final int PACKET_CAPACITY			= 2048;
	private static Integer mPacketID						= 0x0001;
	
	private ByteBuffer mPacketBuf						= null;
	
	CloudPacket() {
		mPacketBuf = ByteBuffer.allocate(PACKET_CAPACITY);
	}
	
	CloudPacket(byte[] buf) {
		mPacketBuf = ByteBuffer.wrap(buf);
		mPacketDirection = PACKET_CLOUD_TO_DEVICE;
		mPacketBuf.position(9);
	}
	
	CloudPacket(ByteBuffer buf) {
		mPacketBuf = buf;
		mPacketDirection = PACKET_CLOUD_TO_DEVICE;
		mPacketBuf.position(9);
	}
	
	public static void MakePacketHeader(ByteBuffer buf, int cmd) {
		int length = 0;

		buf.put("#RY".getBytes());
		buf.put((byte)(cmd >> 8));
		buf.put((byte)cmd);
		buf.put((byte)(length >> 8));
		buf.put((byte)length);
		synchronized (mPacketID) {
			buf.put((byte)(mPacketID >> 8));
			buf.put((byte)(int)mPacketID);
			mPacketID++;
		}
		buf.putLong(UserPreference.mDeviceSn);
		
	}
	
	public static void MakePacketHeader(ByteBuffer buf, int cmd, short ID) {
		int length = 0;
		
		buf.put("#RY".getBytes());
		buf.putShort((short)cmd);
		buf.putShort((short)length);
		buf.putShort(ID);
		buf.putLong(UserPreference.mDeviceSn);
	}
	
	public static void MakePacketTail(ByteBuffer buf) {
		int length;
		buf.put("$$".getBytes());
		length = buf.position() + 0x8000;
		buf.put(PACKET_LENGTH_POS, (byte)(length >> 8));
		buf.put(PACKET_LENGTH_POS + 1, (byte)(length));
		buf.flip();
	}
	
	//use to heart command
	public CloudPacket(int cmd) {
		this();
		MakePacketHeader(mPacketBuf, cmd);
		MakePacketTail(mPacketBuf);
	}
	
	//短信和获取IMEI、参数获取
	public CloudPacket(int cmd, String IMSI) {
		this();
		MakePacketHeader(mPacketBuf, cmd);
		mPacketBuf.put(IMSI.getBytes());
		MakePacketTail(mPacketBuf);
	}
	
	//用于长参数
	public CloudPacket(int cmd, ByteBuffer extra) {
		this();
		MakePacketHeader(mPacketBuf, cmd);
		mPacketBuf.put(extra);
		MakePacketTail(mPacketBuf);
	}
	
	/**
	 * 在传输数据的时候，该接口必须在{@linkplain GetPacketBuffer}之前调用，否则数据包中前4个字节会被当成数据传输出去。该4个字节用来表示该包的传输方式。
	 * @return 当前数据包的传输方式，true表示用TCP传输，false表示用UDP传输
	 */
	public boolean IsPacketTCP() {
		if( mPacketTranslateFlag == PACKET_TCP_FLAG ) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * 新建CloudPacket实例后，可以调用该接口获取数据包的buffer，供外部修改
	 * @return 数据包{@link CloudPacket}的存储{@link ByteBuffer}
	 */
	public ByteBuffer GetPacketBuffer() {
		return mPacketBuf;
	}
	
	/**
	 * 产生一个应答包，需要手动发送给服务器。指令{@linkplain ACK_CMD}
	 * @param cmd 要应答的命令
	 * @param idCode TODO
	 * @return 数据包{@link CloudPacket}
	 */
	public final static CloudPacket TerminalAnswerPacket(int cmd, short idCode) {
		CloudPacket packet = new CloudPacket();
		ByteBuffer buf = packet.GetPacketBuffer();
		MakePacketHeader(buf, ACK_CMD, idCode);
		buf.putShort((short)cmd);
		MakePacketTail(buf);
		packet.mPacketTranslateFlag = PACKET_TCP_FLAG;
		return packet;
	}
	
	/**
	 * 产生一个登录包，需要手动发送给服务器
	 * @return 数据包{@link CloudPacket}
	 */
	public final static CloudPacket TerminalLoginPacket() {
		CloudPacket packet = new CloudPacket();
		byte[] reserve = new byte[15];
		ByteBuffer buf = packet.GetPacketBuffer();
		
		MakePacketHeader(buf, LOGIN_CMD);
		buf.putInt(UserPreference.mSoftwareVersion);
		buf.putInt(CloudDogPreference.mBaseDatabaseVersion);
		buf.put(reserve);
		buf.put(UserPreference.mSimIMSI.getBytes());
		MakePacketTail(buf);
		
		packet.mPacketTranslateFlag = PACKET_UDP_FLAG;
		
		return packet;
	}
	
	/**
	 * 返回一个心跳包，需要手动发送给服务器
	 * @return 数据包{@link CloudPacket}
	 */
	public final static CloudPacket TerminalHeartPacket() {
		CloudPacket packet = new CloudPacket(HEART_CMD);
		packet.mPacketTranslateFlag = PACKET_TCP_FLAG;
		packet.mPacketDirection = PACKET_DEVICE_TO_CLOUD;
		return packet;
	}
	
	/**
	 * 返回一个GPS数据包，需要手动发送给服务器
	 * @param flag 0:轨迹，1:路况
	 * @param gps GPS数据
	 * @param limitspeed 限速
	 * @param overspeed 超速
	 * @param mile 里程
	 * @param status 状态
	 * @param gpsstrength SIM信号强度
	 * @return
	 */
	public final static CloudPacket GPSDataPacket(byte flag, IGpsData gps,
			byte limitspeed,  byte overspeed, int mile, short status, byte gpsstrength) {
		
		CloudPacket packet = new CloudPacket();
		ByteBuffer buf = packet.GetPacketBuffer();
		
		MakePacketHeader(buf, GPS_DATA_CMD);
		buf.put(flag);
		buf.putInt((int)(gps.mLatitude * 10000));
		buf.putInt((int)(gps.mLongtitude * 10000));
		buf.putShort((short)(gps.mSpeed * 10));
		buf.put((byte)(gps.mBearing * 2));
		buf.put((byte)gps.GetYear());
		buf.put((byte)gps.GetMonth());
		buf.put((byte)gps.GetDay());
		buf.put((byte)gps.GetHour());
		buf.put((byte)gps.GetMinute());
		buf.put((byte)gps.GetSecond());
		buf.put(limitspeed);
		buf.put(overspeed);
		buf.putInt(mile);
		buf.putShort(status);
		buf.put(gpsstrength);
		buf.putInt((byte)(gps.mAltitude * 10));
		MakePacketTail(buf);
		
		return packet;
	}
	
	/**
	 * 返回一个短信包， 命令为{@link SMS0_CMD}或者{@link SMS1_CMD}
	 * @param flag 0表示用主号发送，1表示用副号发送
	 * @param MsgContent 类型{@link String}，要发送的短信内容
	 * @return 数据包{@link CloudPacket}
	 */
	public final static CloudPacket SMSPacket(int flag/*0:主号，1:副号*/, String MsgContent) {
		CloudPacket packet = new CloudPacket();//
		int cmd;
		ByteBuffer buf = packet.GetPacketBuffer();
		
		if(flag == 0) {
			cmd = SMS0_CMD;
		} else {
			cmd = SMS1_CMD;
		}
		
		MakePacketHeader(buf, cmd);
		try {
			buf.put(MsgContent.getBytes("GBK"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			buf.put(MsgContent.getBytes());
			e.printStackTrace();
		}
		MakePacketTail(buf);
		
		return packet;
	}
	
	/**
	 * 终端参数返回到服务器，命令为{@linkplain SERVER_REQUEST_PARA_CMD}，由服务器先发请求后，终端把该数据包返回给服务器
	 * @param Para 类型为{@link String}，服务器需求的参数值。多个参数之前用分号隔开，每个参数由四个字母参数名:值1,值2,…;组成。如“SGPS:1,3,600;FSNU:18688886666,13755559999;
	 * @return 返回数据包
	 */
	public final static CloudPacket ServerRequestParameterPacket(String Para) {
		CloudPacket packet = new CloudPacket();
		ByteBuffer buf = packet.GetPacketBuffer();
		
		MakePacketHeader(buf, SERVER_REQUEST_PARA_CMD);
		buf.put(Para.getBytes());
		MakePacketTail(buf);
		
		return packet;
	}
	
	/**
	 * 终端向服务器请求参数，指令{@link TERMINAL_PARA_REQ_CMD}
	 * @param para 终端请求的参数，类型{@link String}。FULL示所有参数，参数名之间用分号隔开，如SGPS；FSNU；具体说明可参见“终端参数说明”
	 * @return 返回数据包
	 */
	public final static CloudPacket TerminalRequestParameterPacket(String para) {
		CloudPacket packet = new CloudPacket();
		ByteBuffer buf = packet.GetPacketBuffer();
		
		MakePacketHeader(buf, TERMINAL_PARA_REQ_CMD);
		try {
			buf.put(para.getBytes("GBK"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			buf.put(para.getBytes());
			e.printStackTrace();
		}
		MakePacketTail(buf);
		
		return packet;
	}
	
	/**
	 * 终端向服务器请求当前位置的天气情况, {@link WEATHER_CMD}
	 * @param latitude 当前位置的纬度
	 * @param longitude 当前位置的经度
	 * @return 数据包
	 */
	public final static CloudPacket TerminalRequestWeathePacket(int latitude, int longitude) {
		CloudPacket packet = new CloudPacket();
		ByteBuffer buf = packet.GetPacketBuffer();
		
		MakePacketHeader(buf, WEATHER_CMD);
		buf.putInt(latitude);
		buf.putInt(longitude);
		MakePacketTail(buf);
		
		return packet;
	}
	
	/**
	 * 终端告知服务器非法状态,{@link WARNING_CMD}
	 * @param gps 当前GPS数据
	 * @param gpsstrength SIM强度
	 * @param VoltagePercent 电池电压百分比(1~100)
	 * @return 数据包
	 */
	public final static CloudPacket TerminalReportWarning(IGpsData gps, byte gpsstrength, byte VoltagePercent) {
		CloudPacket packet = new CloudPacket();
		ByteBuffer buf = packet.GetPacketBuffer();
		
		MakePacketHeader(buf, WARNING_CMD);
		buf.putInt((int)(gps.mLatitude * 10000));
		buf.putInt((int)(gps.mLongtitude * 10000));
		buf.putShort((short)(gps.mSpeed * 10));
		buf.put(gpsstrength);
		buf.put(VoltagePercent);
		MakePacketTail(buf);
		return packet;
	}
	
	/**
	 * 终端告知服务器IMSI串(15 bytes)，{@link SERVER_GET_IMSI_CMD}
	 * @param str IMSI字符串，若为空，则Fill zero
	 * @return 数据包
	 */
	public final static CloudPacket TerminalReportIMSI(String str) {
		CloudPacket packet = new CloudPacket();
		ByteBuffer buf = packet.GetPacketBuffer();
		int i;
		byte[] pStr = null;
		
		MakePacketHeader(buf, SERVER_GET_IMSI_CMD);
		try {
			pStr = str.getBytes("GBK");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			pStr = str.getBytes();
			e.printStackTrace();
		}
		i = pStr.length;
		if(i < 15) {
			buf.put(pStr);
			pStr = new byte[15 - i];
		}
		
		buf.put(pStr);
		MakePacketTail(buf);
		return packet;
	}
	
	/**
	 * 终端请求当前GPS数据所在路况，{@link ROAD_STATUS_CMD}
	 * @param gps 当前GPS数据
	 * @return 数据包
	 */
	public final static CloudPacket TerminalRequestRoadInfo(int latitude, int longitude) {
		CloudPacket packet = new CloudPacket();
		ByteBuffer buf = packet.GetPacketBuffer();
		
		MakePacketHeader(buf, ROAD_INFO_CMD);
		buf.putInt(latitude);
		buf.putInt(longitude);
		MakePacketTail(buf);
		
		return packet;
	}
	
	/**
	 * 终端请求获取当前位置, {@link LOCATION_CMD}
	 * @return 数据包
	 */
	public final static CloudPacket TerminalRequestLocation(int latitude, int longitude) {
		CloudPacket packet = new CloudPacket();
		ByteBuffer buf = packet.GetPacketBuffer();
		
		MakePacketHeader(buf, LOCATION_CMD);
		buf.putInt(latitude);
		buf.putInt(longitude);
		MakePacketTail(buf);
		return packet;
	}
	
	/**
	 * 终端请求SIM卡信息,{@link SIM_INFO_CMD}
	 * @param IMSI串，15bit，不足以0填充
	 * @return 数据包
	 */
	public final static CloudPacket TerminalRequestSIMInfo(String IMSI) {
		CloudPacket packet = new CloudPacket();
		ByteBuffer buf = packet.GetPacketBuffer();
		int i;
		byte[] pStr = null;
		
		MakePacketHeader(buf, SIM_INFO_CMD);
		try {
			pStr = IMSI.getBytes("GBK");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			pStr = IMSI.getBytes();
			e.printStackTrace();
		}
		i = pStr.length;
		if(i < 15) {
			buf.put(pStr);
			pStr = new byte[15 - i];
		}
		
		buf.put(pStr);
		MakePacketTail(buf);
		return packet;
	}
	
	/**
	 * 终端设置服务器消息状态，未设置部分服务器不去推送, {@link MESSAGE_SWITCH_CMD}
	 * @param flag 32bit, 一位表示一个消息开关，0表示关，1表示开
	 * @return 数据包
	 */
	public final static CloudPacket TerminalReportMessageSwitch(int flag) {
		CloudPacket packet = new CloudPacket();
		ByteBuffer buf = packet.GetPacketBuffer();
		
		MakePacketHeader(buf, MESSAGE_SWITCH_CMD);
		buf.putInt(flag);
		MakePacketTail(buf);
		
		return packet;
	}
	
	/**
	 * 终端告知服务器更换SIM卡, {@link SIM_CHANGE_CMD}
	 * @param oldIMSI 旧SIM卡IMSI串，15bit，不足以0填充
	 * @param newIMSI 新SIM卡IMSI串，15bit，不足以0填充
	 * @return 数据包
	 */
	public final static CloudPacket TerminalReportSIMChange(String oldIMSI, String newIMSI) {
		CloudPacket packet = new CloudPacket();
		ByteBuffer buf = packet.GetPacketBuffer();
		byte[] pIMSI = null;
		
		MakePacketHeader(buf, SIM_CHANGE_CMD);
		try {
			pIMSI = oldIMSI.getBytes("GBK");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			pIMSI = oldIMSI.getBytes();
			e.printStackTrace();
		}
		if(pIMSI.length < 15) {
			buf.put(pIMSI);
			pIMSI = new byte[15 - pIMSI.length];
		}
		buf.put(pIMSI);
		
		try {
			pIMSI = newIMSI.getBytes("GBK");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			pIMSI = newIMSI.getBytes();
			e.printStackTrace();
		}
		if(pIMSI.length < 15) {
			buf.put(pIMSI);
			pIMSI = new byte[15 - pIMSI.length];
		}
		buf.put(pIMSI);
		MakePacketTail(buf);
		
		return packet;
	}
	
	/**
	 * 终端请求投诉点录音上传，{@link COMPLAIN_REC_REQUEST_CMD}
	 * @param point 16字节，自建点的信息
	 * @param filesize 文件大小
	 * @param totalPacket 该文件传送需要的总包
	 * @return  数据包
	 */
	public final static CloudPacket TerminalRequestSelfPointUploadRecord(byte[] point, int filesize, short totalPacket){
		CloudPacket packet = new CloudPacket();
		ByteBuffer buf = packet.GetPacketBuffer();
		
		MakePacketHeader(buf, SELFPOINT_REC_REQUEST_CMD);
		buf.put(point);
		buf.putInt(filesize);
		buf.putShort(totalPacket);
		MakePacketTail(buf);
		
		return packet;
	}
	
	/**
	 * 终端上报投诉录音点数据，{@link COMPLAIN_REC_UPLOAD_CMD}
	 * @param packetNum 录音点数据的包序号，0表示结束传输
	 * @param recData 录音点数据
	 * @return 数据包
	 */
	public final static CloudPacket TerminalReportSelfPointRecord(short packetNum, byte[] recData, int length) {
		CloudPacket packet = new CloudPacket();
		ByteBuffer buf = packet.GetPacketBuffer();
		
		MakePacketHeader(buf, SELFPOINT_REC_UPLOAD_CMD);
		buf.putShort(packetNum);
		buf.put(recData, 0, length);
		MakePacketTail(buf);
		
		return packet;
	}
	
	/**
	 * 终端上报投诉点,{@link COMPLAIN_POINT_CMD}
	 * @param point 16bytes，自建点信息
	 * @return 数据包
	 */
	public final static CloudPacket TerminalReportSelfPoint(byte[] point) {
		CloudPacket packet = new CloudPacket();
		ByteBuffer buf = packet.GetPacketBuffer();
		
		MakePacketHeader(buf, SELFPOINT_UPLOAD_CMD);
		buf.put(point);
		MakePacketTail(buf);
		
		return packet;
	}
	
	public final static CloudPacket TerminalRequestUpdate(byte[] pkey, int lat, int lon, byte radius, int time) {
		CloudPacket packet = new CloudPacket();
		ByteBuffer buf = packet.GetPacketBuffer();
		
		MakePacketHeader(buf, UPDATE_REQUEST_CMD);
		buf.put(pkey);
		buf.putInt(lat);
		buf.putInt(lon);
		buf.put(radius);
		buf.putInt(time);
		MakePacketTail(buf);
		
		return packet;
	}
	
	public final static CloudPacket TerminalRequestUpdateData(short totalFrame, short curFrame) {
		CloudPacket packet = new CloudPacket();
		ByteBuffer buf = packet.GetPacketBuffer();
		
		MakePacketHeader(buf, UPDATE_REQUEST_DATA_CMD);
		buf.putShort(totalFrame);
		buf.putShort(curFrame);
		MakePacketTail(buf);
		
		return packet;
	}
	
	public final static CloudPacket TerminalReportUpdateKey(byte[] oldKey, byte[] newKey) {
		CloudPacket packet = new CloudPacket();
		ByteBuffer buf = packet.GetPacketBuffer();
		
		MakePacketHeader(buf, KEY_MODIFY_REPORT_CMD);
		buf.put(oldKey);
		buf.put(newKey);
		MakePacketTail(buf);
		
		return packet;
	}
	
	public final void send(boolean isWait) {
		CloudOperate operate = CloudOperate.getInstance();
		
		if(operate == null) {
			return;
		}
		
		operate.SendPacket(this, isWait);
	}
	
	public final short GetPacketCmd() {
		return (short) mPacketBuf.getShort(3);
	}
	
	public final short GetPacketId() {
		return (short) mPacketBuf.getShort(7);
	}
	
	public final int GetPacketLength() {
		return (int) (mPacketBuf.getShort(5) - 0x8000) & 0xFFFF;
	}
	
	public final int getInt() {
		return mPacketBuf.getInt();
	}
	
	public final byte getByte() {
		return mPacketBuf.get();
	}
	
	public final long getLong() {
		return mPacketBuf.getLong();
	}
	
	public final short getShort() {
		return mPacketBuf.getShort();		
	}
	
	public final int getLength() {
		return mPacketBuf.limit() - mPacketBuf.position();
	}
	
	public final String getString(int length) {
		byte[] dest = new byte[length];
		mPacketBuf.get(dest);
		try {
			return new String(dest, "GBK");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	public final String getString() {
		byte[] dest = new byte[mPacketBuf.remaining() - 2];
		mPacketBuf.get(dest);
		try {
			return new String(dest, "GBK");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	public final byte[] getRemain() {
		byte[] dest = new byte[mPacketBuf.remaining() - 2];
		mPacketBuf.get(dest);
		return dest;
	}
	
	public final byte[] getByteArray(int length) {
		byte[] dest = new byte[length];
		mPacketBuf.get(dest);
		return dest;
	}
}
