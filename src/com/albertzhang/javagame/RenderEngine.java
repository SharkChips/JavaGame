package com.albertzhang.javagame;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class RenderEngine extends JPanel implements Runnable {

    private Thread runThread;

    private boolean isRunning = false;
    private boolean isPaused = false;
    private boolean DEBUG = false;

    private static final int FRAMES_PER_SECOND = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode().getRefreshRate();
    private static final int STARS = 25;

    private static LogicEngine lg;
    private ArrayList<Integer> xValues = new ArrayList<>(), yValues = new ArrayList<>();
    private int width;
    private int height;

    private long frames = 0;
    private long startMS = 0;

    public RenderEngine(LogicEngine logic) {
	lg = logic;
	this.width = Main.width;
	this.height = Main.height;
	initBg();
    }

    public RenderEngine(LogicEngine logic, boolean debug) {
	this(logic);
	DEBUG = debug;
	if (DEBUG) { // If Main.DEBUG, record the starting timeF
	    startMS = System.currentTimeMillis();
	}
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
	    runThread = new Thread(this, "Render-1");
	else if (runThread.isAlive())
	    throw new IllegalStateException("Render Thread has already started.");
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
	    throw new IllegalStateException("Render Thread has not been started yet.");
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
     * Pauses rendering
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
	    System.out.println("Running at VSync: " + FRAMES_PER_SECOND + " FPS");

	long amountToSleep = 0, timeBeforePainting;
	while (isRunning) {
	    timeBeforePainting = System.currentTimeMillis();

	    repaint(); // Renders the frame

	    if (DEBUG) {
		frames++;
	    }

	    try { // Sleep for required amount of time to maintain FRAMES_PER_SECOND
		amountToSleep = (int) (1000 / FRAMES_PER_SECOND) - (System.currentTimeMillis() - timeBeforePainting);
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

    @Override
    public void paintComponent(Graphics graphics) {
	int sprites = 0;

	super.paintComponent(graphics);
	if (graphics == null) {
	    if (DEBUG)
		System.err.println("Graphics is null!");
	    return;
	}

	Graphics2D g = (Graphics2D) graphics;

	// Draw background
	setBackground(lg.getBGColor());
	g.setColor(Color.YELLOW);
	for (int i = 0; i < STARS; i++) { // The stars move down and either left or right
	    yValues.set(i, yValues.get(i) + (int) (Math.random() * 2));
	    xValues.set(i, xValues.get(i) + (int) ((Math.random() > 0.5 ? 1 : -1) * Math.random() * 2));
	}
	for (int i = 0; i < STARS; i++) {
	    g.fillOval(xValues.get(i), yValues.get(i), 2, 2);
	}

	// Here we handle drawing objects
	for (ArrayList<Sprite> a : lg.getShapes().values()) {
	    for (int index = 0; index < a.size(); index++) {
		Sprite s = a.get(index);
		g.drawImage(s.getImage(), s.getX(), s.getY(), null);

		// If DEBUG, draw hitboxes and add 1 to sprites
		if (DEBUG) {
		    g.setColor(new Color(0, 255, 0, 50));
		    g.drawRect(s.getX(), s.getY(), s.getWidth(), s.getHeight());
		    sprites++;
		}
	    }
	}

	// Draw player
	Player p = lg.getPlayer();
	g.drawImage(p.getImage(), p.getX(), p.getY(), null);

	if (DEBUG) { // If DEBUG, display hitboxes
	    g.setColor(new Color(0, 0, 255, 50));
	    g.drawRect(p.getX(), p.getY(), p.getWidth(), p.getHeight());
	}

	// Draws health
	g.setColor(Color.BLUE);
	g.drawString("Health: " + lg.getPlayer().getHealth(), 10, 20);

	if (DEBUG && (System.currentTimeMillis() - startMS) > 1000) { // If DEBUG draw fps
	    g.drawString("FPS: " + frames / ((System.currentTimeMillis() - startMS) / 1000), Main.getWindows()[1].getWidth() - 100, 20);
	    g.drawString("Entities: " + (sprites + 1), width - 100, 40);
	}
    }

    private void initBg() {
	for (int i = 0; i < STARS; i++) {
	    xValues.add((int) (Math.random() * width));
	    yValues.add((int) (Math.random() * height));
	}
    }
}