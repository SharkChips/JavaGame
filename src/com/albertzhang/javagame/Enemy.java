package com.albertzhang.javagame;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

public class Enemy extends Sprite {

    private static final int MOVE_SPEED = 1;
    private static final int BASE_DAMAGE_AMT = 1;
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
	if (!this.intersects(p.getBounds())) {
	    if (this.getX() < p.getX()) {
		this.setX(this.getX() + MOVE_SPEED * (0.9 + (0.1 * difficulty)));
	    } else {
		this.setX(this.getX() - MOVE_SPEED * (0.9 + (0.1 * difficulty)));
	    }

	    if (this.getY() < p.getY()) {
		this.setY(this.getY() + MOVE_SPEED * (0.9 + (0.1 * difficulty)));
	    } else {
		this.setY(this.getY() - MOVE_SPEED * (0.9 + (0.1 * difficulty)));
	    }
	}
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
	try { // Must surround in try/catch because occasionally
	    angle = Math.atan2(p.getY() - this.getY(), p.getX() - this.getX());
	} catch (NullPointerException n) {
	    // Do nothing because these don't matter
	}
	transform.rotate(angle + Math.PI / 2, orig.getWidth() / 2, orig.getHeight() / 2);
	AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
	return op.filter(orig, null);
    }
}
