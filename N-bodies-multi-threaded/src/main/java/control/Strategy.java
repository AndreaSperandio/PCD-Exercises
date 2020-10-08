package control;

import java.util.List;

import model.Body;

public interface Strategy {

	void createBodies(final double minMass, final double maxMass, final double maxPosX, final double maxPosY,
			final double minSpeed, final double maxSpeed);

	void calculateForces();

	void moveBodies();

	List<Body> getBodies();
}
