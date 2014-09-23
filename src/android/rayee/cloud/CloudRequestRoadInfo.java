package android.rayee.cloud;

import android.os.Handler;
import android.rayee.cloud.CloudPacket.PacketListener;
import android.util.Log;

public class CloudRequestRoadInfo implements PacketListener {

	private static final String TAG = "roadstatus";
	private Handler mMainHandler = null;
	
	public CloudRequestRoadInfo(NetService net) {
		// TODO Auto-generated constructor stub
		mMainHandler = net.getHandler();
	}
	
	@Override
	public void onReceive(CloudPacket packet) {
		// TODO Auto-generated method stub
		if(packet.mPacketDirection == CloudPacket.PACKET_DEVICE_TO_CLOUD) {
			Log.e(TAG, "packet send fail");
			return;
		}
		
		if(packet.GetPacketCmd() == CloudPacket.ROAD_INFO_CMD) {
			mMainHandler.sendMessage(mMainHandler.obtainMessage(NetService.EVENT_SERVICE_ROAD_INFO, packet.getString()));
		}
	}

	public void requestRoadStatus(double latitude, double longitude) {
		CloudPacket packet = CloudPacket.TerminalRequestRoadInfo((int)(latitude * 1000000), (int)(longitude * 1000000));
		packet.setListener(this);
		packet.send(true);
	}
}
