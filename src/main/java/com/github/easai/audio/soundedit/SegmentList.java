package com.github.easai.audio.soundedit;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SegmentList extends ArrayList<Segment> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private byte[] buf;
	
	int selected = -1;
	long nBytesRead;
	
	Logger log = LoggerFactory.getLogger(SegmentList.class);

	SegmentList(byte[] buf, long nBytesRead) {
		this.buf = buf;
		this.nBytesRead = nBytesRead;
	}

	public int setSegment(int index, long from, long to) {
		log.info(from + " -- " + to);
		remove(index);
		return addSegment(from, to);
	}

	public int addSegment(long from, long to) {
		if (to < from) {
			long t = from;
			from = to;
			to = t;
		}

		int index = -1;
		int nSeg = size();
		if (0 < nSeg) {
			int i = 0;
			int deleteFrom = -1, deleteTo = -1;
			while (get(i).start < from && ++i < nSeg)
				;
			if (i < nSeg)
				deleteFrom = i;
			i = 0;
			while (get(i).end < to && ++i < nSeg)
				;
			if (i < nSeg)
				deleteTo = i;

			if (deleteFrom == -1)
				deleteFrom = nSeg;
			if (!withinSegment(deleteFrom - 1, from))
				deleteFrom++;
			deleteFrom--;

			if (deleteTo == -1)
				deleteTo = nSeg;
			if (!withinSegment(deleteTo, to))
				deleteTo--;

			if (deleteFrom <= deleteTo) {
				log.info("Deleting elements from " + deleteFrom + " to " + deleteTo);
				removeRange(deleteFrom, deleteTo + 1);
			}
			index = deleteFrom;
		}

		log.info("Adding elements from " + from + " to " + to);

		if (index == -1 || nSeg < index) {
			add(new Segment(from, to));
			index = size() - 1;
		} else
			add(index, new Segment(from, to));
		selected = index;
		return index;
	}

	public byte[] getBuffer() {
		return buf;
	}

	public void split(int threshold, int interval, int duration) {
		int k = 0;
		int start = 0, end = 0;
		clear();
		do {
			while (Math.abs(buf[k]) < threshold && ++k < nBytesRead)
				;
			if (k < nBytesRead) {
				start = k;
				end = k;
				while ((Math.abs(buf[k]) > threshold || k - end < duration) && ++k < nBytesRead) {
					if (Math.abs(buf[k]) > threshold)
						end = k;
				}
				if (end - start > interval && (k - end >= duration || k >= nBytesRead)) {					
					log.info("Segment: " + start + ", " + end);
					add(new Segment(start, end));
				}
			}
		} while (++k < nBytesRead);
	}

	public int getIndex(long x) {
		int nPoints = size();
		int i = 0;
		if (nPoints > 0)
			while (get(i).start <= x && ++i < nPoints)
				;
		return i - 1;
	}

	public boolean withinSegment(int i, long x) {
		if (0 <= i && i < size()) {
			Segment s = get(i);
			return (s.start <= x && x <= s.end);
		} else
			return false;
	}

	public void alterLeft(int index, int delta) {
		if (index != -1) {
			Segment s = get(index);
			long start = Math.max(0, s.start + delta);
			selected = setSegment(selected, start, s.end);
		}
	}

	public void alterRight(int index, int delta) {
		if (index != -1) {
			Segment s = get(index);
			long end = Math.min(nBytesRead - 1, s.end + delta);
			selected = setSegment(selected, s.start, end);
		}
	}

	public void deleteSegment(int index) {
		if (index > -1) {
			remove(index);
		}
	}

	public void splitSegment(int index) {
		if (0 <= index && index < size()) {
			Segment s = get(index);
			long split = (long) ((s.start + s.end) / 2);
			remove(index);
			addSegment(s.start, split);
			addSegment(split + 1, s.end);
		}
	}

	public int concatSegment(int index) {
		if (0 <= index && index < size() - 1) {
			long from, to;
			Segment s0 = get(index);
			Segment s1 = get(index + 1);
			from = s0.start;
			to = s1.end;
			remove(index);
			remove(index);
			index = addSegment(from, to);
		}
		return index;
	}
}
