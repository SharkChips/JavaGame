package kuusisto.tinysound;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import kuusisto.tinysound.internal.ByteList;
import kuusisto.tinysound.internal.MemMusic;
import kuusisto.tinysound.internal.MemSound;
import kuusisto.tinysound.internal.Mixer;
import kuusisto.tinysound.internal.UpdateRunner;

/**
 * TinySound is the main class of the TinySound system. In order to use the TinySound system, it must be initialized. After that, Music
 * and Sound objects can be loaded and used. When finished with the TinySound system, it must be shutdown.
 * 
 * @author Finn Kuusisto
 */
public class TinySound {

    public static final String VERSION = "1.1.1";

    /**
     * The internal format used by TinySound.
     */
    public static final AudioFormat FORMAT = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, // linear signed PCM
	    44100, // 44.1kHz sampling rate
	    16, // 16-bit
	    2, // 2 channels fool
	    4, // frame size 4 bytes (16-bit, 2 channel)
	    44100, // same as sampling rate
	    false // little-endian
    );

    // The system has only one mixer for both music and sounds
    private static Mixer mixer;
    // Need a line to the speakers
    private static SourceDataLine outLine;
    // See if the system has been initialized
    private static boolean inited = false;
    // Auto-updater for the system
    private static UpdateRunner autoUpdater;
    // Counter for unique sound IDs
    private static int soundCount = 0;
    // Error counter

    /**
     * Initialize Tinysound. This must be called before loading audio.
     */
    public static void init() {
	if (TinySound.inited) {
	    return;
	}
	// Try to open a line to the speakers
	DataLine.Info info = new DataLine.Info(SourceDataLine.class, TinySound.FORMAT);
	if (!AudioSystem.isLineSupported(info)) {
	    System.err.println("Unsupported output format!");
	    return;
	}
	TinySound.outLine = TinySound.tryGetLine();
	if (TinySound.outLine == null) {
	    System.err.println("Output line unavailable!");
	    return;
	}
	// Start the line and finish initialization
	TinySound.outLine.start();
	TinySound.finishInit();
    }

    /**
     * Initializes the mixer and updater, and marks TinySound as initialized.
     */
    private static void finishInit() {
	// now initialize the mixer
	TinySound.mixer = new Mixer();
	// initialize and start the updater
	TinySound.autoUpdater = new UpdateRunner(TinySound.mixer, TinySound.outLine);
	Thread updateThread = new Thread(TinySound.autoUpdater);
	try {
	    updateThread.setDaemon(true);
	    updateThread.setPriority(Thread.MAX_PRIORITY);
	} catch (Exception e) {
	}
	TinySound.inited = true;
	updateThread.start();
	// Yield to potentially give the updater a chance
	Thread.yield();
    }

    /**
     * Shutdown TinySound.
     */
    public static void shutdown() {
	if (!TinySound.inited) {
	    return;
	}
	TinySound.inited = false;
	// Stop the auto-updater if running
	TinySound.autoUpdater.stop();
	TinySound.autoUpdater = null;
	TinySound.outLine.stop();
	TinySound.outLine.flush();
	TinySound.mixer.clearMusic();
	TinySound.mixer.clearSounds();
	TinySound.mixer = null;
    }

    /**
     * Determine if TinySound is initialized and ready for use.
     * 
     * @return true if TinySound is initialized, false if TinySound has not been initialized or has subsequently been shutdown
     */
    public static boolean isInitialized() {
	return TinySound.inited;
    }

    /**
     * Get the global volume for all audio.
     * 
     * @return the global volume for all audio, -1.0 if TinySound has not been initialized or has subsequently been shutdown
     */
    public static double getGlobalVolume() {
	if (!TinySound.inited) {
	    return -1.0;
	}
	return TinySound.mixer.getVolume();
    }

    /**
     * Set the global volume. This is an extra multiplier, not a replacement, for all Music and Sound volume settings. It starts at
     * 1.0.
     * 
     * @param volume
     *            the global volume to set
     */
    public static void setGlobalVolume(double volume) {
	if (!TinySound.inited) {
	    return;
	}
	TinySound.mixer.setVolume(volume);
    }

    /**
     * Load a Music by a URL.
     * 
     * @param url
     *            the URL of the Music
     * @param streamFromFile
     *            true if this Music should be streamed from a temporary file to reduce memory overhead
     * @return Music from URL as specified, null if not found/loaded
     */
    public static Music loadMusic(String fileName) {
	// Check if the system is initialized
	if (!TinySound.inited) {
	    System.err.println("TinySound not initialized!");
	    return null;
	}

	InputStream in = TinySound.class.getClassLoader().getResourceAsStream(fileName);

	// Check for failure
	if (in == null) {
	    return null;
	}
	// Get a valid stream of audio data
	AudioInputStream audioStream = TinySound.getValidAudioStream(in);
	// Check for failure
	if (audioStream == null) {
	    return null;
	}

	// Try to read all the bytes
	byte[][] data = TinySound.readAllBytes(audioStream);
	// Check for failure
	if (data == null) {
	    return null;
	}
	// Construct the Music object and register it with the mixer
	return new MemMusic(data[0], data[1], TinySound.mixer);
    }

    /**
     * Load a Sound by a URL. This will store audio data in memory.
     * 
     * @param fileName
     *            the URL of the Sound
     * @param streamFromFile
     *            true if this Music should be streamed from a temporary file to reduce memory overhead
     * @return Sound from URL as specified, null if not found/loaded
     */
    public static Sound loadSound(String fileName) {
	// Check if the system is initialized
	if (!TinySound.inited) {
	    System.err.println("TinySound not initialized!");
	    return null;
	}

	InputStream in = TinySound.class.getClassLoader().getResourceAsStream(fileName);

	// Check for failure
	if (in == null) {
	    return null;
	}
	// Get a valid stream of audio data
	AudioInputStream audioStream = TinySound.getValidAudioStream(in);
	// Check for failure
	if (audioStream == null) {
	    return null;
	}
	// Try to read all the bytes
	byte[][] data = TinySound.readAllBytes(audioStream);
	// Check for failure
	if (data == null) {
	    return null;
	}
	// Construct the Sound object
	TinySound.soundCount++;
	return new MemSound(data[0], data[1], TinySound.mixer, TinySound.soundCount);
    }

    /**
     * Reads all of the bytes from an AudioInputStream.
     * 
     * @param stream
     *            the stream to read
     * @return all bytes from the stream, null if error
     */
    private static byte[][] readAllBytes(AudioInputStream stream) {
	// Left and right channels
	byte[][] data = null;
	int numChannels = stream.getFormat().getChannels();
	// Handle 1-channel
	if (numChannels == 1) {
	    byte[] left = TinySound.readAllBytesOneChannel(stream);
	    // Check failure
	    if (left == null) {
		return null;
	    }
	    data = new byte[2][];
	    data[0] = left;
	    data[1] = left; // Don't copy for the right channel
	} // Handle 2-channel
	else if (numChannels == 2) {
	    data = TinySound.readAllBytesTwoChannel(stream);
	} else { // Something is wrong
	    System.err.println("TinySound cannot read audio with " + numChannels + " channels!");
	}
	return data;
    }

    /**
     * Reads all of the bytes from a 1-channel AudioInputStream.
     * 
     * @param stream
     *            the stream to read
     * @return all bytes from the stream, null if error
     */
    private static byte[] readAllBytesOneChannel(AudioInputStream stream) {
	// Read all the bytes (assuming 1-channel)
	byte[] data = null;
	try {
	    data = TinySound.getBytes(stream);
	} catch (IOException e) {
	    System.err.println("Error reading all bytes from stream!");
	    return null;
	} finally {
	    try {
		stream.close();
	    } catch (IOException e) {
	    }
	}
	return data;
    }

    /**
     * Reads all of the bytes from a 2-channel AudioInputStream.
     * 
     * @param stream
     *            the stream to read
     * @return all bytes from the stream, null if error
     */
    private static byte[][] readAllBytesTwoChannel(AudioInputStream stream) {
	// Read all the bytes (assuming 16-bit, 2-channel)
	byte[][] data = null;
	try {
	    byte[] allBytes = TinySound.getBytes(stream);
	    byte[] left = new byte[allBytes.length / 2];
	    byte[] right = new byte[allBytes.length / 2];
	    for (int i = 0, j = 0; i < allBytes.length; i += 4, j += 2) {
		// Interleaved left then right
		left[j] = allBytes[i];
		left[j + 1] = allBytes[i + 1];
		right[j] = allBytes[i + 2];
		right[j + 1] = allBytes[i + 3];
	    }
	    data = new byte[2][];
	    data[0] = left;
	    data[1] = right;
	} catch (IOException e) {
	    System.err.println("Error reading all bytes from stream!");
	    return null;
	} finally {
	    try {
		stream.close();
	    } catch (IOException e) {
	    }
	}
	return data;
    }

    /**
     * Gets and AudioInputStream in the TinySound system format.
     * 
     * @param in
     *            URL of the resource
     * @return the specified stream as an AudioInputStream stream, null if failure
     */
    private static AudioInputStream getValidAudioStream(InputStream in) {
	AudioInputStream audioStream = null;
	try {
	    audioStream = AudioSystem.getAudioInputStream(in);
	    AudioFormat streamFormat = audioStream.getFormat();
	    // 1-channel can also be treated as stereo
	    AudioFormat mono16 = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 1, 2, 44100, false);
	    // 1 or 2 channel 8-bit may be easy to convert
	    AudioFormat mono8 = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 8, 1, 1, 44100, false);
	    AudioFormat stereo8 = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 8, 2, 2, 44100, false);
	    // Now check formats (attempt conversion as needed)
	    if (streamFormat.matches(TinySound.FORMAT) || streamFormat.matches(mono16)) {
		return audioStream;
	    } // Check conversion to TinySound format
	    else if (AudioSystem.isConversionSupported(TinySound.FORMAT, streamFormat)) {
		audioStream = AudioSystem.getAudioInputStream(TinySound.FORMAT, audioStream);
	    } // Check conversion to mono alternate
	    else if (AudioSystem.isConversionSupported(mono16, streamFormat)) {
		audioStream = AudioSystem.getAudioInputStream(mono16, audioStream);
	    } // Try convert from 8-bit, 2-channel
	    else if (streamFormat.matches(stereo8) || AudioSystem.isConversionSupported(stereo8, streamFormat)) {
		// Convert to 8-bit stereo first?
		if (!streamFormat.matches(stereo8)) {
		    audioStream = AudioSystem.getAudioInputStream(stereo8, audioStream);
		}
		audioStream = TinySound.convertStereo8Bit(audioStream);
	    } // Try convert from 8-bit, 1-channel
	    else if (streamFormat.matches(mono8) || AudioSystem.isConversionSupported(mono8, streamFormat)) {
		// Convert to 8-bit mono first?
		if (!streamFormat.matches(mono8)) {
		    audioStream = AudioSystem.getAudioInputStream(mono8, audioStream);
		}
		audioStream = TinySound.convertMono8Bit(audioStream);
	    } // Tt's time to give up
	    else {
		System.err.println("Unable to convert audio resource!");
		System.err.println(in);
		System.err.println(streamFormat);
		audioStream.close();
		return null;
	    }
	    // Check the frame length
	    long frameLength = audioStream.getFrameLength();
	    // Too long
	    if (frameLength > Integer.MAX_VALUE) {
		System.err.println("Audio resource too long!");
		return null;
	    }
	} catch (UnsupportedAudioFileException e) {
	    System.err.println("Unsupported audio resource!\n" + e.getMessage());
	    return null;
	} catch (IOException e) {
	    System.err.println("Error getting resource stream!\n" + e.getMessage());
	    return null;
	}
	return audioStream;
    }

    /**
     * Converts an 8-bit, signed, 1-channel AudioInputStream to 16-bit, signed, 1-channel.
     * 
     * @param stream
     *            stream to convert
     * @return converted stream
     */
    private static AudioInputStream convertMono8Bit(AudioInputStream stream) {
	// Assuming 8-bit, 1-channel to 16-bit, 1-channel
	byte[] newData = null;
	try {
	    byte[] data = TinySound.getBytes(stream);
	    int newNumBytes = data.length * 2;
	    // Check if size overflowed
	    if (newNumBytes < 0) {
		System.err.println("Audio resource too long!");
		return null;
	    }
	    newData = new byte[newNumBytes];
	    // Convert bytes one-by-one to int, and then to 16-bit
	    for (int i = 0, j = 0; i < data.length; i++, j += 2) {
		// Convert it to a double
		double floatVal = (double) data[i];
		floatVal /= (floatVal < 0) ? 128 : 127;
		if (floatVal < -1.0) { // just in case
		    floatVal = -1.0;
		} else if (floatVal > 1.0) {
		    floatVal = 1.0;
		}
		// Convert it to an int and then to 2 bytes
		int val = (int) (floatVal * Short.MAX_VALUE);
		newData[j + 1] = (byte) ((val >> 8) & 0xFF); // MSB
		newData[j] = (byte) (val & 0xFF); // LSB
	    }
	} catch (IOException e) {
	    System.err.println("Error reading all bytes from stream!");
	    return null;
	} finally {
	    try {
		stream.close();
	    } catch (IOException e) {
	    }
	}
	AudioFormat mono16 = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 1, 2, 44100, false);
	return new AudioInputStream(new ByteArrayInputStream(newData), mono16, newData.length / 2);
    }

    /**
     * Converts an 8-bit, signed, 2-channel AudioInputStream to 16-bit, signed, 2-channel.
     * 
     * @param stream
     *            stream to convert
     * @return converted stream
     */
    private static AudioInputStream convertStereo8Bit(AudioInputStream stream) {
	// Assuming 8-bit, 2-channel to 16-bit, 2-channel
	byte[] newData = null;
	try {
	    byte[] data = TinySound.getBytes(stream);
	    int newNumBytes = data.length * 2 * 2;
	    // Check if size overflowed
	    if (newNumBytes < 0) {
		System.err.println("Audio resource too long!");
		return null;
	    }
	    newData = new byte[newNumBytes];
	    for (int i = 0, j = 0; i < data.length; i += 2, j += 4) {
		// Convert them to doubles
		double leftFloatVal = (double) data[i];
		double rightFloatVal = (double) data[i + 1];
		leftFloatVal /= (leftFloatVal < 0) ? 128 : 127;
		rightFloatVal /= (rightFloatVal < 0) ? 128 : 127;
		if (leftFloatVal < -1.0) { // Just in case
		    leftFloatVal = -1.0;
		} else if (leftFloatVal > 1.0) {
		    leftFloatVal = 1.0;
		}
		if (rightFloatVal < -1.0) { // Just in case
		    rightFloatVal = -1.0;
		} else if (rightFloatVal > 1.0) {
		    rightFloatVal = 1.0;
		}
		// convert them to ints and then to 2 bytes each
		int leftVal = (int) (leftFloatVal * Short.MAX_VALUE);
		int rightVal = (int) (rightFloatVal * Short.MAX_VALUE);
		// Left channel bytes
		newData[j + 1] = (byte) ((leftVal >> 8) & 0xFF); // MSB
		newData[j] = (byte) (leftVal & 0xFF); // LSB
		// Then right channel bytes
		newData[j + 3] = (byte) ((rightVal >> 8) & 0xFF); // MSB
		newData[j + 2] = (byte) (rightVal & 0xFF); // LSB
	    }
	} catch (IOException e) {
	    System.err.println("Error reading all bytes from stream!");
	    return null;
	} finally {
	    try {
		stream.close();
	    } catch (IOException e) {
	    }
	}
	AudioFormat stereo16 = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false);
	return new AudioInputStream(new ByteArrayInputStream(newData), stereo16, newData.length / 4);
    }

    /**
     * Read all of the bytes from an AudioInputStream.
     * 
     * @param stream
     *            the stream from which to read bytes
     * @return all bytes read from the AudioInputStream
     * @throws IOException
     */
    private static byte[] getBytes(AudioInputStream stream) throws IOException {
	// Buffer 1 second at a time
	int bufSize = (int) TinySound.FORMAT.getSampleRate() * TinySound.FORMAT.getChannels() * TinySound.FORMAT.getFrameSize();
	byte[] buf = new byte[bufSize];
	ByteList list = new ByteList(bufSize);
	int numRead = 0;
	while ((numRead = stream.read(buf)) > -1) {
	    for (int i = 0; i < numRead; i++) {
		list.add(buf[i]);
	    }
	}
	return list.asArray();
    }

    /**
     * Iterates through available JavaSound Mixers looking for one that can provide a line to the speakers.
     * 
     * @return an opened SourceDataLine to the speakers
     */
    private static SourceDataLine tryGetLine() {
	// First build our line info and get all available mixers
	DataLine.Info lineInfo = new DataLine.Info(SourceDataLine.class, TinySound.FORMAT);
	javax.sound.sampled.Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
	// Iterate through the mixers trying to find a line
	for (int i = 0; i < mixerInfos.length; i++) {
	    javax.sound.sampled.Mixer mixer = null;
	    try {
		// First try to actually get the mixer
		mixer = AudioSystem.getMixer(mixerInfos[i]);
	    } catch (SecurityException e) {
		// Not much we can do here
	    } catch (IllegalArgumentException e) {
		// This should never happen since we were told the mixer exists
	    }
	    // Check if we got a mixer and our line is supported
	    if (mixer == null || !mixer.isLineSupported(lineInfo)) {
		continue;
	    }
	    // See if we can actually get a line
	    SourceDataLine line = null;
	    try {
		line = (SourceDataLine) mixer.getLine(lineInfo);
		// Don't try to open if already open
		if (!line.isOpen()) {
		    line.open(TinySound.FORMAT);
		}
	    } catch (LineUnavailableException | SecurityException se) {
		// We either failed to get, open, or there was a security exception
		// In all these cases, there is not much that could be done
	    }
	    // Check if we succeeded
	    if (line != null && line.isOpen()) {
		return line;
	    }
	}
	// No good
	return null;
    }

}
