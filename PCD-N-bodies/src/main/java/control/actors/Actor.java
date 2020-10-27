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

/**
 * Actor that receives messages from the MainActor and is responsible for creating bodies, calculating the forces
 * and applying them to the bodies.
 * The recognised messages are those listed in the ActorMsg Interface.
 *
 * @author Andrea Sperandio
 *
 */
public class Actor extends AbstractActor {
	private Body[] bodies;

	public Actor() {
		super();
	}

	/* Receives a CreateBodies message and replies with the Body[] of created bodies */
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
				this.getSelf());
	}

	/* Receives a CalculateForces message and replies with the TriangularMatrix of forces created */
	private void onCalculateForces(final CalculateForces calculateForces) {
		this.bodies = calculateForces.getBodies();
		final int from = calculateForces.getFrom();
		final int nBodies = calculateForces.getnBodies();

		/*final TriangularMatrix matrixBF = new TriangularMatrix(nBodies, this.bodies.length);

		for (int i = 0; i < nBodies; i++) {
			for (int j = i + 1; j < this.bodies.length; j++) {
				matrixBF.set(i, j, Force.get(this.bodies[i], this.bodies[j]));
			}
		}*/
		final TriangularMatrix matrixBF = new TriangularMatrix(nBodies, this.bodies.length - from);

		for (int i = from; i < from + nBodies; i++) {
			for (int j = i + 1; j < this.bodies.length; j++) {
				matrixBF.set(i - from, j - from, Force.get(this.bodies[i], this.bodies[j]));
			}
		}

		calculateForces.getReplyTo().tell(
				new MainActorMsg.ForcesCalculated(calculateForces.getMoveBodies(), matrixBF, from, nBodies),
				this.getSelf());
	}

	/* Receives a MoveBodies message and replies with the Body[] of moved bodies */
	private void onMoveBodies(final MoveBodies moveBodies) {
		final TriangularMatrix matrixBF = moveBodies.getMatrixBF();
		final int from = moveBodies.getFrom();
		final int nBodies = moveBodies.getnBodies();
		final int deltaTime = moveBodies.getDeltaTime();

		final Body[] newBodies = new Body[nBodies];

		/*final int rowSize = matrixBF.getRowSize();
		for (int i = from; i < from + nBodies; i++) {
			final Force[] forcesToSum = new Force[rowSize - 1];
			int f = 0;
			// Horizontal
			for (int j = i + 1; j < rowSize; j++) {
				forcesToSum[f] = matrixBF.get(i, j);
				f++;
			}
			// Vertical
			for (int k = 0; k < i; k++) {
				forcesToSum[f] = Force.revertOrientation(matrixBF.get(k, i));
				f++;
			}
			newBodies[i - from] = this.bodies[i - from].apply(Force.sumForces(forcesToSum), deltaTime);
		}*/

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
				this.getSelf());
	}

	@Override
	/** The recognised messages are those listed in the ActorMsg Interface. */
	public Receive createReceive() {
		return this.receiveBuilder().match(CreateBodies.class, this::onCreateBodies)
				.match(CalculateForces.class, this::onCalculateForces).match(MoveBodies.class, this::onMoveBodies)
				.build();
	}
}