package view.util;

import java.awt.Color;
import java.util.PrimitiveIterator.OfDouble;
import java.util.Random;

import javax.swing.JLabel;

public class NBColor {
	public static final Color LBL_BLUE = new Color(0, 0, 139);
	public static final Color LBL_RED = new Color(220, 20, 60);
	public static final Color LBL_GREY = new JLabel().getForeground();
	public static final Color LBL_LIGHT_GREY = new Color(103, 103, 103);
	public static final Color PNL_BORDER = Color.DARK_GRAY;
	public static final Color PNL_WHITE = Color.WHITE;

	private static final float MIN_COLOR = 0.0F;
	private static final float MAX_COLOR = 0.9F;
	private static final float MAX_ALPHA = 1.0F;

	private static OfDouble colorGenerator = new Random().doubles(NBColor.MIN_COLOR, NBColor.MAX_COLOR).iterator();

	private final float r;
	private final float g;
	private final float b;
	private float a;

	public NBColor() {
		this.r = (float) NBColor.colorGenerator.nextDouble();
		this.g = (float) NBColor.colorGenerator.nextDouble();
		this.b = (float) NBColor.colorGenerator.nextDouble();
		this.a = NBColor.MAX_COLOR;
	}

	/**
	 * Creates a new Color with same rbg as before and a in [0.5-1.0] range
	 */
	public Color getNew(final float _a) {
		this.a = _a <= NBColor.MAX_ALPHA ? (_a + 1.0F) / 2.0F : NBColor.MAX_ALPHA;
		return new Color(this.r, this.g, this.b, this.a);
	}
}
