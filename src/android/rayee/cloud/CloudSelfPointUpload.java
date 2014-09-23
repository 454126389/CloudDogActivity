package android.rayee.cloud;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import android.demo.DebugOperate;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.rayee.cloud.CloudPacket.PacketListener;
import android.util.Log;
import android.util.Xml;

class SelfPointInfo {
	public byte[] spd = null;
	String recordFile = null;
	FileInputStream recIstream = null;
}

public class CloudSelfPointUpload implements PacketListener {
	public String xmlString 						= null;
	private static final String TAG 				= "spu";
	private static List<SelfPointInfo> pointlist 	= new ArrayList<SelfPointInfo>();
	private static final String xmlHeader			= "item";
	private static final String xmlTagData			= "data";
	private static final String xmlTagFile			= "record";
	private SelfPointInfo mUploadingPointInfo		= null;
	private Handler mThreadHandler					= null;
	
	private int curFrame = 0;
	private CloudPacket lastPacket = null;
	private boolean isUpdating						= false;
	private boolean isExiting						= false;
	private File mXmlFile							= null;
	
	private static final int SELF_POINT_UPLOAD_DEL	= 2;
	private static final int SELF_POINT_UPLOAD_POINT= 4;
	private static final int SELF_POINT_UPLOAD_SUCCESS = 5;
	private static final int SELF_POINT_UPLOAD_INIT	= 6;
	private static final int SELF_POINT_UPLOAD_RECORD = 7;

	CloudSelfPointUpload(NetService net) {
		mThreadHandler = new SelfPointUploadHandler(net.getLooper());
	}
	
	private class SelfPointUploadHandler extends Handler {
		
		SelfPointUploadHandler(Looper looper) {
			super(looper);
		}
		
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			SelfPointInfo info = null;
			
			switch(msg.what) {
			case SELF_POINT_UPLOAD_INIT:
				if(parseXml() > 0) {
					this.sendMessage(this.obtainMessage(SELF_POINT_UPLOAD_POINT));
				}
				break;
				
			case SELF_POINT_UPLOAD_DEL:
				delSelfPoint();
				break;
				
			case SELF_POINT_UPLOAD_POINT:
				synchronized (pointlist) {
					if(pointlist.isEmpty()) {
						isUpdating = false;
						break;
					}
					
					info = pointlist.get(0);
				}
				isUpdating = true;
				Log.v(TAG, "start upload point " + DebugOperate.ByteBufferConvertToString(info.spd, ','));
				
				curFrame = 0;
				if(info.recordFile.equals("")) {
					uploadPoint(info);
				} else {
					if(!uploadRecordRequest(info)) {
						this.sendMessage(this.obtainMessage(SELF_POINT_UPLOAD_DEL));
						this.sendMessage(this.obtainMessage(SELF_POINT_UPLOAD_POINT));
					}
				}
				mUploadingPointInfo = info;
				break;
				
			case SELF_POINT_UPLOAD_RECORD:
				int num = msg.arg1;
				Log.v(TAG, "selfpoint record upload frame " + String.valueOf(num));
				if(num == curFrame) {
					lastPacket.send(true);
				} else {
					curFrame = num;
					uploadRecData(mUploadingPointInfo, num);
				}
				break;
				
			case SELF_POINT_UPLOAD_SUCCESS:
				lastPacket = null;
				mUploadingPointInfo = null;
				Log.v(TAG, "selfpoint upload success");
				this.sendMessage(this.obtainMessage(SELF_POINT_UPLOAD_DEL));
				this.sendMessage(this.obtainMessage(SELF_POINT_UPLOAD_POINT));
				break;
			}
		}
		
	}
		
	private void saveSelfPoint() {
		if(mXmlFile != null) {
			synchronized (mXmlFile) {
				mXmlFile.delete();
				FileOutputStream outStream = null;
				XmlSerializer xmlSerializer = Xml.newSerializer();
				
				try {
					outStream = new FileOutputStream(mXmlFile);
					
					xmlSerializer.setOutput(outStream, "UTF-8");
					xmlSerializer.startDocument("UTF-8", true);
					
					synchronized (pointlist) {
						for(SelfPointInfo info : pointlist) {
							xmlSerializer.startTag(null, xmlHeader);
							xmlSerializer.startTag(null, xmlTagData);
							String result = DebugOperate.ByteBufferConvertToString(info.spd, '-');
							//Log.e(TAG, result);
							xmlSerializer.text(result);
							xmlSerializer.endTag(null, xmlTagData);
							xmlSerializer.startTag(null, xmlTagFile);
							xmlSerializer.text(info.recordFile);
							xmlSerializer.endTag(null, xmlTagFile);
							xmlSerializer.endTag(null, xmlHeader);
						}
					}
					
					xmlSerializer.endDocument();
					outStream.flush();
					outStream.close();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					try {
						outStream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	private void delSelfPoint() {
		synchronized(pointlist) {
			pointlist.remove(0);
		}
		saveSelfPoint();
	}
	@Override
	public void onReceive(CloudPacket packet) {
		// TODO Auto-generated method stub
		short cmd = packet.GetPacketCmd();
		
		if(isExiting == true) {
			return;
		}
		
		if(packet.getPacketDirection() == CloudPacket.PACKET_DEVICE_TO_CLOUD) {
			//send this point fail, send again
			Log.e(TAG, "send command:" + Integer.toHexString(cmd) + " id:" + Integer.toHexString(packet.GetPacketId()) + " fail");
			if(mThreadHandler != null) {
				Log.v(TAG, "retry upload after 30s");
				mThreadHandler.sendMessageDelayed(mThreadHandler.obtainMessage(SELF_POINT_UPLOAD_POINT), 30000);
			}
			return;
		}
		
		switch(cmd) {
		case CloudPacket.ACK_CMD:
			if(packet.getShort() == CloudPacket.SELFPOINT_UPLOAD_CMD) {
				Log.v(TAG, "self point upload success");
				if(mThreadHandler != null) {
					mThreadHandler.sendMessage(mThreadHandler.obtainMessage(SELF_POINT_UPLOAD_SUCCESS));
				}
			}
			break;
			
		case CloudPacket.SELFPOINT_REC_REQUEST_CMD:
			int status = packet.getByte();
			if(status == 0) {
				if(mThreadHandler != null) {
					mThreadHandler.sendMessage(mThreadHandler.obtainMessage(SELF_POINT_UPLOAD_RECORD, 1, 0));
				}
			} else if(status == 1) {
				Log.v(TAG, "this record file do not necessary upload");
				if(mThreadHandler != null) {
					mThreadHandler.sendMessage(mThreadHandler.obtainMessage(SELF_POINT_UPLOAD_SUCCESS));
				}
			} else {
				Log.e(TAG, "SELFPOINT_REC_REQUEST_CMD get not reconized status " + Integer.toHexString(status));
				if(mThreadHandler != null) {
					mThreadHandler.sendMessage(mThreadHandler.obtainMessage(SELF_POINT_UPLOAD_POINT));
				}
			}
			break;
			
		case CloudPacket.SELFPOINT_REC_UPLOAD_CMD:
			if(mUploadingPointInfo == null) {
				Log.e(TAG, "current have not selfpoint is uploading");
				break;
			}
			
			short num = packet.getShort();
			if(num == 0) {
				if(mThreadHandler != null) {
					mThreadHandler.sendMessage(mThreadHandler.obtainMessage(SELF_POINT_UPLOAD_SUCCESS));
				}
			} else {
				if(mThreadHandler != null) {
					mThreadHandler.sendMessage(mThreadHandler.obtainMessage(SELF_POINT_UPLOAD_RECORD, num, 0));
				}
			}
			break;
		}
	}
	
	public void uploadPoint(SelfPointInfo info) {
		CloudPacket packet = CloudPacket.TerminalReportSelfPoint(info.spd);
		packet.setListener(this);
		packet.send(true);
	}
	
	public boolean uploadRecordRequest(SelfPointInfo info) {
		File recordFile = new File(info.recordFile);
		if(!recordFile.exists() || !recordFile.isFile()) {
			return false;
		}
		int length = (int) recordFile.length();
		short count = (short) (((length + 1023) / 1024));
		
		try {
			info.recIstream = new FileInputStream(recordFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		Log.v(TAG, "request upload record, total:" + String.valueOf(length) + " frame:" + String.valueOf(count));
		CloudPacket packet = CloudPacket.TerminalRequestSelfPointUploadRecord(info.spd, length, count);
		packet.setListener(this);
		packet.send(true);
		return true;
	}
	
	public void uploadRecData(SelfPointInfo info, int num) {
		byte[] recData = new byte[1024];
		int size = -1;
		try {
			size = info.recIstream.read(recData, 0, 1024);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(size == -1) {
			recData[0] = 0;
			size = 1;
		}
		
		lastPacket = CloudPacket.TerminalReportSelfPointRecord((short)num, recData, size);
		lastPacket.setListener(this);
		lastPacket.send(true);
	}
	
	public void start(File xml) {
		mXmlFile = xml;
		synchronized (pointlist) {
			if(!isUpdating) {
				mThreadHandler.sendEmptyMessage(SELF_POINT_UPLOAD_INIT);
			}
		}
	}
	
	public void start() {
		synchronized (pointlist) {
			if(!isUpdating) {
				mThreadHandler.sendEmptyMessage(SELF_POINT_UPLOAD_POINT);
			}
		}
	}
	
	public void close() {
		isExiting = true;
		synchronized (pointlist) {
			pointlist.clear();
		}
	}
	
	public void addSelfPoint(byte[] data, String recFile) {
		SelfPointInfo info = new SelfPointInfo();
		info.spd = data;
		if(recFile == null) {
			info.recordFile = new String("");
		} else {
			info.recordFile = recFile;
		}
		
		synchronized (pointlist) {
			pointlist.add(info);
		}
		
		saveSelfPoint();
	}
	
	private int parseXml() {
		SelfPointInfo info = null;
		InputStream inputStream = null;
		XmlPullParser xmlParser = Xml.newPullParser();
		if(mXmlFile != null) {
			synchronized (mXmlFile) {
				synchronized (pointlist) {
					try {
						inputStream = new FileInputStream(mXmlFile);
						xmlParser.setInput(inputStream, "utf-8");
						
						int type = xmlParser.getEventType();
						
						while(type != XmlPullParser.END_DOCUMENT) {
							switch(type) {
							case XmlPullParser.START_TAG:
								String tag = xmlParser.getName();
								if(tag.equalsIgnoreCase(xmlHeader)) {
									info = new SelfPointInfo();
								} if(info != null) {
									if(tag.equalsIgnoreCase(xmlTagData)) {
										info.spd = DebugOperate.StringToByteArray(xmlParser.nextText(), '-');
									} else if(tag.equalsIgnoreCase(xmlTagFile)) {
										info.recordFile = xmlParser.nextText();
									}
								}
								break;
								
							case XmlPullParser.END_TAG:
								if(xmlParser.getName().equalsIgnoreCase(xmlHeader) && info != null) {
									synchronized (pointlist) {
										pointlist.add(info);
									}
									info = null;
								}
								break;
								
							default:
								break;
							}
							type = xmlParser.next();
						}
					} catch (XmlPullParserException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (FileNotFoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} finally {
						try {
							if(inputStream != null) {
								inputStream.close();
							}
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}

		return pointlist.size();
	}
}

