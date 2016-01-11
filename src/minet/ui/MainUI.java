package minet.ui;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JTextArea;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JTextPane;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.awt.Color;
import java.awt.ComponentOrientation;

import javax.swing.tree.DefaultTreeModel;

import common.MessageHandler;
import common.datastruct.User;
import minet.ui.misc.BlobLabel;
import minet.ui.misc.MiTreeCellRender;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

public class MainUI extends JFrame {

	/**
     * 
     */
    private static final long serialVersionUID = 1566507555784702779L;
    private JPanel contentPane;
	private JLabel headLabel;
	private JTextArea inputArea;
	private JTextPane viewPane;
	private JLabel nameLabel;
	private JLabel ipLabel;
	private JLabel idLabel;
	private JLabel helloTipsLabel;
	private JTree tree;
	private JButton sendButton; 
	private JButton groupButton;
	private String indentSpace;
	private ArrayList<User> userlist;
	
	public JTree getTree()
    {
        return tree;
    }

    public void setTree(JTree tree)
    {
        this.tree = tree;
    }
    
    public ArrayList<User> getUserList() 
    {
    	return userlist;
    }

    public JLabel getHeadLabel()
    {
        return headLabel;
    }


    public JTextArea getInputArea()
    {
        return inputArea;
    }


    public JTextPane getViewPane()
    {
        return viewPane;
    }


    public JLabel getNameLabel()
    {
        return nameLabel;
    }


    public JLabel getIpLabel()
    {
        return ipLabel;
    }


    public JLabel getIdLabel()
    {
        return idLabel;
    }


    public JLabel getHelloTipsLabel()
    {
        return helloTipsLabel;
    }

    public String getInputText() {
        return inputArea.getText();
    }
    
    public void renewFriendTree(ArrayList<User> userList)
    {
        tree.setModel(new DefaultTreeModel(
                new DefaultMutableTreeNode(new User("小伙伴们", null, -1))
                {
                    private static final long serialVersionUID = -1248389227964517013L;
                    {
                        for (User user : userList) {
                            add(new DefaultMutableTreeNode(user));
                        }
                    }
                }));
    }
    
    public void addOnlineFriend(User user)
    {
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        DefaultMutableTreeNode onlineTree = (DefaultMutableTreeNode) model.getRoot();
        onlineTree.add(new DefaultMutableTreeNode(user));
        model.reload();
    }
    
    public User deleteOnlineFriend(String id)
    {
        User user = null;
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        DefaultMutableTreeNode onlineTree = (DefaultMutableTreeNode) model.getRoot();
        DefaultMutableTreeNode node = new DefaultMutableTreeNode();
        if (onlineTree.getChildCount() > 0) {
            for (Enumeration<?> e = onlineTree.children(); e.hasMoreElements();) {
                node = (DefaultMutableTreeNode) e.nextElement();
                User currentUser = (User) node.getUserObject();
                if (currentUser.getId().equals(id)) {
                    user = currentUser;
                    onlineTree.remove(node);
                    model.reload();
                }
            }
        }
        
        return user;
    }
    
    public void insertMessage(String name, String message, ImageIcon tImg, boolean self) {
        
        
        javax.swing.text.SimpleAttributeSet attributeSet=new javax.swing.text.SimpleAttributeSet();
        
        StyledDocument doc = viewPane.getStyledDocument();  
        
        if (indentSpace == null) {
            indentSpace = "";
            int wLimit = 155;
            FontMetrics fontMetrics = getFontMetrics(doc.getFont(attributeSet));
            while (fontMetrics.stringWidth(indentSpace) < wLimit) {
            	indentSpace += ' ';
            }
        }
               
        try
        {
            doc.insertString(doc.getLength(), "\n" + MessageHandler.getTimeString(name) + "\n", attributeSet);
            BlobLabel blb = new BlobLabel();
            blb.applyText(message);
            blb.setIcon(tImg);
            if (self) {
                doc.insertString(doc.getLength(), indentSpace, attributeSet);
                blb.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            } else {
                blb.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
            }
            viewPane.setCaretPosition(doc.getLength());
            viewPane.insertComponent(blb);
            doc.insertString(doc.getLength(), "\n\n", attributeSet);
        } catch (BadLocationException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
    
    /**
	 * Create the frame.
	 */    
	public MainUI(User user) {
	   
	    
		setResizable(false);
		setTitle("Minet-main");
      
		setBounds(100, 100, 649, 568);
		contentPane = new JPanel(){  
            private static final long serialVersionUID = 1L;  
            
            protected void paintComponent(Graphics g) {  
                Graphics2D g2 = (Graphics2D) g;  
                super.paintComponent(g);  
                // 绘制渐变  
                g2.setPaint(new GradientPaint(0, 0, new Color(255, 225, 255), getWidth()/2,  getHeight()/2, new Color(255, 253, 195)));  
                g2.fillRect(0, 0, getWidth(), getHeight());  
            }  
        }; 
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JScrollPane scrollPaneR = new JScrollPane();
		scrollPaneR.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPaneR.setBounds(430, 125, 193, 395);
		contentPane.add(scrollPaneR);
		
		tree = new JTree();
		tree.setCellRenderer(new MiTreeCellRender());  
		tree.setAutoscrolls(true);
		
		ArrayList<User> userList = new ArrayList<User>();
		userlist = userList;
        tree.setVisibleRowCount(500);
       
       // tree.setMaximumSize(new Dimension(400, 600));
        scrollPaneR.setColumnHeaderView(tree);
		renewFriendTree(userList);
		tree.setAutoscrolls(true);
		headLabel = new JLabel("");
		headLabel.setBounds(527, 10, 80, 80);
		headLabel.setIcon(user.getIcon());
		contentPane.add(headLabel);
		scrollPaneR.setViewportView(tree);
		
		sendButton = new JButton("\u53D1\u9001");
		sendButton.setBounds(10, 497, 80, 23);
		contentPane.add(sendButton);
		
		inputArea = new JTextArea();
		inputArea.setLineWrap(true);
		JScrollPane scrollPaneLD = new JScrollPane(inputArea);
		scrollPaneLD.setBounds(10, 387, 410, 100);
		contentPane.add(scrollPaneLD);
		
		JScrollPane scrollPaneLU = new JScrollPane();
		scrollPaneLU.setBounds(10, 125, 410, 225);
		contentPane.add(scrollPaneLU);
		
		viewPane = new JTextPane();
		viewPane.setEditable(false);
		viewPane.setLayout(null);
		scrollPaneLU.setViewportView(viewPane);
		
		JLabel tipLabel = new JLabel("希望你矜持一点");
		tipLabel.setFont(new Font("宋体", Font.PLAIN, 12));
		tipLabel.setBounds(10, 360, 396, 15);
		contentPane.add(tipLabel);
		
		JLabel surpriseLabel = new JLabel("");
		surpriseLabel.setIcon(new ImageIcon(MainUI.class.getResource("/resource/imgs/surprise.gif")));
		surpriseLabel.setBounds(10, 10, 100, 100);
		contentPane.add(surpriseLabel);
		
		nameLabel = new JLabel(user.getName());
		nameLabel.setFont(new Font("宋体", Font.PLAIN, 12));
		nameLabel.setBounds(429, 22, 88, 15);
		contentPane.add(nameLabel);
		
		idLabel = new JLabel(user.getId());
		idLabel.setForeground(Color.MAGENTA);
		idLabel.setFont(new Font("Arial", Font.PLAIN, 11));
		idLabel.setBounds(430, 47, 88, 15);
		contentPane.add(idLabel);
		
		ipLabel = new JLabel("\u5B9A\u4F4D\u4E2D\u2026\u2026");
		ipLabel.setHorizontalAlignment(SwingConstants.CENTER);
		ipLabel.setVerticalAlignment(SwingConstants.TOP);
		ipLabel.setForeground(new Color(184, 134, 11));
		ipLabel.setFont(new Font("黑体", Font.PLAIN, 11));
		ipLabel.setBounds(505, 100, 124, 15);
		contentPane.add(ipLabel);
		
		helloTipsLabel = new JLabel("\u6211\u5728\u9A6C\u8DEF\u8FB9\uFF0C\u8D70\u5440\u8D70\u5440\u8D70~");
		helloTipsLabel.setFont(new Font("黑体", Font.PLAIN, 12));
		helloTipsLabel.setForeground(SystemColor.textHighlight);
		helloTipsLabel.setBounds(119, 57, 288, 15);
		contentPane.add(helloTipsLabel);
		
		JButton clearButton = new JButton("\u6E05\u5C4F");
		clearButton.setBounds(340, 497, 80, 23);
		clearButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                viewPane.setText("");
            }
        });
		
		groupButton = new JButton("群聊发起");
		groupButton.setBounds(430, 100, 90, 23);
		/*groupButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
            	//
            	GroupChatUI groupchatui = new GroupChatUI(user); // 这里应该获取需要群聊用户的列表
            	groupchatui.setVisible(true);
            }
        });*/
		
		contentPane.add(clearButton);
		contentPane.add(groupButton);
	}


    public JButton getSendButton()
    {
        return sendButton;
    }
    
    public JButton getGroupButton()
    {
        return groupButton;
    }
    
}
