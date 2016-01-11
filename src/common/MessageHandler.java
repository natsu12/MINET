/*
解析和发送数据包
Author zhengxu
 */

package common;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import common.datastruct.DataPackage;


public class MessageHandler {
    private static final char[] HEX_DIGITS = { '0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
    
	
	private static int get2ByteNum(InputStream in) throws IOException {
		int x = in.read();
		if (x == -1) {
			return -1;
		}
		int y = in.read();
		if (y == -1) {
			return -1;
		}
		return x + 256 * y;
	}
	
	private static byte[] getMessageBytes(InputStream in, int len) throws IOException {
		int pos = 0;
		byte[] bytes = new byte[len];
		while (pos < len) {
			pos += in.read(bytes, pos, len - pos);
		}
		return bytes;
	}
	
	
	public static DataPackage readDataPackage(InputStream in) throws IOException {

		DataPackage resp = new DataPackage();
		int len = get2ByteNum(in);
		if (len == -1) {
			return null;
		}
		byte[] b = getMessageBytes(in, len);
		ByteArrayInputStream bin = new ByteArrayInputStream(b);
		int type = bin.read();
		resp.setType(type);
		int obj = bin.read();
		while (obj != -1) {
			if (obj == GlobalTypeDefine.DATA_TYPE_SRC) {
				int l = bin.read();
				byte[] srcb = getMessageBytes(bin, l);
				resp.setSrcId(new String(srcb, GlobalTypeDefine.DECODING));
			} else if (obj == GlobalTypeDefine.DATA_TYPE_DST) {
				int l = bin.read();
				byte[] dstb = getMessageBytes(bin, l);
				resp.setDstId(new String(dstb, GlobalTypeDefine.DECODING));
			} else if (obj == GlobalTypeDefine.DATA_TYPE_ADDRESS) {
				int l = bin.read();
				byte[] addr = getMessageBytes(bin, l);
				resp.setAddress(new String(addr, GlobalTypeDefine.DECODING));
			} else if (obj == GlobalTypeDefine.DATA_TYPE_MESSAGE_STRING) {
				int l = get2ByteNum(bin);
				byte[] msg = getMessageBytes(bin, l);
				resp.setMessageString(new String(msg, GlobalTypeDefine.DECODING));
			} else if (obj == GlobalTypeDefine.DATA_TYPE_DATA) {
				int l = get2ByteNum(bin);
				byte[] data = getMessageBytes(bin, l);
				resp.setData(data);
			} else if (obj == GlobalTypeDefine.DATA_TYPE_DATA_INDEX) {
				int l = get2ByteNum(bin);
				int r = get2ByteNum(bin);
				resp.setDataIndex(l + r * 256 * 256);
			}		
			obj = bin.read();
		}
		return resp;
	}
	
	public static void writeDataPackage(DataPackage dt, ByteBuffer out) {
	    out.clear();
		byte[] bt = dt.toMessageByte();
		out.put((byte) (bt.length % 256));
		out.put((byte) (bt.length / 256));
		out.put(bt);
		out.flip();
	}
	
	public static InetSocketAddress StringtoAddress(String address) {
	    if (address == null || address.equals("Unknown")) {
	        return null;
	    }
	    String[] s = address.split(":");
	    return new InetSocketAddress(s[0], Integer.parseInt(s[1]));
	}
	
	public static String AddresstoString(InetSocketAddress address) {
	    return address.getHostName() + ":" + address.getPort();
	}
	
	public static String getMD5String(String password) {
	    String md5 = null;
	    try
        {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            md.update(password.getBytes());
            byte[] result = md.digest();
            StringBuilder buf = new StringBuilder(result.length * 2);
            for (byte b : result) {
                buf.append(HEX_DIGITS[b >>> 4 & 0xf]);
                buf.append(HEX_DIGITS[b & 0xf]);
            }
            md5 = buf.toString();
        } catch (NoSuchAlgorithmException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	    return md5;
	}
	
	public static boolean checkIdFormat(String id) {
	    Pattern pattern = Pattern.compile("[a-zA-z0-9]{6,12}");
	    Matcher is = pattern.matcher(id);
	    return is.matches();
	}
	
	public static boolean checkPasswordFormat(String password) {
        Pattern pattern = Pattern.compile("[a-zA-z0-9]{6,12}");
        Matcher is = pattern.matcher(password);
        return is.matches();
    }
	
	public static boolean checkNameFormat(String name) {
        Pattern pattern = Pattern.compile("[\u4E00-\u9FA5a-zA-Z0-9_]{1,10}");
        Matcher is = pattern.matcher(name);
        return is.matches();
    }
	
	public static String getTimeString(String name) {
	    long now = System.currentTimeMillis();
	    Calendar c = Calendar.getInstance();  
        c.setTimeInMillis(now);
        String str=null;  
        str=String.format("%s: 于%04d年%d月%d日 - %02d:%02d:%02d", name, c.get(Calendar.YEAR),
        		(c.get(Calendar.MONTH) + 1), c.get(Calendar.DATE), c.get(Calendar.HOUR),
        		c.get(Calendar.MINUTE),c.get(Calendar.SECOND));
        return str;
	}
}
