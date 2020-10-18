package control.actors;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import model.Body;
import model.TriangularMatrix;

public class MainActor extends AbstractActor {

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

	public static class BodiesCreated {
		private final CompletableFuture<Body[]> creationFuture;
		private final Body[] bodies;
		private final int from;

		public BodiesCreated(final CompletableFuture<Body[]> creationFuture, final Body[] bodies, final int from) {
			this.creationFuture = creationFuture;
			this.bodies = bodies;
			this.from = from;
		}
	}

	public static class MoveBodies {
		private final CompletableFuture<Body[]> moveFuture;

		public MoveBodies(final CompletableFuture<Body[]> future) {
			this.moveFuture = future;
		}

		public CompletableFuture<Body[]> getMoveFuture() {
			return this.moveFuture;
		}
	}

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
	}

	public static class BodiesMoved {
		private final CompletableFuture<Body[]> moveFuture;
		private final Body[] bodies;
		private final int from;

		public BodiesMoved(final CompletableFuture<Body[]> moveFuture, final Body[] bodies, final int from) {
			this.moveFuture = moveFuture;
			this.bodies = bodies;
			this.from = from;
		}
	}

	private static final int N_ACTORS = 10;
	private final List<ActorRef> actors;
	private final int nBodies;
	private final int deltaTime;

	private final Body[] bodies;
	private final TriangularMatrix matrixBF;

	private int nActorsBodyCreateCompleted;
	private int nActorsForceCalculateCompleted;
	private int nActorsBodyMoveCompleted;

	public MainActor(final int nBodies, final int deltaTime) {
		super();
		this.actors = new ArrayList<>(MainActor.N_ACTORS);
		this.nBodies = nBodies;
		this.deltaTime = deltaTime;
		this.bodies = new Body[nBodies];
		this.matrixBF = new TriangularMatrix(nBodies);
		this.nActorsBodyCreateCompleted = 0;

		for (int i = 0; i < MainActor.N_ACTORS; i++) {
			this.actors.add(this.getContext().actorOf(Props.create(Actor.class), "actor" + i));
		}
	}

	private void onCreateBodies(final CreateBodies createBodies) {
		this.nActorsBodyCreateCompleted = 0;

		final int nBodiesPerActor = this.nBodies / MainActor.N_ACTORS;
		int firstBody = 0;
		for (int i = 0; i < this.actors.size() - 1; i++) {
			this.actors.get(i).tell(new Actor.CreateBodies(this.getSelf(), createBodies, firstBody, nBodiesPerActor),
					this.getContext().getSelf());
			firstBody += nBodiesPerActor;
		}
		this.actors.get(this.actors.size() - 1).tell(
				new Actor.CreateBodies(this.getSelf(), createBodies, firstBody, this.nBodies - firstBody),
				this.getContext().getSelf());
	}

	private void onBodiesCreated(final BodiesCreated bodiesCreated) {
		final int from = bodiesCreated.from;
		final Body[] newBodies = bodiesCreated.bodies;

		for (int i = from, j = 0; j < newBodies.length; i++, j++) {
			this.bodies[i] = newBodies[j];
		}
		this.nActorsBodyCreateCompleted++;

		if (this.nActorsBodyCreateCompleted == MainActor.N_ACTORS) {
			bodiesCreated.creationFuture.complete(this.bodies);
		}
	}

	private void onMoveBodies(final MoveBodies moveBodies) {
		this.nActorsForceCalculateCompleted = 0;

		final int nBodiesPerActor = this.nBodies / MainActor.N_ACTORS;
		int firstBody = 0;
		for (int i = 0; i < this.actors.size() - 1; i++) {
			this.actors.get(i).tell(
					new Actor.CalculateForces(this.getSelf(), moveBodies, this.bodies, firstBody, nBodiesPerActor),
					this.getContext().getSelf());
			firstBody += nBodiesPerActor;
		}
		this.actors.get(this.actors.size() - 1).tell(
				new Actor.CalculateForces(this.getSelf(), moveBodies, this.bodies, firstBody, this.nBodies - firstBody),
				this.getContext().getSelf());
	}

	private void onForcesCalculated(final ForcesCalculated forcesCalculated) {
		final TriangularMatrix calculatedForces = forcesCalculated.matrixBF;
		final int from = forcesCalculated.from;
		final int nBodiesPerActor = forcesCalculated.nBodies;

		for (int i = from; i < from + nBodiesPerActor; i++) {
			for (int j = i + 1; j < this.bodies.length; j++) {
				this.matrixBF.set(i, j, calculatedForces.get(i - from, j - from));
			}
		}

		this.nActorsForceCalculateCompleted++;

		if (this.nActorsForceCalculateCompleted == MainActor.N_ACTORS) {
			this.moveBodies(forcesCalculated.moveBodies);
		}
	}

	private void moveBodies(final MoveBodies moveBodies) {
		this.nActorsBodyMoveCompleted = 0;

		final int nBodiesPerActor = this.nBodies / MainActor.N_ACTORS;
		int firstBody = 0;
		for (int i = 0; i < this.actors.size() - 1; i++) {
			this.actors.get(i).tell(new Actor.MoveBodies(this.getSelf(), moveBodies, this.bodies, this.matrixBF,
					firstBody, nBodiesPerActor, this.deltaTime), this.getContext().getSelf());
			firstBody += nBodiesPerActor;
		}
		this.actors.get(this.actors.size() - 1).tell(new Actor.MoveBodies(this.getSelf(), moveBodies, this.bodies,
				this.matrixBF, firstBody, this.nBodies - firstBody, this.deltaTime), this.getContext().getSelf());
	}

	private void onBodiesMoved(final BodiesMoved bodiesMoved) {
		final int from = bodiesMoved.from;
		final Body[] movedBodies = bodiesMoved.bodies;

		for (int i = from, j = 0; j < movedBodies.length; i++, j++) {
			this.bodies[i] = movedBodies[j];
		}

		this.nActorsBodyMoveCompleted++;

		if (this.nActorsBodyMoveCompleted == MainActor.N_ACTORS) {
			bodiesMoved.moveFuture.complete(this.bodies);
		}
	}

	@Override
	public Receive createReceive() {
		return this.receiveBuilder().match(CreateBodies.class, this::onCreateBodies)
				.match(BodiesCreated.class, this::onBodiesCreated).match(MoveBodies.class, this::onMoveBodies)
				.match(ForcesCalculated.class, this::onForcesCalculated).match(BodiesMoved.class, this::onBodiesMoved)
				.build();
	}
}
