package model;

/**
 * Represents a two-dimensional Vector in the System, characterised by an x-component and an y-component
 *
 * @author Andrea Sperandio
 *
 */
public class Vector {
	private double xComp;  // x component
	private double yComp;  // y component

	public Vector(final double xComp, final double yComp) {
		super();
		this.xComp = xComp;
		this.yComp = yComp;
	}

	/**
	 * Modifies this Vector, reverting its orientation
	 */
	public Vector revertOrientation() {
		this.xComp *= -1.0;
		this.yComp *= -1.0;
		return this;
	}

	/**
	 * Creates a new Vector, reverting a vector orientation
	 */
	public static Vector revertOrientation(final Vector vector) {
		return new Vector(vector.xComp * -1.0, vector.yComp * -1.0);
	}

	/**
	 * Return this Vector's module, as the sqrt(xComp^2 + yComp^2)
	 */
	public double getModule() {
		return Math.sqrt(Math.pow(this.xComp, 2) + Math.pow(this.yComp, 2));
	}

	/**
	 * Modifies this Vector, performing a vector sum
	 */
	public Vector sum(final Vector vector) {
		this.xComp += vector.xComp;
		this.yComp += vector.yComp;
		return this;
	}

	/**
	 * Creates a new Vector, performing a vector sum
	 */
	public static Vector sum(final Vector vectori, final Vector vectorj) {
		return new Vector(vectori.xComp + vectorj.xComp, vectori.yComp + vectorj.yComp);
	}

	/**
	 * Modifies this Vector, performing a vector product with scalar
	 */
	public Vector multiplyScalar(final double scalar) {
		this.xComp *= scalar;
		this.yComp *= scalar;
		return this;
	}

	/**
	 * Creates a new Vector, performing a vector product with scalar
	 */
	public static Vector multiplyScalar(final Vector vector, final double scalar) {
		return new Vector(vector.xComp * scalar, vector.yComp * scalar);
	}

	/**
	 * Creates a new Position from the vector x and y components
	 */
	public Position toPosition() {
		return new Position(this.xComp, this.yComp);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(this.getXComp());
		result = prime * result + (int) (temp ^ temp >>> 32);
		temp = Double.doubleToLongBits(this.yComp);
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
		final Vector other = (Vector) obj;
		if (Double.doubleToLongBits(this.getXComp()) != Double.doubleToLongBits(other.getXComp())) {
			return false;
		}
		if (Double.doubleToLongBits(this.yComp) != Double.doubleToLongBits(other.yComp)) {
			return false;
		}
		return true;
	}

	public double getXComp() {
		return this.xComp;
	}

	public void setXComp(final double xComp) {
		this.xComp = xComp;
	}

	public double getYComp() {
		return this.yComp;
	}

	public void setYComp(final double yComp) {
		this.yComp = yComp;
	}

	@Override
	public String toString() {
		return "Vector [x=" + this.xComp + ", y=" + this.yComp + "]";
	}

}
