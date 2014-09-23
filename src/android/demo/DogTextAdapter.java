package android.demo;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public final class DogTextAdapter extends BaseAdapter{
	
	public class ItemValue {
		public ItemValue(String str, int user) {
			// TODO Auto-generated constructor stub
			this.strText = str;
			this.itemData = user;
		}
		
		public String strText;
		public int itemData;

	}

	private Context context;
	private int mSelectedItem = -1;
	private ArrayList<ItemValue> mitemList = new ArrayList<ItemValue>();
	
	public final int getSelectedItem() {
		return mSelectedItem;
	}

	public final void setSelectedItem(int mSelectedItem) {
		this.mSelectedItem = mSelectedItem;
	}
	
	public DogTextAdapter(Context context) {
		this.context = context;
	}
	
	public DogTextAdapter(Context context, String[] strArray) {
		// TODO Auto-generated constructor stub
		this.context = context;
		ItemValue item = null;

		for(int i = 0; i < strArray.length; i++) {
			item = new ItemValue(strArray[i], 0);
			mitemList.add(item);
		}
		
		if(getCount() > 0) {
			mSelectedItem = 0;
		}
	}
	
	public DogTextAdapter(Context context, int resArrayId) {
		// TODO Auto-generated constructor stub
		String[] itemList = context.getResources().getStringArray(resArrayId);
		ItemValue item = null;
		
		for(int i = 0; i < itemList.length; i++) {
			item = new ItemValue(itemList[i], 0);
			mitemList.add(item);
		}
		
		if(getCount() > 0) {
			mSelectedItem = 0;
		}
		
		this.context = context;
	}
	
	public final void add(String str, int extra) {
		ItemValue item = new ItemValue(str, extra);
		mitemList.add(item);
	}
	
	public final void add(int resId, int extra) {
		ItemValue item = new ItemValue(context.getResources().getString(resId), extra);
		mitemList.add(item);
	}

	public final void add(int index, int resId, int extra) {
		ItemValue item = new ItemValue(context.getResources().getString(resId), extra);
		mitemList.add(index, item);
	}
	
	public final void add(int index, String str, int extra) {
		ItemValue item = new ItemValue(str, extra);
		mitemList.add(index, item);
	}
	
	public final void clear() {
		mitemList.clear();
	}
	
	public final void remove(int index) {
		mitemList.remove(index);
	}

	@Override
	public final int getCount() {
		// TODO Auto-generated method stub
		return mitemList.size();
	}

	@Override
	public final Object getItem(int position) {
		// TODO Auto-generated method stub
		return mitemList.get(position);
	}

	@Override
	public final long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
	
	public final void setItemText(int position, String str) {
		mitemList.get(position).strText = str;
	}
	
	public final void setItemText(int position, int resId) {
		mitemList.get(position).strText = context.getResources().getString(resId);
	}
	
	public final void setItemText(int position, int arrayId, int offset) {
		mitemList.get(position).strText = context.getResources().getStringArray(arrayId)[offset];
	}

	public final int getItemdata(int position) {
		return mitemList.get(position).itemData;
	}
	
	@Override
	public final View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		TextView view = null;//new TextView(context);
		
		if(convertView == null) {
			view = new TextView(context);
			view.setPadding(15, 5, 0, 5);
			view.setTextSize(TypedValue.COMPLEX_UNIT_PX, 25);
		} else {
			view = (TextView) convertView;
		}

		if(position == mSelectedItem) {
			view.setTextColor(Color.BLACK);
			view.setSelected(true);
			view.setPressed(true);
			view.setBackgroundColor(Color.YELLOW);
		} else {
			view.setTextColor(Color.WHITE);
			view.setSelected(false);
			view.setPressed(false);
			view.setBackgroundColor(Color.TRANSPARENT);
		}

		view.setText(mitemList.get(position).strText);
		
		return view;
	}
}
