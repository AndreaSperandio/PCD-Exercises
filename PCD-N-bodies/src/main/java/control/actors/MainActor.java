package control.actors;

import java.util.ArrayList;
import java.util.List;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import control.actors.MainActorMsg.BodiesCreated;
import control.actors.MainActorMsg.BodiesMoved;
import control.actors.MainActorMsg.CreateBodies;
import control.actors.MainActorMsg.ForcesCalculated;
import control.actors.MainActorMsg.MoveBodies;
import control.actors.MainActorMsg.SetBodies;
import model.Body;
import model.TriangularMatrix;

/**
 * MainActor that receives messages from ActorStrategy and is responsible for the coordination required for
 * creating bodies, calculating the forces and applying them to the bodies.
 * The recognised messages are those listed in the MainActorMsg Interface.
 *
 */
public class MainActor extends AbstractActor {
	private static final int N_ACTORS = 10;
	private final List<ActorRef> actors;
	private final int nBodies;
	private final int deltaTime;

	private Body[] bodies;
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

	/* Receives a CreateBodies message and tells the actors to Create the Bodies */
	private void onCreateBodies(final CreateBodies createBodies) {
		this.nActorsBodyCreateCompleted = 0;

		final int nBodiesPerActor = this.nBodies / MainActor.N_ACTORS;
		int firstBody = 0;
		for (int i = 0; i < this.actors.size() - 1; i++) {
			this.actors.get(i).tell(new ActorMsg.CreateBodies(this.getSelf(), createBodies, firstBody, nBodiesPerActor),
					this.getSelf());
			firstBody += nBodiesPerActor;
		}
		this.actors.get(this.actors.size() - 1).tell(
				new ActorMsg.CreateBodies(this.getSelf(), createBodies, firstBody, this.nBodies - firstBody),
				this.getSelf());
	}

	/* Receives and stores the Created Bodies from the actors */
	private void onBodiesCreated(final BodiesCreated bodiesCreated) {
		final int from = bodiesCreated.getFrom();
		final Body[] newBodies = bodiesCreated.getBodies();

		for (int i = from, j = 0; j < newBodies.length; i++, j++) {
			this.bodies[i] = newBodies[j];
		}
		this.nActorsBodyCreateCompleted++;

		if (this.nActorsBodyCreateCompleted == MainActor.N_ACTORS) {
			bodiesCreated.getCreationFuture().complete(this.bodies);
		}
	}

	/* Receives a MoveBodies message and tells the actors to Calculate the Forces */
	private void onMoveBodies(final MoveBodies moveBodies) {
		this.nActorsForceCalculateCompleted = 0;

		final int nBodiesPerActor = this.nBodies / MainActor.N_ACTORS;
		int firstBody = 0;
		/*for (int i = 0; i < this.actors.size() - 1; i++) {
			this.actors.get(i)
					.tell(new ActorMsg.CalculateForces(this.getSelf(), moveBodies,
							Arrays.copyOfRange(this.bodies, firstBody, this.bodies.length), firstBody, nBodiesPerActor),
							this.getSelf());
			firstBody += nBodiesPerActor;
		}
		this.actors.get(this.actors.size() - 1).tell(new ActorMsg.CalculateForces(this.getSelf(), moveBodies,
				Arrays.copyOfRange(this.bodies, firstBody, this.bodies.length), firstBody, this.nBodies - firstBody),
				this.getSelf());*/
		for (int i = 0; i < this.actors.size() - 1; i++) {
			this.actors.get(i).tell(
					new ActorMsg.CalculateForces(this.getSelf(), moveBodies, this.bodies, firstBody, nBodiesPerActor),
					this.getSelf());
			firstBody += nBodiesPerActor;
		}
		this.actors.get(this.actors.size() - 1).tell(new ActorMsg.CalculateForces(this.getSelf(), moveBodies,
				this.bodies, firstBody, this.nBodies - firstBody), this.getSelf());
	}

	/* Receives and stores the Calculated Forces from the actors */
	private void onForcesCalculated(final ForcesCalculated forcesCalculated) {
		final TriangularMatrix calculatedForces = forcesCalculated.getMatrixBF();
		final int from = forcesCalculated.getFrom();
		final int nBodiesPerActor = forcesCalculated.getnBodies();

		for (int i = from; i < from + nBodiesPerActor; i++) {
			for (int j = i + 1; j < this.bodies.length; j++) {
				this.matrixBF.set(i, j, calculatedForces.get(i - from, j - from));
			}
		}

		this.nActorsForceCalculateCompleted++;

		if (this.nActorsForceCalculateCompleted == MainActor.N_ACTORS) {
			this.moveBodies(forcesCalculated.getMoveBodies());
		}
	}

	/* Tells the actors to Move the Bodies */
	private void moveBodies(final MoveBodies moveBodies) {
		this.nActorsBodyMoveCompleted = 0;

		final int nBodiesPerActor = this.nBodies / MainActor.N_ACTORS;
		int firstBody = 0;
		/*for (int i = 0; i < this.actors.size() - 1; i++) {
			this.actors.get(i)
					.tell(new ActorMsg.MoveBodies(this.getSelf(), moveBodies,
							TriangularMatrix.copyOfRange(this.matrixBF, firstBody + nBodiesPerActor), firstBody,
							nBodiesPerActor, this.deltaTime), this.getSelf());
			firstBody += nBodiesPerActor;
		}*/
		for (int i = 0; i < this.actors.size() - 1; i++) {
			this.actors.get(i).tell(new ActorMsg.MoveBodies(this.getSelf(), moveBodies, this.matrixBF, firstBody,
					nBodiesPerActor, this.deltaTime), this.getSelf());
			firstBody += nBodiesPerActor;
		}
		this.actors.get(this.actors.size() - 1).tell(new ActorMsg.MoveBodies(this.getSelf(), moveBodies, this.matrixBF,
				firstBody, this.nBodies - firstBody, this.deltaTime), this.getSelf());
	}

	/* Receives and stores the Moved Bodies from the actors */
	private void onBodiesMoved(final BodiesMoved bodiesMoved) {
		final int from = bodiesMoved.getFrom();
		final Body[] movedBodies = bodiesMoved.getBodies();

		for (int i = from, j = 0; j < movedBodies.length; i++, j++) {
			this.bodies[i] = movedBodies[j];
		}

		this.nActorsBodyMoveCompleted++;

		if (this.nActorsBodyMoveCompleted == MainActor.N_ACTORS) {
			bodiesMoved.getMoveFuture().complete(this.bodies);
		}
	}

	/* Sets the Bodies */
	private void onSetBodies(final SetBodies setBodies) {
		this.bodies = setBodies.getBodies();
	}

	@Override
	public Receive createReceive() {
		return this.receiveBuilder().match(CreateBodies.class, this::onCreateBodies)
				.match(BodiesCreated.class, this::onBodiesCreated).match(MoveBodies.class, this::onMoveBodies)
				.match(ForcesCalculated.class, this::onForcesCalculated).match(BodiesMoved.class, this::onBodiesMoved)
				.match(SetBodies.class, this::onSetBodies).build();
	}
}