package minet.ui;

import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import java.net.URL;

import javax.swing.SwingConstants;
import javax.swing.JButton;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.ImageIcon;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

import common.MessageHandler;
import common.datastruct.User;
import minet.ui.misc.BlobLabel;

import javax.swing.JLabel;

public class UserChatUI extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3794123843953430482L;
	private JPanel contentPane;
	private JTextArea inputArea;
	private JTextPane chatPane;
	private JLabel headLabel;
	private JLabel labelID;
	private JLabel nameLabel;
	private JLabel label;
	private JLabel label_1;
	private JButton sendButton;
	private JButton imageButton;
	private JButton fileButton;
	private String indentSpace;
	private String id;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UserChatUI frame = new UserChatUI(new User("1", "1", 1));
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public UserChatUI(User user) {
		id = user.getId();
		this.setTitle(user.getName() + "(" + user.getId() + ")" + "的会话");
		setResizable(false);

		setBounds(100, 100, 519, 465);
		contentPane = new JPanel();
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JPanel panelL = new JPanel(){  
            private static final long serialVersionUID = 1L;  
            
            protected void paintComponent(Graphics g) {  
                Graphics2D g2 = (Graphics2D) g;  
                super.paintComponent(g);  
                // 绘制渐变  
                g2.setPaint(new GradientPaint(0, 0, new Color(144, 205, 225), getWidth(),  getHeight(), new Color(240, 240, 255)));  
                g2.fillRect(0, 0, getWidth(), getHeight());  
            }  
        }; 
		panelL.setBorder(null);
		panelL.setBounds(0, 0, 150, 447);
		contentPane.add(panelL);
		panelL.setLayout(null);
		
		headLabel = new JLabel("");
		headLabel.setBorder(null);
		headLabel.setBounds(33, 10, 80, 80);
		headLabel.setIcon(user.getIcon());
		panelL.add(headLabel);
		
		nameLabel = new JLabel(user.getName());
		nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
		nameLabel.setBounds(33, 100, 79, 15);
		panelL.add(nameLabel);
		
		JLabel lblid = new JLabel("\u7269\u79CDID:");
		lblid.setHorizontalAlignment(SwingConstants.CENTER);
		lblid.setFont(new Font("宋体", Font.PLAIN, 12));
		lblid.setBounds(46, 140, 57, 21);
		panelL.add(lblid);
		
		labelID = new JLabel(user.getId());
		labelID.setFont(new Font("Arial", Font.PLAIN, 11));
		labelID.setForeground(Color.MAGENTA);
		labelID.setHorizontalAlignment(SwingConstants.CENTER);
		labelID.setBounds(20, 165, 106, 15);
		panelL.add(labelID);
		
		JLabel lblIP = new JLabel("\u901A\u4FE1\u5730\u5740:");
		lblIP.setHorizontalAlignment(SwingConstants.CENTER);
		lblIP.setFont(new Font("宋体", Font.PLAIN, 12));
		lblIP.setBounds(46, 201, 57, 21);
		panelL.add(lblIP);
		
		label = new JLabel(user.getAddress());
		label.setFont(new Font("Arial", Font.PLAIN, 11));
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setForeground(new Color(184, 134, 11));
		label.setBounds(10, 226, 135, 15);
		panelL.add(label);
		
		JLabel labelWatchCat = new JLabel("");
		labelWatchCat.setBounds(10, 370, 50, 50);
		URL urlc = this.getClass().getResource("/resource/imgs/cat_watch.jpg");
		labelWatchCat.setIcon(new ImageIcon(urlc));
		panelL.add(labelWatchCat);
		
		label_1 = new JLabel("\u901A\u4FE1\u6301\u7EED\u4E2D");
		label_1.setForeground(new Color(50, 205, 50));
		label_1.setHorizontalAlignment(SwingConstants.CENTER);
		label_1.setBounds(70, 389, 75, 15);
		panelL.add(label_1);
		
		JPanel panelR = new JPanel(){  
            private static final long serialVersionUID = 1L;  
            
            protected void paintComponent(Graphics g) {  
                Graphics2D g2 = (Graphics2D) g;  
                super.paintComponent(g);  
                // 绘制渐变  
                g2.setPaint(new GradientPaint(0, 0, new Color(231, 216, 189), getWidth(),  getHeight(), new Color(152, 251, 152)));  
                g2.fillRect(0, 0, getWidth(), getHeight());  
            }  
        }; 
		panelR.setBorder(null);
		panelR.setBounds(150, 0, 367, 447);
		contentPane.add(panelR);
		panelR.setLayout(null);
		
		JScrollPane scrollPaneU = new JScrollPane();
		scrollPaneU.setBounds(8, 8, 347, 270);
		panelR.add(scrollPaneU);
		
		chatPane = new JTextPane();
		chatPane.setEditable(false);
		scrollPaneU.setViewportView(chatPane);
		
		inputArea = new JTextArea();
		inputArea.setLineWrap(true);
		inputArea.setFont(new Font("黑体", Font.PLAIN, 14));
		JScrollPane scrollPaneD = new JScrollPane(inputArea);
		scrollPaneD.setBounds(8, 285, 347, 113);
		panelR.add(scrollPaneD);
		
		sendButton = new JButton("\u53D1\u9001");
		sendButton.setBounds(8, 406, 80, 23);
		panelR.add(sendButton);
		
		fileButton = new JButton("");
		fileButton.setBackground(SystemColor.menu);
		URL urlf = this.getClass().getResource("/resource/imgs/file.png");
		fileButton.setIcon(new ImageIcon(urlf));
		fileButton.setBounds(299, 402, 32, 32);
		panelR.add(fileButton);
		
		imageButton = new JButton("");
		imageButton.setBackground(SystemColor.menu);
		URL urli = this.getClass().getResource("/resource/imgs/image.png");
		imageButton.setIcon(new ImageIcon(urli));
		imageButton.setBounds(256, 402, 32, 32);
		panelR.add(imageButton);
		
		JButton clearButton = new JButton("\u6E05\u5C4F");
		clearButton.setBounds(98, 406, 80, 23);
		clearButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                chatPane.setText("");
            }
        });
		panelR.add(clearButton);
		
	}
	
	public String getId() {
		return id;
	}

	public JTextArea getInputArea()
    {
        return inputArea;
    }

    public JTextPane getChatPane()
    {
        return chatPane;
    }

    public JLabel getHeadLabel()
    {
        return headLabel;
    }

    public JLabel getLabelID()
    {
        return labelID;
    }

    public JLabel getNameLabel()
    {
        return nameLabel;
    }

    public JButton getSendButton()
    {
        return sendButton;
    }

    public JButton getImageButton()
    {
        return imageButton;
    }

    public JButton getFileButton()
    {
        return fileButton;
    }

    public void insertMessage(String name, String message, ImageIcon tImg, boolean self) {
        javax.swing.text.SimpleAttributeSet attributeSet=new javax.swing.text.SimpleAttributeSet();
        
        StyledDocument doc = chatPane.getStyledDocument();  
        
        if (indentSpace == null) {
            indentSpace = "";
            int wLimit = 45;
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
                doc.insertString(doc.getLength(), "     ", attributeSet);
                blb.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            } else {
                blb.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
            }
            chatPane.setCaretPosition(doc.getLength());
            chatPane.insertComponent(blb);
            doc.insertString(doc.getLength(), "\n\n", attributeSet);
        } catch (BadLocationException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
	
    public void insertImg(String name, String path, ImageIcon tImg, boolean self) {
        javax.swing.text.SimpleAttributeSet attributeSet=new javax.swing.text.SimpleAttributeSet();
        
        StyledDocument doc = chatPane.getStyledDocument();  
        
        if (indentSpace == null) {
            indentSpace = "";
            int wLimit = 45;
            FontMetrics fontMetrics = getFontMetrics(doc.getFont(attributeSet));
            while (fontMetrics.stringWidth(indentSpace) < wLimit) {
            	indentSpace += ' ';
            }
        }
        
        
        try
        {
            doc.insertString(doc.getLength(), "\n" + MessageHandler.getTimeString(name) + "\n", attributeSet);
            BlobLabel blb = new BlobLabel();
            
            blb.setIcon(tImg);
            if (self) {
            	blb.applyImg(path, true);
                doc.insertString(doc.getLength(), "     ", attributeSet);
                blb.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            } else {
            	blb.applyImg(path, false);
                blb.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
            }
            chatPane.setCaretPosition(doc.getLength());
            chatPane.insertComponent(blb);
            doc.insertString(doc.getLength(), "\n\n", attributeSet);
        } catch (BadLocationException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    
	public UserChatUI updateInfo(User user) {
	    label.setText(user.getAddress());
	    return this;
	}
}
