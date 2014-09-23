package android.rayee.cloud;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.R.anim;
import android.content.Context;
import android.demo.CloudDogPreference;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.rayee.cloud.CloudPacket.PacketListener;
import android.util.Log;

public class CloudUpdate implements PacketListener {

	private static final String TAG = "CloudUpdate";

	public static final int UPDATE_FAIL = 0;
	public static final int UPDATE_TYPE_BASIC_DATABASE = 1;
	public static final int UPDATE_TYPE_UPDATE_DATABASE = 2;
	public static final int UPDATE_TYPE_EXTRA_VOICE = 3;

	private Context mContext = null;
	private Handler mHandler = null;
	private UpdateThread mUpdateThread = null;

	private int mUpdateLatitude = 0;
	private int mUpdateLongitude = 0;
	private byte mUpdateRadius = 5;
	private byte[] mKey = null;
	private byte[] mReceivedFKey = null;
	private byte[] mReceivedPKey = null;
	private int mVersion = 0;

	private CloudOperate mOperate = null;
	private Handler mThreadHandler = null;

	private File mTmpInsertFile = null;
	private File mTmpDeleteFile = null;
	private File mTmpHighwayFile = null;

	private int mUpdateReceiveFlag = 0;

	public CloudUpdate(NetService net, CloudOperate operate) {
		// TODO Auto-generated constructor stub
		mContext = net.getContext();
		mHandler = net.getHandler();
		mOperate = operate;
		mTmpInsertFile = new File(android.os.Environment.getExternalStorageDirectory(), "u5225t");
		mTmpDeleteFile = new File(android.os.Environment.getExternalStorageDirectory(), "u6499et");
		mTmpHighwayFile = new File(android.os.Environment.getExternalStorageDirectory(), "e3229t");
	}

	public CloudUpdate(NetService net, byte radius, CloudOperate operate) {
		// TODO Auto-generated constructor stub
		this(net, operate);
		mUpdateRadius = radius;
	}

	private void sendMsgToService(int what, int arg1, int arg2, Object obj) {
		Message msg = mHandler.obtainMessage(what, arg1, arg2, obj);
		mHandler.sendMessage(msg);
	}

	public final int getUpdateRadias() {
		return mUpdateRadius;
	}

	public final void setUpdateRadias(byte radius) {
		this.mUpdateRadius = radius;
	}

	public final void start(int lat, int lon, int radius, byte[] key, int version) {
		mUpdateLatitude = lat * 100;
		mUpdateLongitude = lon * 100;
		mUpdateRadius = (byte) radius;
		mKey = key;
		mVersion = version;
		mUpdateThread = new UpdateThread();
		mUpdateThread.start();
	}

	public final void close() {
		if (mUpdateThread != null) {
			mUpdateThread.exit();

			// mUpdateThread.interrupt();
			try {
				mUpdateThread.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		mUpdateThread = null;
	}

	public final boolean isUpdating() {
		if (mUpdateThread == null) {
			return false;
		}

		return true;
	}

	private final int UPDATE_STATUS_SUCCESS = 0;
	private final int UPDATE_STATUS_ERR_KEY = 1;
	private final int UPDATE_STATUS_ERR_FRAME = 2;
	private final int UPDATE_STATUS_RETRY = 3;
	private final int UPDATE_STATUS_ERR_DATA_SEND_FAIL = 4;

	public final void updateFinish(int status) {
		if (UPDATE_STATUS_RETRY == status) {
			Log.v(TAG, "update again");
		} else if (UPDATE_STATUS_SUCCESS == status) {
			Log.v(TAG, "update success");
			CloudDogPreference.mUpdateDatabaseFKey = mReceivedFKey;
			CloudDogPreference.mUpdateDatabasePKey = mReceivedPKey;
			CloudDogPreference.mLastUpdateLatitude = (double)mUpdateLatitude / 1000000;
			CloudDogPreference.mLastUpdateLongitude = (double)mUpdateLongitude / 1000000;
			CloudDogPreference.mUpdateOldKey = CloudDogPreference.mUpdateNewKey;

			if ((mUpdateReceiveFlag & 0x01) == 1) {
				sendMsgToService(NetService.EVENT_SERVICE_UPDATE_FINISH, 1, 0, mTmpInsertFile.getAbsolutePath());
			}

			if ((mUpdateReceiveFlag & 0x04) == 4) {
				sendMsgToService(NetService.EVENT_SERVICE_UPDATE_FINISH, 4, 0, mTmpHighwayFile.getAbsolutePath());
			}
			
			if ((mUpdateReceiveFlag & 0x02) == 2) {
				sendMsgToService(NetService.EVENT_SERVICE_UPDATE_FINISH, 2, 0, mTmpDeleteFile.getAbsolutePath());
			}
			
		} else if (UPDATE_STATUS_ERR_KEY == status || UPDATE_STATUS_ERR_FRAME == status || UPDATE_STATUS_ERR_DATA_SEND_FAIL == status) {
			Log.v(TAG, "update fail code " + String.valueOf(status));
			sendMsgToService(NetService.EVENT_SERVICE_UPDATE_FINISH, UPDATE_FAIL, 0, null);
		}
		
		mUpdateThread.exit();
	}

	public final void InitTmpFile() {
		mTmpInsertFile.delete();
		mTmpDeleteFile.delete();
		mTmpHighwayFile.delete();
		mUpdateReceiveFlag = 0;
	}

	public final void saveDataToTmp(byte[] data) {
		byte type = data[0];
		FileOutputStream os = null;

		try {
			if (type == 1) {
				os = new FileOutputStream(mTmpInsertFile, true);
				mUpdateReceiveFlag |= 1;
			} else if (type == 2) {
				os = new FileOutputStream(mTmpDeleteFile, true);
				mUpdateReceiveFlag |= 2;
			} else if (type == 3) {
				os = new FileOutputStream(mTmpHighwayFile, true);
				mUpdateReceiveFlag |= 4;
			} else {
				Log.e(TAG, "cann't recognized the type " + String.valueOf((int) type));
				return;
			}
		} catch (FileNotFoundException e) {
			// TODO: handle exception
			Log.e(TAG, "cann't find file ");
			e.printStackTrace();
		}

		try {
			os.write(data, 1, data.length - 1);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			os.flush();
			os.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public class UpdateThread extends Thread implements PacketListener {
		private int totalRecord = 0;
		private short totalFrame = 0;
		private short curFrame = 1;

		private static final int UPDATE_GET_RESPONSE_FROM_CLOUD = 1;
		private static final int UPDATE_GET_DATA_FROM_CLOUD = 2;
		private static final int UPDATE_THREAD_EXIT = 3;
		private static final int UPDATE_DATA_SEND_ERR = 4;

		@Override
		public void run() {
			// TODO Auto-generated method stub

			if (mOperate == null) {
				return;
			}

			Looper.prepare();
			mThreadHandler = new Handler() {
				byte flag = 0;

				@Override
				public void handleMessage(Message msg) {
					// TODO Auto-generated method stub
					CloudPacket packet = null;

					switch (msg.what) {
					case UPDATE_GET_RESPONSE_FROM_CLOUD:
						packet = (CloudPacket) msg.obj;
						totalRecord = packet.getInt();
						totalFrame = packet.getShort();
						flag = packet.getByte();
						curFrame = 1;
						if (totalFrame == 0) {
							if (flag == 1) {
								Log.v(TAG, "server is busy, retry later");
								mThreadHandler.postDelayed(updateRequest, 60000);
							} else if (flag == 2) {
								Log.v(TAG, "update key error");
								updateFinish(UPDATE_STATUS_ERR_KEY);
								byte[] tmp = CloudDogPreference.mUpdateOldKey;
								CloudDogPreference.mUpdateOldKey = CloudDogPreference.mUpdateNewKey;
								CloudDogPreference.mUpdateNewKey = tmp;
							}
						} else {
							Log.v(TAG, "total record " + String.valueOf(totalRecord) + ", " + String.valueOf(totalFrame));
							mReceivedFKey = packet.getByteArray(16);
							mReceivedPKey = packet.getByteArray(16);
							InitTmpFile();
							updateDataRequest();
						}
						break;

					case UPDATE_GET_DATA_FROM_CLOUD:
						packet = (CloudPacket) msg.obj;
						int rec_frame = 1;
						try {
							rec_frame = packet.getShort();
						} catch (Exception e) {
							// TODO: handle exception
							e.printStackTrace();
							Log.e(TAG, "len: " + String.valueOf(packet.getLength()) + "cmd: " + Integer.toHexString(packet.GetPacketCmd()) + ", id: " + String.valueOf(packet.GetPacketId()));
						}
						
						if (curFrame != rec_frame) {
							Log.e(TAG, "get data frame index error " + String.valueOf(rec_frame) + ", " + String.valueOf(curFrame));
							if (rec_frame == 0) {
								mThreadHandler.postDelayed(updateRequest, 60000);
							} else {
								updateDataRequest();
								//Message msgExit = this.obtainMessage(UPDATE_THREAD_EXIT);
								//this.sendMessage(msgExit);
								//updateFinish(UPDATE_STATUS_ERR_FRAME);
							}
						} else {
							Log.v(TAG, "get the data for frame " + String.valueOf(curFrame));
							saveDataToTmp(packet.getRemain());
							if (curFrame == totalFrame) {
								// finish
								updateFinish(UPDATE_STATUS_SUCCESS);
							} else {
								curFrame++;
								updateDataRequest();
							}
						}
						break;

					case UPDATE_THREAD_EXIT:
						this.removeCallbacks(updateRequest);
						this.getLooper().quit();
						break;
						
					case UPDATE_DATA_SEND_ERR:
						packet = (CloudPacket) msg.obj;
						Log.e(TAG, "send packet " + Integer.toHexString(packet.GetPacketCmd()) + ":" + String.valueOf(packet.GetPacketId()) + " error");
						updateFinish(UPDATE_STATUS_ERR_DATA_SEND_FAIL);
						break;
					}
				}

			};
			sendRequest();
			Looper.loop();
			//mUpdateThread = null;
			Log.v(TAG, "update thread exit");
		}

		private Runnable updateRequest = new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				sendRequest();
			}
		};

		@Override
		public void onReceive(CloudPacket packet) {
			// TODO Auto-generated method stub
			Message msg = null;
			
			if(packet.getPacketDirection() == CloudPacket.PACKET_DEVICE_TO_CLOUD) {			
				msg = mThreadHandler.obtainMessage(UPDATE_DATA_SEND_ERR, packet);
				mThreadHandler.sendMessage(msg);
				return;
			}

			switch (packet.GetPacketCmd()) {
			case CloudPacket.UPDATE_REQUEST_CMD:
				msg = mThreadHandler.obtainMessage(UPDATE_GET_RESPONSE_FROM_CLOUD, packet);
				mThreadHandler.sendMessage(msg);
				break;

			case CloudPacket.UPDATE_REQUEST_DATA_CMD:
				msg = mThreadHandler.obtainMessage(UPDATE_GET_DATA_FROM_CLOUD, packet);
				mThreadHandler.sendMessage(msg);
				break;
			}
		}

		private void updateDataRequest() {
			CloudPacket newPacket = CloudPacket.TerminalRequestUpdateData(totalFrame, curFrame);
			newPacket.setListener(this);
			mOperate.SendPacket(newPacket, true);
		}

		private void sendRequest() {
			CloudPacket packet = CloudPacket.TerminalRequestUpdate(mKey, mUpdateLatitude, mUpdateLongitude, mUpdateRadius, mVersion);

			packet.setListener(UpdateThread.this);
			mOperate.SendPacket(packet, true);
		}
		
		public void exit() {
			Message msgExit = mThreadHandler.obtainMessage(UPDATE_THREAD_EXIT);
			mThreadHandler.sendMessage(msgExit);
		}
	}

	@Override
	public void onReceive(CloudPacket packet) {
		// TODO Auto-generated method stub

	}
}
