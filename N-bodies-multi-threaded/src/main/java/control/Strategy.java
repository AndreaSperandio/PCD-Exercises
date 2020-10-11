package control;

import java.util.List;

import model.Body;

/**
 * Interface used to define different strategies to
 * - create bodies
 * - calculate the forces between the bodies
 * - move the bodies
 */
public interface Strategy {

	void createBodies(final double minMass, final double maxMass, final double maxPosX, final double maxPosY,
			final double minSpeed, final double maxSpeed);

	void calculateAndMove();

	void interrupt();

	List<Body> getBodies();

}
