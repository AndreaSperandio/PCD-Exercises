package control;

import java.util.Arrays;
import java.util.List;
import java.util.PrimitiveIterator.OfDouble;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import model.Body;
import model.Force;
import model.Position;
import model.TriangularMatrix;
import model.Vector;

/**
 * Strategy that uses java Executors to achieve the goal
 *
 */
public class TaskStrategy implements Strategy {

	private final int nBodies;
	private final int deltaTime;

	private final Body[] bodies;
	private final Body[] backupBodies;
	private final TriangularMatrix matrixBF;
	private final int nThreads;
	private ExecutorService executor;

	private final Boolean[] bodyCalculatedForces;
	private volatile int bodiesMoved;

	public TaskStrategy(final int nBodies, final int deltaTime) {
		super();

		this.nBodies = nBodies;
		this.deltaTime = deltaTime;
		this.bodies = new Body[nBodies];
		this.backupBodies = new Body[nBodies];
		this.matrixBF = new TriangularMatrix(nBodies);
		this.nThreads = Runtime.getRuntime().availableProcessors() + 1;

		this.bodyCalculatedForces = new Boolean[nBodies];
	}

	/**
	 * An Executor using AvailableProcessors() + 1 Threads is used to create the bodies.
	 */
	@Override
	public void createBodies(final double minMass, final double maxMass, final double maxPosX, final double maxPosY,
			final double minSpeed, final double maxSpeed) {
		try {
			this.executor = Executors.newFixedThreadPool(this.nThreads);
			this.executor.execute(() -> {
				final Random random = new Random();
				final OfDouble massGenerator = random.doubles(minMass, maxMass).iterator();
				final OfDouble positionXGenerator = random.doubles(0.0, maxPosX).iterator();
				final OfDouble positionYGenerator = random.doubles(0.0, maxPosY).iterator();
				final OfDouble speedGenerator = random.doubles(minSpeed, maxSpeed).iterator();

				for (int i = 0; i < this.nBodies; i++) {
					TaskStrategy.this.bodies[i] = new Body(massGenerator.nextDouble(),
							new Position(positionXGenerator.nextDouble(), positionYGenerator.nextDouble()),
							new Vector(speedGenerator.nextDouble(), speedGenerator.nextDouble()));
				}
			});

			this.executor.shutdown();
			this.executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * An Executor using AvailableProcessors() + 1 Threads is used to calculate the Forces between the Bodies,
	 * to get the total Force each Body is subjected to and to move the Bodies.
	 *
	 * If the main Thread is requested to stop, it tries to revert any Forces already applied.
	 */
	@Override
	public void calculateAndMove() {
		try {
			// Initialization
			for (int i = 0; i < this.nBodies; i++) {
				this.bodyCalculatedForces[i] = false;
				this.backupBodies[i] = null;
			}
			this.bodiesMoved = 0;

			this.executor = Executors.newFixedThreadPool(this.nThreads);
			this.executor.execute(() -> {
				// Calculate forces
				for (int i = 0; i < this.nBodies; i++) {
					for (int j = i + 1; j < TaskStrategy.this.nBodies; j++) {
						TaskStrategy.this.matrixBF.set(i, j,
								Force.get(TaskStrategy.this.bodies[i], TaskStrategy.this.bodies[j]));
					}
				}

				// Move bodies
				for (int i = 0; i < this.nBodies; i++) {
					final Force[] forcesToSum = new Force[TaskStrategy.this.nBodies - 1];
					int f = 0;
					// Horizontal
					for (int j = i + 1; j < TaskStrategy.this.nBodies; j++) {
						forcesToSum[f] = TaskStrategy.this.matrixBF.get(i, j);
						f++;
					}
					// Vertical
					for (int k = 0; k < i; k++) {
						forcesToSum[f] = Force.revertOrientation(TaskStrategy.this.matrixBF.get(k, i));
						f++;
					}

					TaskStrategy.this.backupBodies[i] = TaskStrategy.this.bodies[i];
					TaskStrategy.this.bodies[i].apply(Force.sumForces(forcesToSum), TaskStrategy.this.deltaTime);
				}
			});

			this.executor.shutdown();
			this.executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (@SuppressWarnings("unused") final InterruptedException e) {
			this.interrupt();
		}
	}

	@Override
	public synchronized void interrupt() {
		this.executor.shutdownNow();
		this.revertAppliedForces();
	}

	@Override
	public List<Body> getBodies() {
		return Arrays.asList(this.bodies);
	}

	private synchronized void revertAppliedForces() {
		if (this.bodiesMoved == 0) {
			return;
		}
		for (int i = 0; i < this.nBodies; i++) {
			if (this.backupBodies[i] != null) {
				this.bodies[i] = this.backupBodies[i];
			}
		}
	}
}
