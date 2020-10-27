package control.distributed;

import java.util.ArrayList;
import java.util.List;

import com.typesafe.config.Config;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.cluster.typed.Cluster;
import akka.cluster.typed.Join;
import control.distributed.MainActorMsg.BodiesCreated;
import control.distributed.MainActorMsg.BodiesMoved;
import control.distributed.MainActorMsg.CreateBodies;
import control.distributed.MainActorMsg.ForcesCalculated;
import control.distributed.MainActorMsg.MoveBodies;
import control.distributed.MainActorMsg.SetBodies;
import model.Body;
import model.TriangularMatrix;

/**
 * MainActor that receives messages from DistributedStrategy and is responsible for the coordination required for
 * creating bodies, calculating the forces and applying them to the bodies.
 * The recognised messages are of the MainActorMsg type.
 *
 * @author Andrea Sperandio
 *
 */
public class MainActor extends AbstractBehavior<MainActorMsg> {
	private static final int N_ACTORS = 10;
	private final List<ActorSystem<ActorMsg>> actors;
	private final int nBodies;
	private final int deltaTime;

	private Body[] bodies;
	private final TriangularMatrix matrixBF;

	private int nActorsBodyCreateCompleted;
	private int nActorsForceCalculateCompleted;
	private int nActorsBodyMoveCompleted;

	public static Behavior<MainActorMsg> create(final Config clusterConfig, final Config noPort, final int nBodies,
			final int deltaTime) {
		return Behaviors.setup(context -> new MainActor(context, clusterConfig, noPort, nBodies, deltaTime));
	}

	private MainActor(final ActorContext<MainActorMsg> context, final Config clusterConfig, final Config noPort,
			final int nBodies, final int deltaTime) {
		super(context);

		this.actors = new ArrayList<>(MainActor.N_ACTORS);
		this.nBodies = nBodies;
		this.deltaTime = deltaTime;
		this.bodies = new Body[nBodies];
		this.matrixBF = new TriangularMatrix(nBodies);
		this.nActorsBodyCreateCompleted = 0;

		for (int i = 0; i < MainActor.N_ACTORS; i++) {
			final ActorSystem<ActorMsg> actorSystem = ActorSystem.create(Actor.create(), "ClusterSystem",//"actor" + i,
					noPort.withFallback(clusterConfig));
			final Cluster cluster = Cluster.get(actorSystem);
			cluster.manager().tell(Join.create(Cluster.get(this.getContext().getSystem()).selfMember().address()));
			this.actors.add(actorSystem);
		}
	}

	@Override
	public Receive<MainActorMsg> createReceive() {
		return this.newReceiveBuilder().onMessage(CreateBodies.class, this::onCreateBodies)
				.onMessage(BodiesCreated.class, this::onBodiesCreated).onMessage(MoveBodies.class, this::onMoveBodies)
				.onMessage(ForcesCalculated.class, this::onForcesCalculated)
				.onMessage(BodiesMoved.class, this::onBodiesMoved).onMessage(SetBodies.class, this::onSetBodies)
				.build();
	}

	/* Receives a CreateBodies message and tells the actors to Create the Bodies */
	private Behavior<MainActorMsg> onCreateBodies(final CreateBodies createBodies) {
		this.nActorsBodyCreateCompleted = 0;

		final int nBodiesPerActor = this.nBodies / MainActor.N_ACTORS;
		int firstBody = 0;
		for (int i = 0; i < this.actors.size() - 1; i++) {
			this.actors.get(i).tell(
					new ActorMsg.CreateBodies(this.getContext().getSelf(), createBodies, firstBody, nBodiesPerActor));
			firstBody += nBodiesPerActor;
		}
		this.actors.get(this.actors.size() - 1).tell(new ActorMsg.CreateBodies(this.getContext().getSelf(),
				createBodies, firstBody, this.nBodies - firstBody));

		return this;
	}

	/* Receives and stores the Created Bodies from the actors */
	private Behavior<MainActorMsg> onBodiesCreated(final BodiesCreated bodiesCreated) {
		final int from = bodiesCreated.getFrom();
		final Body[] newBodies = bodiesCreated.getBodies();

		for (int i = from, j = 0; j < newBodies.length; i++, j++) {
			this.bodies[i] = newBodies[j];
		}
		this.nActorsBodyCreateCompleted++;

		if (this.nActorsBodyCreateCompleted == MainActor.N_ACTORS) {
			bodiesCreated.getCreationFuture().complete(this.bodies);
		}

		return this;
	}

	/* Receives a MoveBodies message and tells the actors to Calculate the Forces */
	private Behavior<MainActorMsg> onMoveBodies(final MoveBodies moveBodies) {
		this.nActorsForceCalculateCompleted = 0;

		final int nBodiesPerActor = this.nBodies / MainActor.N_ACTORS;
		int firstBody = 0;
		for (int i = 0; i < this.actors.size() - 1; i++) {
			this.actors.get(i).tell(new ActorMsg.CalculateForces(this.getContext().getSelf(), moveBodies, this.bodies,
					firstBody, nBodiesPerActor));
			firstBody += nBodiesPerActor;
		}
		this.actors.get(this.actors.size() - 1).tell(new ActorMsg.CalculateForces(this.getContext().getSelf(),
				moveBodies, this.bodies, firstBody, this.nBodies - firstBody));

		return this;
	}

	/* Receives and stores the Calculated Forces from the actors */
	private Behavior<MainActorMsg> onForcesCalculated(final ForcesCalculated forcesCalculated) {
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

		return this;
	}

	/* Tells the actors to Move the Bodies */
	private Behavior<MainActorMsg> moveBodies(final MoveBodies moveBodies) {
		this.nActorsBodyMoveCompleted = 0;

		final int nBodiesPerActor = this.nBodies / MainActor.N_ACTORS;
		int firstBody = 0;
		for (int i = 0; i < this.actors.size() - 1; i++) {
			this.actors.get(i).tell(new ActorMsg.MoveBodies(this.getContext().getSelf(), moveBodies, this.matrixBF,
					firstBody, nBodiesPerActor, this.deltaTime));
			firstBody += nBodiesPerActor;
		}
		this.actors.get(this.actors.size() - 1).tell(new ActorMsg.MoveBodies(this.getContext().getSelf(), moveBodies,
				this.matrixBF, firstBody, this.nBodies - firstBody, this.deltaTime));

		return this;
	}

	/* Receives and stores the Moved Bodies from the actors */
	private Behavior<MainActorMsg> onBodiesMoved(final BodiesMoved bodiesMoved) {
		final int from = bodiesMoved.getFrom();
		final Body[] movedBodies = bodiesMoved.getBodies();

		for (int i = from, j = 0; j < movedBodies.length; i++, j++) {
			this.bodies[i] = movedBodies[j];
		}

		this.nActorsBodyMoveCompleted++;

		if (this.nActorsBodyMoveCompleted == MainActor.N_ACTORS) {
			bodiesMoved.getMoveFuture().complete(this.bodies);
		}

		return this;
	}

	/* Sets the Bodies */
	private Behavior<MainActorMsg> onSetBodies(final SetBodies setBodies) {
		this.bodies = setBodies.getBodies();

		return this;
	}
}