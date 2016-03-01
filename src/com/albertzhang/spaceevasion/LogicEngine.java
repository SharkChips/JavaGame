package com.albertzhang.spaceevasion;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import kuusisto.tinysound.TinySound;

@SuppressWarnings("serial")
public class LogicEngine implements Runnable {

    private static final int TICKS_PER_SECOND = 60;
    private static final int MAX_ENEMIES = 100;
    private static final double BASE_DIFFICULTY = 0.0070;

    private Thread runThread;

    private boolean isRunning = false;
    private boolean isPaused = false;
    private boolean DEBUG = false;

    private Map<String, ArrayList<Sprite>> sprites = new HashMap<>();
    private ArrayList<DyingEnemy> dyingEnemies = new ArrayList<>();
    private Player p;
    private double difficulty = 1d;
    private int score = 0;
    private int width;
    private int height;

    private Color bgColor = Color.BLACK;

    public LogicEngine() {
	this.width = Main.width;
	this.height = Main.height;
	initSprites();
    }

    public LogicEngine(int difficulty, boolean debug) {
	this();
	this.difficulty = difficulty;
	DEBUG = debug;
    }

    public Map<String, ArrayList<Sprite>> getShapes() {
	return this.sprites;
    }

    public ArrayList<DyingEnemy> getDyingEnemies() {
	return dyingEnemies;
    }

    public Color getBGColor() {
	return this.bgColor;
    }

    public Player getPlayer() {
	return p;
    }

    public double getDifficulty() {
	if (Math.abs(this.difficulty - 3d) <= 0.1) { // If insane difficulty
	    return 10d;
	}
	return this.difficulty * 1.1;
    }

    public int getScore() {
	return this.score;
    }

    public void setScore(int score) {
	this.score = score > 0 ? score : 0;
    }

    /**
     * Starts the thread
     */
    public void start() {
	isRunning = true;
	isPaused = false;
	if (runThread == null || !runThread.isAlive())
	    runThread = new Thread(this, "Logic-1");
	else if (runThread.isAlive())
	    throw new IllegalStateException("Logic Thread has already started.");
	runThread.start();
    }

    /**
     * Stops the thread and ends the program
     */
    public void stop() {
	if (runThread == null)
	    throw new IllegalStateException("Logic Thread has not been started yet.");
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
     * Pauses logic
     */
    public void pause() {
	if (runThread == null)
	    throw new IllegalStateException("Thread not started.");
	synchronized (runThread) {
	    isPaused = true;
	}
    }

    /**
     * Resumes game logic thread
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
	    System.out.println("Logic running at: " + TICKS_PER_SECOND + " Ticks per second");

	long amountToSleep = 0, timeBefore;
	while (isRunning) {
	    timeBefore = System.currentTimeMillis();

	    // DO LOGIC HERE
	    doLogic();

	    try { // Sleep for required amount of time to maintain TICKS_PER_SECOND
		amountToSleep = (int) (1000 / TICKS_PER_SECOND) - (System.currentTimeMillis() - timeBefore);
		Thread.sleep(amountToSleep > 0 ? amountToSleep : 0); // Sleep for 'amountToSleep' or 0, whichever is greater.
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

    private void initSprites() {
	setScore(0);
	try {
	    p = new Player(0, 0); // Make a new player to initialize the image so we can set X and Y appropriately
	    p.setX(width / 2 - p.getWidth() / 2);
	    p.setY(height / 2 - p.getHeight() / 2);
	} catch (IOException e) {
	    System.err.println("Player image not found");
	    e.printStackTrace();
	    System.exit(1);
	}

	sprites.put("Enemies", new ArrayList<Sprite>() {
	    {
	    }
	});
	sprites.put("Projectiles", new ArrayList<Sprite>() {
	    {
	    }
	});
    }

    private void doLogic() {
	if (p.getHealth() < 0) {
	    p.setHealth(0); // This prevents the user from seeing health be negative
	    double oldMusVol = AudioEngine.getMusicVolume();
	    double oldSndVol = AudioEngine.getSoundVolume();
	    AudioEngine.setMusicVolume(0);
	    AudioEngine.setSoundVolume(0);
	    JOptionPane.showMessageDialog(Main.getWindows()[1], "You lost with a score of " + getScore() + '!', Launcher.getName(), JOptionPane.INFORMATION_MESSAGE);
	    this.pause();
	    if (JOptionPane.showConfirmDialog(Main.getWindows()[1], "Would you like to play again?", Launcher.getName(), JOptionPane.YES_NO_OPTION,
		    JOptionPane.PLAIN_MESSAGE) == JOptionPane.YES_OPTION) {
		this.initSprites();
		this.resume();
		AudioEngine.setSoundVolume(oldSndVol);
		AudioEngine.setMusicVolume(oldMusVol);
	    } else {
		TinySound.shutdown();
		System.exit(0);
	    }
	}

	ArrayList<Sprite> enemies = (ArrayList<Sprite>) sprites.get("Enemies");
	ArrayList<Sprite> projectiles = sprites.get("Projectiles");

	// Spawns more enemies if there can be more
	if (Math.random() < BASE_DIFFICULTY * this.getDifficulty() && enemies.size() < MAX_ENEMIES) {
	    enemies.add(spawnEnemy());
	    // TODO: Play Spawn sound
	}

	Rectangle2D pBounds = p.getBounds(); // The bounds of the player
	double pX = p.getX();
	double pY = p.getY();
	boolean playAlarm = false;
	for (int index = 0; index < enemies.size(); index++) {
	    Sprite s = enemies.get(index);
	    if (s.getHealth() < 0) { // Removes dead enemies
		dyingEnemies.add(new DyingEnemy(s.getX(), s.getY(), ((Enemy) s).getTheta()));
		enemies.remove(index);
		setScore(getScore() + 55);
		AudioEngine.playSound("Boom");
	    }

	    ((Enemy) s).moveTowardsPlayer();

	    // The following line optimizes collision detection. It decreases time by up to 8 ms.
	    // 91 is the max distance between 2 touching 64px square sprites. TODO: Change this if sprite sizes change
	    if (Math.abs(s.getX() - pX) < 91 && Math.abs(s.getY() - pY) < 91) {
		if (s.intersects(pBounds)) {
		    s.onCollideWithSprite(p); // Subtract player health if collides with player
		    playAlarm = true;
		}
	    }
	}
	AudioEngine.playSoundContinuous("Alarm", playAlarm);

	for (int index = 0; index < projectiles.size(); index++) {
	    Sprite proj = projectiles.get(index);
	    ((Projectile) proj).move();
	    double projX = proj.getX();
	    double projY = proj.getY();
	    for (Sprite en : enemies) {
		// 64 is the maximum distance between a 32x32 sprite and a 64x64 sprite. TODO: Change if sprites change
		if (Math.abs(projX - en.getX()) < 64 && Math.abs(projY - en.getY()) < 64) {
		    if (proj.intersects(en.getBounds())) {
			proj.onCollideWithSprite(en); // Subtract player health if collides with player
			projectiles.remove(index);
			break;
		    }
		}
	    }
	    if (proj.getX() < -32 || proj.getX() > width || proj.getY() < -32 || proj.getY() > height) {
		projectiles.remove(index);
	    }
	}

	// Make the difficulty a little harder each time (1.8 every minute)
	difficulty += 0.0005 * difficulty;
    }

    /**
     * Generates a random color with three random RGB values
     * 
     * @return A random color
     */
    @SuppressWarnings("unused")
    private Color randomColor() {
	return new Color((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255));
    }

    private Enemy spawnEnemy() {
	Enemy e = new Enemy(difficulty, p);
	if (Math.abs(e.getX() - p.getX()) < 128 && Math.abs(e.getY() - p.getY()) < 128) {
	    return spawnEnemy(); // If it is somewhat close to the player, try again
	} else {
	    return e;
	}
    }
}