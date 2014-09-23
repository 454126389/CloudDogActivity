package android.rayee.cloud;

import android.R.integer;
import android.os.Handler;
import android.util.Log;

public class CloudWeather implements CloudPacket.PacketListener{
	
	private static final String TAG = "weather";
	private Handler mMainHandler = null;
	
	public CloudWeather(NetService net) {
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
		
		if(packet.GetPacketCmd() == CloudPacket.WEATHER_CMD) {
			mMainHandler.sendMessage(mMainHandler.obtainMessage(NetService.EVENT_SERVICE_WEATHER_INFO, packet.getString()));
		}
	}
	
	void requestWeather(double latitude, double longitude) {
		CloudPacket packet = CloudPacket.TerminalRequestWeathePacket((int)(latitude * 1000000), (int) (longitude * 1000000));
		packet.setListener(this);
		packet.send(true);
	}

}
