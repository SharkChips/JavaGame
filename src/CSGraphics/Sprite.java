package CSGraphics;

import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Sprite extends java.awt.Image {

    private BufferedImage image;

    private int x;
    private int y;

    public Sprite(File source) {
	try {
	    image = ImageIO.read(source);
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    public Sprite(int x, int y) {
	this.x = x;
	this.y = y;
	image = null;
    }

    public Sprite(int x, int y, File source) {
	this.x = x;
	this.y = y;
	try {
	    image = ImageIO.read(source);
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    public void doSpecialAction(Object... objects) {
	// This method is for each Sprite's special action (eg. following player)
    }

    public int getX() {
	return x;
    }

    public void setX(int x) {
	this.x = x;
    }

    public int getY() {
	return y;
    }

    public void setY(int y) {
	this.y = y;
    }

    public BufferedImage getImage() {
	return image;
    }

    public void setImage(BufferedImage b) {
	image = b;
    }

    public int getWidth() {
	return getImage().getWidth();
    }

    public int getHeight() {
	return getImage().getHeight();
    }

    public Rectangle2D getBounds() {
	return new Rectangle2D.Double(getX(), getY(), getWidth(), getHeight());
    }

    public void moveNorth(int amount) {
	this.setX(this.getX() - amount);
    }

    public void moveSouth(int amount) {
	this.setX(this.getX() + amount);
    }

    public void moveEast(int amount) {
	this.setY(this.getY() + amount);
    }

    public void moveWest(int amount) {
	this.setY(this.getY() - amount);
    }

    public void moveNE(int amount) {
	this.setX(this.getX() - amount);
	this.setY(this.getY() + amount);
    }

    public void moveNW(int amount) {
	this.setX(this.getX() - amount);
	this.setY(this.getY() - amount);
    }

    public void moveSE(int amount) {
	this.setX(this.getX() + amount);
	this.setY(this.getY() + amount);
    }

    public void moveSW(int amount) {
	this.setX(this.getX() + amount);
	this.setY(this.getY() - amount);
    }

    public boolean intersects(Rectangle2D bounds) {
	return bounds.intersects(this.getBounds());
    }

    // -------The following methods are inherited from java.awt.Image

    public int getWidth(ImageObserver observer) {
	return image.getWidth();
    }

    @Override
    public int getHeight(ImageObserver observer) {
	return image.getHeight();
    }

    @Override
    public ImageProducer getSource() {
	return image.getSource();
    }

    @Override
    public Graphics getGraphics() {
	return image.getGraphics();
    }

    @Override
    public Object getProperty(String name, ImageObserver observer) {
	return image.getProperty(name, observer);
    }
}
