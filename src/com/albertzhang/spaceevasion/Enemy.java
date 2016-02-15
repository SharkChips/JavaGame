package com.albertzhang.spaceevasion;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

public class Enemy extends Sprite {

    private static final double MOVE_SPEED = 0.9;
    private static final double BASE_DAMAGE_AMT = 0.75;
    private static final String DEFAULT_IMAGE = "sprites/Enemy0.png";
    private double theta = 0d;

    private Player p;
    private int difficulty = 1;

    public Enemy(String source) {
	super((int) (Math.random() * Main.getFrames()[1].getWidth()), (int) (Math.random() * Main.getFrames()[1].getHeight()), source);
    }

    public Enemy(int d, Player p) {
	super((int) (Math.random() * Main.getFrames()[1].getWidth()), (int) (Math.random() * Main.getFrames()[1].getHeight()), DEFAULT_IMAGE);
	this.difficulty = d;
	this.p = p;
    }

    public double getTheta() {
	return this.theta;
    }

    public void moveTowardsPlayer() {
	double theta = 0d;
	theta = Math.atan2(this.getY() - p.getY(), this.getX() - p.getX()) + Math.PI / 2;
	this.setX(this.getX() - Math.sin(theta) * MOVE_SPEED);
	this.setY(this.getY() + Math.cos(theta) * MOVE_SPEED);
    }

    @Override
    public void onCollideWithEntity(Sprite s, Object... objects) {
	Player p = (Player) s;
	p.setHealth(p.getHealth() - BASE_DAMAGE_AMT * difficulty);
    }

    @Override
    public BufferedImage getImage() {
	BufferedImage orig = super.getImage();
	AffineTransform transform = new AffineTransform();
	this.theta = Math.atan2(p.getY() - this.getY(), p.getX() - this.getX());
	transform.rotate(this.theta + Math.PI / 2, orig.getWidth() / 2, orig.getHeight() / 2);
	AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
	return op.filter(orig, null);
    }
}
