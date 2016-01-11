/*
公共参数
Author zhengxu
 */

package common;

public class GlobalTypeDefine {
	public static final String[] USER_HEAD_IMGS = {"ebf1.png", "ebf2.png", "ebf3.png", "ebf4.png", "ebf5.png", "ebf6.png"};
	
	
    public static final String ENCODING = "UTF-8";
	public static final String DECODING = "UTF-8";
	
	public static final int TYPE_LOGIN = 100;
	public static final int TYPE_REGISTER = 101;
	public static final int TYPE_UDP_REG = 102;
	public static final int TYPE_ONLINE_USER = 103;
	public static final int TYPE_OFFLINE = 104;
	public static final int TYPE_BOARDCAST = 105;
	public static final int TYPE_KICKED = 106;
	public static final int TYPE_P2P_CHAT = 107;
	public static final int TYPE_FILE_SEND_REQUEST = 108;
	public static final int TYPE_FILE_SEND_REFUSED = 109;
	public static final int TYPE_FILE_SEND = 110;
	public static final int TYPE_FILE_ASK_SEND = 111;
	public static final int TYPE_IMG_SEND_REQUEST = 112;
	
	
	public static final int DATA_TYPE_SRC = 40;
	public static final int DATA_TYPE_DST = 41;
	public static final int DATA_TYPE_ADDRESS = 42;
	public static final int DATA_TYPE_MESSAGE_STRING = 43;
	public static final int DATA_TYPE_DATA = 44;
	public static final int DATA_TYPE_DATA_INDEX = 45;
	
	
	public static final String MESSAGE_STRING_YES = "YES";
	public static final String MESSAGE_STRING_NO = "NO";
	public static final String MESSAGE_STRING_EXCEPTION = "EXCEPTION";
	
	public static final int INDEX_NONEED = -1;
	
	public static final int MAX_DATA_LENGTH = 256 * 128;
	public static final int MAX_FILE_SIZE = 50000000;
}
