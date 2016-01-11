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
	//�������ߵ����ߵ��û�
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
    	//����Ҫ�õ��ı�����ʼ��
        stopped = new AtomicBoolean(false);
        loginIDClientMap = new HashMap<String, ClientInfo>();
        userIDMap = new HashMap<String, User>();
        inBuffer = ByteBuffer.allocate(64 * 1024);
        outBuffer = ByteBuffer.allocate(64 * 1024);
        
        selector = Selector.open();
        serverTCPChannel = ServerSocketChannel.open();
        
        //��ʼ������TCP�˿�
        SocketAddress address = new InetSocketAddress(tcpPort);
        serverTCPChannel.socket().setReuseAddress(true);
        serverTCPChannel.bind(address);
        serverTCPChannel.configureBlocking(false);
        serverTCPChannel.register(selector, SelectionKey.OP_ACCEPT);
        
        //��ʼ������UDP�˿�
        address = new InetSocketAddress(udpPort);
        serverUDPChannel = DatagramChannel.open();
        serverUDPChannel.socket().setReuseAddress(true);
        serverUDPChannel.bind(address);
        serverUDPChannel.configureBlocking(false);
        serverUDPChannel.register(selector, SelectionKey.OP_READ);
    }
    
    //�ر�ʱִ��
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

    //��Ҫ��������   ����
    private void readAndHandle() throws IOException
    {
        //����20000�����˳�
        long timeout = 20000;
        //�����¼һ��startʱ��
        long startTime = System.currentTimeMillis();
        long endTime;

        //��Ȼ���������������ѭ��
        while (true)
        {
        	//���⵽timeout <= 0��ʱ�����������ѭ�����ǻ��˳�
            if (timeout <= 0)
            {
                break;
            }
            
            //����nio socket ͨ��ģʽ�µ���Ҫ����
            //�̵߳ȴ����������ǼǵĶ˿ڲ���(�����ӣ��ɶ�)�¼������ȴ�ʱ�䳬��ʣ�µ�ʱ���timeout֮����Ȼû��һ���¼������ͷ���һ���ն���selector
            //���򷵻�һ���¼�����selector
            //selector�������ֺ���
            selector.select(timeout);
            //���ַ������ż�֪ͨ�˾���һ���������������һ��һֱ����Ҳ�����������������ֵȴ���timeout��Ҳû�н��յ��κ��ż�������֪ͨҲ�����ֱ�ӵ���������������selectedKeys���Ͽյ�
            //���ְ���Ϣ֪ͨ���ɹ�����Կ�״���ͨ��selectedKeys()����ȡ��
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext())
            {
            	//������ʱ��һ��һ���ٳ���һ��Կ�ף�����
                SelectionKey selectionKey = iterator.next();
                iterator.remove();
                try
                {
                	//��Կ�״�һ��ͨ��
                    Channel channel = selectionKey.channel();
                    //����tcp�����е�����(������ʱ��)
                    if (selectionKey.isAcceptable())
                    {
                    	//������ͨ�����Լ���tcp�˿�(һ��϶���,��Ϊ�ͻ���tcp����������Ͷ�ݸ�����̶���ַ��)
                        if (channel instanceof ServerSocketChannel)
                        {
                        	//���ܶԷ���TCP����,��������ע�����������ӵĿɶ��¼�
                            ServerSocketChannel server = (ServerSocketChannel) selectionKey.channel();
                            //ͬ������������󣬰ѿͻ���socketͨ�������Լ��Ķ˿�ͨ��
                            SocketChannel client = server.accept();
                            //�������������������ѵķ�ʽ����true)�����Լ�������������
                            client.configureBlocking(false);
                            //�������������ֵ�����ͻ��������Ҷ��ż��������ʱ��֪ͨ��
                            client.register(selector, SelectionKey.OP_READ);
                        }
                    //����tcp/udp�����еĿɶ��¼�
                       //����һ���ͻ��Ŀɶ�����
                    } else if (selectionKey.isReadable())
                    {
                        // data from server
                    	//tcp��ʽ������(��Ϊ��socketchannel)
                        if (channel instanceof SocketChannel)
                        {
                        	//�õ��ͻ��˵Ľӿ�sk,��selectionkey���������
                            SocketChannel sk = (SocketChannel) channel;
                            inBuffer.clear();
                            //��һ����
                            sk.read(inBuffer);

                            inBuffer.flip();
                            InputStream in = new ByteArrayInputStream(inBuffer.array(), 0, inBuffer.limit());

                            while (true)
                            {
                            	//�Ѷ�����������ת�����ż���(dataPackage)
                                DataPackage dataPackage = MessageHandler.readDataPackage(in);
                                if (dataPackage == null)
                                {
                                    break;
                                }
                                //�ż����ͣ���Ӧ����
                                switch (dataPackage.getType())
                                {
                                    case GlobalTypeDefine.TYPE_LOGIN:
                                        loginCheck(sk, dataPackage.getSrcId(), dataPackage.getMessageString());
                                        break;
                                    case GlobalTypeDefine.TYPE_REGISTER:
                                        registerCheck(sk, dataPackage.getSrcId(), dataPackage.getDstId(), dataPackage.getMessageString(), dataPackage.getDataIndex());
                                        break;
                                    case GlobalTypeDefine.TYPE_ONLINE_USER:
                                 
                                    	//�յ��ͻ��˵���������Ϣ�������������ͻ��˷������ż�type�� online_user (���������û�)
                                    	//���ǰѸ��û����ϴλ�Ծʱ���Ϊ��ǰʱ�䣡������
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
                            // udp p2p udp��ʽ���ż�
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
            //ע�⵽ÿ��ִ���꣬timeout������ȥ��һ��ѭ�������ĵ�ʱ��(endTime-startTime)
            endTime = System.currentTimeMillis();
            timeout -= (endTime - startTime);
            startTime = endTime;

        }
    }
    
  
    //�㲥��Ϣ
    //srcID �� ˭���� messageString:��ʲô
    private void BoardCastMessage(String srcId, String messageString)
    {
    	//��һ��DataPackage�� dp
        DataPackage dp = new DataPackage();
        dp.setType(GlobalTypeDefine.TYPE_BOARDCAST);
        dp.setSrcId(srcId);
        dp.setMessageString(messageString);
        
        //dp�������е������û�
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
    
    
    //client����login, id�� password�ṩ
    private void loginCheck(SocketChannel client, String id, String password)
    {
    	//����sqlHandle��ѯid��password�Ƿ���ṩ�Ķ�Ӧ�����ʧ��user����null�����򷵻ض�Ӧuser����Ϣ
        User user = sqlHandler.loginCheck(id, password);
        DataPackage dp = new DataPackage();
        dp.setType(GlobalTypeDefine.TYPE_LOGIN);
        
        //�ɹ���Ūһ��ȷ����Ϣ������ظ� client
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
    
    //�ű��ظ���½�����Ĵ���ϸ������Ȥ�Լ���
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
            dp.setMessageString("ע��ɹ�");
        } else {
            dp.setMessageString("�û����ѱ�ʹ��");
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
    
//���Ƿ������������̣��߳� ��
    public static void main(String[] args) {
    	//�趨�˿�
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
        	
        	//�ڶ�Ӧ�Ķ˿ڿ�������serversocket, ��ʼ�����ݿ⴦����
            server = new MiroServer(tcpPort, udpPort);
            sqlHandler = SqliteHandler.getInstance();
            
            //��һ��С������ʾ�û���������ڹر��˾�ͬʱ�رշ�����
            ServerMonitorUI sm = new ServerMonitorUI(server.getStopped());
            sm.setVisible(true);
            
            //�ڹر�ǰ����ѭ����ÿ��ѭ����Ϊ��������
            while (!server.isStopped()) {
            	//timeout = 20000������ 20��֮�ڶ�ȡ�ͻ������Ӻ����ݣ�������Ӧ��Ӧ(1)
                server.readAndHandle();
                //�鿴�����ϵĿͻ����б���ʱ�������ߺ͵��ߵĿͻ���(2)
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
            //���CLIENT_LIVE_PERIOD������60���
            //������¼�ϵ��û��б���һ������û���κ����������͹������û��ֶ��Ͽ����Ӳ�ˢ�������û��б�
            //�жϷ�ʽ����currentTime - lastActivetime �� 60
            if (time > CLIENT_LIVE_PERIOD) {
                userIDMap.remove(entry.getKey());
                it.remove();
                continue;
            }
        }
        
    }
}
