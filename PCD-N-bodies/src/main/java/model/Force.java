package model;

import java.util.Arrays;

/**
 * Represents a Force between two bodies
 *
 * @author Andrea Sperandio
 *
 */
public class Force extends Vector {
	public static final Force NULL = new Force(0D, 0D);

	private static final double G = 6.67408 * Math.pow(10, -11);  // N*m^2/kg^2

	public Force(final double vectorX, final double vectorY) {
		super(vectorX, vectorY);
	}

	public Force(final Vector vector) {
		super(vector.getXComp(), vector.getYComp());
	}

	/**
	 * Modifies this Force, reverting its orientation
	 */
	@Override
	public Force revertOrientation() {
		super.revertOrientation();
		return this;
	}

	/**
	 * Creates a new Force, reverting a Force orientation
	 */
	public static Force revertOrientation(final Force force) {
		return new Force(Vector.revertOrientation(force));
	}

	/**
	 * Calculates and returns the Force existing between two bodies according to the
	 * Newton's law of universal gravitation.
	 */
	public static Force get(final Body bi, final Body bj) {
		if (bi == null || bj == null) {
			return Force.NULL;
		}
		final Position biPosition = bi.getPosition();
		final Position bjPosition = bj.getPosition();
		final double distance = Position.getDistance(biPosition, bjPosition);
		if (distance == Position.NULL_DISTANCE) {
			return Force.NULL;
		}

		final Vector direction = Position.getDifference(biPosition, bjPosition).toVector();
		final double module = bi.getMass() * bj.getMass() * Force.G / Math.pow(distance, 2);

		/* Lamba is used to create a Force vector which module is exactly "module"
		 * It's the ratio factor to be applied to the direction vector to get the Force vector */
		final double lambda = module / direction.getModule();
		return new Force(direction.multiplyScalar(lambda));
	}

	/**
	 * Calculates and returns the resulting sum Force.
	 */
	public static Force sumForces(final Force... forces) {
		if (forces == null || forces.length == 0) {
			return Force.NULL;
		}
		return Arrays.asList(forces).stream().reduce(new Force(0.0D, 0.0D), (fi, fj) -> (Force) fi.sum(fj));
	}
}
