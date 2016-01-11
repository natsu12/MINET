package minet.ui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import java.awt.Color;
import javax.swing.JPasswordField;
import java.awt.event.KeyAdapter;
import java.net.URL;
import javax.swing.ImageIcon;
import java.awt.event.KeyEvent;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class LoginUI extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1253317409088840959L;
	private JPanel contentPane;
	private JPasswordField passwordField;
	private JButton buttonOK;
	private JButton buttonReg;
	private JLabel mow;
	private JTextField idField;
	private JLabel logStateLabel;
	

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					LoginUI frame = new LoginUI();
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
	public LoginUI() {
	    setResizable(false);
		setTitle("Minet-Login");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 425, 185);
		contentPane = new JPanel();
		contentPane.setBorder(null);
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JPanel fieldPanel = new JPanel(){  
            private static final long serialVersionUID = 1L;  
            
            protected void paintComponent(Graphics g) {  
                Graphics2D g2 = (Graphics2D) g;  
                super.paintComponent(g);  
                // »æÖÆ½¥±ä  
                g2.setPaint(new GradientPaint(0, 0, new Color(255, 255, 225), getWidth(),  getHeight(), new Color(51, 153, 255)));  
                g2.fillRect(0, 0, getWidth(), getHeight());  
            }  
        }; 
		fieldPanel.setBorder(null);
		
		contentPane.add(fieldPanel, BorderLayout.CENTER);
		fieldPanel.setLayout(null);
		
		JLabel labelName = new JLabel("\u7528\u6237\u540D");
		labelName.setBounds(150, 27, 54, 15);
		fieldPanel.add(labelName);
		
		JLabel labelPassword = new JLabel("\u5BC6\u7801");
		labelPassword.setBounds(150, 60, 54, 15);
		fieldPanel.add(labelPassword);
	
		buttonOK = new JButton("\u786E\u8BA4");
		buttonOK.setFocusPainted(false);
		buttonOK.setBounds(156, 106, 67, 23);
		fieldPanel.add(buttonOK);
		
		buttonReg = new JButton("\u6CE8\u518C");
		buttonReg.setFocusPainted(false);
		buttonReg.setBounds(286, 106, 67, 23);
		fieldPanel.add(buttonReg);
		
		mow = new JLabel("");
		URL url = this.getClass().getResource("/resource/imgs/bacon_cat.png");
		mow.setIcon(new ImageIcon(url));
		mow.setBounds(10, 10, 120, 108);
		fieldPanel.add(mow);
		
		idField = new JTextField();
		idField.setBounds(250, 24, 122, 21);
		idField.addKeyListener(new KeyAdapter(){
			public void keyTyped(KeyEvent e) {				
				if(idField.getText().length() >= 12) {	
					e.consume(); 
				}
			}
		});
		
		fieldPanel.add(idField);
		
		
		passwordField = new JPasswordField();
		passwordField.setBounds(250, 57, 122, 21);
		passwordField.addKeyListener(new KeyAdapter(){
			public void keyTyped(KeyEvent e) {				
				if(passwordField.getPassword().length >= 12) {	
					e.consume(); 
				}
			}
		});
		fieldPanel.add(passwordField);
		
		logStateLabel = new JLabel("");
		logStateLabel.setHorizontalAlignment(SwingConstants.CENTER);
		logStateLabel.setForeground(Color.RED);
		logStateLabel.setBounds(139, 135, 233, 15);
		fieldPanel.add(logStateLabel);
		
		
		
	    setLocationRelativeTo(null);
	}

    public JTextField getIdField()
    {
        return idField;
    }

    public JPasswordField getPasswordField()
    {
        return passwordField;
    }

    public JButton getButtonOK()
    {
        return buttonOK;
    }

    public JButton getButtonReg()
    {
        return buttonReg;
    }

    public JTextField getNameField()
    {
        return idField;
    }

    public JLabel getLogStateLabel()
    {
        return logStateLabel;
    }
}
