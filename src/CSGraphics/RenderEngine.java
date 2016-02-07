package CSGraphics;

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

    public RenderEngine(LogicEngine logic) {
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

	    repaint(); // Renders the frame

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
		g.drawImage(s, s.getX(), s.getY(), null);
	    }
	}

	g.drawImage(lg.getPlayer().getImage(), lg.getPlayer().getX(), lg.getPlayer().getY(), null);
    }
}