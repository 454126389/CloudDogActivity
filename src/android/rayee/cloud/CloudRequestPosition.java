package android.rayee.cloud;

import android.os.Handler;
import android.util.Log;

public class CloudRequestPosition implements CloudPacket.PacketListener {

	private static final String TAG = "getPosition";
	private Handler mMainHandler = null;

	public CloudRequestPosition(NetService net) {
		mMainHandler  = net.getHandler();
	}
	
	@Override
	public void onReceive(CloudPacket packet) {
		// TODO Auto-generated method stub
		if(packet.mPacketDirection == CloudPacket.PACKET_DEVICE_TO_CLOUD) {
			Log.e(TAG, "packet send fail");
			return;
		}
		
		if(packet.GetPacketCmd() == CloudPacket.LOCATION_CMD) {
			mMainHandler.sendMessage(mMainHandler.obtainMessage(NetService.EVENT_SERVICE_ADDRESS_INFO, packet.getString()));
		}
	}
	
	
	public void requestCurrentPosition(double latitude, double longitude) {
		CloudPacket packet = CloudPacket.TerminalRequestLocation((int)(latitude * 1000000), (int)(longitude * 1000000));
		packet.setListener(this);
		packet.send(true);
	}
}
