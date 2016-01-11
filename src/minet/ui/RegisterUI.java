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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.URL;
import javax.swing.JTextArea;
import javax.swing.UIManager;

public class RegisterUI extends JFrame {

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
					RegisterUI frame = new RegisterUI();
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
	public RegisterUI() {
		setResizable(false);
		setTitle("Minet-Register");
		setBounds(100, 100, 340, 382);
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
		
		JLabel labelPassword = new JLabel("\u5BC6\u7801");
		labelPassword.setBounds(47, 117, 54, 15);
		contentPane.add(labelPassword);
		
		JLabel labelVPassword = new JLabel("\u786E\u8BA4\u5BC6\u7801");
		labelVPassword.setBounds(47, 154, 54, 15);
		contentPane.add(labelVPassword);
		
		JLabel labelHeadImg = new JLabel("\u5934\u9885\u9009\u62E9");
		labelHeadImg.setBounds(47, 201, 54, 15);
		contentPane.add(labelHeadImg);
		
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
		
		passwordField = new JPasswordField();
		passwordField.setBounds(111, 114, 110, 21);
		passwordField.addKeyListener(new KeyAdapter(){
			public void keyTyped(KeyEvent e) {				
				if(passwordField.getPassword().length >= 12) {	
					e.consume(); 
				}
			}
		});
		contentPane.add(passwordField);
		
		passwordFieldV = new JPasswordField();
		passwordFieldV.setBounds(111, 151, 110, 21);
		passwordFieldV.addKeyListener(new KeyAdapter(){
			public void keyTyped(KeyEvent e) {				
				if(passwordFieldV.getPassword().length >= 12) {	
					e.consume(); 
				}
			}
		});
		contentPane.add(passwordFieldV);
		
		buttonReg = new JButton("\u6CE8\u518C");
		buttonReg.setBounds(123, 310, 81, 23);
		contentPane.add(buttonReg);
		
		JLabel label = new JLabel("\u8FF7\u9014\u7684\u65C5\u8005\u554A,\u6B22\u8FCE\u60A8\u7684\u6765\u8BBF!");
		label.setForeground(new Color(128, 128, 0));
		label.setFont(UIManager.getFont("Button.font"));
		label.setBounds(90, 14, 172, 15);
		contentPane.add(label);
		
		
		
		
		ImageIcon[] images = new ImageIcon[GlobalTypeDefine.USER_HEAD_IMGS.length];
		int tmp = 0;
		for (String s : GlobalTypeDefine.USER_HEAD_IMGS) {
			URL url = this.getClass().getResource("/resource/imgs/userimgs/" + s);
			images[tmp++] = new ImageIcon(url);
		}
		comboBox = new JComboBox<ImageIcon>(images);
		comboBox.setBounds(115, 198, 100, 80);
		
		contentPane.add(comboBox);
		
		
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
		
		JLabel labelPwTip = new JLabel("6-12\u5B57\u7B26");
		labelPwTip.setForeground(SystemColor.textHighlight);
		labelPwTip.setFont(new Font("黑体", Font.PLAIN, 10));
		labelPwTip.setBounds(231, 117, 67, 15);
		contentPane.add(labelPwTip);
		
		JLabel labelPwVTip = new JLabel("\u4E0E\u5BC6\u7801\u4E00\u81F4");
		labelPwVTip.setForeground(SystemColor.textHighlight);
		labelPwVTip.setFont(new Font("黑体", Font.PLAIN, 10));
		labelPwVTip.setBounds(231, 154, 67, 15);
		contentPane.add(labelPwVTip);
		
		JLabel mowLabel = new JLabel("");
		mowLabel.setBounds(231, 209, 100, 135);
		URL url = this.getClass().getResource("/resource/imgs/cho_cat.png");
		mowLabel.setIcon(new ImageIcon(url));
		contentPane.add(mowLabel);
		
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
