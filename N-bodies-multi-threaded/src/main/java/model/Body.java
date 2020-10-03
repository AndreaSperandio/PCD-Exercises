package model;

public class Body {

	private final double mass;  // kg
	private final Position position;  // m
	private final Vector speed;  // m/s 

	public Body(final double mass, final Position position, final Vector speed) {
		super();
		this.mass = mass;
		this.position = position;
		this.speed = speed;
	}

	public Body apply(final Force force, final double deltaTime) {
		final double accX = force.getXComp() / this.mass;
		final double accY = force.getYComp() / this.mass;
		final double deltaSpeedX = accX * deltaTime;
		final double deltaSpeedY = accY * deltaTime;
		this.position.move(new Position((deltaSpeedX / 2 + this.speed.getXComp()) * deltaTime,
				(deltaSpeedY / 2 + this.speed.getYComp()) * deltaTime));
		this.speed.setXComp(this.speed.getXComp() + deltaSpeedX);
		this.speed.setYComp(this.speed.getYComp() + deltaSpeedY);
		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(this.mass);
		result = prime * result + (int) (temp ^ temp >>> 32);
		result = prime * result + (this.position == null ? 0 : this.position.hashCode());
		result = prime * result + (this.speed == null ? 0 : this.speed.hashCode());
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
		final Body other = (Body) obj;
		if (Double.doubleToLongBits(this.mass) != Double.doubleToLongBits(other.mass)) {
			return false;
		}
		if (this.position == null) {
			if (other.position != null) {
				return false;
			}
		} else if (!this.position.equals(other.position)) {
			return false;
		}
		if (this.speed == null) {
			if (other.speed != null) {
				return false;
			}
		} else if (!this.speed.equals(other.speed)) {
			return false;
		}
		return true;
	}

	public double getMass() {
		return this.mass;
	}

	public Position getPosition() {
		return this.position;
	}

	public Vector getSpeed() {
		return this.speed;
	}

	@Override
	public String toString() {
		return "Body [mass=" + this.mass + ", position=" + this.position + ", speed=" + this.speed + "]";
	}
}
