package com.github.easai.audio.soundedit;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WavCanvas extends JPanel implements MouseListener, MouseMotionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	WavFile split = null;
	ArrayList<Integer> selectedSegments = new ArrayList<Integer>();
	int from = 0;
	int screenWidth = 1000, screenHeight = 600;

	double scale = 1.0;
	JScrollPane scroll;
	long fixed = 0;

	Logger log = LoggerFactory.getLogger(WavCanvas.class);

	WavCanvas(WavFile split, JScrollPane scroll) {
		this.split = split;
		this.scroll = scroll;
		setScreenWidth(screenWidth);
		addMouseListener(this);
		addMouseMotionListener(this);
	}

	public void setScreenWidth(int w) {
		// Insets insets=split.getInsets();
		// int inside=split.getWidth()-insets.right-insets.left;
		screenWidth = w;
		scale = (double) split.nBytesRead / screenWidth;
		setPreferredSize(new Dimension(screenWidth, screenHeight));
		repaint();
	}

	public void adjustHeight() {
		int scrollBarHeight = scroll.getHorizontalScrollBar().getHeight();
		if (scrollBarHeight > 0)
			screenHeight -= scrollBarHeight;
		setPreferredSize(new Dimension(screenWidth, screenHeight));
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		int x = e.getX();
		int delta = x - from;
		// long x0Value = (long) (from * scale);
		// long x1Value = (long) (x * scale);
		// int i=split.list.getIndex(x0Value);

		/*
		 * if(!split.list.withinSegment(selectedIndex,x0Value)) { if(debug)
		 * System.out.println("from "+Math.min(x0Value,x1Value)+" to "
		 * +Math.max(x0Value,x1Value));
		 * split.list.selected=split.list.setSegment(Math.min(x0Value, x1Value),
		 * Math.max(x0Value,x1Value)); } else
		 */
		{
			if (split.list.selected >= 0 && split.list.selected < split.list.size()) {
				Segment s = split.list.get(split.list.selected);
				int dValue = (int) (delta * scale);
				long changed = 0;
				if (fixed == s.start) {
					changed = s.end + dValue;
				} else {
					changed = s.start + dValue;
				}

				split.list.setSegment(split.list.selected, fixed, changed);
				/*
				 * if(-dValue>s.end-s.start) {
				 * split.list.selected=split.list.setSegment(x1Value,s.start); }
				 * else { split.list.alterRight(split.list.selected, dValue); }
				 * } else {
				 * split.list.setSegment(split.list.selected,s.start+dValue,
				 * s.end);
				 * 
				 * if(debug) System.out.println("mouseDragged left "+dValue);
				 * if(dValue>s.end-s.start) {
				 * split.list.selected=split.list.setSegment(s.end,x1Value); }
				 * else { split.list.alterLeft(split.list.selected, dValue); } }
				 */
			}
		}

		from = x;
		repaint();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		int x = e.getX();
		int index = split.list.getIndex((int) (x * scale));
		int clickCount = e.getClickCount();

		if (index > -1) {
			if (clickCount == 2) {
				split.playSegment(index);
			} else if (clickCount == 1) {
				from = x;
				split.list.selected = index;
				repaint();
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		int x = e.getX();
		from = x;
		int scaled = (int) (x * scale);

		int index = split.list.getIndex(scaled);
		if (0 <= index && split.list.withinSegment(index, scaled))
			split.list.selected = index;
		else {
			split.list.selected = split.list.addSegment(scaled, scaled);
		}
		Segment s = split.list.get(split.list.selected);
		if (scaled < (s.start + s.end) / 2)
			fixed = s.end;
		else
			fixed = s.start;

		log.info("index= " + split.list.selected);
		repaint();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		from = -1;
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	public void paint(Graphics g) {
		log.info("selected=" + split.list.selected);
		g.setColor(Color.white);
		g.fillRect(0, 0, getWidth(), getHeight());
		if (split.list == null)
			return;
		g.setColor(Color.black);
		byte[] buf = split.list.getBuffer();
		for (int x = 0; x < screenWidth; x++) {
			if ((x + 1) * scale < split.nBytesRead) {
				g.drawLine(x, buf[(int) (x * scale)] + screenHeight / 2, x + 1,
						buf[(int) ((x + 1) * scale)] + screenHeight / 2);
			}
		}
		g.setColor(new Color(255, 0, 0, 64));
		Segment s;
		int nSegments = split.list.size();
		log.info("total " + nSegments + " segments");
		for (int i = 0; i < nSegments; i++) {
			s = split.list.get(i);
			g.fillRect((int) (s.start / scale), 0, (int) ((s.end - s.start) / scale), screenHeight);
			log.info("Segment " + i + " (" + s.start + ", " + s.end + ")");
		}
		if (split.list.selected >= 0 && split.list.selected < nSegments) {
			s = split.list.get(split.list.selected);
			g.setColor(Color.red);
			g.drawRect((int) (s.start / scale), 0, (int) ((s.end - s.start) / scale), screenHeight);
		}
	}
}
