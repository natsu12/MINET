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
        //取得你要连接的数据库地址
        URL url = this.getClass().getResource("/resource/db/miusers.db");
        String path = url.getPath();
        try
        {
        	//jdbc连接命令，按格式炒的，不用问我细节
            sqlConnection = DriverManager.getConnection("jdbc:sqlite:" + path);
            statement = sqlConnection.createStatement();
            statement.setQueryTimeout(2);
        } catch (SQLException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }             
    }
    
    //单例模式，转笔用，也许并没有设么卵用
    private static class SqliteHandlerLoader {
        private static final SqliteHandler instance = new SqliteHandler();
    }
    
    public static SqliteHandler getInstance() {
        return SqliteHandlerLoader.instance;
    }
    
    //登录认证
    public User loginCheck(String id, String password) {
        User user = null;
       
        //先检查传来的格式是不是合法
        if (!MessageHandler.checkIdFormat(id) || !MessageHandler.checkPasswordFormat(password)) {
            return null;
        }
        //密码md5加密处理
        String mdPass = MessageHandler.getMD5String(password);
        
        //抄的查询命令 
        ResultSet rs = null;
        //这个能看懂吧
        String query = "select * from " + USER_TABLE_NAME + " where id = \"" + id + "\"";
        try
        {
        	//执行查询，结果存入rs里面
            rs = statement.executeQuery(query);
            if (rs.next()) {
                String pass = rs.getString("password");
                String name = rs.getString("name");
                int index = rs.getInt("imageindex");
                //密码验证
                if (!pass.equals(mdPass)) {
                    return null;
                }
                //如果都通过，构造user信息
                user = new User(name, id, index);
            }
        } catch (SQLException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //如果验证失败，返回的是null
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
