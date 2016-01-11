package minet.ui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import common.GlobalTypeDefine;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import javax.swing.JComboBox;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.io.Closeable;
import java.net.URL;
import javax.swing.JTextArea;
import javax.swing.UIManager;

public class AddMemberUI extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7558230556473795575L;
	private JPanel contentPane;
	private JTextField textFieldID;
	private JTextField textFieldName;
	private JPasswordField passwordField;
	private JPasswordField passwordFieldV;
	private JTextArea textHint;
	private JComboBox<ImageIcon> comboBox;
	private JButton buttonReg;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					AddMemberUI frame = new AddMemberUI();
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
	public AddMemberUI() {
		setResizable(false);
		setTitle("Minet-AddMember");
		setBounds(100, 100, 340, 200);
		contentPane = new JPanel(){  
            private static final long serialVersionUID = 1L;  
            
            protected void paintComponent(Graphics g) {  
                Graphics2D g2 = (Graphics2D) g;  
                super.paintComponent(g);  
                // 绘制渐变  
                g2.setPaint(new GradientPaint(0, 0, new Color(255, 255, 225), getWidth(),  getHeight(), new Color(232, 105, 180)));  
                g2.fillRect(0, 0, getWidth(), getHeight());  
            }  
        }; 
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel labelID = new JLabel("\u7528\u6237\u540D");
		labelID.setBounds(47, 46, 54, 15);
		contentPane.add(labelID);
		
		JLabel labelName = new JLabel("\u6635\u79F0");
		labelName.setBounds(47, 82, 54, 15);
		contentPane.add(labelName);
		
		textFieldID = new JTextField();
		textFieldID.setBounds(111, 44, 110, 21);
		textFieldID.addKeyListener(new KeyAdapter(){
			public void keyTyped(KeyEvent e) {				
				if(textFieldID.getText().length() >= 12) {	
					e.consume(); 
				}
			}
		});
		contentPane.add(textFieldID);
		textFieldID.setColumns(10);
		
		textFieldName = new JTextField();
		textFieldName.setColumns(10);
		textFieldName.setBounds(111, 79, 110, 21);
		textFieldName.addKeyListener(new KeyAdapter(){
			public void keyTyped(KeyEvent e) {				
				if(textFieldName.getText().length() >= 12) {	
					e.consume(); 
				}
			}
		});
		contentPane.add(textFieldName);
		
		buttonReg = new JButton("确定");
		buttonReg.setBounds(123, 115, 81, 23);
		contentPane.add(buttonReg);
		
		JLabel label = new JLabel("请输入加入群聊的用户的用户名和ID");
		label.setForeground(new Color(128, 128, 0));
		label.setFont(UIManager.getFont("Button.font"));
		label.setBounds(70, 14, 220, 15);
		contentPane.add(label);
		
		
		JLabel labelIDTip = new JLabel("6-12\u5B57\u7B26");
		labelIDTip.setForeground(SystemColor.textHighlight);
		labelIDTip.setFont(new Font("黑体", Font.PLAIN, 10));
		labelIDTip.setBounds(232, 47, 67, 15);
		contentPane.add(labelIDTip);
		
		JLabel labelNameTip = new JLabel("\u4E2D\u82F1\u5B57\u7B26,\u975E\u7A7A");
		labelNameTip.setForeground(SystemColor.textHighlight);
		labelNameTip.setFont(new Font("黑体", Font.PLAIN, 10));
		labelNameTip.setBounds(231, 83, 67, 15);
		contentPane.add(labelNameTip);
		
		textHint = new JTextArea();
		textHint.setOpaque(false);
		textHint.setEditable(false);
		textHint.setForeground(new Color(250, 20, 90));
		textHint.setLineWrap(true);
		textHint.setFont(new Font("黑体", Font.PLAIN, 10));
		textHint.setText("");
		textHint.setBounds(20, 271, 81, 35);
		contentPane.add(textHint);
	
		setLocationRelativeTo(null);
	}

    public JComboBox<ImageIcon> getComboBox()
    {
        return comboBox;
    }

    public JButton getButtonReg()
    {
        return buttonReg;
    }

    public JTextField getTextFieldID()
    {
        return textFieldID;
    }

    public JTextField getTextFieldName()
    {
        return textFieldName;
    }

    public JPasswordField getPasswordField()
    {
        return passwordField;
    }

    public JPasswordField getPasswordFieldV()
    {
        return passwordFieldV;
    }

    public JTextArea getTextHint()
    {
        return textHint;
    }
}
