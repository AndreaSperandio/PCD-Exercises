package model;

public class Vector {
	private double xComp;  // x component
	private double yComp;  // y component

	public Vector(final double xComp, final double yComp) {
		super();
		this.xComp = xComp;
		this.yComp = yComp;
	}

	public double getModule() {
		return Vector.getModule(this.xComp, this.yComp);
	}

	protected static double getModule(final double vectX, final double vectY) {
		return Math.sqrt(Math.pow(vectX, 2) + Math.pow(vectY, 2));
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
