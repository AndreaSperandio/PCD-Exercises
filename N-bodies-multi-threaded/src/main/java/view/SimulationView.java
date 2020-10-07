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
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PrimitiveIterator.OfDouble;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import model.Body;
import model.DurationTracker;
import model.Force;
import model.Position;
import model.Vector;
import view.component.NBButton;
import view.component.NBCheckBox;
import view.component.NBIntTextField;
import view.util.BodyColor;
import view.util.NBLocalizator;
import view.util.NBMessage;
import view.util.NBResource;

public class SimulationView extends JFrame {
	private static final long serialVersionUID = 846132440578478084L;

	private static final NBLocalizator LOC = new NBLocalizator(SimulationView.class);

	private static final int MIN_BODY_SIZE = 1;
	private static final int N_BODIES = 20;
	private static final int DELTA_TIME = 10000;  // seconds
	private static final int REFRESH_RATE = 1000;  // millis
	private static final boolean CLEAR_TRACES = true;

	private static final double BODY_MIN_MASS = 100000.0;  // Kg
	private static final double BODY_MAX_MASS = 1000000.0;  // Kg
	private static final double BODY_MIN_SPEED = -0.000001;  // m/s
	private static final double BODY_MAX_SPEED = 0.000001;  // m/s

	private static final int PNL_CONTROL_WIDTH = 200;

	private JPanel pnlBodies;
	private final JPanel pnlControl = new JPanel();
	private final NBButton btnStart = new NBButton(SimulationView.LOC.getRes("btnStart"), NBResource.getStartImage());
	private final NBButton btnStop = new NBButton(SimulationView.LOC.getRes("btnStop"), NBResource.getStopImage());
	private final NBButton btnClear = new NBButton(SimulationView.LOC.getRes("btnClear"), NBResource.getClearImage());
	private final NBCheckBox chkClearTraces = new NBCheckBox(SimulationView.LOC.getRes("chkClearTraces"));
	private final JLabel lblParams = new JLabel(SimulationView.LOC.getRes("lblParams"));
	private final JLabel lblNBodies = new JLabel(SimulationView.LOC.getRes("lblNBodies"));
	private final NBIntTextField txtNBodies = new NBIntTextField();
	private final JLabel lblDeltaTime = new JLabel(SimulationView.LOC.getRes("lblDeltaTime"));
	private final NBIntTextField txtDeltaTime = new NBIntTextField();
	private final JLabel lblRefreshRate = new JLabel(SimulationView.LOC.getRes("lblRefreshRate"));
	private final NBIntTextField txtRefreshRate = new NBIntTextField();
	private final JLabel lblKey = new JLabel(SimulationView.LOC.getRes("lblKey"));
	private final JLabel lblKeySize = new JLabel(
			SimulationView.LOC.getRes("lblKeySize", SimulationView.BODY_MIN_MASS, SimulationView.BODY_MAX_MASS));
	private final JLabel lblKeyColor = new JLabel(
			SimulationView.LOC.getRes("lblKeyColor", SimulationView.BODY_MIN_SPEED, SimulationView.BODY_MAX_SPEED));

	private final List<Body> bodies = new ArrayList<>();
	private final List<BodyColor> colors = new ArrayList<>();
	private int deltaTime;
	private int refreshRate;

	private Thread thread = null;
	private volatile boolean tStopped = false;
	private boolean forcePnlBodiesClear = false;
	private boolean debug;

	private BufferedImage pnlBodiesImg = null;

	public SimulationView() {
		this.setup();
		this.init();
	}

	private void setup() {
		this.setTitle(SimulationView.LOC.getRes("title"));
		final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.setSize(screenSize);
		this.setPreferredSize(screenSize);
		this.setExtendedState(Frame.MAXIMIZED_BOTH);
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				if (SimulationView.this.debug
						|| NBMessage.showConfirmWarnDialog(SimulationView.this, SimulationView.LOC.getRes("cnfExit"))) {
					SimulationView.this.dispose();
					System.exit(0);
				}
			}
		});
		this.setLayout(new BorderLayout());

		this.pnlBodies = new JPanel() {
			private static final long serialVersionUID = -2832881083038073186L;

			@Override
			public void paintComponent(final Graphics g) {
				if (SimulationView.this.chkClearTraces.isSelected() || SimulationView.this.forcePnlBodiesClear) {
					((Graphics2D) g).clearRect(0, 0, this.getWidth(), this.getHeight());
					SimulationView.this.pnlBodiesImg = null;
					SimulationView.this.forcePnlBodiesClear = false;
				}

				if (SimulationView.this.pnlBodiesImg == null) {
					SimulationView.this.pnlBodiesImg = new BufferedImage(this.getWidth(), this.getHeight(),
							BufferedImage.TYPE_INT_ARGB);
				}
				final Graphics2D g2d = (Graphics2D) SimulationView.this.pnlBodiesImg.getGraphics();
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

				if (SimulationView.this.bodies.isEmpty()) {
					return;
				}

				DurationTracker dt = new DurationTracker("Calculate paintings").start();
				if (SimulationView.this.colors.isEmpty()) {
					for (int i = 0; i < SimulationView.this.bodies.size(); i++) {
						SimulationView.this.colors.add(new BodyColor());
					}
				}

				final double maxModuleSpeed = SimulationView.this.bodies.stream()
						.mapToDouble(b -> b.getSpeed().getModule()).max().getAsDouble();

				Body body;
				Position position;
				int size;
				for (int i = 0; i < SimulationView.this.bodies.size(); i++) {
					body = SimulationView.this.bodies.get(i);
					position = body.getPosition();
					size = (int) (SimulationView.MIN_BODY_SIZE * body.getMass() / SimulationView.BODY_MIN_MASS);
					g2d.setColor(SimulationView.this.colors.get(i)
							.getNew((float) (body.getSpeed().getModule() / maxModuleSpeed)));
					g2d.fillOval((int) Math.round(position.getX()), (int) Math.round(position.getY()), size, size);
				}
				dt.stop();

				dt = new DurationTracker("Paint bodies").start();
				g2d.dispose();
				super.paintComponent(g);
				((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				((Graphics2D) g).drawImage(SimulationView.this.pnlBodiesImg, null, 0, 0);
				dt.stop();
				System.out.println();
			}
		};
		this.add(this.pnlBodies, BorderLayout.CENTER);
		final Dimension pnlBodiesDim = new Dimension((int) screenSize.getWidth() - SimulationView.PNL_CONTROL_WIDTH,
				(int) screenSize.getHeight());
		this.pnlBodies.setSize(pnlBodiesDim);
		this.pnlBodies.setPreferredSize(pnlBodiesDim);
		this.pnlBodies.setBackground(Color.WHITE);
		this.pnlBodies.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.DARK_GRAY));

		this.add(this.pnlControl, BorderLayout.EAST);
		final Dimension pnlControlDim = new Dimension(SimulationView.PNL_CONTROL_WIDTH, (int) screenSize.getHeight());
		this.pnlControl.setSize(pnlControlDim);
		this.pnlControl.setPreferredSize(pnlControlDim);
		this.pnlControl.setBorder(BorderFactory.createMatteBorder(1, 1, 0, 0, Color.DARK_GRAY));
		this.pnlControl.setLayout(null);
		this.pnlControl.add(this.btnStart);
		this.pnlControl.add(this.btnStop);
		this.pnlControl.add(this.btnClear);
		this.pnlControl.add(this.chkClearTraces);
		this.pnlControl.add(this.lblParams);
		this.pnlControl.add(this.lblNBodies);
		this.pnlControl.add(this.txtNBodies);
		this.pnlControl.add(this.lblDeltaTime);
		this.pnlControl.add(this.txtDeltaTime);
		this.pnlControl.add(this.lblRefreshRate);
		this.pnlControl.add(this.txtRefreshRate);
		this.pnlControl.add(this.lblKey);
		this.pnlControl.add(this.lblKeySize);
		this.pnlControl.add(this.lblKeyColor);

		final int heightComp = 30;
		final int heightMargin = heightComp + 10;
		int y = 20;
		this.btnStart.setBounds(10, y, 120, heightComp);
		y += heightMargin;
		this.btnStop.setBounds(10, y, 120, heightComp);
		y += heightMargin;
		this.btnClear.setBounds(10, y, 120, heightComp);
		y += heightMargin;
		this.chkClearTraces.setBounds(10, y, 120, heightComp);
		y += heightMargin * 2;
		this.lblParams.setBounds(10, y, 190, heightComp);
		y += heightMargin + 10;
		this.lblNBodies.setBounds(10, y, 120, heightComp);
		this.txtNBodies.setBounds(130, y, 60, heightComp);
		y += heightMargin;
		this.lblDeltaTime.setBounds(10, y, 120, heightComp);
		this.txtDeltaTime.setBounds(130, y, 60, heightComp);
		y += heightMargin;
		this.lblRefreshRate.setBounds(10, y, 120, heightComp);
		this.txtRefreshRate.setBounds(130, y, 60, heightComp);
		y += heightMargin * 2;
		this.lblKey.setBounds(10, y, 190, heightComp);
		y += heightMargin + 10;
		this.lblKeySize.setBounds(10, y, 190, heightComp * 2);
		y += heightComp * 2 + 10;
		this.lblKeyColor.setBounds(10, y, 190, heightComp * 2);

		this.chkClearTraces.setToolTipText(SimulationView.LOC.getRes("chkClearTracesToolTip"));
		this.lblParams.setForeground(new Color(0, 0, 139));
		this.lblParams.setToolTipText(SimulationView.LOC.getRes("lblParamsToolTip"));
		this.lblNBodies.setToolTipText(SimulationView.LOC.getRes("lblNBodiesToolTip"));
		this.lblDeltaTime.setToolTipText(SimulationView.LOC.getRes("lblDeltaTimeToolTip"));
		this.lblRefreshRate.setToolTipText(SimulationView.LOC.getRes("lblRefreshRateToolTip"));
		this.lblKey.setForeground(new Color(0, 0, 139));

		this.btnStart.addActionListener(e -> this.btnStartActionPerformed());
		this.btnStop.addActionListener(e -> this.btnStopActionPerformed());
		this.btnClear.addActionListener(e -> this.btnClearActionPerformed());

		this.pack();
		this.setVisible(true);
	}

	private void btnStartActionPerformed() {
		if (this.bodies.isEmpty()) {
			if (!this.checkParams()) {
				return;
			}
			this.createBodies();
			this.deltaTime = this.txtDeltaTime.getInt();
			this.refreshRate = this.txtRefreshRate.getInt();
		}
		this.updateButtonStatus(true, false);
		this.start();
	}

	private void btnStopActionPerformed() {
		this.updateButtonStatus(false, false);
		if (this.thread != null) {
			this.tStopped = true;
			this.thread.interrupt();
			this.thread = null;
		}
	}

	private void btnClearActionPerformed() {
		this.updateButtonStatus(false, true);
		this.bodies.clear();
		this.colors.clear();
		this.forcePnlBodiesClear = true;
		this.pnlBodies.repaint();
		this.lblKeyColor.setText(
				SimulationView.LOC.getRes("lblKeyColor", SimulationView.BODY_MIN_SPEED, SimulationView.BODY_MAX_SPEED));
		this.repaint();
	}

	private void updateButtonStatus(final boolean started, final boolean cleared) {
		this.btnStart.setEnabled(!started);
		this.btnStop.setEnabled(started);
		this.btnClear.setEnabled(!started && !cleared);
		if (!started) {
			this.btnStart.setText(SimulationView.LOC.getRes(cleared ? "btnStart" : "btnResume"));
		}
	}

	private void init() {
		this.updateButtonStatus(false, true);
		this.chkClearTraces.setSelected(SimulationView.CLEAR_TRACES);
		this.txtNBodies.setValue(SimulationView.N_BODIES);
		this.txtDeltaTime.setValue(SimulationView.DELTA_TIME);
		this.txtRefreshRate.setValue(SimulationView.REFRESH_RATE);

		this.debug = true;
	}

	private boolean checkParams() {
		if (this.txtNBodies.getInt() <= 0) {
			NBMessage.showErrDialog(this,
					this.lblNBodies.getText() + " " + SimulationView.LOC.getRes("errNegativeValue"));
			return false;
		}

		if (this.txtDeltaTime.getInt() <= 0) {
			NBMessage.showErrDialog(this,
					this.lblDeltaTime.getText() + " " + SimulationView.LOC.getRes("errNegativeValue"));
			return false;
		}

		if (this.txtRefreshRate.getInt() <= 0) {
			NBMessage.showErrDialog(this,
					this.lblRefreshRate.getText() + " " + SimulationView.LOC.getRes("errNegativeValue"));
			return false;
		}
		return true;
	}

	private void createBodies() {
		final DurationTracker dt = new DurationTracker("Create bodies").start();
		final Random random = new Random();
		final OfDouble massGenerator = random.doubles(SimulationView.BODY_MIN_MASS, SimulationView.BODY_MAX_MASS)
				.iterator();
		final OfDouble positionXGenerator = random.doubles(0.0, this.pnlBodies.getSize().getWidth()).iterator();
		final OfDouble positionYGenerator = random.doubles(0.0, this.pnlBodies.getSize().getHeight()).iterator();
		final OfDouble speedGenerator = random.doubles(SimulationView.BODY_MIN_SPEED, SimulationView.BODY_MAX_SPEED)
				.iterator();

		for (int i = 0; i < this.txtNBodies.getInt(); i++) {
			this.bodies.add(new Body(massGenerator.nextDouble(),
					new Position(positionXGenerator.nextDouble(), positionYGenerator.nextDouble()),
					new Vector(speedGenerator.nextDouble(), speedGenerator.nextDouble())));
		}
		dt.stop();
		System.out.println();
	}

	private void start() {
		this.tStopped = false;
		this.thread = new Thread(() -> {
			final Map<Body, Force> mapBF = new HashMap<>();
			while (!this.tStopped) {
				try {
					DurationTracker dt = new DurationTracker("Calculate forces").start();
					mapBF.clear();
					for (final Body b : this.bodies) {
						mapBF.put(b, Force.sumForces(this.bodies.stream().filter(b1 -> !b1.equals(b))
								.map(b1 -> Force.get(b, b1)).toArray(Force[]::new)));
					}
					dt.stop();

					dt = new DurationTracker("Move bodies").start();
					mapBF.keySet().stream().forEach(b -> {
						b.apply(mapBF.get(b), this.deltaTime);
					});
					dt.stop();

					this.pnlBodies.repaint();

					dt = new DurationTracker("Calculate bodies min/max speeds").start();
					this.lblKeyColor.setText(SimulationView.LOC.getRes("lblKeyColor",
							this.bodies.stream().mapToDouble(b -> b.getSpeed().getModule()).min().getAsDouble(),
							this.bodies.stream().mapToDouble(b -> b.getSpeed().getModule()).max().getAsDouble()));
					dt.stop();

					Thread.sleep(this.refreshRate);
				} catch (@SuppressWarnings("unused") final InterruptedException e) {
					continue;
				}
			}
		});
		this.thread.start();
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
