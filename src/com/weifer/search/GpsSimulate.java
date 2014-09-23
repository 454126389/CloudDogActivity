package com.weifer.search;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.http.ParseException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.demo.DebugOperate;
import android.demo.GPSControl;
import android.demo.HandlerMessageCode;
import android.demo.IGpsData;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Xml;

public class GpsSimulate extends Thread {
	private static final String TAG = "GpsSimulate";
	public GpsParser gpsParserInstance = new GpsParser();
	private FileInputStream mInputStream = null;
	private SerialPort mSerialPort = null;
	private Handler mHandler = null;
	
	public class GpsParser{
		public String gpsString = new String();
		public final String rmcHeader = new String("$GPRMC");
		public final String gpsEnd = new String("\r\n");
		
		public void GpsStringAdd(String str){
			gpsString += str;
			//Log.v(TAG, gpsString);
			int rmcHeaderPos = gpsString.indexOf(rmcHeader);
			//Log.v(TAM_TAG, "Header len: "+rmcHeaderPos);
			if(rmcHeaderPos >= 0) {
				int rmcEndPos = gpsString.indexOf(gpsEnd,rmcHeaderPos);
				//Log.v(TAM_TAG, "End len: "+rmcEndPos);
				if(rmcEndPos > 0)
				{
					String rmcSubString = null;
					rmcSubString = gpsString.substring(rmcHeaderPos, rmcEndPos);
					GpsRmcParser(rmcSubString);

					gpsString = gpsString.substring(rmcEndPos + 2, gpsString.length());
				} else if(gpsString.length()>1024) {
					gpsString = "";
				}
			} else if(gpsString.length()>1024) {
				gpsString = "";
			}
		}

		public void GpsRmcParser(String rmcString)
		{
			//Log.d(TAG, rmcString);
			String[] rmcArray = rmcString.split("\\,");
			try {
				double latTmp = Double.parseDouble(rmcArray[3]);
				long lat = ((long)latTmp/100)*10000+(((long)(latTmp*10000))%1000000)/60;

				double lonTmp = Double.parseDouble(rmcArray[5]);
				long lon = ((long)lonTmp/100)*10000+(((long)(lonTmp*10000))%1000000)/60;

				double spdTmp = Double.parseDouble(rmcArray[7]);
				int spd = (int)(spdTmp*1852)/1000;

				int cur = (int)Double.parseDouble(rmcArray[8]);

				byte gns = (byte) rmcArray[4].charAt(0);
				if(gns == (byte)'S') {
					lat = 0-lat;
				}

				byte gew = (byte) rmcArray[6].charAt(0);
				if(gew == (byte)'W') {
					lon = 0-lon;
				}
				
				byte gStatus = 0;
				if( rmcArray[2].charAt(0) == 'A') {
					gStatus=1;
					if(mHandler != null) {
						IGpsData gps = new IGpsData((double)lat / 10000, (double)lon / 10000, 0.0, Double.parseDouble(rmcArray[8]), System.currentTimeMillis(), spd);
						Message msg = mHandler.obtainMessage(HandlerMessageCode.HMC_GPS_DATA_CHANGE, gps);
						mHandler.sendMessage(msg);
					}
				}
				
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}

			
			
			//Log.v(TAM_TAG, "sta: "+gStatus+" lat: "+lat+" "+(char)gns+" lon: "+lon+" "+(char)gew+"spd: "+spd+" cur: "+cur);
			//for(String strTmp : rmcArray)
			//	Log.v(TAM_TAG, strTmp);
			
			
		}
	}
	
	public GpsSimulate(Handler handler) {
		// TODO Auto-generated constructor stub
		mHandler = handler;
	}
	
	public void SerialPortSetup() {
		try {
			mSerialPort = new SerialPort(new File("/dev/ttyS1"), 115200);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			Log.v(TAG, "IO excpetion in tSetup");
			return;
		}
	      
		mInputStream = (FileInputStream) mSerialPort.getInputStream();
	}
	
	@Override
	public void run() {
		//for gpx simulate
		IGpsData gps = null;
		File gpxFile = new File(android.os.Environment.getExternalStorageDirectory(), "track.xml");
		InputStream in = null;
		XmlPullParser xmlParser = Xml.newPullParser();
		
		try {
			in = new FileInputStream(gpxFile);	
			xmlParser.setInput(in, "UTF-8");
			int type = xmlParser.getEventType();
			
			while(type != XmlPullParser.END_DOCUMENT && !isInterrupted()) {
				switch(type) {
				case XmlPullParser.START_TAG:
					String tag = xmlParser.getName();
					if(tag.equalsIgnoreCase("trkpt")) {
						gps = new IGpsData();
						gps.mLatitude = Double.parseDouble(xmlParser.getAttributeValue(null, "lat"));
						gps.mLongtitude = Double.parseDouble(xmlParser.getAttributeValue(null, "lon"));
					} else if(tag.equalsIgnoreCase("ele")) {
						if(gps != null) {
							String val = xmlParser.nextText();
							gps.mAltitude = Double.parseDouble(val);
						}
					} else if(tag.equalsIgnoreCase("speed")) {
						if(gps != null) {
							gps.mSpeed = Float.parseFloat(xmlParser.nextText());
						}
					} else if(tag.equalsIgnoreCase("magvar")) {
						if(gps != null) {
							gps.mBearing = Double.parseDouble(xmlParser.nextText());
						}
					} else if(tag.equalsIgnoreCase("time")) {
						if(gps != null) {
							SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
							try {
								gps.setDate(format.parse(xmlParser.nextText()));
							} catch (java.text.ParseException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
					break;
					
				case XmlPullParser.END_TAG:
					String end_tag = xmlParser.getName();
					if(end_tag.equalsIgnoreCase("trkpt") && gps != null) {
						Message msg = mHandler.obtainMessage(HandlerMessageCode.HMC_GPS_DATA_CHANGE, gps);
						mHandler.sendMessage(msg);
						sleep(1*1000);
					}
					break;
					
				default:
					break;
				}
				type = xmlParser.next();
			}
		} catch(InterruptedException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			// TODO: handle exception
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO: handle exception
			e.printStackTrace();
		} catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		if(in != null) {
			try {
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		Log.v(TAG, "simulate exit");
	}
	
	public void Exit() {
		this.interrupt();
	}

	
	//@Override
	public void uart_run() {

		SerialPortSetup();
		byte[] buffer = new byte[128];
		while(!isInterrupted()) {
			int size;
			try {
				if (mInputStream == null) {
					return;
				}

				size = mInputStream.read(buffer);
				if (size > 0) {
					String printTmp = new String(buffer,0,size);
					//Log.i(TAG, printTmp);
					gpsParserInstance.GpsStringAdd(printTmp);
				}
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
			//mReception.append(new String("recive"));
		}
	}
	
	public void close() {
		this.interrupt();
		if(mInputStream != null) {
			try {
				mInputStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mSerialPort.close();
		}
	}
}
