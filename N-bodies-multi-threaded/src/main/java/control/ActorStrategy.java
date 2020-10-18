package control;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import control.actors.MainActor;
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

	public ActorStrategy(final int nBodies, final int deltaTime) {
		super();

		this.nBodies = nBodies;
		this.deltaTime = deltaTime;

		this.system = ActorSystem.create("system");
		this.mainActor = this.system.actorOf(Props.create(MainActor.class, this.nBodies, this.deltaTime), "mainActor");

		this.bodies = new Body[nBodies];
	}

	/**
	 * An Executor using AvailableProcessors() + 1 Threads is used to create the bodies.
	 */
	@Override
	public void createBodies(final double minMass, final double maxMass, final double maxPosX, final double maxPosY,
			final double minSpeed, final double maxSpeed) {

		final CompletableFuture<Body[]> future = new CompletableFuture<>();
		this.mainActor.tell(new MainActor.CreateBodies(future, minMass, maxMass, maxPosX, maxPosY, minSpeed, maxSpeed),
				null);
		this.bodies = future.join();
	}

	/**
	 * An Executor using AvailableProcessors() + 1 Threads is used to calculate the Forces between the Bodies,
	 * to get the total Force each Body is subjected to and to move the Bodies.
	 *
	 * If the main Thread is requested to stop, it tries to revert any Forces already applied.
	 */
	@Override
	public void calculateAndMove() {

		final CompletableFuture<Body[]> future = new CompletableFuture<>();
		this.mainActor.tell(new MainActor.MoveBodies(future), null);
		this.bodies = future.join();
	}

	@Override
	public synchronized void interrupt() {
		// TODO
	}

	@Override
	public List<Body> getBodies() {
		return Arrays.asList(this.bodies);
	}
}
