package control.actors;

import java.util.PrimitiveIterator.OfDouble;
import java.util.Random;

import akka.actor.AbstractActor;
import control.actors.ActorMsg.CalculateForces;
import control.actors.ActorMsg.CreateBodies;
import control.actors.ActorMsg.MoveBodies;
import control.actors.MainActorMsg.BodiesCreated;
import model.Body;
import model.Force;
import model.Position;
import model.TriangularMatrix;
import model.Vector;

public class Actor extends AbstractActor {
	private Body[] bodies;

	public Actor() {
		super();
	}

	private void onCreateBodies(final CreateBodies createBodies) {
		final MainActorMsg.CreateBodies values = createBodies.getCreateBodies();
		final Random random = new Random();
		final OfDouble massGenerator = random.doubles(values.getMinMass(), values.getMaxMass()).iterator();
		final OfDouble positionXGenerator = random.doubles(0.0, values.getMaxPosX()).iterator();
		final OfDouble positionYGenerator = random.doubles(0.0, values.getMaxPosY()).iterator();
		final OfDouble speedGenerator = random.doubles(values.getMinSpeed(), values.getMaxSpeed()).iterator();

		final int nBodies = createBodies.getnBodies();
		final Body[] newBodies = new Body[nBodies];
		for (int i = 0; i < nBodies; i++) {
			newBodies[i] = new Body(massGenerator.nextDouble(),
					new Position(positionXGenerator.nextDouble(), positionYGenerator.nextDouble()),
					new Vector(speedGenerator.nextDouble(), speedGenerator.nextDouble()));
		}

		createBodies.getReplyTo().tell(new BodiesCreated(values.getCreationFuture(), newBodies, createBodies.getFrom()),
				this.getContext().getSelf());
	}

	private void onCalculateForces(final CalculateForces calculateForces) {
		this.bodies = calculateForces.getBodies();
		final int from = calculateForces.getFrom();
		final int nBodies = calculateForces.getnBodies();

		final TriangularMatrix matrixBF = new TriangularMatrix(this.bodies.length - from);

		for (int i = from; i < from + nBodies; i++) {
			for (int j = i + 1; j < this.bodies.length; j++) {
				matrixBF.set(i - from, j - from, Force.get(this.bodies[i], this.bodies[j]));
			}
		}

		calculateForces.getReplyTo().tell(
				new MainActorMsg.ForcesCalculated(calculateForces.getMoveBodies(), matrixBF, from, nBodies),
				this.getContext().getSelf());
	}

	private void onMoveBodies(final MoveBodies moveBodies) {
		final TriangularMatrix matrixBF = moveBodies.getMatrixBF();
		final int from = moveBodies.getFrom();
		final int nBodies = moveBodies.getnBodies();
		final int deltaTime = moveBodies.getDeltaTime();

		final Body[] newBodies = new Body[nBodies];

		for (int i = from; i < from + nBodies; i++) {
			final Force[] forcesToSum = new Force[this.bodies.length - 1];
			int f = 0;
			// Horizontal
			for (int j = i + 1; j < this.bodies.length; j++) {
				forcesToSum[f] = matrixBF.get(i, j);
				f++;
			}
			// Vertical
			for (int k = 0; k < i; k++) {
				forcesToSum[f] = Force.revertOrientation(matrixBF.get(k, i));
				f++;
			}

			newBodies[i - from] = this.bodies[i].apply(Force.sumForces(forcesToSum), deltaTime);
		}

		moveBodies.getReplyTo().tell(
				new MainActorMsg.BodiesMoved(moveBodies.getMoveBodies().getMoveFuture(), newBodies, from),
				this.getContext().getSelf());
	}

	@Override
	public Receive createReceive() {
		return this.receiveBuilder().match(CreateBodies.class, this::onCreateBodies)
				.match(CalculateForces.class, this::onCalculateForces).match(MoveBodies.class, this::onMoveBodies)
				.build();
	}
}
