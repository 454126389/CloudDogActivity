package android.demo;

import java.util.ArrayList;

import android.location.Location;
import android.util.Log;

public class SelfPointDataSample {
	private static SelfPointDataSample hInstance = null;
	private static OnSampleFinishListener mFListener = null;
	private static OnSampleFinishListener mBListener = null;
	
	public static final OnSampleFinishListener getForwardListener() {
		return mFListener;
	}

	public static final void setForwardListener(OnSampleFinishListener listener) {
		SelfPointDataSample.mFListener = listener;
	}

	public static final OnSampleFinishListener getBackwardListener() {
		return mBListener;
	}

	public static final void setBackwardListener(OnSampleFinishListener listener) {
		SelfPointDataSample.mBListener = listener;
	}

	public static final SelfPointDataSample getInstance() {
		if(hInstance == null) {
			hInstance = new SelfPointDataSample();
		}
		
		return hInstance;
	}
	
	public interface OnSampleFinishListener {
		void onSampleFinish(boolean status);
	}
	
	private class PointNode {
		public int latitude;
		public int longitude;
		//public int speed;
		public int cursor;
		public int distancetoLast;

		PointNode() {
			distancetoLast = 0;
		}
	}
	
	private static ArrayList<PointNode> mFPointList = new ArrayList<SelfPointDataSample.PointNode>();
	private static ArrayList<PointNode> mBPointList = new ArrayList<SelfPointDataSample.PointNode>();
	private static IGpsData CurGpsData = null;
	private static int mFDistanceInList = 0;
	private static int mBDistanceInList = 0;
	
	private static boolean mSelfPointBackwardEnable = false;
	private static boolean mSelfPointForwardEnable = false;
	private static int mSpeedLevel = 0;
	
	private static int mBackwardNeedDistance = 500;
	
	private static int getNeedDistanceBySpeed(int speedLevel) {
		switch(speedLevel) {
		case 1:
		case 2:
		case 3:
			return 150;
			
		case 4:
		case 5:
		case 6:
			return 300;
			
		case 0:
		case 7:
		case 8:
			return 500;
			
		case 9:
			return 700;

		default:
			return 1000;
		}
	}
	
	public static final void StartSample(boolean isForward) {
		if(isForward) {
			if(mFDistanceInList < 500) {
				if(mFListener != null) {
					mFListener.onSampleFinish(false);
				}
				return;
			}
			
			mSelfPointForwardEnable = true;
			mSelfPointBackwardEnable = false;
			
		} else {
			mSelfPointBackwardEnable = true;
			mSelfPointForwardEnable = false;
		}
		
		if(CurGpsData != null) {
			mSpeedLevel = (int) (CurGpsData.mSpeed / 10);
		} else {
			mSpeedLevel = 8;
		}
		
		mBackwardNeedDistance = getNeedDistanceBySpeed(mSpeedLevel);
	}

	public SelfPointDataSample() {
		// TODO Auto-generated constructor stub
	}
	
	public static final void onGpsLost() {
		
	}
	
	public static final void giveUpSample() {
		if(mSelfPointForwardEnable) {
			mSelfPointForwardEnable = false;
		}
		
		if(mSelfPointBackwardEnable) {
			mSelfPointBackwardEnable = false;
			mBPointList.clear();
			mBDistanceInList = 0;
		}
	}
	
	private final PointNode gpsToNode(IGpsData gps) {
		PointNode tmp = new PointNode();
		tmp.latitude = (int) (gps.mLatitude * 10000);
		tmp.longitude = (int) (gps.mLongtitude * 10000);
		//tmp.speed = (int) (gps.mSpeed * 1852 / 1000);
		tmp.cursor = (int) (gps.mBearing);
		
		return tmp;
	}
	
	private final int AddGpsPointToList(ArrayList<PointNode> list, IGpsData gps, int interDis, int tdis){
		PointNode last = null;
		float[] distance = new float[1];
		
		synchronized (list) {
			if(list.isEmpty()) {
				list.add(gpsToNode(gps));
				return 0;
			}
			
			last = list.get(list.size() - 1);
			Location.distanceBetween(gps.mLatitude, gps.mLongtitude, (double)last.latitude / 10000, (double)last.longitude / 10000, distance);
			
			if ((int) distance[0] >= interDis) {
				last = gpsToNode(gps);
				last.distancetoLast = (int) distance[0];
				list.add(last);
			}
		}
		
		return (int) (tdis + distance[0]);
	}
	
	private final int CheckListDistance(ArrayList<PointNode> list, int distance, int small, int big, boolean resver) {
		PointNode first = null;
		
		synchronized (list) {
			if(list.isEmpty() || list.size() < 2) {
				return 0;
			}

			while(distance > big) {
				
				if(list.size() < 2) {
					break;
				}
				
				if(resver) {
					first = list.get(list.size() - 1);
				} else {
					first = list.get(1);
				}

				if(distance - first.distancetoLast > small) {
					distance = distance - first.distancetoLast;
					
					if(resver) {
						list.remove(list.size() - 1);
					} else {
						list.remove(0);
						first.distancetoLast = 0;
					}
					
				} else {
					break;
				}
			}
		}
		
		return distance;
	}
	
	public final void AddGpsPoint(IGpsData gps) {
		CurGpsData = gps;

		mFDistanceInList = AddGpsPointToList(mFPointList, gps, 30, mFDistanceInList);
		
		if(mFDistanceInList > 500) {
			mFDistanceInList = CheckListDistance(mFPointList, mFDistanceInList, 1000, 1300, false);
		} else {
			mFDistanceInList = CheckListDistance(mFPointList, mFDistanceInList, 500, 700, false);
		}
		
		if(mSelfPointForwardEnable && mFDistanceInList >= 500) {
			if(mFListener != null) {
				mFListener.onSampleFinish(true);
			}
			mSelfPointForwardEnable = false;
		}
		
		if(mSelfPointBackwardEnable == true) {
			mBDistanceInList = AddGpsPointToList(mBPointList, gps, mBackwardNeedDistance / 10, mBDistanceInList);
			if(mBDistanceInList >= mBackwardNeedDistance) {
				if(mBListener != null) {
					mBListener.onSampleFinish(true);
				}
				mSelfPointBackwardEnable = false;
			}
		}
	}
	
	public int getDistanceBySpeed(int tDis, int speed) {
		if(speed <= 6) {
			return 300;
		} else if(speed <= 8) {
			return 500;
		} else {
			if(tDis < 1000) {
				return 500;
			} else {
				return 1000;
			}
		}
	}
	
	public int getHeadByDistance(ArrayList<PointNode> list, int distance) {
		PointNode tail = null;
		int index = list.size() - 1;
		int tmp_dis = 0;
		
		while(tmp_dis < distance) {
			tail = list.get(index);

			tmp_dis += tail.distancetoLast;
			
			if(index > 0) {
				index -= 1;
			} else {
				break;
			}
		}

		
		return index;
	}
	
	private int SampleToArray(ArrayList<PointNode> list, int tDis, int level, byte[] result, boolean resver) {
		if(list.size() < 2) {
			return 0;
		}
		
		PointNode head, tail;
		float[] distance = new float[1];

		head = list.get(0);
		tail = list.get(list.size() - 1);

		Location.distanceBetween(head.latitude, head.longitude, tail.latitude, tail.longitude, distance);
		
		if(distance[0] < 100) {
			tDis = CheckListDistance(list, tDis, 200, 0, resver);
		}
		
		if(resver) {
			tail = list.get(0);
			head = list.get(list.size() - 1);
		} else {
			int headIndex = getHeadByDistance(list, getDistanceBySpeed(tDis, level));
			head = list.get(headIndex);
			tail = list.get(list.size() - 1);
		}

		//Log.v("sample", String.valueOf(tail.latitude) + ", " + String.valueOf(tail.longitude) + ", " + String.valueOf(head.latitude) + "," + String.valueOf(head.longitude));
		result[2] = (byte) (tail.cursor / 2);
		result[3] = (byte) (head.cursor / 2);
		
		//latitude
		result[5] = (byte) ((tail.latitude >> 16) & 0xFF);
		result[6] = (byte) ((tail.latitude >> 8) & 0xFF);
		result[7] = (byte) ((tail.latitude) & 0xFF);
		
		//longitude
		result[9] = (byte) ((tail.longitude >> 16) & 0xFF);
		result[10] = (byte) ((tail.longitude >> 8) & 0xFF);
		result[11] = (byte) ((tail.longitude) & 0xFF);
		
		//
		result[12] = (byte) ((head.latitude - tail.latitude) >> 8);
		result[13] = (byte) ((head.latitude - tail.latitude));
		
		result[14] = (byte) ((head.longitude - tail.longitude) >> 8);
		result[15] = (byte) ((head.longitude - tail.longitude));
		
		return tDis;
	}
	
	public final void getForwardSampleData(byte[] sample) {
		if(sample.length != 16) {
			return;
		}
		synchronized (mFPointList) {
			mFDistanceInList = SampleToArray(mFPointList, mFDistanceInList, mSpeedLevel, sample, false);
		}
	}
	
	public final void getBackwardSampleData(byte[] sample) {
		if(sample.length != 16) {
			return;
		}
		
		synchronized (mBPointList) {
			mBDistanceInList = SampleToArray(mBPointList, mBDistanceInList, 8, sample, true);
			mBDistanceInList = 0;
			mBPointList.clear();
		}
	}
	
	public final static void resetBackwardList() {
		synchronized (mBPointList) {
			mBPointList.clear();
			mBDistanceInList = 0;
		}
	}
	
	public final void DeInit() {
		mBPointList.clear();
		mFPointList.clear();
	}
}
