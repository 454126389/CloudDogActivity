package android.demo;

import java.io.FileDescriptor;

public class FileOperate {
	private FileDescriptor mFD										= null;
	private String TAG												= "FILE";
	
	public FileDescriptor OpenDev(String node) {
		return open(node);
	}
	
	public int CloseDev() {
		return close();
	}

	public native FileDescriptor open(String file);
	public native int close();
	
	{
		System.loadLibrary("file");
	}
}
