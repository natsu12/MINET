package miro.sqlconnect;

import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import common.MessageHandler;
import common.datastruct.User;

public class SqliteHandler
{
    public static final String USER_TABLE_NAME = "user"; 
    Connection sqlConnection;
    Statement statement;
    private SqliteHandler() {
        try
        {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        sqlConnection = null;
        //ȡ����Ҫ���ӵ����ݿ��ַ
        URL url = this.getClass().getResource("/resource/db/miusers.db");
        String path = url.getPath();
        try
        {
        	//jdbc�����������ʽ���ģ���������ϸ��
            sqlConnection = DriverManager.getConnection("jdbc:sqlite:" + path);
            statement = sqlConnection.createStatement();
            statement.setQueryTimeout(2);
        } catch (SQLException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }             
    }
    
    //����ģʽ��ת���ã�Ҳ��û����ô����
    private static class SqliteHandlerLoader {
        private static final SqliteHandler instance = new SqliteHandler();
    }
    
    public static SqliteHandler getInstance() {
        return SqliteHandlerLoader.instance;
    }
    
    //��¼��֤
    public User loginCheck(String id, String password) {
        User user = null;
       
        //�ȼ�鴫���ĸ�ʽ�ǲ��ǺϷ�
        if (!MessageHandler.checkIdFormat(id) || !MessageHandler.checkPasswordFormat(password)) {
            return null;
        }
        //����md5���ܴ���
        String mdPass = MessageHandler.getMD5String(password);
        
        //���Ĳ�ѯ���� 
        ResultSet rs = null;
        //����ܿ�����
        String query = "select * from " + USER_TABLE_NAME + " where id = \"" + id + "\"";
        try
        {
        	//ִ�в�ѯ���������rs����
            rs = statement.executeQuery(query);
            if (rs.next()) {
                String pass = rs.getString("password");
                String name = rs.getString("name");
                int index = rs.getInt("imageindex");
                //������֤
                if (!pass.equals(mdPass)) {
                    return null;
                }
                //�����ͨ��������user��Ϣ
                user = new User(name, id, index);
            }
        } catch (SQLException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //�����֤ʧ�ܣ����ص���null
        return user;
    }

    public boolean registerCheck(String id, String password, String name, int index)
    {
        if (!MessageHandler.checkIdFormat(id) || !MessageHandler.checkPasswordFormat(password) 
                || !MessageHandler.checkNameFormat(name)) {
            return false;
        }
        String mdPass = MessageHandler.getMD5String(password);
        ResultSet rs = null;
        try
        {
            synchronized (this)
            {
                String query = "select id from " + USER_TABLE_NAME + " where id = \"" + id + "\"";
                rs = statement.executeQuery(query);
                if (rs.next())
                {
                    return false;
                }
                String ins = "insert into " + USER_TABLE_NAME + "(id, name, password, imageindex) values (" +
                "\"" + id + "\",\"" + name + "\",\"" + mdPass + "\",\"" + index + "\");";
                System.out.println(ins);
                statement.executeUpdate(ins);
            }
        } catch (SQLException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return true;
    }
    
}
