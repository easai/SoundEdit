package com.github.easai.audio.soundedit;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WavFile {
	SegmentList list;
	String fileName = "";
	AudioInputStream audioInputStream = null;
	AudioFormat audioFormat = null;
	long nBytesRead = 0;
	long frameSize;
	byte[] buf;
	SourceDataLine line = null;
	DataLine.Info info;

	Logger log = LoggerFactory.getLogger(WavFile.class);

	public void readURL(URL url) {
		try {
			InputStream stream = url.openConnection().getInputStream();
			readStream(stream);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

	public void readFile(String fileName) {
		try {
			AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(fileName));
			// BufferedInputStream bufferedInputStream = new
			// BufferedInputStream(audioInputStream);
			// audioInputStream = new AudioInputStream(bufferedInputStream,
			// audioInputStream.getFormat(), audioInputStream.getFrameLength());
			buf = new byte[audioInputStream.available()];
			audioInputStream.read(buf, 0, buf.length);
			audioFormat = audioInputStream.getFormat();
			frameSize = audioFormat.getFrameSize();
			nBytesRead = frameSize * audioInputStream.getFrameLength();
			list = new SegmentList(buf, nBytesRead);
			info = new DataLine.Info(SourceDataLine.class, audioFormat);
			// readStream(stream);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

	public void readStream(InputStream stream) {
		try {
			if (stream != null) {
				audioInputStream = AudioSystem.getAudioInputStream(stream);
				buf = new byte[audioInputStream.available()];
				audioInputStream.read(buf, 0, buf.length);
				audioFormat = audioInputStream.getFormat();
				frameSize = audioFormat.getFrameSize();
				nBytesRead = frameSize * audioInputStream.getFrameLength();
				list = new SegmentList(buf, nBytesRead);
				info = new DataLine.Info(SourceDataLine.class, audioFormat);
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

	public void playSegment(int index) {
		if (0 <= index) {
			Segment s = list.get(index);
			long start = s.start;
			long end = s.end;
			start -= start % frameSize;
			end -= end % frameSize;
			long length = end - start;
			playSegment(start, length);
		}
	}

	public void playSegment(long start, long length) {

		try {
			log.info("Playing segment ["+start+", "+(start+length)+"]");
			/*
			 * long maxint=Integer.MAX_VALUE+1; if(maxint<start+length) { long
			 * count=length/maxint; byte buffer[]=new byte[count][maxint];
			 * for(long i=start;i<start+length;i+=maxint) { for(int
			 * j=0;j<maxint;j++) buffer[i][j]=buf[i+j]; } for(int
			 * i=0;i<count;i++) { if(i<start+length)
			 * line.write(buffer[i],i,maxint); else
			 * line.write(buffer[i],i,length%maxint); } } else
			 */
			// line.write(buf,(int)start,(int)length);
			line = (SourceDataLine) AudioSystem.getLine(info);
			line.open(audioFormat);
			line.start();
			line.write(buf, (int)start, (int) length);
			log.info("Playing [{}, {}]", start, start+length);
			// stream = new AudioInputStream(line, buf, (int) start, (int)
			// length);
			// stream.run();
			line.drain();
			line.close();

		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

	public void quit() {
		try {
			if (line != null) {
				line.close();
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

	public void playAll() {
		playSegment(0, nBytesRead);
	}

	public void stopLine() {
		try {
			line.stop();
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}
}
