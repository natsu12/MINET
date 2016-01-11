package minet.ui.misc;

import java.awt.EventQueue;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import javax.swing.SwingConstants;
import java.awt.Font;

public class FileOperation extends JFrame {
    /**
     * 
     */
    private static final long serialVersionUID = -6805373469680744146L;
    private JPanel contentPane;
	private JButton buttonOK;
	private JButton buttonCancel;
	private JLabel fileNameLabel;
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					FileOperation frame = new FileOperation("哈哈", "啊啊", "dddd");
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public FileOperation(String name, String id, String fileName) {
		setTitle("文件传输请求");
		setResizable(false);
		setBounds(100, 100, 300, 150);
		contentPane = new JPanel();
		contentPane.setBorder(null);
		contentPane.setLayout(null);
		JLabel labelName = new JLabel(name + "(" + id + ")");
		labelName.setHorizontalAlignment(SwingConstants.CENTER);
		labelName.setBounds(72, 10, 150, 23);
		contentPane.add(labelName);
		buttonOK = new JButton("同意");
		buttonOK.setFocusPainted(false);
		buttonOK.setBounds(62, 80, 67, 23);
		contentPane.add(buttonOK);
		buttonCancel = new JButton("拒绝");
		buttonCancel.setFocusPainted(false);
		buttonCancel.setBounds(166, 80, 67, 23);
		contentPane.add(buttonCancel);
		setContentPane(contentPane);
		
		fileNameLabel = new JLabel("想向你发送文件: " + fileName);
		fileNameLabel.setHorizontalAlignment(SwingConstants.CENTER);
		fileNameLabel.setFont(new Font("黑体", Font.PLAIN, 12));
		fileNameLabel.setBounds(47, 43, 204, 15);
		contentPane.add(fileNameLabel);
	}

	public JButton getButtonOK() {
		return buttonOK;
	}

	public JButton getButtonCancel() {
		return buttonCancel;
	}
}
