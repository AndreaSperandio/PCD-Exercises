package model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ForceTest {

	private Body bi;
	private Body bj;
	private Body bk;

	private Force forceij;
	private Force forceji;
	private Force forceik;
	private Force forceki;
	private Force forcejk;
	private Force forcekj;

	@Before
	public void initialize() {
		this.bi = new Body(100000.0, new Position(2.0, 2.0), new Vector(10.0, 1.0));
		this.bj = new Body(200000.0, new Position(6.0, 5.0), new Vector(20.0, 2.0));
		this.bk = new Body(300000.0, new Position(6.0, 3.0), new Vector(30.0, 3.0));

		this.forceij = Force.get(this.bi, this.bj);
		this.forceji = Force.get(this.bj, this.bi);
		this.forceik = Force.get(this.bi, this.bk);
		this.forceki = Force.get(this.bk, this.bi);
		this.forcejk = Force.get(this.bj, this.bk);
		this.forcekj = Force.get(this.bk, this.bj);
	}

	@Test
	public void testGet() {
		Assert.assertEquals("VectorX ij test", 0.2135705600000, this.forceij.getXComp(), 0.0000000000001);
		Assert.assertEquals("VectorY ij test", 0.1601779200000, this.forceij.getYComp(), 0.0000000000001);
		Assert.assertEquals("Module ij test", 0.2669632000000, this.forceij.getModule(), 0.0000000000001);

		Assert.assertEquals("VectorX ji test", -0.2135705600000, this.forceji.getXComp(), 0.0000000000001);
		Assert.assertEquals("VectorY ji test", -0.1601779200000, this.forceji.getYComp(), 0.0000000000001);
		Assert.assertEquals("Module ji test", 0.2669632000000, this.forceji.getModule(), 0.0000000000001);

		Assert.assertEquals("VectorX ik test", 0.4711115294118, this.forceik.getXComp(), 0.0000000000001);
		Assert.assertEquals("VectorY ik test", 0.1177778823529, this.forceik.getYComp(), 0.0000000000001);
		Assert.assertEquals("Module ik test", 0.4856106493027, this.forceik.getModule(), 0.0000000000001);

		Assert.assertEquals("VectorX ki test", -0.4711115294118, this.forceki.getXComp(), 0.0000000000001);
		Assert.assertEquals("VectorY ki test", -0.1177778823529, this.forceki.getYComp(), 0.0000000000001);
		Assert.assertEquals("Module ki test", 0.4856106493027, this.forceki.getModule(), 0.0000000000001);

		Assert.assertEquals("VectorX jk test", 0.0, this.forcejk.getXComp(), 0.0000000000001);
		Assert.assertEquals("VectorY jk test", -2.0022240000000, this.forcejk.getYComp(), 0.0000000000001);
		Assert.assertEquals("Module jk test", 2.0022240000000, this.forcejk.getModule(), 0.0000000000001);

		Assert.assertEquals("VectorX kj test", 0.0, this.forcekj.getXComp(), 0.0000000000001);
		Assert.assertEquals("VectorY kj test", 2.0022240000000, this.forcekj.getYComp(), 0.0000000000001);
		Assert.assertEquals("Module kj test", 2.0022240000000, this.forcekj.getModule(), 0.0000000000001);
	}

	@Test
	public void testSum() {
		final Force forcei = Force.sum(this.forceij, this.forceik);
		Assert.assertEquals("VectorX i test", 0.6846820894118, forcei.getXComp(), 0.0000000000001);
		Assert.assertEquals("VectorY i test", 0.2779558023529, forcei.getYComp(), 0.0000000000001);
		Assert.assertEquals("Module i test", 0.7389512782470, forcei.getModule(), 0.0000000000001);

		final Force forcej = Force.sum(this.forceji, this.forcejk);
		Assert.assertEquals("VectorX i test", -0.2135705600000, forcej.getXComp(), 0.0000000000001);
		Assert.assertEquals("VectorY i test", -2.1624019200000, forcej.getYComp(), 0.0000000000001);
		Assert.assertEquals("Module i test", 2.1729230192803, forcej.getModule(), 0.0000000000001);

		final Force forcek = Force.sum(this.forceki, this.forcekj);
		Assert.assertEquals("VectorX i test", -0.4711115294118, forcek.getXComp(), 0.0000000000001);
		Assert.assertEquals("VectorY i test", 1.8844461176471, forcek.getYComp(), 0.0000000000001);
		Assert.assertEquals("Module i test", 1.9424425972110, forcek.getModule(), 0.0000000000001);
	}
}
