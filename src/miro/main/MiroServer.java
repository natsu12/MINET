package miro.main;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import common.GlobalTypeDefine;
import common.MessageHandler;
import common.datastruct.DataPackage;
import common.datastruct.User;
import miro.sqlconnect.SqliteHandler;
import miro.ui.ServerMonitorUI;

public class MiroServer {
	//两分钟踢掉掉线的用户
    public final static long CLIENT_LIVE_PERIOD = 120000;
    
    private AtomicBoolean stopped;
    private Selector selector;
    private ServerSocketChannel serverTCPChannel;
    private DatagramChannel serverUDPChannel;
    private ByteBuffer inBuffer;
    private ByteBuffer outBuffer;
    
    
    private static SqliteHandler sqlHandler;
    
    private class ClientInfo {
        public ClientInfo(SocketChannel client, long time) {
            this.client = client;
            this.lastActiveTime = time;
        }
        public SocketChannel client;
        public long lastActiveTime;
    }
    
    private HashMap<String, ClientInfo> loginIDClientMap;
    private HashMap<String, User> userIDMap;
    
    
    public MiroServer(int tcpPort, int udpPort) throws IOException {
    	//各种要用到的变量初始化
        stopped = new AtomicBoolean(false);
        loginIDClientMap = new HashMap<String, ClientInfo>();
        userIDMap = new HashMap<String, User>();
        inBuffer = ByteBuffer.allocate(64 * 1024);
        outBuffer = ByteBuffer.allocate(64 * 1024);
        
        selector = Selector.open();
        serverTCPChannel = ServerSocketChannel.open();
        
        //初始化本地TCP端口
        SocketAddress address = new InetSocketAddress(tcpPort);
        serverTCPChannel.socket().setReuseAddress(true);
        serverTCPChannel.bind(address);
        serverTCPChannel.configureBlocking(false);
        serverTCPChannel.register(selector, SelectionKey.OP_ACCEPT);
        
        //初始化本地UDP端口
        address = new InetSocketAddress(udpPort);
        serverUDPChannel = DatagramChannel.open();
        serverUDPChannel.socket().setReuseAddress(true);
        serverUDPChannel.bind(address);
        serverUDPChannel.configureBlocking(false);
        serverUDPChannel.register(selector, SelectionKey.OP_READ);
    }
    
    //关闭时执行
    public void closeChannel() {
        if (serverTCPChannel != null) {
            try
            {
                serverTCPChannel.close();
            } catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        if (serverUDPChannel != null) {
            try
            {
                serverUDPChannel.close();
            } catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
     
    public boolean isStopped()
    {
        return stopped.get();
    }

    //重要，主流程   ！！
    private void readAndHandle() throws IOException
    {
        //运行20000毫秒退出
        long timeout = 20000;
        //这里记录一个start时间
        long startTime = System.currentTimeMillis();
        long endTime;

        //虽然这个看起来是无限循环
        while (true)
        {
        	//留意到timeout <= 0即时间槽用完后这个循环还是会退出
            if (timeout <= 0)
            {
                break;
            }
            
            //这是nio socket 通信模式下的主要过程
            //线程等待它上面所登记的端口产生(可连接，可读)事件，当等待时间超过剩下的时间槽timeout之后仍然没有一个事件触发就返回一个空队列selector
            //否则返回一个事件队列selector
            //selector当成助手好了
            selector.select(timeout);
            //助手发现有信件通知了就下一步到这里，否则在上一步一直卡（也叫做阻塞）或者助手等待过timeout了也没有接收到任何信件和请求通知也会放弃直接到这里，但是这种情况selectedKeys集合空的
            //助手吧信息通知做成关联的钥匙串，通过selectedKeys()可以取得
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext())
            {
            	//工作的时候一个一个抠出下一个钥匙，处理
                SelectionKey selectionKey = iterator.next();
                iterator.remove();
                try
                {
                	//用钥匙打开一个通道
                    Channel channel = selectionKey.channel();
                    //处理tcp连接中的握手(可连接时间)
                    if (selectionKey.isAcceptable())
                    {
                    	//如果这个通道是自己的tcp端口(一般肯定是,因为客户的tcp连接请求是投递给这个固定地址的)
                        if (channel instanceof ServerSocketChannel)
                        {
                        	//接受对方的TCP连接,非阻塞，注册监听这个连接的可读事件
                            ServerSocketChannel server = (ServerSocketChannel) selectionKey.channel();
                            //同意这个连接请求，把客户的socket通道加入自己的端口通信
                            SocketChannel client = server.accept();
                            //被动处理，采用助手提醒的方式，（true)就是自己发现主动处理
                            client.configureBlocking(false);
                            //备档，告诉助手当这个客户发出让我读信件的请求的时候通知我
                            client.register(selector, SelectionKey.OP_READ);
                        }
                    //处理tcp/udp连接中的可读事件
                       //这是一个客户的可读请求
                    } else if (selectionKey.isReadable())
                    {
                        // data from server
                    	//tcp方式发来的(因为是socketchannel)
                        if (channel instanceof SocketChannel)
                        {
                        	//得到客户端的接口sk,从selectionkey那里得来的
                            SocketChannel sk = (SocketChannel) channel;
                            inBuffer.clear();
                            //读一个流
                            sk.read(inBuffer);

                            inBuffer.flip();
                            InputStream in = new ByteArrayInputStream(inBuffer.array(), 0, inBuffer.limit());

                            while (true)
                            {
                            	//把读到的流最终转化成信件类(dataPackage)
                                DataPackage dataPackage = MessageHandler.readDataPackage(in);
                                if (dataPackage == null)
                                {
                                    break;
                                }
                                //信件类型，相应处理
                                switch (dataPackage.getType())
                                {
                                    case GlobalTypeDefine.TYPE_LOGIN:
                                        loginCheck(sk, dataPackage.getSrcId(), dataPackage.getMessageString());
                                        break;
                                    case GlobalTypeDefine.TYPE_REGISTER:
                                        registerCheck(sk, dataPackage.getSrcId(), dataPackage.getDstId(), dataPackage.getMessageString(), dataPackage.getDataIndex());
                                        break;
                                    case GlobalTypeDefine.TYPE_ONLINE_USER:
                                 
                                    	//收到客户端的心跳包信息的情况，种情况客户端发来的信件type是 online_user (我是在线用户)
                                    	//于是把该用户的上次活跃时间记为当前时间！！！！
                                        loginIDClientMap.get(dataPackage.getSrcId()).lastActiveTime = System.currentTimeMillis();
                                        break;
                                    case GlobalTypeDefine.TYPE_OFFLINE:
                                        User offUser = dataPackage.getUser();
                                        userIDMap.remove(offUser.getId());
                                        break;
                                    case GlobalTypeDefine.TYPE_BOARDCAST:
                                        BoardCastMessage(dataPackage.getSrcId(), dataPackage.getMessageString());
                                        break;
                                }

                            }
                            // udp p2p udp方式的信件
                        } else if (channel instanceof DatagramChannel)
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
                                    case GlobalTypeDefine.TYPE_UDP_REG:
                                        User user = dataPackage.getUser();
                                        user.setAddress(MessageHandler.AddresstoString(address));
                                        regUserUDPAddr(user);
                                        break;
                                }
                            }
                        }
                    }
                } catch (IOException e)
                {
                    //e.printStackTrace();
                    selectionKey.cancel();
                }
            }
            //注意到每次执行完，timeout本身会减去这一次循环所消耗的时间(endTime-startTime)
            endTime = System.currentTimeMillis();
            timeout -= (endTime - startTime);
            startTime = endTime;

        }
    }
    
  
    //广播信息
    //srcID ： 谁发的 messageString:发什么
    private void BoardCastMessage(String srcId, String messageString)
    {
    	//做一个DataPackage包 dp
        DataPackage dp = new DataPackage();
        dp.setType(GlobalTypeDefine.TYPE_BOARDCAST);
        dp.setSrcId(srcId);
        dp.setMessageString(messageString);
        
        //dp发给所有的在线用户
        for (ClientInfo clientInfo : loginIDClientMap.values()) {
            MessageHandler.writeDataPackage(dp, outBuffer);
            try {
                clientInfo.client.write(outBuffer);
            } catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
    }

    private void regUserUDPAddr(User user) {
      
        SocketChannel client = loginIDClientMap.get(user.getId()).client;
        //use tcp connection to tell the client udp reg has started
        DataPackage regStarted = new DataPackage();
        regStarted.setType(GlobalTypeDefine.TYPE_UDP_REG);
        regStarted.setAddress(user.getAddress());
        MessageHandler.writeDataPackage(regStarted, outBuffer);
        try
        {
            client.write(outBuffer);
        } catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        DataPackage onlineInfo = new DataPackage();
        onlineInfo.setType(GlobalTypeDefine.TYPE_ONLINE_USER);
        
        DataPackage selfInfo = new DataPackage();
        selfInfo.putUser(user);
        selfInfo.setType(GlobalTypeDefine.TYPE_ONLINE_USER);
        userIDMap.put(user.getId(), user);
        
        //receive online user and tell online user self online-ness
        for (User onlineUser : userIDMap.values()) {
            if (onlineUser.getId().equals(user.getId())) {
                continue;
            }
            onlineInfo.putUser(onlineUser);
            MessageHandler.writeDataPackage(onlineInfo, outBuffer);
            try
            {
                client.write(outBuffer);
            } catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if (!loginIDClientMap.containsKey(onlineUser.getId())) {
                continue;
            }
            SocketChannel otherClient = loginIDClientMap.get(onlineUser.getId()).client;
            MessageHandler.writeDataPackage(selfInfo, outBuffer);
            try
            {
                otherClient.write(outBuffer);
            } catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
        }
        
    }
    
    
    //client端想login, id和 password提供
    private void loginCheck(SocketChannel client, String id, String password)
    {
    	//调用sqlHandle查询id的password是否和提供的对应，如果失败user返回null，否则返回对应user的消息
        User user = sqlHandler.loginCheck(id, password);
        DataPackage dp = new DataPackage();
        dp.setType(GlobalTypeDefine.TYPE_LOGIN);
        
        //成功则弄一条确认消息打包返回给 client
        if (user != null) {
            dp.putUser(user);
            if (userIDMap.containsKey(id)) {
                if (loginIDClientMap.containsKey(id)) {
                	SocketChannel client2 = loginIDClientMap.get(id).client;
                	try {
						if (!client.getRemoteAddress().equals(client2.getRemoteAddress())) {
							sendKickMessage(client2);
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                }
            }
            userIDMap.put(id, user);
            loginIDClientMap.put(id, new ClientInfo(client, System.currentTimeMillis()));
        }
        
        MessageHandler.writeDataPackage(dp, outBuffer);
        try
        {
            client.write(outBuffer);
        } catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    //号被重复登陆顶掉的处理，细节有兴趣自己看
    private void sendKickMessage(SocketChannel client) {
        DataPackage dp = new DataPackage();
        dp.setType(GlobalTypeDefine.TYPE_KICKED);
        MessageHandler.writeDataPackage(dp, outBuffer);
        try
        {
            client.write(outBuffer);
        } catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    private void registerCheck(SocketChannel client, String id, String password, String name, int index)
    {
        boolean success = sqlHandler.registerCheck(id, password, name, index);
        DataPackage dp = new DataPackage();
        dp.setType(GlobalTypeDefine.TYPE_REGISTER);
        if (success) {
            dp.setMessageString("注册成功");
        } else {
            dp.setMessageString("用户名已被使用");
        }
        MessageHandler.writeDataPackage(dp, outBuffer);
        try
        {
            client.write(outBuffer);
        } catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if(success) {
            loginCheck(client, id, password);
        }
    }
    
//这是服务器的主过程（线程 ）
    public static void main(String[] args) {
    	//设定端口
        MiroServer server = null;
        int tcpPort = 9527;
        int udpPort = 9527;
        if (args != null) {
        	for (int i = 0; i < args.length - 1; ++i) {
        		if (args[i].equals("-tcp")) {
        			tcpPort = Integer.parseInt(args[i + 1]);
        		}
        		if (args[i].equals("-udp")) {
        			udpPort = Integer.parseInt(args[i + 1]);
        		}
        	}
        }
        
        try {
        	
        	//在对应的端口开启监听serversocket, 初始化数据库处理类
            server = new MiroServer(tcpPort, udpPort);
            sqlHandler = SqliteHandler.getInstance();
            
            //开一个小窗口提示用户，这个窗口关闭了就同时关闭服务器
            ServerMonitorUI sm = new ServerMonitorUI(server.getStopped());
            sm.setVisible(true);
            
            //在关闭前无限循环，每次循环分为两个部分
            while (!server.isStopped()) {
            	//timeout = 20000，即在 20秒之内读取客户端连接和数据，做出对应相应(1)
                server.readAndHandle();
                //查看连接上的客户端列表，及时清理下线和掉线的客户端(2)
                server.checkHeartBeat();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (server != null) {
                server.closeChannel();
            }
        }
    }

    public AtomicBoolean getStopped() {
		return stopped;
	}
    
	private void checkHeartBeat()
    {
        Iterator<Map.Entry<String, ClientInfo>> it = loginIDClientMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, ClientInfo> entry = it.next();
            long time = System.currentTimeMillis() - entry.getValue().lastActiveTime;
            //这个CLIENT_LIVE_PERIOD好像是60秒吧
            //遍历登录上的用户列表，把一分钟内没有任何心跳包发送过来的用户手动断开连接并刷新在线用户列表
            //判断方式就是currentTime - lastActivetime 》 60
            if (time > CLIENT_LIVE_PERIOD) {
                userIDMap.remove(entry.getKey());
                it.remove();
                continue;
            }
        }
        
    }
}
