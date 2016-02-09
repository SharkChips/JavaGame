package CSGraphics;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

public class Enemy extends Sprite {

    private static final int MOVE_SPEED = 1;
    private static final int DAMAGE_AMT = 1;

    private static final String DEFAULT_IMAGE = "Entity0.png";

    private Player p;

    public Enemy(String source) {
	super((int) (Math.random() * Main.getFrames()[0].getWidth()), (int) (Math.random() * Main.getFrames()[0].getHeight()), source);
    }

    public Enemy() {
	super((int) (Math.random() * Main.getFrames()[0].getWidth()), (int) (Math.random() * Main.getFrames()[0].getHeight()), DEFAULT_IMAGE);
    }

    @Override
    public void doSpecialAction(Object... objects) {
	p = (Player) objects[0];
	if (!this.intersects(p.getBounds())) {
	    if (this.getX() < p.getX()) {
		this.setX(this.getX() + MOVE_SPEED);
	    } else {
		this.setX(this.getX() - MOVE_SPEED);
	    }

	    if (this.getY() < p.getY()) {
		this.setY(this.getY() + MOVE_SPEED);
	    } else {
		this.setY(this.getY() - MOVE_SPEED);
	    }
	}
    }

    @Override
    public void onCollideWithEntity(Sprite s, Object... objects) {
	Player p = (Player) s;
	p.setHealth(p.getHealth() - DAMAGE_AMT);
    }

    public BufferedImage getImage() {
	BufferedImage orig = super.getImage();
	AffineTransform transform = new AffineTransform();
	double angle = Math.atan2(p.getY() - this.getY(), p.getX() - this.getX());
	transform.rotate(angle + Math.PI / 2, orig.getWidth() / 2, orig.getHeight() / 2);
	AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
	return op.filter(orig, null);
    }
}
