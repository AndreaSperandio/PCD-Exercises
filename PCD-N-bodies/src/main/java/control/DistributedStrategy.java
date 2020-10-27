package control;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.typed.Cluster;
import akka.cluster.typed.Join;
import akka.cluster.typed.Leave;
import control.distributed.MainActor;
import control.distributed.MainActorMsg;
import model.Body;

/**
 * Strategy that uses Akka Distributed Actors to achieve the goal
 *
 */
public class DistributedStrategy implements Strategy {

	private final int nBodies;
	private final int deltaTime;

	private final Config clusterConfig;
	private final Config noPort;
	private final Cluster cluster;
	private final Cluster clusterMainActor;

	private final ActorSystem<Object> system;
	private final ActorSystem<MainActorMsg> mainActor;

	private Body[] bodies;
	private volatile boolean interrupted;

	public DistributedStrategy(final int nBodies, final int deltaTime) {
		super();

		this.nBodies = nBodies;
		this.deltaTime = deltaTime;

		this.clusterConfig = ConfigFactory
				.parseString("akka { \n" + "  actor.provider = cluster \n" + "  remote.artery { \n"
						+ "    canonical { \n" + "      hostname = \"127.0.0.1\" \n" + "      port = 2551 \n"
						+ "    } \n" + "  } \n" + "}  \n " + " akka.cluster.jmx.multi-mbeans-in-same-jvm = on ");

		this.noPort = ConfigFactory.parseString(
				"      akka.remote.classic.netty.tcp.port = 0 \n" + "      akka.remote.artery.canonical.port = 0 \n");

		this.system = ActorSystem.create(Behaviors.empty(), "ClusterSystem",
				this.noPort.withFallback(this.clusterConfig));
		this.mainActor = ActorSystem.create(
				MainActor.create(this.clusterConfig, this.noPort, this.nBodies, this.deltaTime), "ClusterSystem",
				this.noPort.withFallback(this.clusterConfig));

		// Cluster create
		this.cluster = Cluster.get(this.system);
		this.clusterMainActor = Cluster.get(this.mainActor);
		// Cluster join
		this.cluster.manager().tell(Join.create(this.cluster.selfMember().address()));
		this.clusterMainActor.manager().tell(Join.create(this.cluster.selfMember().address()));

		this.bodies = new Body[nBodies];
		this.interrupted = false;
	}

	/**
	 * A Main Actor is used to coordinate the Body Creation job, dividing the work between other distributed Actors.
	 */
	@Override
	public void createBodies(final double minMass, final double maxMass, final double maxPosX, final double maxPosY,
			final double minSpeed, final double maxSpeed) {

		final CompletableFuture<Body[]> future = new CompletableFuture<>();
		this.mainActor
				.tell(new MainActorMsg.CreateBodies(future, minMass, maxMass, maxPosX, maxPosY, minSpeed, maxSpeed));
		this.bodies = future.join();
	}

	/**
	 * A Main Actor is used to coordinate the Forces Calculation and Bodies Moving jobs, dividing the work between
	 * other distributed Actors.
	 */
	@Override
	public void calculateAndMove() {

		final CompletableFuture<Body[]> future = new CompletableFuture<>();
		this.mainActor.tell(new MainActorMsg.MoveBodies(future));
		if (!this.interrupted) {
			this.bodies = future.join();
		} else {
			this.mainActor.tell(new MainActorMsg.SetBodies(this.bodies));
			this.interrupted = false;
		}
	}

	@Override
	public synchronized void interrupt() {
		this.interrupted = true;
	}

	@Override
	public synchronized void clear() {
		this.cluster.manager().tell(Leave.create(this.cluster.selfMember().address()));
		this.system.terminate();
	}

	@Override
	public List<Body> getBodies() {
		return Arrays.asList(this.bodies);
	}
}
