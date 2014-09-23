package android.demo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class TestForInfomax{

	public final void process() {
		byte[] buffer = new byte[512];
		try {
			int i = 2;
			while((--i) != 0) {
				InputStream in = new FileInputStream(new File(android.os.Environment.getExternalStorageDirectory(), "test"));
				in.read(buffer);
				in.close();
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			try {
				OutputStream out = new FileOutputStream(new File(android.os.Environment.getExternalStorageDirectory(), "test"));
				out.write(buffer);
				out.flush();
				out.close();
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			e.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
}
