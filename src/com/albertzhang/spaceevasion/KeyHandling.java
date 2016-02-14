package com.albertzhang.spaceevasion;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import kuusisto.tinysound.TinySound;

public class KeyHandling implements Runnable, KeyListener {

    private enum Bounds {
	NORTH, SOUTH, EAST, WEST, NE, NW, SE, SW, NONE;
    }

    private Thread runThread;

    private boolean isRunning = false;
    private boolean isPaused = false;
    private boolean DEBUG = false;

    private static final int HANDLES_PER_SECOND = 60;

    private static LogicEngine l;

    private boolean wPressed = false;
    private boolean aPressed = false;
    private boolean sPressed = false;
    private boolean dPressed = false;

    private static final int dX_DIRECTIONAL = 5;
    private static final int dX_DIAGONAL = 3;

    public KeyHandling(LogicEngine lg) {
	l = lg;
    }

    public KeyHandling(LogicEngine lg, boolean debug) {
	l = lg;
	DEBUG = debug;
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
	    runThread = new Thread(this, "KeyHandle-1");
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
	    throw new IllegalStateException("KeyHandling Thread has not been started yet.");
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
     * Pauses KeyHandling
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
     * Resumes KeyHandling thread
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
	if (DEBUG)
	    System.out.println("Key Handling running at: " + HANDLES_PER_SECOND + " times per second");

	long amountToSleep = 0, timeBefore;
	while (isRunning) {
	    timeBefore = System.currentTimeMillis();

	    // Checks keys and moves stuffs
	    checkKeys();

	    try { // Sleep for required amount of time to maintain FRAMES_PER_SECOND
		amountToSleep = (int) (1000 / HANDLES_PER_SECOND) - (System.currentTimeMillis() - timeBefore);
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
    public void keyTyped(KeyEvent e) {
	// Unused
    }

    @Override
    public void keyPressed(KeyEvent e) {
	switch (e.getKeyCode()) {
	    case KeyEvent.VK_W:
		wPressed = true;
		break;
	    case KeyEvent.VK_A:
		aPressed = true;
		break;
	    case KeyEvent.VK_S:
		sPressed = true;
		break;
	    case KeyEvent.VK_D:
		dPressed = true;
		break;
	    default:
		break;
	}
    }

    @Override
    public void keyReleased(KeyEvent e) {
	switch (e.getKeyCode()) {
	    case KeyEvent.VK_W:
		wPressed = false;
		break;
	    case KeyEvent.VK_A:
		aPressed = false;
		break;
	    case KeyEvent.VK_S:
		sPressed = false;
		break;
	    case KeyEvent.VK_D:
		dPressed = false;
		break;
	    case KeyEvent.VK_SPACE:
		ArrayList<Sprite> projectiles = l.getShapes().get("Projectiles");
		Player p = l.getPlayer();
		// TODO: change 16 to half the final sprite's width
		projectiles.add(new Projectile(p.getX() + p.getWidth() / 2 - 16, p.getY() + p.getHeight() / 2 - 16, Player.dirToRad(p.getDirection())));
		System.out.println("laser!");
		AudioEngine.playSound("Laser");
		break;
	    case KeyEvent.VK_ESCAPE:
		wPressed = false;
		aPressed = false;
		sPressed = false;
		dPressed = false;
		l.pause();
		double priorVol = TinySound.getGlobalVolume();
		TinySound.setGlobalVolume(0);
		if (JOptionPane.showConfirmDialog(Main.getWindows()[1], "Would you like to exit?", Launcher.getName(), JOptionPane.YES_NO_OPTION,
			JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
		    System.exit(0);
		} else {
		    l.resume();
		    TinySound.setGlobalVolume(priorVol);
		}
	    default:
		break;
	}
    }

    public void checkKeys() {
	if (wPressed) {
	    if (aPressed) { // W&A
		if (checkBounds() == Bounds.NORTH) {
		    l.getPlayer().moveWest(dX_DIRECTIONAL);
		} else if (checkBounds() == Bounds.WEST) {
		    l.getPlayer().moveNorth(dX_DIRECTIONAL);
		} else if (checkBounds() != Bounds.NW) {
		    l.getPlayer().moveNW(dX_DIAGONAL);
		}
	    } else if (dPressed) { // W&D
		if (checkBounds() == Bounds.NORTH) {
		    l.getPlayer().moveEast(dX_DIRECTIONAL);
		} else if (checkBounds() == Bounds.EAST) {
		    l.getPlayer().moveNorth(dX_DIRECTIONAL);
		} else if (checkBounds() != Bounds.NE) {
		    l.getPlayer().moveNE(dX_DIAGONAL);
		}
	    } else if (checkBounds() != Bounds.NORTH && checkBounds() != Bounds.NW && checkBounds() != Bounds.NE) { // Just W
		l.getPlayer().moveNorth(dX_DIRECTIONAL);
	    }
	} else if (sPressed) {
	    if (aPressed) { // S&A
		if (checkBounds() == Bounds.SOUTH) {
		    l.getPlayer().moveWest(dX_DIRECTIONAL);
		} else if (checkBounds() == Bounds.WEST) {
		    l.getPlayer().moveSouth(dX_DIRECTIONAL);
		} else if (checkBounds() != Bounds.SW) {
		    l.getPlayer().moveSW(dX_DIAGONAL);
		}
	    } else if (dPressed) { // S&D
		if (checkBounds() == Bounds.SOUTH) {
		    l.getPlayer().moveEast(dX_DIRECTIONAL);
		} else if (checkBounds() == Bounds.EAST) {
		    l.getPlayer().moveSouth(dX_DIRECTIONAL);
		} else if (checkBounds() != Bounds.SE) {
		    l.getPlayer().moveSE(dX_DIAGONAL);
		}
	    } else if (checkBounds() != Bounds.SOUTH && checkBounds() != Bounds.SW && checkBounds() != Bounds.SE) { // Just S
		l.getPlayer().moveSouth(dX_DIRECTIONAL);
	    }
	} else if (aPressed && checkBounds() != Bounds.WEST && checkBounds() != Bounds.NW && checkBounds() != Bounds.SW) { // Just A
	    l.getPlayer().moveWest(dX_DIRECTIONAL);
	} else if (dPressed && checkBounds() != Bounds.EAST && checkBounds() != Bounds.NE && checkBounds() != Bounds.SE) { // Just D
	    l.getPlayer().moveEast(dX_DIRECTIONAL);
	} else { // No keys
	    // Do nothing for right now
	}
    }

    private Bounds checkBounds() {
	Player p = l.getPlayer();
	Rectangle2D top = new Rectangle2D.Double(-10, -10, Main.getWindows()[1].getWidth() + 20, 10);
	Rectangle2D bottom = new Rectangle2D.Double(-10, Main.getWindows()[1].getHeight() - p.getHeight() / 2, Main.getWindows()[1].getWidth() + 20, 10);
	Rectangle2D left = new Rectangle2D.Double(-10, -10, 10, Main.getWindows()[1].getHeight() + 20);
	// TODO: remove the "-10" when we get new pictures
	Rectangle2D right = new Rectangle2D.Double(Main.getWindows()[1].getWidth() - 10, -10, 10, Main.getWindows()[1].getHeight() + 10);

	if (p.getBounds().intersects(top)) {
	    if (p.getBounds().intersects(left)) {
		return Bounds.NW;
	    } else if (p.getBounds().intersects(right)) {
		return Bounds.NE;
	    } else {
		return Bounds.NORTH;
	    }
	} else if (p.getBounds().intersects(bottom)) {
	    if (p.getBounds().intersects(left)) {
		return Bounds.SW;
	    } else if (p.getBounds().intersects(right)) {
		return Bounds.SE;
	    } else {
		return Bounds.SOUTH;
	    }
	} else if (p.getBounds().intersects(left)) {
	    return Bounds.WEST;
	} else if (p.getBounds().intersects(right)) {
	    return Bounds.EAST;
	} else {
	    return Bounds.NONE;
	}
    }
}
