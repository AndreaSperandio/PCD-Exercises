package control;

import java.util.List;

import model.Body;
import view.SimulationView;

/**
 * Abstract class used to define different strategies to
 * - create bodies
 * - calculate the forces between the bodies
 * - move the bodies
 */
public abstract class Strategy extends Thread {
	private SimulationView simulationView;
	private int refreshRate;

	private volatile boolean working;
	private volatile boolean stopped;

	protected Strategy() {
		this.working = true;
		this.stopped = false;
	}

	@Override
	public void run() {
		final DurationTracker dtCB = new DurationTracker("Create bodies").start();
		this.createBodies(this.simulationView.getBodyMinMass(), this.simulationView.getBodyMaxMass(),
				this.simulationView.getPositionMaxX(), this.simulationView.getPositionMaxY(),
				this.simulationView.getBodyMinSpeed(), this.simulationView.getBodyMaxSpeed());
		final long durationCreateBodies = DurationTracker.toMillsDuration(dtCB.stop(this.simulationView.isDebug()));
		if (this.simulationView.isDebug()) {
			System.out.println();
		}

		while (!this.stopped) {
			try {
				if (!this.working) {
					synchronized (this) {
						this.wait();
						continue;
					}
				}

				final DurationTracker dtCM = new DurationTracker("Calculate & Move").start();
				this.calculateAndMove();
				final long durationCalcAndMove = DurationTracker
						.toMillsDuration(dtCM.stop(this.simulationView.isDebug()));
				final long durationTotal = this.simulationView.notifyBodiesMoved(durationCreateBodies,
						durationCalcAndMove);

				if (durationTotal < this.refreshRate) {
					Thread.sleep(this.refreshRate - durationTotal);
				}
			} catch (@SuppressWarnings("unused") final InterruptedException e) {
				continue;
			}
		}
	}

	// Public for test purposes
	public abstract void createBodies(final double minMass, final double maxMass, final double maxPosX,
			final double maxPosY, final double minSpeed, final double maxSpeed);

	// Public for test purposes
	public abstract void calculateAndMove();

	public abstract List<Body> getBodies();

	public void setWorking(final boolean working) {
		this.working = working;
	}

	public void terminate() {
		this.stopped = true;
	}

	public boolean isWorking() {
		return this.working;
	}

	public void setViewListener(final SimulationView simulationView, final int refreshRate) {
		this.simulationView = simulationView;
		this.refreshRate = refreshRate;
	}
}
