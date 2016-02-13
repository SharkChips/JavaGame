package com.albertzhang.spaceevasion;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

public class Enemy extends Sprite {

    private static final double MOVE_SPEED = 0.9;
    private static final double BASE_DAMAGE_AMT = 0.75;
    private static final String DEFAULT_IMAGE = "Entity0.png";

    private Player p;
    private int difficulty = 1;

    public Enemy(String source) {
	super((int) (Math.random() * Main.getFrames()[1].getWidth()), (int) (Math.random() * Main.getFrames()[1].getHeight()), source);
    }

    public Enemy(int d) {
	super((int) (Math.random() * Main.getFrames()[1].getWidth()), (int) (Math.random() * Main.getFrames()[1].getHeight()), DEFAULT_IMAGE);
	this.difficulty = d;
    }

    @Override
    public void doSpecialAction(Object... objects) {
	p = (Player) objects[0];
	double theta = 0d;
	try { // Must surround in try/catch because occasionally there will be a random NullPointerException
	    theta = Math.atan2(this.getY() - p.getY(), this.getX() - p.getX()) + Math.PI / 2;
	} catch (NullPointerException n) {
	    // Do nothing because these don't matter
	}
	this.setX(this.getX() - Math.sin(theta) * MOVE_SPEED);
	this.setY(this.getY() + Math.cos(theta) * MOVE_SPEED);
    }

    @Override
    public void onCollideWithEntity(Sprite s, Object... objects) {
	Player p = (Player) s;
	p.setHealth(p.getHealth() - BASE_DAMAGE_AMT * difficulty);
    }

    public BufferedImage getImage() {
	BufferedImage orig = super.getImage();
	AffineTransform transform = new AffineTransform();
	double angle = 0d;
	try { // Must surround in try/catch because occasionally there will be a random NullPointerException
	    angle = Math.atan2(p.getY() - this.getY(), p.getX() - this.getX());
	} catch (NullPointerException n) {
	    // Do nothing because these don't matter
	}
	transform.rotate(angle + Math.PI / 2, orig.getWidth() / 2, orig.getHeight() / 2);
	AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
	return op.filter(orig, null);
    }
}
