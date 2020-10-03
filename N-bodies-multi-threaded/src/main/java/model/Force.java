package model;

import java.util.Arrays;

public class Force extends Vector {
	public static final Force NULL = new Force(0D, 0D);

	private static final double G = 6.67408 * Math.pow(10, -11);  // N*m^2/kg^2

	public Force(final double vectorX, final double vectorY) {
		super(vectorX, vectorY);
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

		final Position direction = Position.getDifference(biPosition, bjPosition);
		final double module = bi.getMass() * bj.getMass() * Force.G / Math.pow(distance, 2);
		final double lambda = module / Vector.getModule(direction.getX(), direction.getY());

		return new Force(direction.getX() * lambda, direction.getY() * lambda);
	}

	/**
	 * Calculates and returns the resulting sum Force.
	 */
	public static Force sum(final Force... forces) {
		if (forces == null || forces.length == 0) {
			return Force.NULL;
		}
		return Arrays.asList(forces).stream().reduce(new Force(0D, 0D),
				(fi, fj) -> new Force(fi.getXComp() + fj.getXComp(), fi.getYComp() + fj.getYComp()));
	}
}
