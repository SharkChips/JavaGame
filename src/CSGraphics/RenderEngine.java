package CSGraphics;

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

    private static final int FRAMES_PER_SECOND = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode().getRefreshRate();
    private static LogicEngine lg;

    private long frames = 0;
    private long startMS = 0;

    public RenderEngine(LogicEngine logic) {
	if (Main.DEBUG) { // If Main.DEBUG, record the starting timeF
	    startMS = System.currentTimeMillis();
	}

	lg = logic;
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
	    runThread = new Thread(this);
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

    public void run() {
	if (Main.DEBUG)
	    System.out.println("Running at VSync: " + FRAMES_PER_SECOND + " FPS");

	long amountToSleep = 0, timeBeforePainting;
	while (isRunning) {
	    timeBeforePainting = System.currentTimeMillis();

	    repaint(); // Renders the frame\

	    if (Main.DEBUG) {
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

    public void paintComponent(Graphics graphics) {
	int sprites = 0;

	super.paintComponent(graphics);
	if (graphics == null) {
	    if (Main.DEBUG)
		System.err.println("Graphics is null!");
	    return;
	}

	Graphics2D g = (Graphics2D) graphics;

	setBackground(lg.getBGColor());
	// Here we handle drawing objects
	for (ArrayList<Sprite> a : lg.getShapes().values()) {
	    for (Sprite s : a) {
		g.drawImage(s.getImage(), s.getX(), s.getY(), null);

		// If DEBUG, draw hitboxes and add 1 to sprites
		if (Main.DEBUG) {
		    g.setColor(new Color(0, 255, 0, 50));
		    g.drawRect(s.getX(), s.getY(), s.getWidth(), s.getHeight());
		    sprites++;
		}
	    }
	}

	// Draw player
	g.drawImage(lg.getPlayer().getImage(), lg.getPlayer().getX(), lg.getPlayer().getY(), null);

	if (Main.DEBUG) { // If DEBUG, display hitboxes
	    g.setColor(new Color(0, 0, 255, 50));
	    g.drawRect(lg.getPlayer().getX(), lg.getPlayer().getY(), lg.getPlayer().getWidth(), lg.getPlayer().getHeight());
	}

	// Draws health
	g.setColor(Color.BLUE);
	g.drawString("Health: " + lg.getPlayer().getHealth(), 10, 20);

	if ((System.currentTimeMillis() - startMS) > 1000) { // If Main.DEBUG draw fps
	    g.drawString("FPS: " + frames / ((System.currentTimeMillis() - startMS) / 1000), Main.getWindows()[0].getWidth() - 100, 20);
	    g.drawString("Entities: " + (sprites + 1), Main.getWindows()[0].getWidth() - 100, 40);
	}
    }
}