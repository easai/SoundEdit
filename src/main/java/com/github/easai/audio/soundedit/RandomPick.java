package com.github.easai.audio.soundedit;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class RandomPick extends JFrame implements Runnable, ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Thread thread = new Thread(this);
	ResultSet resultSet = null;
	int nRow = 0;
	File files[];
	JLabel label = new JLabel();
	String texts[];
	String number[];
	JButton alterDatabase = new JButton("Database");
	JButton pause = new JButton("Pause");
	boolean isPaused = false;
	String database = "KoreanAudio";
	String driver = "sun.jdbc.odbc.JdbcOdbcDriver";
	String url = "jdbc:odbc:dic";

	public synchronized void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if (command.equals("Database")) {
			database = JOptionPane.showInputDialog(this, "Database: ");
			initDatabase();
		} else {
			if (isPaused)
				pause.setText("Restart");
			else
				pause.setText("Pause");
			isPaused = !isPaused;
		}
		if (!isPaused)
			notify();
	}

	Connection connection;
	PreparedStatement ps;

	public void initDatabase() {
		String sql = "SELECT * from " + database;
		try {
			ps = connection.prepareStatement(sql);
			resultSet = ps.executeQuery();
			while (resultSet.next()) {
				nRow++;
			}
			files = new File[nRow];
			texts = new String[nRow];
			number = new String[nRow];
			resultSet = ps.executeQuery();
			int index = 0;
			String file;
			String list[];
			while (resultSet.next()) {
				file = new String(resultSet.getBytes("file"), "UTF8");
				file = file.split(".wav")[0];
				list = file.split("/");
				number[index] = list[list.length - 1];
				texts[index] = new String(resultSet.getBytes("sentence"), "UTF8");
				files[index++] = new File(resultSet.getString("file"));
			}
			System.out.println("");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void init() {
		try {
			Class.forName(driver);
			connection = DriverManager.getConnection(url);
			initDatabase();
		} catch (Exception e) {
			e.printStackTrace();
		}
		thread.start();
		label.setFont(new Font("Arial Unicode MS", Font.PLAIN, 20));
		JPanel control = new JPanel();
		control.add(pause);
		control.add(alterDatabase);
		getContentPane().add(control, BorderLayout.NORTH);
		getContentPane().add(label, BorderLayout.CENTER);
		alterDatabase.addActionListener(this);
		pause.addActionListener(this);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		setTitle("RandomPick");
		setSize(500, 500);
		setVisible(true);
	}

	public void run() {
		while (true) {
			try {
				synchronized (this) {
					while (isPaused)
						wait();
				}
				int rand = (int) (Math.random() * nRow + 1);
				label.setText(texts[rand]);
				System.out.println(number[rand]);
				AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(files[rand]);
				AudioFormat format = audioInputStream.getFormat();
				DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
				SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
				line.open(format);
				line.start();
				int nBytesRead = 0;
				byte buf[] = new byte[1280000];
				while (nBytesRead != -1) {
					nBytesRead = audioInputStream.read(buf, 0, buf.length);
					if (nBytesRead >= 0)
						line.write(buf, 0, nBytesRead);
				}
				line.drain();
				line.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	public static void main(String args[]) {
		RandomPick rp = new RandomPick();
		rp.init();
	}
}