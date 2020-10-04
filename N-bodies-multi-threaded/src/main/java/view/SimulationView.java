package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PrimitiveIterator.OfDouble;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import model.Body;
import model.Force;
import model.Position;
import model.Vector;

public class SimulationView extends JFrame {
	private class BodyColor {
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
		public Color setA(final float a) {
			this.a = a <= BodyColor.MAX_ALPHA ? (a + 1.0F) / 2.0F : BodyColor.MAX_ALPHA;
			return new Color(this.r, this.g, this.b, this.a);
		}
	}

	private static final long serialVersionUID = 846132440578478084L;
	private static final int MIN_BODY_SIZE = 1;
	private static final int N_BODIES = 20;
	private static final double DELTA_TIME = 1000.0;  // seconds
	private static final long VIEW_REFRESH_RATE = 1000;  // millis
	private static final int N_CYCLES = 1000000;
	private static final boolean CLEAR_TRACES = false;

	private static final double BODY_MIN_MASS = 100000.0;  // Kg
	private static final double BODY_MAX_MASS = 1000000.0;  // Kg
	private static final double BODY_MIN_SPEED = -0.000001;  // m/s
	private static final double BODY_MAX_SPEED = 0.000001;  // m/s

	private final JPanel pnlBodies = new JPanel();
	private final List<Body> bodies = new ArrayList<>();
	private final List<BodyColor> colors = new ArrayList<>();

	public SimulationView() {
		this.setup();
		this.init();

		new Thread(() -> {
			final Map<Body, Force> mapBF = new HashMap<>();
			for (int i = 0; i < SimulationView.N_CYCLES; i++) {
				mapBF.clear();
				for (final Body b : this.bodies) {
					mapBF.put(b, Force.sumForces(this.bodies.stream().filter(b1 -> !b1.equals(b))
							.map(b1 -> Force.get(b, b1)).toArray(Force[]::new)));
				}
				mapBF.keySet().stream().forEach(b -> {
					b.apply(mapBF.get(b), SimulationView.DELTA_TIME);
				});

				this.repaint();
				try {
					Thread.sleep(SimulationView.VIEW_REFRESH_RATE);
				} catch (final InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	private void setup() {
		this.setTitle("N-Bodies Simulation");
		final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.setSize(screenSize);
		this.setVisible(true);
		this.setExtendedState(Frame.MAXIMIZED_BOTH);
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				/*if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(SimulationView.this,
						"Do you really want to exit?", "Confirm Exit", JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE)) {*/
				SimulationView.this.dispose();
				System.exit(0);
				//}
			}
		});
		this.setLayout(new BorderLayout());

		this.add(this.pnlBodies, BorderLayout.CENTER);
		this.setBackground(Color.WHITE);
	}

	private void init() {
		final Random random = new Random();
		final OfDouble massGenerator = random.doubles(SimulationView.BODY_MIN_MASS, SimulationView.BODY_MAX_MASS)
				.iterator();
		final OfDouble positionXGenerator = random.doubles(0.0, this.getSize().getWidth()).iterator();
		final OfDouble positionYGenerator = random.doubles(0.0, this.getSize().getHeight()).iterator();
		final OfDouble speedGenerator = random.doubles(SimulationView.BODY_MIN_SPEED, SimulationView.BODY_MAX_SPEED)
				.iterator();

		this.bodies.clear();
		for (int i = 0; i < SimulationView.N_BODIES; i++) {
			this.bodies.add(new Body(massGenerator.nextDouble(),
					new Position(positionXGenerator.nextDouble(), positionYGenerator.nextDouble()),
					new Vector(speedGenerator.nextDouble(), speedGenerator.nextDouble())));
		}
	}

	@Override
	public void paint(final Graphics g) {
		final Graphics2D g2d = (Graphics2D) g;
		if (SimulationView.CLEAR_TRACES) {
			g2d.clearRect(0, 0, this.getSize().width, this.getSize().height);
		}
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		if (this.colors.isEmpty()) {
			for (int i = 0; i < this.bodies.size(); i++) {
				this.colors.add(new BodyColor());
			}
		}

		final double maxModuleSpeed = this.bodies.stream().mapToDouble(b -> b.getSpeed().getModule()).max()
				.getAsDouble();

		Body body;
		Position position;
		int size;
		for (int i = 0; i < this.bodies.size(); i++) {
			body = this.bodies.get(i);
			position = body.getPosition();
			size = (int) (SimulationView.MIN_BODY_SIZE * body.getMass() / SimulationView.BODY_MIN_MASS);
			g2d.setColor(this.colors.get(i).setA((float) (body.getSpeed().getModule() / maxModuleSpeed)));
			g2d.fillOval((int) Math.round(position.getX()), (int) Math.round(position.getY()), size, size);
		}
	}

	public static void main(final String[] args) {
		EventQueue.invokeLater(() -> {
			try {
				final SimulationView frame = new SimulationView();
				frame.setVisible(true);
			} catch (final Exception e) {
				e.printStackTrace();
			}
		});
	}

}
