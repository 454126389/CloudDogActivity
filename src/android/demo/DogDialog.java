package android.demo;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

public class DogDialog extends Dialog implements OnItemClickListener {

	public DogDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public DogDialog(Context context, int theme) {
		super(context, theme);
		// TODO Auto-generated constructor stub
	}

	public DogDialog(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		// TODO Auto-generated constructor stub
	}

	/**
	 * ��Ҫ��setContentView֮�����
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public void setWindowSize(int x, int y, int width, int height) {
		/* 
         * ��ȡʥ����Ĵ��ڶ��󼰲����������޸ĶԻ���Ĳ�������,
         * ����ֱ�ӵ���getWindow(),��ʾ������Activity��Window
         * ����,�����������ͬ���ķ�ʽ�ı����Activity������.
         */
        Window dialogWindow = this.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        dialogWindow.setGravity(Gravity.LEFT | Gravity.TOP);

        /*
         * lp.x��lp.y��ʾ�����ԭʼλ�õ�ƫ��.
         * ������ֵ����Gravity.LEFTʱ,�Ի�����������,����lp.x�ͱ�ʾ�����ߵ�ƫ��,��ֵ����.
         * ������ֵ����Gravity.RIGHTʱ,�Ի���������ұ�,����lp.x�ͱ�ʾ����ұߵ�ƫ��,��ֵ����.
         * ������ֵ����Gravity.TOPʱ,�Ի���������ϱ�,����lp.y�ͱ�ʾ����ϱߵ�ƫ��,��ֵ����.
         * ������ֵ����Gravity.BOTTOMʱ,�Ի���������±�,����lp.y�ͱ�ʾ����±ߵ�ƫ��,��ֵ����.
         * ������ֵ����Gravity.CENTER_HORIZONTALʱ
         * ,�Ի���ˮƽ����,����lp.x�ͱ�ʾ��ˮƽ���е�λ���ƶ�lp.x����,��ֵ�����ƶ�,��ֵ�����ƶ�.
         * ������ֵ����Gravity.CENTER_VERTICALʱ
         * ,�Ի���ֱ����,����lp.y�ͱ�ʾ�ڴ�ֱ���е�λ���ƶ�lp.y����,��ֵ�����ƶ�,��ֵ�����ƶ�.
         * gravity��Ĭ��ֵΪGravity.CENTER,��Gravity.CENTER_HORIZONTAL |
         * Gravity.CENTER_VERTICAL.
         * 
         * ����setGravity�Ĳ���ֵΪGravity.LEFT | Gravity.TOPʱ�Ի���Ӧ�����ڳ�������Ͻ�,����
         * ���ֻ��ϲ���ʱ���־�������ϱ߶���һС�ξ���,���Ҵ�ֱ����ѳ��������Ҳ����������,
         * Gravity.LEFT, Gravity.TOP, Gravity.BOTTOM��Gravity.RIGHT�������,�ݱ߽���һС�ξ���
         */
        lp.x = x; // ��λ��X����
        lp.y = y; // ��λ��Y����
        lp.width = width; // ���
        lp.height = height; // �߶�
        lp.alpha = 1.0f; // ͸����

        // ��Window��Attributes�ı�ʱϵͳ����ô˺���,����ֱ�ӵ�����Ӧ������Դ��ڲ����ĸ���,Ҳ������setAttributes
        //this.onWindowAttributesChanged(lp);
        dialogWindow.setAttributes(lp);

        /*
         * ���Ի���Ĵ�С����Ļ��С�İٷֱ�����
         */
//        WindowManager m = getWindowManager();
//        Display d = m.getDefaultDisplay(); // ��ȡ��Ļ������
//        WindowManager.LayoutParams p = dialogWindow.getAttributes(); // ��ȡ�Ի���ǰ�Ĳ���ֵ
//        p.height = (int) (d.getHeight() * 0.6); // �߶�����Ϊ��Ļ��0.6
//        p.width = (int) (d.getWidth() * 0.65); // �������Ϊ��Ļ��0.65
//        dialogWindow.setAttributes(p);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setCancelable(true);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		DogTextAdapter adapter = (DogTextAdapter) parent.getAdapter();
		adapter.setSelectedItem(position);
		adapter.notifyDataSetInvalidated();
		dismiss();
	}
	
	
}
