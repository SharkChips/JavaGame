package CSGraphics;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * The Player class basically is a Sprite with 8 images, one for each direction
 * 
 * @author Albert
 */
public class Player extends Sprite {
    enum Direction {
	NORTH(0), SOUTH(1), EAST(2), WEST(3), NE(4), NW(5), SE(6), SW(7);
	private int value;

	private Direction(int value) {
	    this.value = value;
	}

	private int getValue() {
	    return value;
	}
    }

    private BufferedImage[] images = new BufferedImage[8];

    private Direction direction = Direction.NORTH;

    public Player(double x, double y) throws IOException {
	super(x, y);
	try {
	    for (int a = 0; a < images.length; a++) {
		images[a] = ImageIO.read(this.getClass().getClassLoader().getResourceAsStream("Player" + a + ".png"));
	    }
	} catch (IOException e) {
	    System.err.println("Error reading player images.");
	    e.printStackTrace();
	    System.exit(1);
	}
    }

    @Override
    public BufferedImage getImage() {
	return images[direction.getValue()];
    }

    @Override
    public void setImage(BufferedImage i) {
	throw new UnsupportedOperationException("Cannot create player sprite with one image. Must use setImage(BufferedImage[] i)");
    }

    public void setImage(BufferedImage[] i) {
	this.images = i;
    }

    public Direction getDirection() {
	return direction;
    }

    public void setDirection(Direction direction) {
	this.direction = direction;
    }

    @Override
    public void moveNorth(int amount) {
	setDirection(Direction.NORTH);
	this.setY(this.getY() - amount);
    }

    @Override
    public void moveSouth(int amount) {
	setDirection(Direction.SOUTH);
	this.setY(this.getY() + amount);
    }

    @Override
    public void moveEast(int amount) {
	setDirection(Direction.EAST);
	this.setX(this.getX() + amount);
    }

    @Override
    public void moveWest(int amount) {
	setDirection(Direction.WEST);
	this.setX(this.getX() - amount);
    }

    @Override
    public void moveNE(int amount) {
	setDirection(Direction.NE);
	this.setY(this.getY() - amount);
	this.setX(this.getX() + amount);
    }

    @Override
    public void moveNW(int amount) {
	setDirection(Direction.NW);
	this.setY(this.getY() - amount);
	this.setX(this.getX() - amount);
    }

    @Override
    public void moveSE(int amount) {
	setDirection(Direction.SE);
	this.setY(this.getY() + amount);
	this.setX(this.getX() + amount);
    }

    @Override
    public void moveSW(int amount) {
	setDirection(Direction.SW);
	this.setY(this.getY() + amount);
	this.setX(this.getX() - amount);
    }

    public static double dirToRad(Direction d) {
	switch (d.getValue()) {
	    case 0:
		return Math.PI / 2;
	    case 1:
		return 3 * Math.PI / 2;
	    case 2:
		return 0;
	    case 3:
		return Math.PI;
	    case 4:
		return Math.PI / 4;
	    case 5:
		return 3 * Math.PI / 4;
	    case 6:
		return 7 * Math.PI / 4;
	    case 7:
		return 5 * Math.PI / 4;
	    default:
		throw new IllegalStateException("Direction not valid");
	}
    }
}
