package miro.ui;

import java.awt.BorderLayout;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class ServerMonitorUI extends JFrame {

	private static final long serialVersionUID = 114360671955915884L;
	private JPanel contentPane;
	private AtomicBoolean stopped;

	public ServerMonitorUI(AtomicBoolean b) {
		stopped = b;
		setTitle("Miro");
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 260, 130);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JLabel textLabel = new JLabel("Miro\u670D\u52A1\u5668\u5DF2\u542F\u52A8\uFF0C\u70B9\u51FB\u5173\u95ED\u6309\u94AE\u505C\u6B62\u3002");
		textLabel.setHorizontalAlignment(SwingConstants.CENTER);
		contentPane.add(textLabel, BorderLayout.CENTER);
		
		addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
            	stopped.set(true);
            	dispose();
            }
        });
		
	}

}
