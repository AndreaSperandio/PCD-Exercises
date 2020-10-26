package model;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import control.MultiThreadStrategy;
import control.Strategy;
import control.StrategyBuilder;

public class TaskStrategyTest {
	private int nBodies;
	private int deltaTime;
	private Strategy tStrat;
	private Strategy mtStrat;

	private double minMass;
	private double maxMass;
	private double maxPosX;
	private double maxPosY;
	private double minSpeed;
	private double maxSpeed;

	@Before
	public void initialize() {
		this.nBodies = 1000;
		this.deltaTime = 10000;
		this.tStrat = StrategyBuilder.buildStrategy(StrategyBuilder.TASK, this.nBodies, this.deltaTime);
		this.mtStrat = StrategyBuilder.buildStrategy(StrategyBuilder.MULTI_THREAD, this.nBodies, this.deltaTime);

		this.minMass = 100000.0;
		this.maxMass = 1000000.0;
		this.maxPosX = 300.0;
		this.maxPosY = 300.0;
		this.minSpeed = -0.000001;
		this.maxSpeed = 0.000001;
	}

	@Test
	public void testCorrectness() {
		this.tStrat.createBodies(this.minMass, this.maxMass, this.maxPosX, this.maxPosY, this.minSpeed, this.maxSpeed);
		Assert.assertTrue("All bodies are created",
				this.tStrat.getBodies().stream().filter(b -> b != null).toArray().length == this.nBodies);

		((MultiThreadStrategy) this.mtStrat).setBodies(this.tStrat.getBodies());

		this.tStrat.calculateAndMove();
		this.mtStrat.calculateAndMove();

		final List<Body> mtStratBodies = this.tStrat.getBodies();
		final List<Body> sStratBodies = this.mtStrat.getBodies();
		Body msStratBody;
		Body sStratBody;
		for (int i = 0; i < this.nBodies; i++) {
			msStratBody = mtStratBodies.get(i);
			sStratBody = sStratBodies.get(i);

			// Need to lower the delta because MultiThreadStrategy is more precise than StreamStrategy memorising 2 more decimals
			Assert.assertEquals("Body Mass is moved correctly", msStratBody.getMass(), sStratBody.getMass(),
					0.0000000000001);
			Assert.assertEquals("Body Position x is moved correctly", msStratBody.getPosition().getX(),
					sStratBody.getPosition().getX(), 0.00000000001);
			Assert.assertEquals("Body Position y is moved correctly", msStratBody.getPosition().getY(),
					sStratBody.getPosition().getY(), 0.00000000001);
			Assert.assertEquals("Body Speed x is moved correctly", msStratBody.getSpeed().getXComp(),
					sStratBody.getSpeed().getXComp(), 0.00000000001);
			Assert.assertEquals("Body Speed y is moved correctly", msStratBody.getSpeed().getYComp(),
					sStratBody.getSpeed().getYComp(), 0.00000000001);
		}
	}
}
