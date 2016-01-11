/*
为了实现通过传输字节流的通信,使用DataPackage类作为基本单位储存字节流解析出来的信息,
通过toMessageByte()可以将其转化为字节数组,
通过Message类提供的getDataPackage方法可以从当前的输入流得到一个DataPackage对象，
手写序列化操作处理中文编码
Author zhengxu
*/

package common.datastruct;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import common.GlobalTypeDefine;

public class DataPackage {
	private int type;
	private String messageString;
	private int dataIndex;
	private byte[] data;
	private String srcId;
	private String dstId;
	private String address;
	
	public DataPackage() {
		messageString = null;
		dataIndex = GlobalTypeDefine.INDEX_NONEED;
		data = null;
		srcId = null;
		dstId = null;
		address = null;
	}
	
	public byte[] toMessageByte() {
		ByteArrayOutputStream resp = new ByteArrayOutputStream();
		resp.write(type);
		
		if (srcId != null) {
			try {
				byte[] src = srcId.getBytes(GlobalTypeDefine.ENCODING);
				resp.write(GlobalTypeDefine.DATA_TYPE_SRC);
				resp.write(src.length);
				resp.write(src);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
		}
		
		if (dstId != null) {
			try {
				byte[] dst = dstId.getBytes(GlobalTypeDefine.ENCODING);
				resp.write(GlobalTypeDefine.DATA_TYPE_DST);
				resp.write(dst.length);
				resp.write(dst);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
		}
		
		if (address != null) {
			try {
				byte[] grp = address.getBytes(GlobalTypeDefine.ENCODING);
				resp.write(GlobalTypeDefine.DATA_TYPE_ADDRESS);
				resp.write(grp.length);
				resp.write(grp);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
		}
		
		if (messageString != null) {
			try {
				byte[] msg = messageString.getBytes(GlobalTypeDefine.ENCODING);
				resp.write(GlobalTypeDefine.DATA_TYPE_MESSAGE_STRING);
				resp.write(msg.length % 256);
				resp.write(msg.length / 256);
				resp.write(msg);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}				
		}
		
		if (dataIndex != GlobalTypeDefine.INDEX_NONEED) {
			resp.write(GlobalTypeDefine.DATA_TYPE_DATA_INDEX);
			resp.write(dataIndex % 256);	
			resp.write(dataIndex % (256 * 256) / 256);
			resp.write(dataIndex / (256 * 256) % 256);
			resp.write(dataIndex / (256 * 256 * 256));
		}
		
		if (data != null) {
			try {
				resp.write(GlobalTypeDefine.DATA_TYPE_DATA);
				resp.write(data.length % 256);
				resp.write(data.length / 256);
				resp.write(data);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		return resp.toByteArray();
	}
	
	public User getUser() {
	    if (srcId == null) {
	        return null;
	    }
	    return new User(messageString, srcId, dataIndex);
	}
	
	public void putUser(User user) {
	    if (user == null) {
	        return;
	    }
        messageString = user.getName();
        srcId = user.getId();
        dataIndex = user.getIconIndex();
        address = user.getAddress();
    }
	
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getMessageString() {
		return messageString;
	}
	public void setMessageString(String messageString) {
		this.messageString = messageString;
	}
	public int getDataIndex() {
		return dataIndex;
	}
	public void setDataIndex(int dataIndex) {
		this.dataIndex = dataIndex;
	}
	public byte[] getData() {
		return data;
	}
	public void setData(byte[] data) {
		this.data = data;
	}
	public String getSrcId() {
		return srcId;
	}
	public void setSrcId(String srcId) {
		this.srcId = srcId;
	}
	public String getDstId() {
		return dstId;
	}
	public void setDstId(String dstId) {
		this.dstId = dstId;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	
	
}
