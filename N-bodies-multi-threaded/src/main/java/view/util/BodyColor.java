package view.util;

import java.awt.Color;
import java.util.PrimitiveIterator.OfDouble;
import java.util.Random;

public class BodyColor {
	public static final float MIN_COLOR = 0.0F;
	public static final float MAX_COLOR = 0.9F;
	public static final float MAX_ALPHA = 1.0F;

	private final float r;
	private final float g;
	private final float b;
	private float a;

	public BodyColor() {
		final Random random = new Random();
		final OfDouble colorGenerator = random.doubles(BodyColor.MIN_COLOR, BodyColor.MAX_COLOR).iterator();
		this.r = (float) colorGenerator.nextDouble();
		this.g = (float) colorGenerator.nextDouble();
		this.b = (float) colorGenerator.nextDouble();
		this.a = BodyColor.MAX_COLOR;
	}

	/**
	 * Creates a new Color with same rbg as before and a in [0.5-1.0] range
	 */
	public Color getNew(final float _a) {
		this.a = _a <= BodyColor.MAX_ALPHA ? (_a + 1.0F) / 2.0F : BodyColor.MAX_ALPHA;
		return new Color(this.r, this.g, this.b, this.a);
	}
}
