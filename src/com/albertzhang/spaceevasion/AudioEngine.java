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
    private double musicVolume;

    private static final int SECONDS_PER_SONG = 60;
    private static final String[] SONGS = {};
    private static HashMap<String, Sound> SOUNDS = new HashMap<>();
    private static HashMap<String, Music> CONTINUOUS_SOUNDS = new HashMap<>();

    public AudioEngine(double musicVol, double soundVol, boolean debug) {
	DEBUG = debug;
	this.musicVolume = musicVol;
	try {
	    TinySound.init();
	    TinySound.setGlobalVolume(soundVol);
	} catch (Exception e) {
	    System.err.println("Could not load sound system!");
	    e.printStackTrace();
	}

	if (DEBUG)
	    System.out.println("Audio Volume: " + TinySound.getGlobalVolume() * 100);

	// Load Sounds
	SOUNDS.put("Boom", TinySound.loadSound("audio/Boom.ogg"));
	SOUNDS.put("Laser", TinySound.loadSound("audio/Laser.ogg"));
	CONTINUOUS_SOUNDS.put("Alarm", TinySound.loadMusic("audio/Alarm.ogg"));
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
	if (DEBUG)
	    System.out.println("Running at: " + SECONDS_PER_SONG + " Seconds per song");

	while (isRunning) {
	    switchSong();

	    try { // Sleep for required amount
		Thread.sleep(SECONDS_PER_SONG);
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
     */
    public static void playSoundContinuous(String name) {
	Music m = CONTINUOUS_SOUNDS.get(name);
	System.out.println("m.playing: " + m.playing());
	System.out.println("m.done: " + m.done());
	if (!m.playing() || m.done()) {
	    m.play(false);
	}
    }

    private void switchSong() {
	if (SONGS.length == 0) {
	    return;
	}
	String s = SONGS[(int) (Math.random() * SONGS.length)];
	if (DEBUG)
	    System.out.println("Playing new song: " + s);
	Music m = TinySound.loadMusic(s);
	m.setVolume(musicVolume);
	m.play(false);
    }
}