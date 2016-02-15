package com.albertzhang.spaceevasion;

import java.util.HashMap;

import kuusisto.tinysound.Music;
import kuusisto.tinysound.Sound;
import kuusisto.tinysound.TinySound;

public class AudioEngine implements Runnable {

    private Thread runThread;

    private boolean isRunning = false;
    private boolean isPaused = false;
    private boolean DEBUG = false;
    private static double musicVolume;
    private static double soundVolume;

    private static final int SECONDS_PER_SONG = 60;
    private static final String[] SONGS = { "Music1", "Music2", "Music3", "Music4" };
    private static HashMap<String, Sound> SOUNDS = new HashMap<>();
    private static HashMap<String, Music> CONTINUOUS_SOUNDS = new HashMap<>();
    private static Music current;

    // Workaround because Music.playing() and Music.done() is broken.
    private static HashMap<String, Boolean> CONT_SOUNDS_PLAYING = new HashMap<>();

    public AudioEngine(double musicVol, double soundVol, boolean debug) {
	DEBUG = debug;
	musicVolume = musicVol;
	soundVolume = soundVol;
	if (DEBUG)
	    System.out.println("Audio Running at: " + SECONDS_PER_SONG + " Seconds per song");
	try {
	    TinySound.init();
	    TinySound.setGlobalVolume(soundVol);
	} catch (Exception e) {
	    System.err.println("Could not load sound system!");
	    e.printStackTrace();
	}

	if (DEBUG) {
	    System.out.println("Sound Volume: " + TinySound.getGlobalVolume() * 100);
	    System.out.println("Music Volume: " + getMusicVolume() * 100);
	}

	// Load Sounds
	SOUNDS.put("Boom", TinySound.loadSound("audio/Boom.ogg"));
	SOUNDS.put("Laser", TinySound.loadSound("audio/Laser.ogg"));
	CONTINUOUS_SOUNDS.put("Alarm", TinySound.loadMusic("audio/Alarm.ogg"));
	CONT_SOUNDS_PLAYING.put("Alarm", false); // Workaround (described above in declaration)
    }

    public static double getMusicVolume() {
	return musicVolume;
    }

    public static void setMusicVolume(double musicVolume) {
	AudioEngine.musicVolume = musicVolume;
	current.setVolume(musicVolume);
    }

    public static double getSoundVolume() {
	return soundVolume;
    }

    public static void setSoundVolume(double soundVolume) {
	AudioEngine.soundVolume = soundVolume;
	TinySound.setGlobalVolume(soundVolume);
    }
    

    /**
     * Starts the thread
     * 
     * @throws IllegalStateException
     *             If the thread has already been started
     */
    public void start() {
	isRunning = true;
	isPaused = false;
	if (runThread == null || !runThread.isAlive())
	    runThread = new Thread(this, "Music-1");
	else if (runThread.isAlive())
	    throw new IllegalStateException("Music Thread has already started.");
	runThread.start();
    }

    /**
     * Stops the thread and ends the program
     * 
     * @throws IllegalStateException
     *             If the thread has not been started yet
     */
    public void stop() {
	if (runThread == null)
	    throw new IllegalStateException("Music Thread has not been started yet.");
	synchronized (runThread) {
	    try {
		isRunning = false;
		runThread.notify();
		runThread.join();
		System.exit(0);
	    } catch (InterruptedException e) {
		e.printStackTrace();
		System.exit(1);
	    }
	}
    }

    /**
     * Pauses Audio
     * 
     * @throws IllegalStateException
     *             If the thread has not been started yet
     */
    public void pause() {
	if (runThread == null)
	    throw new IllegalStateException("Thread not started.");
	synchronized (runThread) {
	    isPaused = true;
	}
    }

    /**
     * Resumes Audio thread
     * 
     * @throws IllegalStateException
     *             If the thread has not been started yet
     */
    public void resume() {
	if (runThread == null)
	    throw new IllegalStateException("Thread not started.");
	synchronized (runThread) {
	    isPaused = false;
	    runThread.notify();
	}
    }

    @Override
    public void run() {
	while (isRunning) {
	    switchSong();

	    try { // Sleep for required amount
		Thread.sleep(SECONDS_PER_SONG * 1000);
	    } catch (InterruptedException ex) {
		ex.printStackTrace();
	    }

	    synchronized (runThread) {
		if (isPaused) {
		    try {
			runThread.wait();
		    } catch (InterruptedException e) {
			e.printStackTrace();
		    }
		}
	    }
	}
	isPaused = false;
    }

    public static void playSound(String name) {
	SOUNDS.get(name).play();
    }

    /**
     * This method differs from {@code playSound} in that if the sound is already playing, the sound will not play again. Useful, for
     * example, in the Alarm sound.
     * 
     * @param name
     *            The name of the sound to be played
     * @param play
     *            Whether to play the sound or not. Used as a workaround because Music.playing() and Music.done() doesn't work
     */
    public static void playSoundContinuous(String name, boolean play) {
	Music m = CONTINUOUS_SOUNDS.get(name);
	Boolean b = CONT_SOUNDS_PLAYING.get(name);
	if (m == null || b == null)
	    return;
	if (!play) {
	    m.stop();
	    b = false;
	} else if (!b) {
	    m.play(true);
	    m.setVolume(soundVolume);
	}
    }

    private void switchSong() {
	String s = "audio/" + SONGS[(int) (Math.random() * SONGS.length)] + ".ogg";
	if (DEBUG)
	    System.out.println("Playing new song: " + s);
	Music m = TinySound.loadMusic(s);
	if (m == null) {
	    System.err.println("Could not find Music: " + s);
	    return;
	}
	m.setVolume(musicVolume);
	m.play(false);
	current = m;
    }
}