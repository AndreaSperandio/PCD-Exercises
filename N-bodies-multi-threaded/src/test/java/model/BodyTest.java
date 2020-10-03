package model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BodyTest {

	private Body bi;
	private Body bj;
	private Body bk;
	private List<Body> bodies;

	@Before
	public void initialize() {
		this.bi = new Body(100000.0, new Position(2.0, 2.0), new Vector(10.0, 1.0));
		this.bj = new Body(200000.0, new Position(6.0, 5.0), new Vector(20.0, 2.0));
		this.bk = new Body(300000.0, new Position(6.0, 3.0), new Vector(30.0, 3.0));
		this.bodies = Arrays.asList(this.bi, this.bj, this.bk);
	}

	@Test
	public void testApply() {
		Assert.assertEquals("Apply bi test",
				new Body(100000.0, new Position(11.999985, 3.00001), new Vector(9.99997, 1.00002)),
				this.bi.apply(new Force(-3.0, 2.0), 1.0));
		Assert.assertEquals("Apply bj test",
				new Body(200000.0, new Position(85.99988, 13.00008), new Vector(19.99994, 2.00004)),
				this.bj.apply(new Force(-3.0, 2.0), 4.0));
		Assert.assertEquals("Apply bk test",
				new Body(300000.0, new Position(230.99971875, 25.5001875), new Vector(29.999925, 3.00005)),
				this.bk.apply(new Force(-3.0, 2.0), 7.5));
	}

	@Test
	public void testMultipleApply() {
		final Map<Body, Force> mapBF = new HashMap<>();
		for (final Body b : this.bodies) {
			mapBF.put(b, Force.sum(this.bodies.stream().filter(b1 -> !b1.equals(b)).map(b1 -> Force.get(b, b1))
					.toArray(Force[]::new)));
		}
		mapBF.keySet().stream().forEach(b -> b.apply(mapBF.get(b), 3.0));

		Assert.assertEquals("MultipleApply bi posX test", 32.00000706389490, this.bi.getPosition().getX(),
				0.0000000000001);
		Assert.assertEquals("MultipleApply bi posY test", 5.00000272704123, this.bi.getPosition().getY(),
				0.0000000000001);
		Assert.assertEquals("MultipleApply bi speedX test", 10.00000470926320, this.bi.getSpeed().getXComp(),
				0.0000000000001);
		Assert.assertEquals("MultipleApply bi speedY test", 1.00000181802749, this.bi.getSpeed().getYComp(),
				0.0000000000001);

		Assert.assertEquals("MultipleApply bj posX test", 65.99999903893250, this.bj.getPosition().getX(),
				0.0000000000001);
		Assert.assertEquals("MultipleApply bj posY test", 10.99997675417940, this.bj.getPosition().getY(),
				0.0000000000001);
		Assert.assertEquals("MultipleApply bj speedX test", 19.99999935928830, this.bj.getSpeed().getXComp(),
				0.0000000000001);
		Assert.assertEquals("MultipleApply bj speedY test", 1.99998450278624, this.bj.getSpeed().getYComp(),
				0.0000000000001);

		Assert.assertEquals("MultipleApply bk posX test", 95.99999828608010, this.bk.getPosition().getX(),
				0.0000000000001);
		Assert.assertEquals("MultipleApply bk posY test", 12.00001458820000, this.bk.getPosition().getY(),
				0.0000000000001);
		Assert.assertEquals("MultipleApply bk speedX test", 29.99999885738670, this.bk.getSpeed().getXComp(),
				0.0000000000001);
		Assert.assertEquals("MultipleApply bk speedY test", 3.00000972546668, this.bk.getSpeed().getYComp(),
				0.0000000000001);

	}
}
