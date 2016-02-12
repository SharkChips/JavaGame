package com.albertzhang.javagame;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

public class Projectile extends Sprite {

    private static final String DEFAULT_IMAGE = "Entity1.png";
    private static final int DAMAGE_AMT = 1001;
    private static final int MOV_SPEED = 8;

    private double theta = 0d;

    public Projectile() {
	super(DEFAULT_IMAGE);
    }

    public Projectile(double x, double y, double theta) {
	super(x, y, DEFAULT_IMAGE);
	this.theta = theta;
    }

    public double getTheta() {
	return this.theta;
    }

    @Override
    public void doSpecialAction(Object... objects) {
	this.setX(this.getX() + Math.sin(theta + Math.PI / 2) * MOV_SPEED);
	this.setY(this.getY() + Math.cos(theta + Math.PI / 2) * MOV_SPEED);
    }

    @Override
    public void onCollideWithEntity(Sprite s, Object... objects) {
	s.setHealth(s.getHealth() - DAMAGE_AMT);
    }

    public BufferedImage getImage() {
	BufferedImage orig = super.getImage();
	AffineTransform transform = new AffineTransform();
	transform.rotate(-(this.theta - Math.PI / 2), orig.getWidth() / 2, orig.getHeight() / 2);
	AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
	return op.filter(orig, null);
    }
}
