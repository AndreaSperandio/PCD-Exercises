package control.actors;

import akka.actor.ActorRef;
import model.Body;
import model.TriangularMatrix;

/**
 * Messages that are recognised by the Actor Class.
 *
 */
public interface ActorMsg {
	/**
	 * Message used to Create nBodies Bodies.
	 *
	 */
	public static class CreateBodies {
		private final ActorRef replyTo;
		private final MainActorMsg.CreateBodies createBodies;
		private final int from;
		private final int nBodies;

		public CreateBodies(final ActorRef actorRef, final MainActorMsg.CreateBodies createBodies, final int from,
				final int nBodies) {
			this.replyTo = actorRef;
			this.createBodies = createBodies;
			this.from = from;
			this.nBodies = nBodies;
		}

		public MainActorMsg.CreateBodies getCreateBodies() {
			return this.createBodies;
		}

		public int getnBodies() {
			return this.nBodies;
		}

		public int getFrom() {
			return this.from;
		}

		public ActorRef getReplyTo() {
			return this.replyTo;
		}
	}

	/**
	 * Message used to Calculate the Forces between nBodies Bodies.
	 *
	 */
	public static class CalculateForces {
		private final ActorRef replyTo;
		private final MainActorMsg.MoveBodies moveBodies;
		private final Body[] bodies;
		private final int from;
		private final int nBodies;

		public CalculateForces(final ActorRef actorRef, final MainActorMsg.MoveBodies moveBodies, final Body[] bodies,
				final int from, final int nBodies) {
			this.replyTo = actorRef;
			this.moveBodies = moveBodies;
			this.bodies = bodies;
			this.from = from;
			this.nBodies = nBodies;
		}

		public Body[] getBodies() {
			return this.bodies;
		}

		public int getFrom() {
			return this.from;
		}

		public int getnBodies() {
			return this.nBodies;
		}

		public MainActorMsg.MoveBodies getMoveBodies() {
			return this.moveBodies;
		}

		public ActorRef getReplyTo() {
			return this.replyTo;
		}
	}

	/**
	 * Message used to Move nBodies Bodies.
	 *
	 */
	public static class MoveBodies {
		private final ActorRef replyTo;
		private final MainActorMsg.MoveBodies moveBodies;
		private final TriangularMatrix matrixBF;
		private final int from;
		private final int nBodies;
		private final int deltaTime;

		public MoveBodies(final ActorRef actorRef, final MainActorMsg.MoveBodies moveBodies,
				final TriangularMatrix matrixBF, final int from, final int nBodies, final int deltaTime) {
			this.replyTo = actorRef;
			this.moveBodies = moveBodies;
			this.matrixBF = matrixBF;
			this.from = from;
			this.nBodies = nBodies;
			this.deltaTime = deltaTime;
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

		public int getDeltaTime() {
			return this.deltaTime;
		}

		public MainActorMsg.MoveBodies getMoveBodies() {
			return this.moveBodies;
		}

		public ActorRef getReplyTo() {
			return this.replyTo;
		}
	}
}
