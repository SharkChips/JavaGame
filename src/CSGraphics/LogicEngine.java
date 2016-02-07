package CSGraphics;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("serial")
public class LogicEngine implements Runnable {

    private static final int TICKS_PER_SECOND = 60;

    private Thread runThread;

    private boolean isRunning = false;
    private boolean isPaused = false;

    private Map<String, ArrayList<Sprite>> sprites = new HashMap<>();
    private Player p;
    private Color bgColor = Color.WHITE;

    Map<String, ArrayList<Sprite>> getShapes() {
	return this.sprites;
    }

    Color getBGColor() {
	return this.bgColor;
    }

    Player getPlayer() {
	return p;
    }

    public LogicEngine() {
	initSprites();
    }

    /**
     * Starts the thread
     */
    public void start() {
	isRunning = true;
	isPaused = false;
	if (runThread == null || !runThread.isAlive())
	    runThread = new Thread(this);
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

    public void run() {
	if (Main.DEBUG)
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
	try {
	    p = new Player(0, 0); // Make a new player to initialize the image so we can set X and Y appropriately
	    p.setX(Main.getWindows()[0].getWidth() / 2 - p.getWidth() / 2);
	    p.setY(Main.getWindows()[0].getHeight() / 2 - p.getHeight() / 2);
	} catch (IOException e) {
	    System.err.println("Player image not found");
	    e.printStackTrace();
	    System.exit(1);
	}

	sprites.put("Squares", new ArrayList<Sprite>() {
	    {
	    }
	});
	sprites.put("Ellipses", new ArrayList<Sprite>() {
	    {
	    }
	});
    }

    private void doLogic() {
	ArrayList<Sprite> squares = sprites.get("Squares");
	for (Sprite s : squares) {
	    if (s.intersects(p.getBounds())) {
		System.out.println("Intersected!");
	    }
	}
    }

    /**
     * Generates a random color with three random RGB values
     * 
     * @return A random color
     */
    private Color randomColor() {
	return new Color((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255));
    }
}