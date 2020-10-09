package control;

import java.util.Arrays;
import java.util.List;
import java.util.PrimitiveIterator.OfDouble;
import java.util.Random;

import model.Body;
import model.Force;
import model.Position;
import model.TriangularMatrix;
import model.Vector;

/**
 * Strategy that uses java (parallel) streams to achieve the goal
 *
 */
public class MultiThreadStrategy implements Strategy {

	private final int nBodies;
	private final int deltaTime;

	private final Body[] bodies;
	private final TriangularMatrix matrixBF;
	private final int nThreads;
	final int bodiesPerThread;

	public MultiThreadStrategy(final int nBodies, final int deltaTime) {
		super();
		this.nBodies = nBodies;
		this.deltaTime = deltaTime;
		this.bodies = new Body[nBodies];
		this.matrixBF = new TriangularMatrix(nBodies);
		this.nThreads = Runtime.getRuntime().availableProcessors() + 1;
		this.bodiesPerThread = this.nBodies / this.nThreads;
	}

	@Override
	public void createBodies(final double minMass, final double maxMass, final double maxPosX, final double maxPosY,
			final double minSpeed, final double maxSpeed) {
		try {
			final BodyCreator[] bodyCreators = new BodyCreator[this.nThreads];

			int from = 0;
			for (int i = 0; i < this.nThreads - 1; i++) {
				bodyCreators[i] = new BodyCreator(from, this.bodiesPerThread, minMass, maxMass, maxPosX, maxPosY,
						minSpeed, maxSpeed);
				bodyCreators[i].start();
				from += this.bodiesPerThread;
			}
			bodyCreators[bodyCreators.length - 1] = new BodyCreator(from, this.nBodies - from, minMass, maxMass,
					maxPosX, maxPosY, minSpeed, maxSpeed);
			bodyCreators[bodyCreators.length - 1].start();

			for (final BodyCreator bodyCreator : bodyCreators) {
				bodyCreator.join();
			}
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}

		/*this.bodies[0] = new Body(676538.992294392, new Position(49.999161343847234, 202.3487007018702),
				new Vector(-8.002058364555631E-7, 5.030866834400992E-7));
		this.bodies[1] = new Body(715151.6119889957, new Position(62.06471254509351, 228.9417147714563),
				new Vector(-9.65404969347027E-7, -8.275612471106132E-7));
		this.bodies[2] = new Body(806024.9552248112, new Position(184.21621494856348, 123.7150656476162),
				new Vector(1.7412983111365325E-7, -3.742288872054757E-7));
		this.bodies[3] = new Body(715288.5086594607, new Position(34.929262683847014, 75.14271748874748),
				new Vector(9.377608373012914E-7, -5.943472242523683E-7));
		this.bodies[4] = new Body(727428.5256414945, new Position(266.76016140416, 237.5366423324724),
				new Vector(-9.648598015725882E-7, -4.698432477988046E-7));*/

	}

	@Override
	public void calculateForces() {
		try {
			final ForceCalculator[] forceCalculators = new ForceCalculator[this.nThreads];

			int from = 0;
			for (int i = 0; i < this.nThreads - 1; i++) {
				forceCalculators[i] = new ForceCalculator(from, this.bodiesPerThread);
				forceCalculators[i].start();
				from += this.bodiesPerThread;
			}
			forceCalculators[forceCalculators.length - 1] = new ForceCalculator(from, this.nBodies - from);
			forceCalculators[forceCalculators.length - 1].start();

			for (final ForceCalculator forceCalculator : forceCalculators) {
				forceCalculator.join();
			}

			//MultiThreadStrategy.this.matrixBF.print();
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
		/*this.mapBF.clear();
		for (final Body b : this.bodies) {
			this.mapBF.put(b, Force.sumForces(this.bodies.stream().parallel().filter(b1 -> !b1.equals(b))
					.map(b1 -> Force.get(b, b1)).toArray(Force[]::new)));
		}*/

	}

	@Override
	public void moveBodies() {
		try {
			final BodyMover[] bodyMovers = new BodyMover[this.nThreads];

			int from = 0;
			for (int i = 0; i < this.nThreads - 1; i++) {
				bodyMovers[i] = new BodyMover(from, this.bodiesPerThread);
				bodyMovers[i].start();
				from += this.bodiesPerThread;
			}
			bodyMovers[bodyMovers.length - 1] = new BodyMover(from, this.nBodies - from);
			bodyMovers[bodyMovers.length - 1].start();

			for (final BodyMover bodyMover : bodyMovers) {
				bodyMover.join();
			}
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
		/*this.mapBF.keySet().stream().forEach(b -> {
			b.apply(this.mapBF.get(b), this.deltaTime);
		});*/
	}

	@Override
	public List<Body> getBodies() {
		return Arrays.asList(this.bodies);
	}

	private class BodyCreator extends Thread {
		private final int from;
		private final int n;
		private final OfDouble massGenerator;
		private final OfDouble positionXGenerator;
		private final OfDouble positionYGenerator;
		private final OfDouble speedGenerator;

		public BodyCreator(final int from, final int n, final double minMass, final double maxMass,
				final double maxPosX, final double maxPosY, final double minSpeed, final double maxSpeed) {
			this.from = from;
			this.n = n;

			final Random random = new Random();
			this.massGenerator = random.doubles(minMass, maxMass).iterator();
			this.positionXGenerator = random.doubles(0.0, maxPosX).iterator();
			this.positionYGenerator = random.doubles(0.0, maxPosY).iterator();
			this.speedGenerator = random.doubles(minSpeed, maxSpeed).iterator();
		}

		@Override
		public void run() {
			final int to = this.from + this.n;
			for (int i = this.from; i < to; i++) {
				MultiThreadStrategy.this.bodies[i] = new Body(this.massGenerator.nextDouble(),
						new Position(this.positionXGenerator.nextDouble(), this.positionYGenerator.nextDouble()),
						new Vector(this.speedGenerator.nextDouble(), this.speedGenerator.nextDouble()));
			}
		}
	}

	private class ForceCalculator extends Thread {
		private final int from;
		private final int n;

		public ForceCalculator(final int from, final int n) {
			this.from = from;
			this.n = n;
		}

		@Override
		public void run() {
			final int to = this.from + this.n;
			for (int i = this.from; i < to; i++) {
				for (int j = i + 1; j < MultiThreadStrategy.this.nBodies; j++) {
					MultiThreadStrategy.this.matrixBF.set(i, j,
							Force.get(MultiThreadStrategy.this.bodies[i], MultiThreadStrategy.this.bodies[j]));
				}
			}
			/*this.mapBF.put(b, Force.sumForces(this.bodies.stream().parallel().filter(b1 -> !b1.equals(b))
					.map(b1 -> Force.get(b, b1)).toArray(Force[]::new)));*/
		}
	}

	private class BodyMover extends Thread {
		private final int from;
		private final int n;

		public BodyMover(final int from, final int n) {
			this.from = from;
			this.n = n;
		}

		@Override
		public void run() {
			final int to = this.from + this.n;

			final Force[] forcesToSum = new Force[MultiThreadStrategy.this.nBodies - 1];
			int f = 0;
			for (int i = this.from; i < to; i++) {
				f = 0;
				// Horizontal
				for (int j = i + 1; j < MultiThreadStrategy.this.nBodies; j++) {
					forcesToSum[f] = MultiThreadStrategy.this.matrixBF.get(i, j);
					f++;
				}
				// Vertical
				for (int k = 0; k < i; k++) {
					forcesToSum[f] = Force.revertOrientation(MultiThreadStrategy.this.matrixBF.get(k, i));
					f++;
				}

				if (f != MultiThreadStrategy.this.nBodies - 1) {
					System.err.println("NOOOOO");
				}
				/*if (i == 2) {
					MultiThreadStrategy.this.matrixBF.print();

					for (int j = 0; j < forcesToSum.length; j++) {
						System.out.println(forcesToSum[j]);
					}
					System.out.println("SUM1 = " + Force.sumForces(forcesToSum));
				}*/

				MultiThreadStrategy.this.bodies[i].apply(Force.sumForces(forcesToSum),
						MultiThreadStrategy.this.deltaTime);
			}

			/*this.mapBF.keySet().stream().forEach(b -> {
			b.apply(this.mapBF.get(b), this.deltaTime);
			});*/
		}
	}
}
