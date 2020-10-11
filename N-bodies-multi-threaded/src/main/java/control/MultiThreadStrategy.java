package control;

import java.util.Arrays;
import java.util.List;
import java.util.PrimitiveIterator.OfDouble;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import model.Body;
import model.Force;
import model.Position;
import model.TriangularMatrix;
import model.Vector;

/**
 * Strategy that uses java Threads to achieve the goal
 *
 */
public class MultiThreadStrategy implements Strategy {

	private final int nBodies;
	private final int deltaTime;

	private final Body[] bodies;
	private final Body[] backupBodies;
	private final TriangularMatrix matrixBF;
	private final int nThreads;
	private final int bodiesPerThread;

	final Worker[] workers;
	private final BlockingQueue<Integer> forcesToCalculate;
	private final Boolean[] bodyCalculatedForces;
	private final BlockingDeque<Integer> bodiesToMove;
	private volatile int bodiesMoved;

	public MultiThreadStrategy(final int nBodies, final int deltaTime) {
		super();
		this.nBodies = nBodies;
		this.deltaTime = deltaTime;
		this.bodies = new Body[nBodies];
		this.backupBodies = new Body[nBodies];
		this.matrixBF = new TriangularMatrix(nBodies);
		this.nThreads = Runtime.getRuntime().availableProcessors() + 1;
		this.bodiesPerThread = this.nBodies / this.nThreads;

		this.workers = new Worker[this.nThreads];
		this.forcesToCalculate = new ArrayBlockingQueue<>(nBodies);
		this.bodyCalculatedForces = new Boolean[nBodies];
		this.bodiesToMove = new LinkedBlockingDeque<>(nBodies);
	}

	/**
	 * AvailableProcessors() + 1 Threads are used to create the bodies, dividing the work evenly.
	 */
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
	}

	/**
	 * A BlockingQueue of "Forces to Calculate" is filled with nBodies Integers.
	 * Each one stands for a job of calculation of the Force between the i-th Body and all the subsequent ones.
	 * AvailableProcessors() + 1 Threads (Workers) are used to complete these jobs.
	 *
	 * When a job is completed, the Worker checks if all the required Forces to move the body have been calculated.
	 * If so, it puts an Integer into a BlockingDeque "Bodies to Move".
	 * Each element in this queue stands for a job of calculation of the total Force the i-th Body is subjected to and
	 * its application.
	 *
	 * When the "Forces to Calculate" queue is empty, the Worker retrieves elements from the "Bodies to Move" queue.
	 * After moving a body, the Thread updates the bodies moved count.
	 * When all of the nBodies have been moved, all the Workers are requested to stop.
	 *
	 * If the main Thread is requested to stop, it tries to revert any Forces already applied.
	 */
	@Override
	public void calculateAndMove() {
		try {
			// Initialization
			for (int i = 0; i < this.nBodies; i++) {
				this.forcesToCalculate.offer(i);
				this.bodyCalculatedForces[i] = false;
				this.backupBodies[i] = null;
			}
			this.bodiesMoved = 0;

			for (int i = 0; i < this.nThreads; i++) {
				this.workers[i] = new Worker(i);
				this.workers[i].start();
			}

			for (final Worker worker : this.workers) {
				worker.join();
			}
		} catch (@SuppressWarnings("unused") final InterruptedException e) {
			this.revertAppliedForces();
		}
	}

	@Override
	public List<Body> getBodies() {
		return Arrays.asList(this.bodies);
	}

	private synchronized void forcesCalculated(final int i) {
		this.bodyCalculatedForces[i] = true;
		if (Arrays.stream(this.bodyCalculatedForces).parallel().limit(i).filter(b -> b == true).count() == i) {
			this.bodiesToMove.offer(i);

			int next = i + 1;
			while (next < this.nBodies && this.bodyCalculatedForces[next]) {
				this.bodiesToMove.offer(next);
				next++;
			}
		}
	}

	private synchronized void bodyMoved(final int i) {
		this.bodiesMoved++;
		if (this.bodiesMoved == this.nBodies) {
			for (final Worker worker : this.workers) {
				worker.stopped = true;
				worker.interrupt();
			}
		}
	}

	private void revertAppliedForces() {
		if (this.bodiesMoved == 0) {
			return;
		}
		for (int i = 0; i < this.nBodies; i++) {
			if (this.backupBodies[i] != null) {
				this.bodies[i] = this.backupBodies[i];
			}
		}
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

	private class Worker extends Thread {
		private volatile boolean stopped;
		private final int name;

		public Worker(final int name) {
			this.name = name;
			this.stopped = false;
		}

		@Override
		public void run() {
			// Calculate forces
			while (!MultiThreadStrategy.this.forcesToCalculate.isEmpty()) {
				final Integer i = MultiThreadStrategy.this.forcesToCalculate.poll();
				if (i == null) {
					continue;
				}

				for (int j = i + 1; j < MultiThreadStrategy.this.nBodies; j++) {
					MultiThreadStrategy.this.matrixBF.set(i, j,
							Force.get(MultiThreadStrategy.this.bodies[i], MultiThreadStrategy.this.bodies[j]));
				}
				MultiThreadStrategy.this.forcesCalculated(i);
			}

			// Move bodies
			while (!this.stopped) {
				try {
					final Integer i = this.name % 2 == 0 ? MultiThreadStrategy.this.bodiesToMove.takeFirst()
							: MultiThreadStrategy.this.bodiesToMove.takeLast();

					final Force[] forcesToSum = new Force[MultiThreadStrategy.this.nBodies - 1];
					int f = 0;
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

					MultiThreadStrategy.this.backupBodies[i] = MultiThreadStrategy.this.bodies[i];
					MultiThreadStrategy.this.bodies[i].apply(Force.sumForces(forcesToSum),
							MultiThreadStrategy.this.deltaTime);
					MultiThreadStrategy.this.bodyMoved(i);
				} catch (@SuppressWarnings("unused") final InterruptedException e) {
					continue;
				}
			}
		}
	}
}
