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
    private double volume;

    private static final int SECONDS_PER_SONG = 60;
    private static final String[] SONGS = {};
    private static HashMap<String, Sound> SOUNDS = new HashMap<>();

    public AudioEngine(double volume, boolean debug) {
	DEBUG = debug;
	this.volume = volume;

	// Load Sounds
	SOUNDS.put("Boom", TinySound.loadSound("audio/Boom.ogg"));
	SOUNDS.put("Alarm", TinySound.loadSound("audio/Alarm.ogg"));
	SOUNDS.put("Laser", TinySound.loadSound("audio/Laser.ogg"));
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
     * Pauses music
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
     * Resumes rendering thread
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
	    System.out.println("Running at: " + SECONDS_PER_SONG + " FPS");

	while (isRunning) {
	    switchSong();

	    try { // Sleep for required amount of time to maintain FRAMES_PER_SECOND
		Thread.sleep(SECONDS_PER_SONG); // Sleep for 'amountToSleep' or 0, whichever is greater.
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

    private void switchSong() {
	if (SONGS.length == 0) {
	    return;
	}
	String s = SONGS[(int) (Math.random() * SONGS.length)];
	if (DEBUG)
	    System.out.println("Playing new song: " + s);
	Music m = TinySound.loadMusic(s);
	m.setVolume(volume);
    }

}