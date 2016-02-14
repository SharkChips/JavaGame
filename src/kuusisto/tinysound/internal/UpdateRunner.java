package kuusisto.tinysound.internal;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.sound.sampled.SourceDataLine;

import kuusisto.tinysound.TinySound;

/**
 * The UpdateRunner class implements Runnable and is what performs automatic updates of the TinySound system. UpdateRunner is an
 * internal class of the TinySound system and should be of no real concern to the average user of TinySound.
 * 
 * @author Finn Kuusisto
 */
public class UpdateRunner implements Runnable {

    private AtomicBoolean running;
    private SourceDataLine outLine;
    private Mixer mixer;

    /**
     * Constructs a new UpdateRunner to update the TinySound system.
     * 
     * @param mixer
     *            the mixer to read audio data from
     * @param outLine
     *            the line to write audio data to
     */
    public UpdateRunner(Mixer mixer, SourceDataLine outLine) {
	this.running = new AtomicBoolean();
	this.mixer = mixer;
	this.outLine = outLine;
    }

    /**
     * Stop this UpdateRunner from updating the TinySound system.
     */
    public void stop() {
	this.running.set(false);
    }

    @Override
    public void run() {
	// Mark the updater as running
	this.running.set(true);
	// 1 second buffer
	int bufSize = (int) TinySound.FORMAT.getFrameRate() * TinySound.FORMAT.getFrameSize();
	byte[] audioBuffer = new byte[bufSize];
	// Only buffer some maximum number of frames each update (25ms)
	int maxFramesPerUpdate = (int) ((TinySound.FORMAT.getFrameRate() / 1000) * 25);
	int numBytesRead = 0;
	double framesAccrued = 0;
	long lastUpdate = System.nanoTime();
	// Keep running until told to stop
	while (this.running.get()) {
	    // Check the time
	    long currTime = System.nanoTime();
	    // Accrue frames
	    double delta = currTime - lastUpdate;
	    double secDelta = (delta / 1000000000L);
	    framesAccrued += secDelta * TinySound.FORMAT.getFrameRate();
	    // Read frames if needed
	    int framesToRead = (int) framesAccrued;
	    int framesToSkip = 0;
	    // Check if we need to skip frames to catch up
	    if (framesToRead > maxFramesPerUpdate) {
		framesToSkip = framesToRead - maxFramesPerUpdate;
		framesToRead = maxFramesPerUpdate;
	    }
	    // Skip frames
	    if (framesToSkip > 0) {
		int bytesToSkip = framesToSkip * TinySound.FORMAT.getFrameSize();
		this.mixer.skip(bytesToSkip);
	    }
	    // Read frames
	    if (framesToRead > 0) {
		// Read from the mixer
		int bytesToRead = framesToRead * TinySound.FORMAT.getFrameSize();
		int tmpBytesRead = this.mixer.read(audioBuffer, numBytesRead, bytesToRead);
		numBytesRead += tmpBytesRead; // mark how many read
		// Fill rest with zeroes
		int remaining = bytesToRead - tmpBytesRead;
		for (int i = 0; i < remaining; i++) {
		    audioBuffer[numBytesRead + i] = 0;
		}
		numBytesRead += remaining; // Mark zeroes read
	    }
	    // Mark frames read and skipped
	    framesAccrued -= (framesToRead + framesToSkip);
	    // Write to speakers
	    if (numBytesRead > 0) {
		this.outLine.write(audioBuffer, 0, numBytesRead);
		numBytesRead = 0;
	    }
	    // Mark last update
	    lastUpdate = currTime;
	    // Give the CPU back to the OS for a bit
	    try {
		Thread.sleep(1);
	    } catch (InterruptedException e) {
	    }
	}
    }
}