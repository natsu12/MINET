package minet.main;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.text.Normalizer.Form;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import common.GlobalTypeDefine;
import common.MessageHandler;
import common.datastruct.DataPackage;
import common.datastruct.FileInfo;
import common.datastruct.User;
import minet.ui.LoginUI;
import minet.ui.MainUI;
import minet.ui.RegisterUI;
import minet.ui.UserChatUI;
import minet.ui.misc.FileOperation;

public class MinetClient {
    
    
    private ImageIcon smallIcons[];
    private Selector selector;
    private SocketChannel socketChannel;
    private DatagramChannel datagramChannel;
    private ByteBuffer inBuffer;
    private ByteBuffer outBuffer;
    private ConcurrentHashMap<String, UserInfo> userIDMap;
    private LoginUI loginUI;
    private MainUI mainUI;
    private RegisterUI registerUI;
    private ConcurrentHashMap<String, UserChatUI> userUIs;
    private User me;
    private AtomicBoolean stopped;
    private InetSocketAddress serverUDPAddr;
    private boolean udpReged;
    private FileInfo fileInfo;
    private int tempTimeCount;
  
    private class UserInfo {
        public User user;
        public long lastActiveTime;
        public UserInfo(User user, long time) {
            this.user = user;
            this.lastActiveTime = time;
        }
    }
    
    public MinetClient(InetSocketAddress serverTCPAddress, InetSocketAddress serverUDPAddress) throws IOException {
        tempTimeCount = 0;
        fileInfo = new FileInfo();
    	udpReged = false;
        stopped = new AtomicBoolean(false);
        userUIs = new ConcurrentHashMap<String, UserChatUI>();
        inBuffer = ByteBuffer.allocate(64 * 1024);
        outBuffer = ByteBuffer.allocate(64 * 1024);
        userIDMap = new ConcurrentHashMap<String, UserInfo>();
        selector = Selector.open();
        socketChannel = SocketChannel.open(serverTCPAddress);
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);
        datagramChannel = DatagramChannel.open();
        datagramChannel.configureBlocking(false);
        datagramChannel.register(selector, SelectionKey.OP_READ);
        serverUDPAddr = serverUDPAddress;
        smallIcons = new ImageIcon[GlobalTypeDefine.USER_HEAD_IMGS.length];
        for (int i = 0; i < GlobalTypeDefine.USER_HEAD_IMGS.length; ++i) {
            smallIcons[i] = new ImageIcon(this.getClass().getResource("/resource/imgs/userimgs/small_" + GlobalTypeDefine.USER_HEAD_IMGS[i]));
        }
    }
    
    public void closeAll() {
    	clearOtherUIs();
    	if (mainUI != null) {
    		mainUI.dispose();
    	}
        if (socketChannel != null) {
            try
            {
                socketChannel.close();
            } catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        if (datagramChannel != null) {
            try
            {
                datagramChannel.close();
            } catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        System.out.println("delall");
    }
    
    private void viewFileTransInfo() {
    	  //In file receiving processing
        if (mainUI != null) {
        	if (fileInfo.getStat() != FileInfo.FREE && fileInfo.isTimedOut()) {
        		String s = "文件传输响应超时，即将重置";
        		mainUI.getHelloTipsLabel().setText(s);
        		if (fileInfo.getStat() == FileInfo.RECEIVING) {
        		    refuseSendFile(fileInfo.getDstID(), "传输过程出错!");
        		}
        		fileInfo.reset();
        	}
        	else 
        	if (fileInfo.getStat() == FileInfo.RECEIVING) {
	        	String s = "文件接收中:" + fileInfo.getFileName() + " " + fileInfo.getProgSize() + "b/" +
	        				fileInfo.getFileSize() + "b";
	        	mainUI.getHelloTipsLabel().setText(s);
	        	requestFileData();
        	}
        	else 
        	if (fileInfo.getStat() == FileInfo.SENDING) {
	        	String s = "文件发送中:" + fileInfo.getFileName() + " " + fileInfo.getProgSize() + "b/" +
	        				fileInfo.getFileSize() + "b";
	        	mainUI.getHelloTipsLabel().setText(s);
        	}
        	else if (fileInfo.getStat() == FileInfo.DONE) {
        	    tempTimeCount = 5;
        		String s = "文件 " + fileInfo.getFileName() + " 传输完毕";
        		String id = fileInfo.getDstID();
        		mainUI.getHelloTipsLabel().setText(s);
        		
        		if (fileInfo.getFileType() == "image") {
        			String path = System.getProperty("user.dir") + "/images/" + fileInfo.getFileName();
            		UserInfo userInfo = userIDMap.get(id);
                    String name = "UnKnown";
                    if (userInfo != null) {
                        name = userInfo.user.getName()  + " (" + id + ")";;
                    } else {
                        return;
                    }
                    UserChatUI chatUI = getChatUI(userInfo.user);
                    ImageIcon tImg = null;
                    int idx = userInfo.user.getIconIndex();
                    if (idx >= 0 && idx < smallIcons.length) {
                        tImg = smallIcons[idx];
                    }
                    chatUI.insertImg(name, path, tImg, false);
                    chatUI.setVisible(true);
        		}
        		
        		fileInfo.reset();
        	}
        	else if (fileInfo.getStat() == FileInfo.FREE) {
        	    if (tempTimeCount <= 0) {
        	        String s = "今天天气晴朗!";
        	        mainUI.getHelloTipsLabel().setText(s);
        	    } else {
        	        --tempTimeCount;
        	    }
        	}
        }
    }
    
    private void readAndHandle() throws IOException
    {
        // LogCollectorExecutors.intervalInMillsForReader;
        long timeout = 1000;
        long startTime = System.currentTimeMillis();
        long endTime;
        
        //register udp address to server each time if not registered
        if (mainUI != null && !udpReged) {
            regUDPAddr();
        }
        
        viewFileTransInfo();
        
        while (!stopped.get()) {
            if (timeout <= 0) {
                break;
            }
            selector.select(timeout);
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            
            while (iterator.hasNext() && !stopped.get()) {
                SelectionKey selectionKey = iterator.next();
                iterator.remove();
                Channel channel = selectionKey.channel();
                
                if (selectionKey.isReadable()) {
                    if (channel instanceof SocketChannel) {
                        SocketChannel sk = (SocketChannel) channel;
                        inBuffer.clear();
                        sk.read(inBuffer);
                        inBuffer.flip();
                        InputStream in = new ByteArrayInputStream(inBuffer.array(), 0, inBuffer.limit());
                        
                        while (true) {
                            DataPackage dataPackage = MessageHandler.readDataPackage(in);
                            if (dataPackage == null) {
                                break;
                            }
                            switch (dataPackage.getType()) 
                            {
                                case GlobalTypeDefine.TYPE_LOGIN:
                                    //fail
                                    if (dataPackage.getSrcId() == null) {
                                        if (loginUI != null) {
                                            loginUI.getLogStateLabel().setText("错误的用户名或密码");
                                        }
                                    }
                                    //success
                                    else {
                                        me = new User(dataPackage.getMessageString(), dataPackage.getSrcId(), dataPackage.getDataIndex());
                                        if (loginUI != null) {
                                            loginUI.dispose();
                                        }
                                        if (registerUI != null) {
                                            registerUI.dispose();
                                        }
                                        if (mainUI == null) {
	                                        initMainUI(me);
	                                        regUDPAddr();
                                        }
                                    }
                                    break;
                                case GlobalTypeDefine.TYPE_REGISTER:
                                    if (dataPackage.getMessageString() != null) {
                                        if (registerUI != null) {
                                            registerUI.getTextHint().setText(dataPackage.getMessageString());
                                        }
                                    }
                                    break;                               
                                case GlobalTypeDefine.TYPE_UDP_REG:
                                    udpReged = true;
                                    if (mainUI != null) {
                                        mainUI.getIpLabel().setText(dataPackage.getAddress());
                                    }
                                    break;
                                case GlobalTypeDefine.TYPE_ONLINE_USER:
                                    User user = dataPackage.getUser();
                                    user.setAddress(dataPackage.getAddress());
                                    userIDMap.put(user.getId(), new UserInfo(user, System.currentTimeMillis()));
                                    if (!userUIs.containsKey(user.getId())) {
                                        getChatUI(user);
                                        mainUI.addOnlineFriend(user);
                                    }
                                    break;
                                case GlobalTypeDefine.TYPE_KICKED:
                                    if (mainUI != null) {
                                        mainUI.getHelloTipsLabel().setText("您的帐号被重新在其他客户端登陆!");
                                        try
                                        {
                                            stopped.set(true);
                                            Thread.sleep(5000);
                                        } catch (InterruptedException e)
                                        {
                                            // TODO Auto-generated catch block
                                            e.printStackTrace();
                                        }
                                        clearOtherUIs();
                                        mainUI.dispose();
                                    }
                                    break;
                                case GlobalTypeDefine.TYPE_BOARDCAST:
                                	doBoardCast(dataPackage); 
                                    break;
                            }
                           
                            
                        }
                    }
                    else if (channel instanceof DatagramChannel)
                    {
                        DatagramChannel dk = (DatagramChannel) channel;
                        inBuffer.clear();
                        InetSocketAddress address = (InetSocketAddress) dk.receive(inBuffer);
                        inBuffer.flip();
                        InputStream in = new ByteArrayInputStream(inBuffer.array(), 0, inBuffer.limit());
                        while (true)
                        {
                            DataPackage dataPackage = MessageHandler.readDataPackage(in);
                            if (dataPackage == null)
                            {
                                break;
                            }
                            switch (dataPackage.getType())
                            {
                                case GlobalTypeDefine.TYPE_ONLINE_USER:
                                    User user = dataPackage.getUser();
                                    user.setAddress(MessageHandler.AddresstoString(address));
                                    userIDMap.put(user.getId(), new UserInfo(user, System.currentTimeMillis()));
                                    //System.out.println("actives user: " + user.getId());
                                    break;
                                case GlobalTypeDefine.TYPE_OFFLINE:
                                    User offUser = dataPackage.getUser();
                                    removeUser(offUser.getId());
                                    break;
                                case GlobalTypeDefine.TYPE_P2P_CHAT:
                                    String id = dataPackage.getSrcId();
                                    String text = dataPackage.getMessageString();
                                    viewP2Pchat(id, text);
                                    break;
                                case GlobalTypeDefine.TYPE_FILE_SEND_REQUEST:
                                    if (fileInfo.getStat() == FileInfo.FREE) {
                                        fileInfo.setQuerying();
                                    	viewFileRequest(dataPackage);
                                    } else {
                                        refuseSendFile(dataPackage.getSrcId(), "对方正忙");
                                    }
                                	break;
                                case GlobalTypeDefine.TYPE_IMG_SEND_REQUEST:
                                	if (fileInfo.getStat() == FileInfo.FREE) {
	                                	fileInfo.setQuerying();
	                                	viewImgRequest(dataPackage);
                                	} else {
                                        refuseSendFile(dataPackage.getSrcId(), "对方正忙");
                                    }
                                	break;
                                case GlobalTypeDefine.TYPE_FILE_SEND_REFUSED:
                                    String id2 = dataPackage.getSrcId();
                                    String text2 = dataPackage.getMessageString();
                                    viewP2Pchat(id2, text2);
                                    fileInfo.reset();
                                    break;
                                case GlobalTypeDefine.TYPE_FILE_ASK_SEND:
                                    if (fileInfo.getStat() == FileInfo.SENDING) {
                                        sendCurrentFilePart(dataPackage.getDataIndex());
                                    }
                                	break;
                                case GlobalTypeDefine.TYPE_FILE_SEND:
                                	int recvIndex = fileInfo.appendPackage(dataPackage);
                                	if (recvIndex == -1) {
                                	    refuseSendFile(fileInfo.getDstID(), "传输命令超时!");
                                	}
                                	else {
                                    	if (fileInfo.getStat() != FileInfo.DONE) {
                                    		requestFileData();
                                    	}
                                	}
                                	break;
                                	
                            }
                        }
                    }
                }
            }

            endTime = System.currentTimeMillis();
            timeout -= (endTime - startTime);
            startTime = endTime;
        }
    }
    
    private void sendCurrentFilePart(int index) {
    	DataPackage dp = fileInfo.getPackage(index);
    	if (dp != null) {
    		  MessageHandler.writeDataPackage(dp, outBuffer);
    	        InetSocketAddress address = fileInfo.getAddress();
    	        if (address != null && !address.equals("Unknown")) {
    	            try
    	            {
    	                datagramChannel.send(outBuffer, address);
    	            } catch (IOException e)
    	            {
    	                // TODO Auto-generated catch block
    	                e.printStackTrace();
    	            }
    	        }
    	}
    }
    
    
    private void viewFileRequest(DataPackage dp) {
    	FileOperation fo = new FileOperation(dp.getMessageString(), dp.getSrcId(), dp.getDstId());
    	 fo.addWindowListener(new java.awt.event.WindowAdapter() {
    		 String id = dp.getSrcId();
             @Override
             public void windowClosing(java.awt.event.WindowEvent e) {
            	 final SwingWorker<Integer, Void> worker = new SwingWorker<Integer, Void>() {
                     @Override
                     protected Integer doInBackground() throws Exception
                     {
                    	 refuseSendFile(id, "关闭了发送请求");
                         return 0;
                     }                                 
                 };
                 worker.execute();
                 fo.dispose();
             }
         });
    	 fo.getButtonOK().addActionListener(new ActionListener()
         {
    		 String id = dp.getSrcId();
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 synchronized(fileInfo) {
                     if (fileInfo.getStat() != FileInfo.QUERYING) {
                         final SwingWorker<Integer, Void> worker = new SwingWorker<Integer, Void>() {
                             @Override
                             protected Integer doInBackground() throws Exception
                             {
                                 System.out.println(fileInfo.getStat());
                                 refuseSendFile(id, "已经有其他文件在传输中或者请求超时");
                                 fileInfo.reset();
                                 System.out.println(fileInfo.getStat());
                                 return 0;
                             }                                 
                         };
                         worker.execute();
                         fo.dispose();
                         return;
                     }
                	 JFileChooser jfc=new JFileChooser();  
                     jfc.setFileSelectionMode(JFileChooser.FILES_ONLY); 
                     File file = new File(dp.getDstId());
                     
                     jfc.setSelectedFile(file);
                     int flag = jfc.showSaveDialog(new JLabel());
                     file = null;
                     file = jfc.getSelectedFile();
                     if (flag == JFileChooser.APPROVE_OPTION && file != null) {
    	                 System.out.println("文件:"+file.getAbsolutePath());
    	                 InetSocketAddress address = null;
    	                 if (userIDMap.containsKey(id)) {
    	                     String userAddress = userIDMap.get(id).user.getAddress();
    	                     address = MessageHandler.StringtoAddress(userAddress);
    	                 }
    	                 if (address != null && !address.equals("Unknown"))  {
    	                	 System.out.println("Start write");
    	                	 fileInfo.setWriteMode(file, dp.getDataIndex(), me.getId(), id, address, "file");
    	                 }
                     } else {
                    	 final SwingWorker<Integer, Void> worker = new SwingWorker<Integer, Void>() {
                             @Override
                             protected Integer doInBackground() throws Exception
                             {
                            	 refuseSendFile(id, "取消了保存文件");
                            	 fileInfo.reset();
                                 return 0;
                             }                                 
                         };
                         worker.execute();
                     }
                     fo.dispose();
                 }
             }
         });
    	 
    	 fo.getButtonCancel().addActionListener(new ActionListener()
         {
    		 String id = dp.getSrcId();
             @Override
             public void actionPerformed(ActionEvent e)
             {
            	 final SwingWorker<Integer, Void> worker = new SwingWorker<Integer, Void>() {
                     @Override
                     protected Integer doInBackground() throws Exception
                     {
                    	 refuseSendFile(id, "拒绝了发送请求");
                    	 fileInfo.reset();
                         return 0;
                     }                                 
                 };
                 worker.execute();
                 fo.dispose();
             }
         });
    	
    	fo.setVisible(true);
    }
    
    private void viewImgRequest(DataPackage dp) {
        String id = dp.getSrcId();
        String path = System.getProperty("user.dir") + "/images/" + dp.getDstId();
        synchronized(fileInfo) {
            if (fileInfo.getStat() != FileInfo.QUERYING) {
                final SwingWorker<Integer, Void> worker = new SwingWorker<Integer, Void>() {
                    @Override
                    protected Integer doInBackground() throws Exception
                    {
                        System.out.println(fileInfo.getStat());
                        refuseSendFile(id, "已经有其他文件在传输中或者请求超时");
                        fileInfo.reset();
                        System.out.println(fileInfo.getStat());
                        return 0;
                    }                                 
                };
                worker.execute();
                return;
            }
                
            File file = new File(path);
            InetSocketAddress address = null;
            if (userIDMap.containsKey(id)) {
                String userAddress = userIDMap.get(id).user.getAddress();
                address = MessageHandler.StringtoAddress(userAddress);
            }
            if (address != null && !address.equals("Unknown"))  {
              System.out.println("Start write");
              fileInfo.setWriteMode(file, dp.getDataIndex(), me.getId(), id, address, "image");
            }
        }
    }
    
    
    private void requestFileData() {
  	  	ByteBuffer outBuffer = ByteBuffer.allocate(2048);
  	  	DataPackage dp = new DataPackage();
        dp.setType(GlobalTypeDefine.TYPE_FILE_ASK_SEND);
        dp.setSrcId(me.getId());
        dp.setDataIndex(fileInfo.getProgIndex());
        MessageHandler.writeDataPackage(dp, outBuffer);
        InetSocketAddress address = fileInfo.getAddress();
        if (address != null && !address.equals("Unknown")) {
            try
            {
                datagramChannel.send(outBuffer, address);
            } catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
  }
    
    
    
    
    private void refuseSendFile(String id, String reason) {
    	  ByteBuffer outBuffer = ByteBuffer.allocate(2048);
    	  DataPackage dp = new DataPackage();
          dp.setType(GlobalTypeDefine.TYPE_FILE_SEND_REFUSED);
          dp.setSrcId(me.getId());
          dp.setDstId(id);
          dp.setMessageString(reason);
          MessageHandler.writeDataPackage(dp, outBuffer);
          InetSocketAddress address = null;
          if (userIDMap.containsKey(id)) {
              String userAddress = userIDMap.get(id).user.getAddress();
              address = MessageHandler.StringtoAddress(userAddress);
          }
          if (address != null && !address.equals("Unknown")) {
              try
              {
                  datagramChannel.send(outBuffer, address);
              } catch (IOException e)
              {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
              }
          }
    }
    
    
    private void viewP2Pchat(String id, String text) {
         UserInfo userInfo = userIDMap.get(id);
         String name = "UnKnown";
         if (userInfo != null) {
             name = userInfo.user.getName()  + " (" + id + ")";;
         } else {
             return;
         }
         UserChatUI chatUI = getChatUI(userInfo.user);
         ImageIcon tImg = null;
         int idx = userInfo.user.getIconIndex();
         if (idx >= 0 && idx < smallIcons.length) {
             tImg = smallIcons[idx];
         }                
         chatUI.insertMessage(name, text, tImg, false);
         chatUI.setVisible(true);
    }
    
    private void doBoardCast(DataPackage dataPackage) {
    	String id = dataPackage.getSrcId();
        String text = dataPackage.getMessageString();
        UserInfo userInfo = userIDMap.get(id);
        if (mainUI != null) {
            String name = "UnKnown";
            int idx = 0;
            boolean self = false;
            if (id.equals(me.getId())) {
                name = me.getName() + " (" + id + ")";
                idx = me.getIconIndex();
                self = true;
            }
            else {
                name = userInfo.user.getName() + " (" + id + ")";
                idx = userInfo.user.getIconIndex();
                
            }
            ImageIcon tImg = null;

            if (idx >= 0 && idx < smallIcons.length) {
                tImg = smallIcons[idx];
            }
            mainUI.insertMessage(name, text, tImg, self);
        }
   }
   
    
    
    private void regUDPAddr()
    {
        DataPackage dp = new DataPackage();
        dp.setType(GlobalTypeDefine.TYPE_UDP_REG);
        dp.putUser(me);
        MessageHandler.writeDataPackage(dp, outBuffer);
        try
        {
            datagramChannel.send(outBuffer, serverUDPAddr);
        } catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }

    protected void removeUser(String id) {
        userIDMap.remove(id);
        if (userUIs.containsKey(id)) {
            userUIs.get(id).dispose();
            userUIs.remove(id);
        }
        if (mainUI != null) {
            mainUI.deleteOnlineFriend(id);
        }
    }
    
    private void sentHeartBeat()
    {
        if (me == null) {
            return;
        }
        
        DataPackage selfOnline = new DataPackage();
        selfOnline.setType(GlobalTypeDefine.TYPE_ONLINE_USER);
        selfOnline.putUser(me);
        MessageHandler.writeDataPackage(selfOnline, outBuffer);
        //tcp server tell you are online
        try
        {
            socketChannel.write(outBuffer);
        } catch (IOException e1)
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        
         //udp tell other onliners that you are online and remove timeout unconnected client
        Iterator<Map.Entry<String, UserInfo>> it = userIDMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, UserInfo> entry = it.next();
            UserInfo onlineUserInfo = entry.getValue();
            long time = System.currentTimeMillis() - onlineUserInfo.lastActiveTime;
            if (time > 60000) {
                removeUser(entry.getKey());
                continue;
            }
            
            User onlineUser = onlineUserInfo.user;
            String addr = onlineUser.getAddress();
            if (addr == null || addr.equals("Unknown")) {
                continue;
            }
            InetSocketAddress address = MessageHandler.StringtoAddress(addr);
            try
            {
                MessageHandler.writeDataPackage(selfOnline, outBuffer);
                datagramChannel.send(outBuffer, address);
            } catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
    }
      
    protected void initLoginUI(LoginUI loginUI)
    {
        this.loginUI = loginUI;
        this.loginUI.getButtonOK().addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                String id = loginUI.getIdField().getText();
                String password = String.valueOf(loginUI.getPasswordField().getPassword());
                loginUI.getLogStateLabel().setText("验证中，请稍候");
                final SwingWorker<Integer, Void> worker = new SwingWorker<Integer, Void>() {
                    @Override
                    protected Integer doInBackground() throws Exception
                    {
                        login(id, password);
                        return 0;
                    }                                 
                };
                worker.execute();
            }
        });
        this.loginUI.getButtonReg().addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (registerUI != null) {
                    registerUI.setVisible(true);
                }
                else
                {
                    initRegUI();
                }
            }
        });
    }
    
    protected void initRegUI()
    {
        registerUI = new RegisterUI();
        registerUI.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                registerUI.setVisible(false);
            }
        });
        registerUI.getButtonReg().addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                String id = registerUI.getTextFieldID().getText();
                String name = registerUI.getTextFieldName().getText();
                String pass = String.valueOf(registerUI.getPasswordField().getPassword());
                String passV = String.valueOf(registerUI.getPasswordFieldV().getPassword());
                int index = registerUI.getComboBox().getSelectedIndex();
                
                final SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
                    @Override
                    protected String doInBackground() throws Exception
                    {
                        return register(name, id, pass, passV, index);
                    }      
                  
                    @Override
                    protected void done() {
                        registerUI.getTextFieldID().setText("");
                        registerUI.getPasswordField().setText("");
                        registerUI.getPasswordFieldV().setText("");
                        registerUI.getTextFieldName().setText("");
                        String result;
                        try
                        {
                            result = get();
                            if (result != null) {
                                registerUI.getTextHint().setText(result);
                            }
                        } catch (InterruptedException e)
                        {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (ExecutionException e)
                        {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                       
                    }
                };
                worker.execute();
            }
        });
        
        registerUI.setVisible(true);
    }

    protected void initMainUI(User user)
    {
    	if (mainUI != null) {
    		mainUI.dispose();
    	}
        mainUI = new MainUI(user);
        mainUI.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                final SwingWorker<Integer, Void> worker = new SwingWorker<Integer, Void>() {

                    @Override
                    protected Integer doInBackground() throws Exception
                    {
                        clearOtherUIs();
                        sendExitRequest();
                        return 0;
                    }
                    
                    @Override
                    protected void done() {
                        mainUI.dispose();
                        stopped.set(true);
                        
                        //TODO check that if it is force to
                       // System.exit(0);
                    }
                 
                                    
                };
                worker.execute();
            }
        });
        
        
        mainUI.getSendButton().addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                String text = mainUI.getInputText();
                final SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
                    @Override
                    protected String doInBackground() throws Exception
                    {
                        sendBoardCastMessage(text);
                        return text;
                    }      
                    @Override
                    protected void done() {
                        mainUI.getInputArea().setText("");
                    }
                };
                worker.execute();
            }
        });
        
        mainUI.getTree().addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) { 
                if (e.getClickCount() == 2) {
                    JTree tree = (JTree) e.getSource();
                    int rowLocation = tree.getRowForLocation(e.getX(), e.getY());
                    TreePath treepath = tree.getPathForRow(rowLocation);
                    if (treepath != null) {
                        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) treepath.getLastPathComponent();
                        if (treeNode.getUserObject() instanceof User) {
                            User user = (User)treeNode.getUserObject();
                            if (user.getId() != null) {
                                showChatUI(user);
                            }
                        }
                    }
                }
                
            }
        });
        
        
        mainUI.setVisible(true); 
    }
    
    protected void clearOtherUIs() {
        for (UserChatUI uc : userUIs.values()) {
            if (uc != null) {
                uc.dispose();
            }
        }

        if (loginUI != null) {
            loginUI.dispose();
        }
        if (registerUI != null) {
            registerUI.dispose();
        }
    }
       
    protected UserChatUI initAndReturnChatUI(User user) {
        UserChatUI chatUI = new UserChatUI(user);
        chatUI.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                
                if (stopped.get()) {
                    chatUI.dispose();
                } else {
                    chatUI.setVisible(false);
                }
                
            }
        });
        chatUI.getSendButton().addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {

                String text = chatUI.getInputArea().getText();
                ImageIcon tImg = null;
                int idx = me.getIconIndex();
                if (idx >= 0 && idx < smallIcons.length) {
                    tImg = smallIcons[idx];
                }                
                chatUI.insertMessage(me.getName() + " (" + me.getId() + ")", text, tImg, true);
                final SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
                    @Override
                    protected String doInBackground() throws Exception
                    {
                        sendChatMessage(chatUI.getId(), text);
                        return text;
                    }      
                    @Override
                    protected void done() {
                        chatUI.getInputArea().setText("");
                    }
                };
                worker.execute();
            }
        });
        
        //SendFile
        chatUI.getFileButton().addActionListener(new ActionListener()
        {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (fileInfo.getStat() != FileInfo.FREE) {
					ImageIcon tImg = null;
		                int idx = me.getIconIndex();
		                if (idx >= 0 && idx < smallIcons.length) {
		                    tImg = smallIcons[idx];
		                }        
					chatUI.insertMessage(me.getName() + "(" + me.getId() + ")", "已经有其他文件正在传输 ", tImg , true);
					return;
				}
				JFileChooser jfc=new JFileChooser(); 
				jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				int selected = jfc.showDialog(new JLabel(), "选择");
				if (selected == JFileChooser.APPROVE_OPTION) {
    				File file=jfc.getSelectedFile();
    				prepareToSendFile(file, chatUI);
				}
			}
        });
        
        //SendImage
        chatUI.getImageButton().addActionListener(new ActionListener()
        {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (fileInfo.getStat() != FileInfo.FREE) {
					ImageIcon tImg = null;
		                int idx = me.getIconIndex();
		                if (idx >= 0 && idx < smallIcons.length) {
		                    tImg = smallIcons[idx];
		                }        
					chatUI.insertMessage(me.getName() + "(" + me.getId() + ")", "已经有其他图片正在传输 ", tImg , true);
					return;
				}
				
				ImageIcon tImg = null;
                int idx = me.getIconIndex();
                if (idx >= 0 && idx < smallIcons.length) {
                    tImg = smallIcons[idx];
                }
				
				JFileChooser jfc=new JFileChooser(); 
                FileNameExtensionFilter filter = new FileNameExtensionFilter(
                    "请选择图片文件", "png", "jpg");//文件名过滤器
                jfc.setFileFilter(filter);//给文件选择器加入文件过滤器
                
				int selected = jfc.showOpenDialog(null);
				if (selected == JFileChooser.APPROVE_OPTION) {
    				File file=jfc.getSelectedFile();
    				chatUI.insertImg(me.getName() + " (" + me.getId() + ")", file.getAbsolutePath(), tImg, true);
    				prepareToSendImg(file, chatUI);
				}
			}
        });
        
        return chatUI;
    }
    
    protected void prepareToSendFile(File file, UserChatUI chatUI) {
    	 if (file == null ||!file.exists()||file.length() > GlobalTypeDefine.MAX_FILE_SIZE ) {
    		 ImageIcon tImg = null;
             int idx = me.getIconIndex();
             if (idx >= 0 && idx < smallIcons.length) {
                 tImg = smallIcons[idx];
             }        
			chatUI.insertMessage(me.getName(), "文件不存在或者文件过大！", tImg , true);
			return;
    	 }
    	 System.out.println("文件:"+file.getAbsolutePath());  
         System.out.println(file.length());
         ByteBuffer outBuffer = ByteBuffer.allocate(2048);
         DataPackage dp = new DataPackage();
         dp.setType(GlobalTypeDefine.TYPE_FILE_SEND_REQUEST);
         dp.setSrcId(me.getId());
         dp.setDstId(file.getName());
         dp.setMessageString(me.getName());
         dp.setDataIndex((int) file.length());
         MessageHandler.writeDataPackage(dp, outBuffer);
         InetSocketAddress address = null;
         if (userIDMap.containsKey(chatUI.getId())) {
             String userAddress = userIDMap.get(chatUI.getId()).user.getAddress();
             address = MessageHandler.StringtoAddress(userAddress);
         }
         if (address != null && !address.equals("Unknown")) {
             try
             {
                 datagramChannel.send(outBuffer, address);
                 fileInfo.setReadMode(file, me.getId(), chatUI.getId(), address);
             } catch (IOException e)
             {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
         }
         
    }
    
    protected void prepareToSendImg(File file, UserChatUI chatUI) {
	   	if (file == null ||!file.exists()||file.length() > GlobalTypeDefine.MAX_FILE_SIZE ) {
	   		 ImageIcon tImg = null;
	         int idx = me.getIconIndex();
	         if (idx >= 0 && idx < smallIcons.length) {
	        	 tImg = smallIcons[idx];
	         }        
	         chatUI.insertMessage(me.getName(), "图片不存在或者图片过大！", tImg , true);
			 return;
	   	}
   	 	System.out.println("文件:"+file.getAbsolutePath());  
        System.out.println(file.length());
        ByteBuffer outBuffer = ByteBuffer.allocate(2048);
        DataPackage dp = new DataPackage();
        dp.setType(GlobalTypeDefine.TYPE_IMG_SEND_REQUEST);
        dp.setSrcId(me.getId());
        dp.setDstId(file.getName());
        dp.setMessageString(me.getName());
        dp.setDataIndex((int) file.length());
        MessageHandler.writeDataPackage(dp, outBuffer);
        InetSocketAddress address = null;
        if (userIDMap.containsKey(chatUI.getId())) {
            String userAddress = userIDMap.get(chatUI.getId()).user.getAddress();
            address = MessageHandler.StringtoAddress(userAddress);
        }
        if (address != null && !address.equals("Unknown")) {
            try
            {
                datagramChannel.send(outBuffer, address);
                fileInfo.setReadMode(file, me.getId(), chatUI.getId(), address);
            } catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
   }
    
    protected UserChatUI getChatUI(User user) {
        if (!userUIs.containsKey(user.getId())) {
            userUIs.put(user.getId(), initAndReturnChatUI(user));
        } 
        return userUIs.get(user.getId()).updateInfo(user);
  
    }
    
    
    protected void showChatUI(User user) {
        getChatUI(user).setVisible(true);
    }
    
    protected void sendChatMessage(String dstId, String text) {
        ByteBuffer outBuffer = ByteBuffer.allocate(32768);
        if (text.length() > 5000) {
            text = text.substring(0,5000);
        }
        DataPackage dp = new DataPackage();
        dp.setMessageString(text);
        dp.setSrcId(me.getId());
        dp.setDstId(dstId);
        dp.setType(GlobalTypeDefine.TYPE_P2P_CHAT);
        MessageHandler.writeDataPackage(dp, outBuffer);
        InetSocketAddress address = null;
        if (userIDMap.containsKey(dstId)) {
            String userAddress = userIDMap.get(dstId).user.getAddress();
            address = MessageHandler.StringtoAddress(userAddress);
        }
        if (address != null && !address.equals("Unknown")) {
            try
            {
                datagramChannel.send(outBuffer, address);
            } catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    
    protected void sendBoardCastMessage(String text) {
        ByteBuffer outBuffer = ByteBuffer.allocate(32768);
        if (text.length() > 5000) {
            text = text.substring(0,5000);
        }
        DataPackage dp = new DataPackage();
        dp.setMessageString(text);
        dp.setSrcId(me.getId());
        dp.setType(GlobalTypeDefine.TYPE_BOARDCAST);
        MessageHandler.writeDataPackage(dp, outBuffer);
        try
        {
            socketChannel.write(outBuffer);
        } catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    protected void sendExitRequest()
    {
        //do nothing if it's already been stopped (possibly has been kicked)
        if (stopped.get()) {
            return;
        }
        
        ByteBuffer outBuffer = ByteBuffer.allocate(2048);
        DataPackage dp = new DataPackage();
        dp.setType(GlobalTypeDefine.TYPE_OFFLINE);
        dp.putUser(me);
        
        //tell every one to remove me
        MessageHandler.writeDataPackage(dp, outBuffer);
        try
        {
            socketChannel.write(outBuffer);
        } catch (IOException e1)
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        
        
        for (UserInfo userInfo : userIDMap.values()) {
            InetSocketAddress address = MessageHandler.StringtoAddress(userInfo.user.getAddress());
            if (address != null) {
                MessageHandler.writeDataPackage(dp, outBuffer);
                try
                {
                    datagramChannel.send(outBuffer, address);
                } catch (IOException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                
            }     
        }
        
    }

    protected void login(String id, String password) {
        ByteBuffer outBuffer = ByteBuffer.allocate(2048);
        DataPackage dp = new DataPackage();
        dp.setType(GlobalTypeDefine.TYPE_LOGIN);
        dp.setSrcId(id);
        dp.setMessageString(password);
        MessageHandler.writeDataPackage(dp, outBuffer);
        try
        {
            socketChannel.write(outBuffer);
        } catch (IOException e)
        {
            loginUI.getLogStateLabel().setText("未知错误01");
            e.printStackTrace();
        }
    }
    
    protected String register(String name, String id, String pass, String passV, int index) {
        if (!MessageHandler.checkIdFormat(id)) {
            return "用户名格式错误";
        }
        if (!MessageHandler.checkNameFormat(name)) {
            return "昵称格式错误";
        }
        if (!MessageHandler.checkPasswordFormat(pass) || !MessageHandler.checkPasswordFormat(passV)) {
            return "密码格式错误";
        }
        if (!pass.equals(passV)) {
            return "两次密码不匹配";
        }
        ByteBuffer outBuffer = ByteBuffer.allocate(2048);
        DataPackage dp = new DataPackage();
        dp.setType(GlobalTypeDefine.TYPE_REGISTER);
        dp.setSrcId(id);
        dp.setMessageString(name);
        dp.setDataIndex(index);
        dp.setDstId(pass);
        MessageHandler.writeDataPackage(dp, outBuffer);
        try
        {
            socketChannel.write(outBuffer);
        } catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
    
    public static void main(String[] args) {
        MinetClient client = null;
        //moren
        int tcpPort = 9527;
        int udpPort = 9527;
        String tcpAddr = "127.0.0.1";
        String udpAddr = "127.0.0.1";
        
        //如果有变量 ，根据变量去改 
        if (args != null) {
        	for (int i = 0; i < args.length - 1; ++i) {
        		if (args[i].equals("-tcp_port")) {
        			tcpPort = Integer.parseInt(args[i + 1]);
        		}
        		if (args[i].equals("-udp_port")) {
        			udpPort = Integer.parseInt(args[i + 1]);
        		}
        		if (args[i].equals("-tcp_host")) {
        			tcpAddr = args[i + 1];
        		}
        		if (args[i].equals("-udp_host")) {
        			udpAddr = args[i + 1];
        		}
        	}
        }
              
        LoginUI loginUI = new LoginUI();
        loginUI.setVisible(true);
        try {
            client = new MinetClient(new InetSocketAddress(tcpAddr, tcpPort), new InetSocketAddress(udpAddr, udpPort));
            client.initLoginUI(loginUI);
        
            while (!client.isStopped()) {
                client.readAndHandle();
                client.sentHeartBeat();
            }
            System.out.println("done");
           
        } catch (IOException e) {
            // TODO Auto-generated catch block
           loginUI.getLogStateLabel().setText("连接服务器出错,请检查设置T.T");
           e.printStackTrace();
        } finally {
        	System.out.println("clearing");
            if (client != null) {
               client.closeAll();
            }
           // System.exit(0);
        }
    }

    private boolean isStopped()
    {
        return stopped.get();
    }

    public LoginUI getLoginUI()
    {
        return loginUI;
    }

    public MainUI getMainUI()
    {
        return mainUI;
    }

    public void setMainUI(MainUI mainUI)
    {
        this.mainUI = mainUI;
    }

    public RegisterUI getRegisterUI()
    {
        return registerUI;
    }

    public void setRegisterUI(RegisterUI registerUI)
    {
        this.registerUI = registerUI;
    }

   
}
