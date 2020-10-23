package control;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import control.actors.MainActor;
import control.actors.MainActorMsg;
import model.Body;

/**
 * Strategy that uses Akka Actors to achieve the goal
 *
 */
public class ActorStrategy implements Strategy {

	private final int nBodies;
	private final int deltaTime;

	private final ActorSystem system;
	private final ActorRef mainActor;

	private Body[] bodies;
	private volatile boolean interrupted;

	public ActorStrategy(final int nBodies, final int deltaTime) {
		super();

		this.nBodies = nBodies;
		this.deltaTime = deltaTime;

		this.system = ActorSystem.create("system");
		this.mainActor = this.system.actorOf(Props.create(MainActor.class, this.nBodies, this.deltaTime), "mainActor");

		this.bodies = new Body[nBodies];
		this.interrupted = false;
	}

	@Override
	public void createBodies(final double minMass, final double maxMass, final double maxPosX, final double maxPosY,
			final double minSpeed, final double maxSpeed) {

		final CompletableFuture<Body[]> future = new CompletableFuture<>();
		this.mainActor.tell(
				new MainActorMsg.CreateBodies(future, minMass, maxMass, maxPosX, maxPosY, minSpeed, maxSpeed), null);
		this.bodies = future.join();
	}

	@Override
	public void calculateAndMove() {

		final CompletableFuture<Body[]> future = new CompletableFuture<>();
		this.mainActor.tell(new MainActorMsg.MoveBodies(future), null);
		if (!this.interrupted) {
			this.bodies = future.join();
		} else {
			this.mainActor.tell(new MainActorMsg.SetBodies(this.bodies), null);
			this.interrupted = false;
		}
	}

	@Override
	public synchronized void interrupt() {
		this.interrupted = true;
	}

	@Override
	public synchronized void clear() {
		this.system.terminate();
	}

	@Override
	public List<Body> getBodies() {
		return Arrays.asList(this.bodies);
	}
}
