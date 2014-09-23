package android.demo;

import java.nio.ByteBuffer;

public class DebugOperate {

	public DebugOperate() {
		// TODO Auto-generated constructor stub
	}
	
	//use to print debug data
    private static String hexString				="0123456789ABCDEF";
	
    public final static String ByteBufferConvertToString(ByteBuffer Bbuf, char c) {
        StringBuilder sb;
        int size;
        
        size = (Bbuf.limit() - Bbuf.position());
        sb = new StringBuilder(size * 3);
        
        for(int i = Bbuf.position(); i < Bbuf.limit(); i++) {
            sb.append(hexString.charAt((Bbuf.get(i) & 0xF0) >> 4));
            sb.append(hexString.charAt((Bbuf.get(i) & 0x0F)));
            
            if(i > 64) {
            	sb.append("...");
            	break;
            }
            
            if(i != (Bbuf.limit() - 1)) {
				sb.append(c);
			}
        }
        
        return sb.toString();
    }
	
	public final static String ByteBufferConvertToString(byte[] Bbuf, char c) {
        StringBuilder sb;
        int size;
        
        size = Bbuf.length;
        sb = new StringBuilder(size * 3);
        
        for(int i = 0; i < size; i++) {
            sb.append(hexString.charAt((Bbuf[i] & 0xF0) >> 4));
            sb.append(hexString.charAt((Bbuf[i] & 0x0F)));
            
            if(i != (size - 1)) {
				sb.append(c);
			}
        }
        
        return sb.toString();
    }
	
	private static byte charToByte(char c) {  
	     return (byte) "0123456789ABCDEF".indexOf(c);  
	 }  
	
	public final static byte[] StringToByteArray(String str, char c) {
		String[] strArray = str.split(String.valueOf(c));
		byte[] array = new byte[strArray.length];
		char[] hex = null;
		for(int i = 0; i < strArray.length; i++) {
			hex = strArray[i].toCharArray();
			array[i] = (byte) ((charToByte(hex[0]) << 4) | (charToByte(hex[1])));
		}
		
		return array;
	}
}
