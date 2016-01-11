package common.datastruct;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;

import common.GlobalTypeDefine;

public class FileInfo {
	public static final int FREE = 0;
	public static final int SENDING = 1;
	public static final int RECEIVING = 2;
	public static final int DONE = 3;
	public static final int QUERYING = 4;
	public static final long TIMED_OUT = 60000;
	
	static final int PER_SIZE = 512;
	
	private File file;
	private long fileSize;
	private long progSize;
	private int progIndex;
	private String srcID;
	private String dstID;
	private int stat;
	private BufferedOutputStream out;
	private BufferedInputStream in;

	private String type;
	
	private DataPackage lastDataPackage;
	private InetSocketAddress address;
	private long lastActive;
	
	public FileInfo() {
		this.file = null;
		this.stat = FREE;
	}
	
	public void setQuerying() {
	   this.lastActive = System.currentTimeMillis();
	    this.stat = QUERYING;
	}
	
	public void setReadMode(File file, String srcID, String dstID, InetSocketAddress address) {
	    this.lastActive = System.currentTimeMillis();
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		this.srcID = srcID;
		this.dstID = dstID;
		this.address = address;
		this.file = file;
		this.fileSize = file.length();
		this.stat = SENDING;
		this.progSize = 0;
		this.progIndex = 0;
		try {
			in = new BufferedInputStream(new FileInputStream(file));
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			reset();
		}
		
	}
	
	public void setWriteMode(File file, long length, String srcID, String dstID, InetSocketAddress address, String rec_type) {
		this.lastActive = System.currentTimeMillis();
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				reset();
			}
		}
		this.srcID = srcID;
		this.dstID = dstID;
		this.stat = RECEIVING;
		this.file = file;
		this.fileSize = length;
		this.progSize = 0;
		this.progIndex = 0;
		this.address = address;
		try {
			out = new BufferedOutputStream(new FileOutputStream(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			reset();
		}
		this.type = rec_type;
	}
	
	public int appendPackage(DataPackage dp) {
		this.lastActive = System.currentTimeMillis();
		//System.out.println("append start" + dp.getDataIndex() + " " + progIndex + " " + srcID + " " + dp.getSrcId());
		if (dp.getType() != GlobalTypeDefine.TYPE_FILE_SEND|| stat != RECEIVING ||!dstID.equals(dp.getSrcId())) {
			return -1;
		}
		if (dp.getDataIndex() != progIndex) {
		    return progIndex;
		}
		
		try {
			//System.out.println("data size to write: " + dp.getData().length);
			out.write(dp.getData());
			this.progIndex++;
			this.progSize += dp.getData().length;
			if (progSize >= fileSize) {
				out.close();
				stat = DONE;
			}
		} catch (IOException e) {
				e.printStackTrace();
				reset();
		}	
		return progIndex;
	}
	
	public DataPackage getPackage(int index) {
		this.lastActive = System.currentTimeMillis();
		if (stat != SENDING || index < progIndex - 1) {
			return null;
		}
		//last package provided again
		if (index == progIndex - 1) {
			return lastDataPackage;
		}
		
		//provide current package
		DataPackage dp = new DataPackage();
		
		int len = (int) Math.min(fileSize - progSize, PER_SIZE);
		byte[] b = new byte[len];
		try {
			in.read(b);
			dp.setSrcId(srcID);
			dp.setDstId(dstID);
			dp.setDataIndex(progIndex);
			dp.setType(GlobalTypeDefine.TYPE_FILE_SEND);
			dp.setData(b);
			progSize += len;
			lastDataPackage = dp;
			++progIndex;
			
			if (progSize >= fileSize) {
				in.close();
				stat = DONE;
			}
		} catch (IOException e) {
			e.printStackTrace();
			reset();
		}	
		//System.out.println("data  read size: " + len);
		return dp;
	}
	
	
	public void reset() {
		if (out != null) {
			try {
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (in != null) {
			try {
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		this.address = null;
		this.file = null;
		this.stat = FREE;
		this.fileSize = 0;
		this.progSize = 0;
		this.progIndex = 0;
		this.lastActive = System.currentTimeMillis();
		
		this.type = "";
	}
	

	public String getSrcID() {
		return srcID;
	}
	
	public String getDstID() {
        return dstID;
    }

	public InetSocketAddress getAddress() {
		return address;
	}

	public String getFileName() {
		return file.getName();
	}

	public long getFileSize() {
		return fileSize;
	}

	public long getProgSize() {
		return progSize;
	}

	public int getProgIndex() {
		return progIndex;
	}

	public int getStat() {
		return stat;
	}
	
	protected void finalize() {
		reset();
	}
	
	public boolean isTimedOut() {
		long t = System.currentTimeMillis() - lastActive;
		return t > TIMED_OUT;
	}
	
	public String getFileType() {
		return type;
	}
}
