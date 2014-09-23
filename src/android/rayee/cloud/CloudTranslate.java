package android.rayee.cloud;

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;

import android.R.integer;
import android.demo.DebugOperate;
import android.os.Handler;
import android.util.Log;

public class CloudTranslate {
	private static final String TAG					= "CloudTranslate";
	private Handler mMainHandler					= null;
	
	private static String CloudServerIP 			= "183.251.83.25";
	private static int CloudServerTCPPort 			= 8124;//8124;//8001;
	private static int CloudServerUDPPort 			= 8123;//8123;//8000;
    
    private InetSocketAddress TcpSocketAddress = null;
    private InetSocketAddress UdpSocketAddress = null;
    
    //for test
    //private byte[]	login = {0x23, 0x52, 0x59, 0x10, 0x02, (byte)0x80, 0x39, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x11, 0x11, 0x11, 0x19, 0x53, 0x69, 0x07, 0x00, 0x52, (byte)0xdc, (byte)0xb2, 0x4f, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x34, 0x36, 0x30, 0x30, 0x32, 0x33, 0x38, 0x30, 0x33, 0x32, 0x32, 0x35, 0x35, 0x31, 0x31, 0x24, 0x24};
    //private byte[]	heart = {0x23, 0x52, 0x59, 0x10, 0x01, (byte)0x80, 0x13, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x11, 0x11, 0x11, 0x19, 0x24, 0x24};
    
    
    private ReadThread mThread					= null;
    private UdpReadThread mUdpThread			= null;
    private Socket TCPSocket					= null;
    private DatagramSocket UDPSocket			= null;
    
    private static final byte[] PACKET_HEADER	= {'#', 'R', 'Y'};
	private static final byte[] PACKET_TAIL		= {'$', '$'};
    
    static {
    	java.lang.System.setProperty("java.net.preferIPv4Stack", "true");
    	java.lang.System.setProperty("java.net.preferIPv6Addresses", "false");
    }
    
	CloudTranslate(CloudOperate operate) {
		mMainHandler = operate.getHandler();
	}
	
	private final boolean InitSocket() throws IOException {
		InetAddress server = InetAddress.getByName(CloudServerIP);
		TcpSocketAddress = new InetSocketAddress(server, CloudServerTCPPort);
        UdpSocketAddress = new InetSocketAddress(server, CloudServerUDPPort);

        TCPSocket = new Socket();
        UDPSocket = new DatagramSocket();

        return true;
	}

	public boolean ConnectServer() {
		try {
			InitSocket();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		mThread = new ReadThread();
	    mThread.start();
		return true;
	}
	
	public boolean start() {
		return ConnectServer();
	}
	
	public void close() {
		try {
			if(TCPSocket != null) {
				if(TCPSocket.isConnected()) {
					TCPSocket.shutdownOutput();
					TCPSocket.shutdownInput();
				}
				
				if(!TCPSocket.isClosed()) {
					TCPSocket.close();
				}
			}
			
			if(UDPSocket != null) {
				UDPSocket.close();
			}
			
			if(mThread != null) {
				mThread.interrupt();
				try {
					mThread.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		Log.v(TAG, "close");
	}
	
	private boolean checkHeader(byte[] dat, int[] offset, int length) {
		
		
		while(offset[0] < length) {
			if((length - offset[0]) < 3) {
				return false;
			}
			
			if(dat[offset[0]] == PACKET_HEADER[0] && dat[offset[0] + 1] == PACKET_HEADER[1] && dat[offset[0] + 2] == PACKET_HEADER[2]) {
				return true;
			}
			
			offset[0]++;
		}

		return false;
	}
	
	private boolean checkTail(byte[] dat, int[] offset, int length) {
		while(offset[0] < length) {
			if((length - offset[0]) < 2) {
				return false;
			}
			
			if(dat[offset[0]] == PACKET_TAIL[0] && dat[offset[0] + 1] == PACKET_TAIL[1]){
				return true;
			}
			offset[0]++;
		}

		return false;
	}
	
	void checkPacket(ByteBuffer buffer, byte[] data, int length) {
		int tail_pos = 0;
		int[] pos = {0};
		int len = 0;
		
		if(buffer.position() == 0) {
			if(!checkHeader(data, pos, length)) {
				return;
			}
		}
		
		buffer.put(data, pos[0], length);
		
		while(true) {
			tail_pos = buffer.position();
			if(tail_pos < 11) {
				return;
			}
			
			len = ((int)buffer.getShort(5) & 0xFFFF) - 0x8000;
			
			if(tail_pos < len) {
				return;
			}
			
			if(buffer.get(len - 2) == PACKET_TAIL[0] && buffer.get(len - 1) == PACKET_TAIL[1]) {
				byte[] buf = new byte[len];
				buffer.flip();
				buffer.get(buf);
				buffer.compact();
				mMainHandler.sendMessage(mMainHandler.obtainMessage(CloudOperate.OPERATE_RECEIVE_PACKET, new CloudPacket(ByteBuffer.wrap(buf))));
			} else {
				buffer.clear();
			}
		}
		
	}
	
	class UdpReadThread extends Thread {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			byte[] data = new byte[1024];
			ByteBuffer buffer = ByteBuffer.allocate(2048);

			try {
				while(!isInterrupted()) {
					DatagramPacket packet = new DatagramPacket(data, data.length);
					UDPSocket.receive(packet);

					checkPacket(buffer, data, packet.getLength());
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				if(!UDPSocket.isClosed()) {
					UDPSocket.close();
				}
			}
			
			Log.v(TAG, "udp read Thread exit");
		}
		
	}
	
	class ReadThread extends Thread {
		byte buf[] = null;
		int size = 0;
		
		private void connect() throws IOException {
			mMainHandler.sendMessage(mMainHandler.obtainMessage(CloudOperate.OPERATE_CONNECT_STATUS, 0, 0));

			TCPSocket.connect(TcpSocketAddress);

			Log.v(TAG, "connect success");
			mMainHandler.sendMessage(mMainHandler.obtainMessage(CloudOperate.OPERATE_CONNECT_STATUS, 1, 0));
		}
		
        public void run() {
        	buf = new byte[1024];
        	ByteBuffer buffer = ByteBuffer.allocate(2048);
        	InputStream in = null;;
        	try {
        		connect();
        		
        		in = TCPSocket.getInputStream();
        		mUdpThread = new UdpReadThread();
        		mUdpThread.start();
        		
        		while(!isInterrupted()) {
	        		if(TCPSocket != null) {
						size = in.read(buf);
						if(size > 0) {
							checkPacket(buffer, buf, size);
						} else if(size == -1) {
							//disconnect
							mMainHandler.sendMessage(mMainHandler.obtainMessage(CloudOperate.OPERATE_CONNECT_STATUS, 2, 0));
							break;
						} else {
							break;
						}
					}
        		}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				mMainHandler.sendMessage(mMainHandler.obtainMessage(CloudOperate.OPERATE_CONNECT_STATUS, 2, 0));
			} finally {
				try {
					if(in != null) {
						in.close();
						in = null;
					}
					
					if(TCPSocket != null) {
						TCPSocket.close();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
        	Log.v(TAG, "tcp read Thread exit");
    	}
    }
    
	/**
	 * 往TCPChannel发送一个数据包，以{@link ByteBuffer}存储数据
	 * @param datBuf 需要发送的数据
	 * @throws IOException
	 */
    public void TCPSend(ByteBuffer datBuf) throws IOException{
    	byte[] data = new byte[datBuf.remaining()];
    	datBuf.get(data);
    	if(TCPSocket != null && TCPSocket.isConnected()) {
    		TCPSocket.getOutputStream().write(data);
		}
    }
    
    /**
     * 往UDPChannel发送一个数据包，以{@link ByteBuffer}存储数据
     * @param datBuf 需要发送的数据
     * @throws IOException
     */
    public void UDPSend(ByteBuffer datBuf) throws IOException {
    	byte[] data = new byte[datBuf.remaining()];
    	datBuf.get(data);
    	DatagramPacket packet = new DatagramPacket(data, data.length, UdpSocketAddress);
    	if(UDPSocket != null) {
    		UDPSocket.send(packet);
    	}
    }
    
    /**
     * 把数据包发送给服务器，会根据 {@link IsPacketTCP}判断当前包是用TCP还是UDP发送
     * @param packet 需要发送的数据包
     * @return TODO
     * @throws IOException 如果发送过程出错，抛出IO异常
     */
    public boolean ClientSendPacket(CloudPacket packet) throws IOException {
    	if( packet == null) {
    		return false;
    	}
    	ByteBuffer buf = packet.GetPacketBuffer();
    	buf.rewind();
    	if(packet.IsPacketTCP()) {
    		Log.v(TAG, "TCP Send " + DebugOperate.ByteBufferConvertToString(buf, ','));
    		try {
    			TCPSend(buf);
    		} catch (IOException e) {
    			e.printStackTrace();
    			Log.v(TAG, "fail to send tcp packet");
    		}
    	}
    	else {
    		Log.v(TAG, "UDP Send " + DebugOperate.ByteBufferConvertToString(buf, ','));
    		UDPSend(packet.GetPacketBuffer());
    	}
    	
    	return true;
    }
}
