package control;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.PrimitiveIterator.OfDouble;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import model.Body;
import model.Force;
import model.Position;
import model.Vector;

/**
 * Strategy that uses java (parallel) streams to achieve the goal
 * This Strategy doesn't attempt to backup and recover the bodies position if the process is paused.
 *
 */
public class StreamStrategy implements Strategy {

	private final int nBodies;
	private final int deltaTime;

	private List<Body> bodies;
	private final Map<Body, Force> mapBF;

	public StreamStrategy(final int nBodies, final int deltaTime) {
		super();
		this.nBodies = nBodies;
		this.deltaTime = deltaTime;
		this.bodies = new ArrayList<>();
		this.mapBF = new ConcurrentHashMap<>(nBodies);
	}

	@Override
	public void createBodies(final double minMass, final double maxMass, final double maxPosX, final double maxPosY,
			final double minSpeed, final double maxSpeed) {

		final Random random = new Random();
		final OfDouble massGenerator = random.doubles(minMass, maxMass).iterator();
		final OfDouble positionXGenerator = random.doubles(0.0, maxPosX).iterator();
		final OfDouble positionYGenerator = random.doubles(0.0, maxPosY).iterator();
		final OfDouble speedGenerator = random.doubles(minSpeed, maxSpeed).iterator();

		for (int i = 0; i < this.nBodies; i++) {
			this.bodies.add(new Body(massGenerator.nextDouble(),
					new Position(positionXGenerator.nextDouble(), positionYGenerator.nextDouble()),
					new Vector(speedGenerator.nextDouble(), speedGenerator.nextDouble())));
		}
	}

	/**
	 * Calculates all existing Forces between any couple of Bodies and stores into a map the total Force each body is
	 * subjected to.
	 *
	 * For each Body, then, it applies the total Force for a deltaTime time.
	 */
	@Override
	public void calculateAndMove() {
		this.mapBF.clear();
		for (final Body b : this.bodies) {
			this.mapBF.put(b, Force.sumForces(this.bodies.stream().parallel().filter(b1 -> !b1.equals(b))
					.map(b1 -> Force.get(b, b1)).toArray(Force[]::new)));
		}

		this.mapBF.keySet().stream().parallel().forEach(b -> {
			b.apply(this.mapBF.get(b), this.deltaTime);
		});
	}

	@Override
	public void interrupt() {
		// Do nothing
	}

	@Override
	public synchronized void clear() {
		// Do nothing
	}

	@Override
	public List<Body> getBodies() {
		return this.bodies;
	}

	/**
	 * Used for test purposes
	 */
	public void setBodies(final List<Body> bodies) {
		this.bodies = bodies.stream()
				.map(b -> new Body(b.getMass(), new Position(b.getPosition().getX(), b.getPosition().getY()),
						new Vector(b.getSpeed().getXComp(), b.getSpeed().getYComp())))
				.collect(Collectors.toList());
	}
}
