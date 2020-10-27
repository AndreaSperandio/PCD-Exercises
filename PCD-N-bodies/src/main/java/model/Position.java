package model;

/**
 * Represents a Body's two-dimensional Position in the System, characterised by a (x, y) couple
 *
 * @author Andrea Sperandio
 *
 */
public class Position {
	public static final Position NULL_POSITION = new Position(0D, 0D);
	public static final double NULL_DISTANCE = 0.0000000000001;

	private double x;  // m
	private double y;  // m

	public Position(final double x, final double y) {
		super();
		this.x = x;
		this.y = y;
	}

	/**
	 * Moves the current position performing a sum operation
	 */
	public void move(final Position deltaP) {
		this.x += deltaP.x;
		this.y += deltaP.y;
	}

	/**
	 * Calculates the distance between two positions, as sqrt(dx^2+dy^2)
	 * If this is 0, it return a value slightly greater to avoid collisions
	 */
	public static double getDistance(final Position pi, final Position pj) {
		if (pi == null || pj == null) {
			return Position.NULL_DISTANCE;
		}

		return Math.max(Math.sqrt(Math.pow(pj.x - pi.x, 2) + Math.pow(pj.y - pi.y, 2)), Position.NULL_DISTANCE);
	}

	/**
	 * Calculates the distance between two positions, as Position(dx, dy)
	 */
	public static Position getDifference(final Position pi, final Position pj) {
		if (pi == null || pj == null) {
			return Position.NULL_POSITION;
		}

		return new Position(pj.x - pi.x, pj.y - pi.y);
	}

	/**
	 * Creates a new Vector from the position x and y
	 */
	public Vector toVector() {
		return new Vector(this.x, this.y);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(this.x);
		result = prime * result + (int) (temp ^ temp >>> 32);
		temp = Double.doubleToLongBits(this.y);
		result = prime * result + (int) (temp ^ temp >>> 32);
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		final Position other = (Position) obj;
		if (Double.doubleToLongBits(this.x) != Double.doubleToLongBits(other.x)) {
			return false;
		}
		if (Double.doubleToLongBits(this.y) != Double.doubleToLongBits(other.y)) {
			return false;
		}
		return true;
	}

	public double getX() {
		return this.x;
	}

	public void setX(final double x) {
		this.x = x;
	}

	public double getY() {
		return this.y;
	}

	public void setY(final double y) {
		this.y = y;
	}

	@Override
	public String toString() {
		return "Position [x=" + this.x + ", y=" + this.y + "]";
	}

}
