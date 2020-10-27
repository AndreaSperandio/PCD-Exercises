package control.actors;

import java.util.concurrent.CompletableFuture;

import model.Body;
import model.TriangularMatrix;

/**
 * Messages that are recognised by the MainActor Class.
 *
 * @author Andrea Sperandio
 *
 */
public interface MainActorMsg {
	/**
	 * Message used to express the bounds needed to Create nBodies Bodies.
	 *
	 */
	public static class CreateBodies {
		private final CompletableFuture<Body[]> creationFuture;
		private final double minMass;
		private final double maxMass;
		private final double maxPosX;
		private final double maxPosY;
		private final double minSpeed;
		private final double maxSpeed;

		public CreateBodies(final CompletableFuture<Body[]> creationFuture, final double minMass, final double maxMass,
				final double maxPosX, final double maxPosY, final double minSpeed, final double maxSpeed) {
			super();
			this.creationFuture = creationFuture;
			this.minMass = minMass;
			this.maxMass = maxMass;
			this.maxPosX = maxPosX;
			this.maxPosY = maxPosY;
			this.minSpeed = minSpeed;
			this.maxSpeed = maxSpeed;
		}

		public CompletableFuture<Body[]> getCreationFuture() {
			return this.creationFuture;
		}

		public double getMinMass() {
			return this.minMass;
		}

		public double getMaxMass() {
			return this.maxMass;
		}

		public double getMaxPosX() {
			return this.maxPosX;
		}

		public double getMaxPosY() {
			return this.maxPosY;
		}

		public double getMinSpeed() {
			return this.minSpeed;
		}

		public double getMaxSpeed() {
			return this.maxSpeed;
		}
	}

	/**
	 * Message used to receive the Created Bodies.
	 *
	 */
	public static class BodiesCreated {
		private final CompletableFuture<Body[]> creationFuture;
		private final Body[] bodies;
		private final int from;

		public BodiesCreated(final CompletableFuture<Body[]> creationFuture, final Body[] bodies, final int from) {
			this.creationFuture = creationFuture;
			this.bodies = bodies;
			this.from = from;
		}

		public int getFrom() {
			return this.from;
		}

		public Body[] getBodies() {
			return this.bodies;
		}

		public CompletableFuture<Body[]> getCreationFuture() {
			return this.creationFuture;
		}
	}

	/**
	 * Message used to Calculate the Forces and Move the Bodies.
	 *
	 */
	public static class MoveBodies {
		private final CompletableFuture<Body[]> moveFuture;

		public MoveBodies(final CompletableFuture<Body[]> future) {
			this.moveFuture = future;
		}

		public CompletableFuture<Body[]> getMoveFuture() {
			return this.moveFuture;
		}
	}

	/**
	 * Message used to receive the Calculated Forces.
	 *
	 */
	public static class ForcesCalculated {
		private final MoveBodies moveBodies;
		private final TriangularMatrix matrixBF;
		private final int from;
		private final int nBodies;

		public ForcesCalculated(final MoveBodies moveBodies, final TriangularMatrix matrixBF, final int from,
				final int nBodies) {
			this.moveBodies = moveBodies;
			this.matrixBF = matrixBF;
			this.from = from;
			this.nBodies = nBodies;
		}

		public TriangularMatrix getMatrixBF() {
			return this.matrixBF;
		}

		public int getFrom() {
			return this.from;
		}

		public int getnBodies() {
			return this.nBodies;
		}

		public MoveBodies getMoveBodies() {
			return this.moveBodies;
		}
	}

	/**
	 * Message used to receive the Moved Bodies.
	 *
	 */
	public static class BodiesMoved {
		private final CompletableFuture<Body[]> moveFuture;
		private final Body[] bodies;
		private final int from;

		public BodiesMoved(final CompletableFuture<Body[]> moveFuture, final Body[] bodies, final int from) {
			this.moveFuture = moveFuture;
			this.bodies = bodies;
			this.from = from;
		}

		public int getFrom() {
			return this.from;
		}

		public Body[] getBodies() {
			return this.bodies;
		}

		public CompletableFuture<Body[]> getMoveFuture() {
			return this.moveFuture;
		}
	}

	/**
	 * Message used to receive the Bodies to set, used for backup purposes.
	 *
	 */
	public static class SetBodies {
		private final Body[] bodies;

		public SetBodies(final Body[] bodies) {
			this.bodies = bodies;
		}

		public Body[] getBodies() {
			return this.bodies;
		}
	}
}
