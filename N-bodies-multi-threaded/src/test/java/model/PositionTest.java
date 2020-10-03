package model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PositionTest {

	private Position pi;
	private Position pj;
	private Position pk;

	@Before
	public void initialize() {
		this.pi = new Position(2.0, 2.0);
		this.pj = new Position(6.0, 5.0);
		this.pk = new Position(6.0, 3.0);
	}

	@Test
	public void testDistance() {
		Assert.assertEquals("Distance ij test", 5.0, Position.getDistance(this.pi, this.pj), 0.0000000000001);
		Assert.assertEquals("Distance ji test", 5.0, Position.getDistance(this.pj, this.pi), 0.0000000000001);

		Assert.assertEquals("Distance ik test", 4.12310562561766, Position.getDistance(this.pi, this.pk),
				0.0000000000001);
		Assert.assertEquals("Distance ki test", 4.12310562561766, Position.getDistance(this.pk, this.pi),
				0.0000000000001);

		Assert.assertEquals("Distance jk test", 2.0, Position.getDistance(this.pj, this.pk), 0.0000000000001);
		Assert.assertEquals("Distance kj test", 2.0, Position.getDistance(this.pk, this.pj), 0.0000000000001);
	}

	@Test
	public void testDifference() {
		Assert.assertEquals("Difference ij test", new Position(4.0, 3.0), Position.getDifference(this.pi, this.pj));
		Assert.assertEquals("Difference ji test", new Position(-4.0, -3.0), Position.getDifference(this.pj, this.pi));

		Assert.assertEquals("Difference ij test", new Position(4.0, 1.0), Position.getDifference(this.pi, this.pk));
		Assert.assertEquals("Difference ji test", new Position(-4.0, -1.0), Position.getDifference(this.pk, this.pi));

		Assert.assertEquals("Difference ij test", new Position(0.0, -2.0), Position.getDifference(this.pj, this.pk));
		Assert.assertEquals("Difference ji test", new Position(0.0, 2.0), Position.getDifference(this.pk, this.pj));
	}
}
