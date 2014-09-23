package android.rayee.cloud;

import java.nio.ByteBuffer;

import android.demo.IGpsData;
import android.demo.UserPreference;
import android.os.Handler;
import android.rayee.cloud.CloudPacket.PacketListener;
import android.util.Log;

public class CloudPositionReport implements PacketListener {
	
	private static final String TAG = "reploa";
	private Handler mMainHandler = null;
	
	CloudPositionReport(NetService net) {
		mMainHandler = net.getHandler();
	}
	
	public void ReportPoint(IGpsData gpsData) {
		ByteBuffer buffer = ByteBuffer.allocate(31);
		byte speed = 80;
		
		buffer.put((byte)1);
		buffer.putInt((int) (gpsData.mLatitude * 1000000));
		buffer.putInt((int) (gpsData.mLongtitude * 1000000));
		buffer.putShort((short) (gpsData.mSpeed * 10));
		buffer.put((byte) gpsData.mBearing);
		buffer.put((byte) (gpsData.GetYear() - 2000));
		buffer.put((byte) gpsData.GetMonth());
		buffer.put((byte) gpsData.GetDay());
		buffer.put((byte) gpsData.GetHour());
		buffer.put((byte) gpsData.GetMinute());
		buffer.put((byte) gpsData.GetSecond());
		
		if(UserPreference.mUserCruiseSpeed > 0) {
			speed = (byte) (UserPreference.mUserCruiseSpeed);
		}
		buffer.put(speed);
		
		speed = (byte) ((gpsData.mSpeed > speed) ? (gpsData.mSpeed - speed) : 0);
		buffer.put(speed);
		buffer.putInt(UserPreference.mTotalDistance);
		buffer.putShort((short) 0);
		buffer.put((byte) 100);
		buffer.putInt((int) (gpsData.mAltitude * 10));
		
		buffer.flip();
		CloudPacket packet = new CloudPacket(CloudPacket.GPS_DATA_CMD, buffer);
		packet.setListener(this);
		packet.send(true);
	}

	@Override
	public void onReceive(CloudPacket packet) {
		// TODO Auto-generated method stub
		if(packet.mPacketDirection == CloudPacket.PACKET_DEVICE_TO_CLOUD) {
			Log.e(TAG, "report location error");
			return;
		}
	}
	
	
}
