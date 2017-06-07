package com.github.easai.audio.soundedit;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.text.DateFormat;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Properties;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.easai.audio.soundedit.SoundEditMenu.MENUITEM;

public class SoundEdit extends JFrame implements ActionListener, KeyListener {

	/**
	 * 
	 */

	JScrollPane scroll;
	WavCanvas canvas;
	String outputFile;
	WavControl control = new WavControl();
	WavFile wavFile = new WavFile();

	SoundEditMenu menu = new SoundEditMenu();
	Properties properties = new Properties();
	String propertyFile = "SoundEdit.properties";

	String wavDirectory = ".";

	private static final long serialVersionUID = 1L;
	private static final String OPTION_HELP = "help";
	private static final String OPTION_FILE = "file";

	Logger log = LoggerFactory.getLogger(SoundEdit.class);

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		int modifiers = e.getModifiers();
		if (key == KeyEvent.VK_N || key == KeyEvent.VK_RIGHT) {
			if ((modifiers & InputEvent.SHIFT_MASK) != 0) {
				wavFile.list.selected = wavFile.list.concatSegment(wavFile.list.selected);
			} else if ((modifiers & InputEvent.CTRL_MASK) != 0) {
				wavFile.list.alterRight(wavFile.list.selected, 100);
			} else {
				wavFile.list.selected = (wavFile.list.selected + 1) % wavFile.list.size();
			}
			repaint();
			wavFile.playSegment(wavFile.list.selected);
		} else if (key == KeyEvent.VK_P || key == KeyEvent.VK_LEFT) {
			if ((modifiers & InputEvent.SHIFT_MASK) != 0) {
				wavFile.list.selected = wavFile.list.concatSegment(wavFile.list.selected - 1);
			} else if ((modifiers & InputEvent.CTRL_MASK) != 0) {
				wavFile.list.alterLeft(wavFile.list.selected, 100);
			} else {
				int size = wavFile.list.size();
				wavFile.list.selected = (wavFile.list.selected - 1 + size) % size;
			}
			repaint();
			wavFile.playSegment(wavFile.list.selected);
		} else if (key == KeyEvent.VK_R || key == KeyEvent.VK_UP) {
			wavFile.playSegment(wavFile.list.selected);
		} else if (key == KeyEvent.VK_F) {
			wavFile.list.alterRight(wavFile.list.selected, 100);
			repaint();
		} else if (key == KeyEvent.VK_B) {
			wavFile.list.alterLeft(wavFile.list.selected, 100);
			repaint();
		} else if (key == KeyEvent.VK_C) {
			wavFile.list.concatSegment(wavFile.list.selected - 1);
			repaint();
		} else if (key == KeyEvent.VK_D) {
			delete();
		} else if (key == KeyEvent.VK_V) {
			divide();
		} else if (key == KeyEvent.VK_DOWN) {
			if ((modifiers & InputEvent.CTRL_MASK) != 0) {
				delete();
				repaint();
			}
		} else if (key == KeyEvent.VK_0) {
			wavFile.list.selected = 0;
			repaint();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		MENUITEM n = menu.comp.get(source);
		if (n != null) {
			switch (n) {
			case nFileOpen:
				selectFile();
				break;
			case nFileSave:
				saveSegment();
				break;
			case nFileSaveAs:
				saveAs();
				break;
			case nFileSaveAll:
				saveAll();
				break;
			case nFileQuit:
				quit();
				break;
			case nEditDivide:
				divide();
				break;
			case nEditDelete:
				delete();
				break;
			case nEditPlay:
				wavFile.playSegment(wavFile.list.selected);
				break;
			case nEditPlayAll:
				wavFile.playAll();
				break;
			case nEditStop:
				wavFile.stopLine();
				break;
			case nViewResize:
				resize();
				break;
			case nViewRefresh:
				repaint();
				scroll.revalidate();
				break;
			case nViewSplit:
				split();
				break;
			case nToolsRight:
				wavFile.list.alterRight(wavFile.list.selected, 10000);
				break;
			case nToolsLeft:
				wavFile.list.alterLeft(wavFile.list.selected, -10000);
				break;
			case nHelpAbout:
				control.aboutDialog();
				break;
			}
		}
	}

	public void resize() {
		try {
			String sizeString = JOptionPane.showInputDialog(this, "Expand the frame by the factor of: ");
			double scale = 1.0;
			if (sizeString != null && !sizeString.equals(""))
				scale = Double.parseDouble(sizeString);
			canvas.setScreenWidth((int) (canvas.screenWidth * scale));
			scroll.getViewport().revalidate();
			// canvas.adjustHeight();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			;
		}
	}

	private void readProperties() {
		log.info("Reading property file: " + propertyFile);
		FileInputStream stream = null;
		try {
			File file = new File(propertyFile);
			if (file.exists()) {
				stream = new FileInputStream(file);
				properties.loadFromXML(stream);
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (Exception e) {
					log.error(e.getMessage(), e);
					;
				}
			}
		}

		String property;
		if ((property = properties.getProperty("File Name")) != null && !property.equals(""))
			wavFile.fileName = property;
		if ((property = properties.getProperty("Threshold")) == null || property.equals(""))
			properties.setProperty("Threshold", "12");
		if ((property = properties.getProperty("Interval")) == null || property.equals(""))
			properties.setProperty("Interval", "900");
		if ((property = properties.getProperty("Duration")) == null || property.equals(""))
			properties.setProperty("Duration", "470");
		if ((property = properties.getProperty("Directory")) == null || property.equals(""))
			properties.setProperty("Directory", ".");
	}

	public void writeProperties() {
		String property;
		if ((property = properties.getProperty("File Name")) != null && property.equals(""))
			properties.setProperty("File Name", "");
		if (properties.getProperty("Threshold").equals(""))
			properties.setProperty("Threshold", "12");
		if (properties.getProperty("Interval").equals(""))
			properties.setProperty("Interval", "900");
		if (properties.getProperty("Duration").equals(""))
			properties.setProperty("Duration", "470");
		if (properties.getProperty("Directory").equals(""))
			properties.setProperty("Directory", ".");

		FileOutputStream stream = null;
		try {
			stream = new FileOutputStream(propertyFile);
			log.info("Saving properties: " + propertyFile);
			DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL, Locale.GERMANY);
			properties.storeToXML(stream, dateFormat.format(new java.util.Date()));
		} catch (Exception e) {
			log.error(e.getMessage());
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (Exception e) {
					log.error(e.getMessage());
				}
			}
		}
	}

	public void quit() {
		writeProperties();
		wavFile.quit();
		System.exit(0);
	}

	public void saveAll() {
		try {
			String outputFile = "";
			int nSegments = wavFile.list.size();
			for (int i = 0; i < nSegments; i++) {
				if (wavFile.fileName.toLowerCase().endsWith(".wav"))
					outputFile = wavFile.fileName.substring(0, wavFile.fileName.length() - 4) + "-";
				int k = 0;
				if (i + 1 >= 9)
					k = (int) Math.log10(i + 1);
				for (int j = k; j < (int) Math.log10(nSegments); j++) {
					outputFile += "0";
				}
				outputFile += (i + 1) + ".wav";
				saveSegment(i, outputFile);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void saveAs() {
		try {
			if (wavFile.list.selected < 0) {
				JOptionPane.showMessageDialog(this, "Select a segment first");
				return;
			}
			JFileChooser dlg = new JFileChooser(wavDirectory);
			dlg.setFileSelectionMode(JFileChooser.FILES_ONLY);
			dlg.setDialogType(JFileChooser.OPEN_DIALOG);
			dlg.setMultiSelectionEnabled(false);
			int retval = dlg.showDialog(this, "Save As");
			if (retval == JFileChooser.APPROVE_OPTION) {
				outputFile = dlg.getSelectedFile().getPath();
				saveSegment(wavFile.list.selected, outputFile);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			;
		}
	}

	public void saveSegment() {
		if (wavFile.list.selected > -1) {
			JOptionPane.showMessageDialog(this, "Select a segment first");
			return;
		}
		if (outputFile != null && !outputFile.equals(""))
			saveSegment(wavFile.list.selected, outputFile);
	}

	public void saveSegment(int i, String outputFile) {
		try {
			int frameSize = wavFile.audioFormat.getFrameSize();
			Segment s = wavFile.list.get(i);
			long start = s.start;
			long end = s.end;
			start -= start % frameSize;
			end -= end % frameSize;
			int length = (int) (end - start);
			ByteArrayInputStream byteStream = new ByteArrayInputStream(wavFile.list.getBuffer(), (int) start, length);
			AudioInputStream stream = new AudioInputStream(byteStream, wavFile.audioFormat, length / frameSize);
			log.info("Saving segment: " + outputFile);
			File file = new File(outputFile);
			AudioSystem.write(stream, AudioFileFormat.Type.WAVE, file);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			;
		}
	}

	public void split() {
		control.ok.addActionListener(this);
		JFrame controlFrame = new JFrame();
		controlFrame.add(control);
		controlFrame.setTitle("Split");
		controlFrame.pack();
		controlFrame.setVisible(true);
	}

	public void delete() {
		wavFile.list.deleteSegment(wavFile.list.selected);
		repaint();
	}

	public void divide() {
		wavFile.list.splitSegment(wavFile.list.selected);
		repaint();
	}

	public void init() {
		readProperties();
		try {
			if (wavFile.fileName != null && !wavFile.fileName.isEmpty()) {
				wavFile.readFile(wavFile.fileName);
				log.info("Reading wav file: " + wavFile.fileName);
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		if (wavFile.list == null)
			selectFile();
		if (wavFile.list == null)
			System.exit(0);
		control.split(wavFile);
		if (0 < wavFile.list.size())
			wavFile.list.selected = 0;

		scroll = new JScrollPane();
		canvas = new WavCanvas(wavFile, scroll);
		// canvas.setSize(100,600);
		Container panel = getContentPane();
		scroll.getViewport().add(canvas);
		panel.add(scroll, BorderLayout.CENTER);
		/*
		 * control=new WavControl(); control.split(wavFile);
		 * control.ok.addActionListener(this); panel.add(control,
		 * BorderLayout.NORTH);
		 */

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		menu.setMenu(this, this, Locale.US);

		// setSize(800,500);
		pack();
		setTitle("SoundEdit");
		setVisible(true);
		addKeyListener(this);
	}

	class SplitFileFilter implements FilenameFilter {
		String outputFile;

		public boolean accept(File dir, String name) {
			return name.endsWith(".wav");
		}
	}

	public void selectFile() {
		try {
			JFileChooser dlg = new JFileChooser(wavDirectory);
			dlg.setFileSelectionMode(JFileChooser.FILES_ONLY);
			dlg.setDialogType(JFileChooser.OPEN_DIALOG);
			dlg.setMultiSelectionEnabled(false);
			int retval = dlg.showDialog(this, "Select File");
			if (retval == JFileChooser.APPROVE_OPTION) {
				wavFile.fileName = dlg.getSelectedFile().getPath();

				wavFile.readFile(wavFile.fileName);
				if (canvas != null) {
					canvas.setScreenWidth(800);
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

	public static void main(String args[]) {
		SoundEdit wavSplit = new SoundEdit();
		Options opt = new Options();
		try {
			opt.addOption("?", OPTION_HELP, false, "print this message");

			Option option = Option.builder("f").longOpt(OPTION_FILE).hasArgs().desc("file name").build();
			opt.addOption(option);

			CommandLineParser parser = new DefaultParser();
			CommandLine cmd = parser.parse(opt, args);
			if (cmd.hasOption(OPTION_HELP)) {
				throw new Exception();
			} else if (cmd.hasOption(OPTION_FILE)) {
				String fileName = cmd.getOptionValue(OPTION_FILE);
				wavSplit.wavFile.fileName = fileName;
			}

			wavSplit.init();
		} catch (Exception e) {
			wavSplit.log.error(e.getMessage());
			HelpFormatter help = new HelpFormatter();
			help.printHelp("WavSplit", opt, true);
		}
	}
}
