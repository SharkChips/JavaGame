package com.albertzhang.spaceevasion;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

public class DyingEnemy extends Sprite {

    private static final String DEFAULT_IMAGE = "sprites/Enemy1.png";
    private float deathStage = 1f;
    private double theta;

    public DyingEnemy(double x, double y, double theta) {
	super(x, y, DEFAULT_IMAGE);
	this.theta = theta;
    }

    public float getDeathStage() {
	return deathStage -= 0.05;
    }

    @Override
    public BufferedImage getImage() {
	BufferedImage orig = super.getImage();
	AffineTransform transform = new AffineTransform();
	transform.rotate(this.theta + Math.PI / 2, orig.getWidth() / 2, orig.getHeight() / 2);
	AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
	return op.filter(orig, null);
    }

    @Override
    public void onCollideWithSprite(Sprite s) {
	// Do nothing because it's dead :(
    }
}
