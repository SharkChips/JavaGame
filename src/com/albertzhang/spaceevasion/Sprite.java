package com.albertzhang.spaceevasion;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

public abstract class Sprite {

    private BufferedImage image;

    private double x;
    private double y;

    private double health = 1000;

    public Sprite(String imageSrc) {
	try {
	    image = ImageIO.read(this.getClass().getClassLoader().getResourceAsStream(imageSrc));
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    public Sprite(double x, double y) {
	this.x = x;
	this.y = y;
	image = null;
    }

    public Sprite(double x, double y, String source) {
	this.x = x;
	this.y = y;
	try {
	    image = ImageIO.read(this.getClass().getClassLoader().getResourceAsStream(source));
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    public double getHealth() {
	return health;
    }

    public void setHealth(double d) {
	this.health = d;
    }

    public double getX() {
	return x;
    }

    public void setX(double x) {
	this.x = x;
    }

    public double getY() {
	return y;
    }

    public void setY(double y) {
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

    public boolean intersects(Rectangle2D bounds) {
	return bounds.intersects(this.getBounds());
    }

    public abstract void onCollideWithSprite(Sprite s);
}
