package android.demo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.xmlpull.v1.XmlSerializer;

import android.content.Context;
import android.util.Xml;

public class GpxRecord {
	private FileOutputStream mOutput	= null;
	private File mGpxXml				= null;
	private XmlSerializer mXmlSerialliSerializer = null;
	private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
	
	GpxRecord(Context context) {
		File dirFile = new File(android.os.Environment.getExternalStorageDirectory(), "track");
		
		if(!dirFile.exists()) {
			dirFile.mkdir();
		}
		
		mGpxXml = new File(dirFile, new SimpleDateFormat("MM-dd HH-mm-ss").format(new Date()) + ".xml");
		mXmlSerialliSerializer = Xml.newSerializer();
		
		try {
			mOutput = new FileOutputStream(mGpxXml);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		InitHeader();
	}
	
	private final void InitHeader() {
		try {
			mXmlSerialliSerializer.setOutput(mOutput, "UTF-8");
			mXmlSerialliSerializer.startDocument("UTF-8", true);
			mXmlSerialliSerializer.startTag(null, "gpx");
			mXmlSerialliSerializer.attribute(null, "version", "1.1");
			mXmlSerialliSerializer.attribute(null, "creator", "CloudDog");
			mXmlSerialliSerializer.attribute(null, "xmlns", "http://www.topografix.com/GPX/1/1");
			mXmlSerialliSerializer.startTag(null, "metadata");
			mXmlSerialliSerializer.startTag(null, "time");
			mXmlSerialliSerializer.text(mDateFormat.format(new Date()));
			mXmlSerialliSerializer.endTag(null, "time");
			mXmlSerialliSerializer.endTag(null, "metadata");
			mXmlSerialliSerializer.startTag(null, "trk");
			mXmlSerialliSerializer.startTag(null, "name");
			mXmlSerialliSerializer.text(mDateFormat.format(new Date()));
			mXmlSerialliSerializer.endTag(null, "name");
			mXmlSerialliSerializer.startTag(null, "trkseg");
			mXmlSerialliSerializer.flush();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public final void AddPoint(IGpsData data) {
		try {
			mXmlSerialliSerializer.startTag(null, "trkpt");
			mXmlSerialliSerializer.attribute(null, "lat", Double.toString(data.mLatitude));
			mXmlSerialliSerializer.attribute(null, "lon", Double.toString(data.mLongtitude));
			mXmlSerialliSerializer.startTag(null, "ele");
			mXmlSerialliSerializer.text(Double.toString(data.mAltitude));
			mXmlSerialliSerializer.endTag(null, "ele");
			mXmlSerialliSerializer.startTag(null, "time");
			mXmlSerialliSerializer.text(mDateFormat.format(new Date()));
			mXmlSerialliSerializer.endTag(null, "time");
			mXmlSerialliSerializer.startTag(null, "speed");
			mXmlSerialliSerializer.text(Float.toString(data.mSpeed));
			mXmlSerialliSerializer.endTag(null, "speed");
			mXmlSerialliSerializer.startTag(null, "magvar");
			mXmlSerialliSerializer.text(Float.toString((float) data.mBearing));
			mXmlSerialliSerializer.endTag(null, "magvar");
			mXmlSerialliSerializer.endTag(null, "trkpt");
			mXmlSerialliSerializer.flush();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public final void EndFile() {
		try {
			mXmlSerialliSerializer.endTag(null, "trkseg");
			mXmlSerialliSerializer.endTag(null, "trk");
			mXmlSerialliSerializer.endTag(null, "gpx");
			mXmlSerialliSerializer.endDocument();
			mXmlSerialliSerializer.flush();
			mOutput.close();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
