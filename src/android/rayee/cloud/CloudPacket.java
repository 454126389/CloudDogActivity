package android.rayee.cloud;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import android.demo.CloudDogPreference;
import android.demo.IGpsData;
import android.demo.UserPreference;

/**
 * ͨ�����ݰ���{@link CloudPacket}������������{@link CloudTranslate}ȥ���䵽��������
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
	
	
	public int mPacketWaitTime						= 20;		//Ĭ��20�볬ʱ
	public int mPacketRetryTimer					= 4;		//Ĭ��ֻ��4��
	
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
	 * �������������ݺ��ն˷��ظ�ָ��
	 */
	public static final short ACK_CMD					= (short) 0xA001;
	/**
	 * ����ָ�{@link HeartPacket}
	 */
	public static final short HEART_CMD 				= 0x1001;
	/**
	 * ��¼ָ�{@link LoginPacket}
	 */
	public static final short LOGIN_CMD 				= 0x1002;
	/**
	 * GPS���ݷ���ָ��
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
	
	//���źͻ�ȡIMEI��������ȡ
	public CloudPacket(int cmd, String IMSI) {
		this();
		MakePacketHeader(mPacketBuf, cmd);
		mPacketBuf.put(IMSI.getBytes());
		MakePacketTail(mPacketBuf);
	}
	
	//���ڳ�����
	public CloudPacket(int cmd, ByteBuffer extra) {
		this();
		MakePacketHeader(mPacketBuf, cmd);
		mPacketBuf.put(extra);
		MakePacketTail(mPacketBuf);
	}
	
	/**
	 * �ڴ������ݵ�ʱ�򣬸ýӿڱ�����{@linkplain GetPacketBuffer}֮ǰ���ã��������ݰ���ǰ4���ֽڻᱻ�������ݴ����ȥ����4���ֽ�������ʾ�ð��Ĵ��䷽ʽ��
	 * @return ��ǰ���ݰ��Ĵ��䷽ʽ��true��ʾ��TCP���䣬false��ʾ��UDP����
	 */
	public boolean IsPacketTCP() {
		if( mPacketTranslateFlag == PACKET_TCP_FLAG ) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * �½�CloudPacketʵ���󣬿��Ե��øýӿڻ�ȡ���ݰ���buffer�����ⲿ�޸�
	 * @return ���ݰ�{@link CloudPacket}�Ĵ洢{@link ByteBuffer}
	 */
	public ByteBuffer GetPacketBuffer() {
		return mPacketBuf;
	}
	
	/**
	 * ����һ��Ӧ�������Ҫ�ֶ����͸���������ָ��{@linkplain ACK_CMD}
	 * @param cmd ҪӦ�������
	 * @param idCode TODO
	 * @return ���ݰ�{@link CloudPacket}
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
	 * ����һ����¼������Ҫ�ֶ����͸�������
	 * @return ���ݰ�{@link CloudPacket}
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
	 * ����һ������������Ҫ�ֶ����͸�������
	 * @return ���ݰ�{@link CloudPacket}
	 */
	public final static CloudPacket TerminalHeartPacket() {
		CloudPacket packet = new CloudPacket(HEART_CMD);
		packet.mPacketTranslateFlag = PACKET_TCP_FLAG;
		packet.mPacketDirection = PACKET_DEVICE_TO_CLOUD;
		return packet;
	}
	
	/**
	 * ����һ��GPS���ݰ�����Ҫ�ֶ����͸�������
	 * @param flag 0:�켣��1:·��
	 * @param gps GPS����
	 * @param limitspeed ����
	 * @param overspeed ����
	 * @param mile ���
	 * @param status ״̬
	 * @param gpsstrength SIM�ź�ǿ��
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
	 * ����һ�����Ű��� ����Ϊ{@link SMS0_CMD}����{@link SMS1_CMD}
	 * @param flag 0��ʾ�����ŷ��ͣ�1��ʾ�ø��ŷ���
	 * @param MsgContent ����{@link String}��Ҫ���͵Ķ�������
	 * @return ���ݰ�{@link CloudPacket}
	 */
	public final static CloudPacket SMSPacket(int flag/*0:���ţ�1:����*/, String MsgContent) {
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
	 * �ն˲������ص�������������Ϊ{@linkplain SERVER_REQUEST_PARA_CMD}���ɷ������ȷ�������ն˰Ѹ����ݰ����ظ�������
	 * @param Para ����Ϊ{@link String}������������Ĳ���ֵ���������֮ǰ�÷ֺŸ�����ÿ���������ĸ���ĸ������:ֵ1,ֵ2,��;��ɡ��硰SGPS:1,3,600;FSNU:18688886666,13755559999;
	 * @return �������ݰ�
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
	 * �ն�����������������ָ��{@link TERMINAL_PARA_REQ_CMD}
	 * @param para �ն�����Ĳ���������{@link String}��FULLʾ���в�����������֮���÷ֺŸ�������SGPS��FSNU������˵���ɲμ����ն˲���˵����
	 * @return �������ݰ�
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
	 * �ն������������ǰλ�õ��������, {@link WEATHER_CMD}
	 * @param latitude ��ǰλ�õ�γ��
	 * @param longitude ��ǰλ�õľ���
	 * @return ���ݰ�
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
	 * �ն˸�֪�������Ƿ�״̬,{@link WARNING_CMD}
	 * @param gps ��ǰGPS����
	 * @param gpsstrength SIMǿ��
	 * @param VoltagePercent ��ص�ѹ�ٷֱ�(1~100)
	 * @return ���ݰ�
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
	 * �ն˸�֪������IMSI��(15 bytes)��{@link SERVER_GET_IMSI_CMD}
	 * @param str IMSI�ַ�������Ϊ�գ���Fill zero
	 * @return ���ݰ�
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
	 * �ն�����ǰGPS��������·����{@link ROAD_STATUS_CMD}
	 * @param gps ��ǰGPS����
	 * @return ���ݰ�
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
	 * �ն������ȡ��ǰλ��, {@link LOCATION_CMD}
	 * @return ���ݰ�
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
	 * �ն�����SIM����Ϣ,{@link SIM_INFO_CMD}
	 * @param IMSI����15bit��������0���
	 * @return ���ݰ�
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
	 * �ն����÷�������Ϣ״̬��δ���ò��ַ�������ȥ����, {@link MESSAGE_SWITCH_CMD}
	 * @param flag 32bit, һλ��ʾһ����Ϣ���أ�0��ʾ�أ�1��ʾ��
	 * @return ���ݰ�
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
	 * �ն˸�֪����������SIM��, {@link SIM_CHANGE_CMD}
	 * @param oldIMSI ��SIM��IMSI����15bit��������0���
	 * @param newIMSI ��SIM��IMSI����15bit��������0���
	 * @return ���ݰ�
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
	 * �ն�����Ͷ�ߵ�¼���ϴ���{@link COMPLAIN_REC_REQUEST_CMD}
	 * @param point 16�ֽڣ��Խ������Ϣ
	 * @param filesize �ļ���С
	 * @param totalPacket ���ļ�������Ҫ���ܰ�
	 * @return  ���ݰ�
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
	 * �ն��ϱ�Ͷ��¼�������ݣ�{@link COMPLAIN_REC_UPLOAD_CMD}
	 * @param packetNum ¼�������ݵİ���ţ�0��ʾ��������
	 * @param recData ¼��������
	 * @return ���ݰ�
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
	 * �ն��ϱ�Ͷ�ߵ�,{@link COMPLAIN_POINT_CMD}
	 * @param point 16bytes���Խ�����Ϣ
	 * @return ���ݰ�
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
