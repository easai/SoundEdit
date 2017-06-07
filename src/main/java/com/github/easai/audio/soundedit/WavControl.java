package com.github.easai.audio.soundedit;

import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WavControl extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	Logger log = LoggerFactory.getLogger(WavControl.class);

	@Override
	public void actionPerformed(ActionEvent e) {
		aboutDialog();
	}

	JTextField thresholdField = new JTextField();
	JTextField intevalField = new JTextField();
	JTextField durationField = new JTextField();
	JTextField numberField = new JTextField();
	JButton credit = new JButton("About WavSplit");
	JButton ok = new JButton("OK");

	WavControl() {
		thresholdField.setText((new Long(125)).toString() + "   ");
		intevalField.setText((new Long(900)).toString() + "   ");
		durationField.setText((new Long(10000)).toString() + "   ");

		setLayout(new FlowLayout());
		add(new JLabel("Threshold:"));
		add(thresholdField);
		add(new JLabel("Interval:"));
		add(intevalField);
		add(new JLabel("Duration:"));
		add(durationField);
		add(ok);
		add(credit);
		credit.addActionListener(this);
	}

	public void split(WavFile wavFile) {
		if (wavFile == null)
			return;
		if (wavFile.list == null)
			return;
		try {
			setCursor(new Cursor(Cursor.WAIT_CURSOR));
			wavFile.list.clear();
			int threshold = 12;
			int interval = 900;
			int duration = 470;

			String input;
			input = thresholdField.getText().trim();
			if (!input.equals(""))
				threshold = Integer.parseInt(input);
			input = intevalField.getText().trim();
			if (!input.equals(""))
				interval = Integer.parseInt(input);
			input = durationField.getText().trim();
			if (!input.equals(""))
				duration = Integer.parseInt(input);

			wavFile.list.split(threshold, interval, duration);

			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

	void aboutDialog() {
		String message = "WavSplit.java\n";
		message += "Copyright (C) 2004 - 2017 by easai";
		JOptionPane.showMessageDialog(this, message, "About WavSplit", JOptionPane.INFORMATION_MESSAGE);
	}
}
