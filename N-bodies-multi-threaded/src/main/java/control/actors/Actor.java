package control.actors;

import java.util.PrimitiveIterator.OfDouble;
import java.util.Random;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import control.actors.MainActor.BodiesCreated;
import model.Body;
import model.Force;
import model.Position;
import model.TriangularMatrix;
import model.Vector;

public class Actor extends AbstractActor {

	public static class CreateBodies {
		private final ActorRef replyTo;
		private final control.actors.MainActor.CreateBodies createBodies;
		private final int from;
		private final int nBodies;

		public CreateBodies(final ActorRef actorRef, final control.actors.MainActor.CreateBodies createBodies,
				final int from, final int nBodies) {
			this.replyTo = actorRef;
			this.createBodies = createBodies;
			this.from = from;
			this.nBodies = nBodies;
		}
	}

	public static class CalculateForces {
		private final ActorRef replyTo;
		private final control.actors.MainActor.MoveBodies moveBodies;
		private final Body[] bodies;
		private final int from;
		private final int nBodies;

		public CalculateForces(final ActorRef actorRef, final control.actors.MainActor.MoveBodies moveBodies,
				final Body[] bodies, final int from, final int nBodies) {
			this.replyTo = actorRef;
			this.moveBodies = moveBodies;
			this.bodies = bodies;
			this.from = from;
			this.nBodies = nBodies;
		}
	}

	public static class MoveBodies {
		private final ActorRef replyTo;
		private final control.actors.MainActor.MoveBodies moveBodies;
		private final Body[] bodies;
		private final TriangularMatrix matrixBF;
		private final int from;
		private final int nBodies;
		private final int deltaTime;

		public MoveBodies(final ActorRef actorRef, final control.actors.MainActor.MoveBodies moveBodies,
				final Body[] bodies, final TriangularMatrix matrixBF, final int from, final int nBodies,
				final int deltaTime) {
			this.replyTo = actorRef;
			this.moveBodies = moveBodies;
			this.bodies = bodies;
			this.matrixBF = matrixBF;
			this.from = from;
			this.nBodies = nBodies;
			this.deltaTime = deltaTime;
		}
	}

	public Actor() {
		super();
	}

	private void onCreateBodies(final CreateBodies createBodies) {
		final control.actors.MainActor.CreateBodies values = createBodies.createBodies;
		final Random random = new Random();
		final OfDouble massGenerator = random.doubles(values.getMinMass(), values.getMaxMass()).iterator();
		final OfDouble positionXGenerator = random.doubles(0.0, values.getMaxPosX()).iterator();
		final OfDouble positionYGenerator = random.doubles(0.0, values.getMaxPosY()).iterator();
		final OfDouble speedGenerator = random.doubles(values.getMinSpeed(), values.getMaxSpeed()).iterator();

		final int nBodies = createBodies.nBodies;
		final Body[] bodies = new Body[nBodies];
		for (int i = 0; i < nBodies; i++) {
			bodies[i] = new Body(massGenerator.nextDouble(),
					new Position(positionXGenerator.nextDouble(), positionYGenerator.nextDouble()),
					new Vector(speedGenerator.nextDouble(), speedGenerator.nextDouble()));
		}

		createBodies.replyTo.tell(new BodiesCreated(values.getCreationFuture(), bodies, createBodies.from),
				this.getContext().getSelf());
	}

	private void onCalculateForces(final CalculateForces calculateForces) {
		final Body[] bodies = calculateForces.bodies;
		final int from = calculateForces.from;
		final int nBodies = calculateForces.nBodies;

		final TriangularMatrix matrixBF = new TriangularMatrix(bodies.length - from);

		for (int i = from; i < from + nBodies; i++) {
			for (int j = i + 1; j < bodies.length; j++) {
				matrixBF.set(i - from, j - from, Force.get(bodies[i], bodies[j]));
			}
		}

		calculateForces.replyTo.tell(
				new MainActor.ForcesCalculated(calculateForces.moveBodies, matrixBF, from, nBodies),
				this.getContext().getSelf());
	}

	private void onMoveBodies(final MoveBodies moveBodies) {
		final Body[] bodies = moveBodies.bodies;
		final TriangularMatrix matrixBF = moveBodies.matrixBF;
		final int from = moveBodies.from;
		final int nBodies = moveBodies.nBodies;
		final int deltaTime = moveBodies.deltaTime;

		final Body[] newBodies = new Body[nBodies];

		for (int i = from; i < from + nBodies; i++) {
			final Force[] forcesToSum = new Force[bodies.length - 1];
			int f = 0;
			// Horizontal
			for (int j = i + 1; j < bodies.length; j++) {
				forcesToSum[f] = matrixBF.get(i, j);
				f++;
			}
			// Vertical
			for (int k = 0; k < i; k++) {
				forcesToSum[f] = Force.revertOrientation(matrixBF.get(k, i));
				f++;
			}

			newBodies[i - from] = bodies[i].apply(Force.sumForces(forcesToSum), deltaTime);
		}

		moveBodies.replyTo.tell(new MainActor.BodiesMoved(moveBodies.moveBodies.getMoveFuture(), newBodies, from),
				this.getContext().getSelf());
	}

	@Override
	public Receive createReceive() {
		return this.receiveBuilder().match(CreateBodies.class, this::onCreateBodies)
				.match(CalculateForces.class, this::onCalculateForces).match(MoveBodies.class, this::onMoveBodies)
				.build();
	}
}
