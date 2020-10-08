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
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import control.DurationTracker;
import control.Strategy;
import control.StrategyBuilder;
import model.Body;
import model.Position;
import view.component.NBButton;
import view.component.NBCheckBox;
import view.component.NBComboBox;
import view.component.NBComboBoxItem;
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
	private final JLabel lblStrategy = new JLabel(SimulationView.LOC.getRes("lblStrategy"));
	private final NBComboBox<NBComboBoxItem<StrategyBuilder>, StrategyBuilder> cmbStrategy = new NBComboBox<>();
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
	private final JLabel lblDuration = new JLabel(SimulationView.LOC.getRes("lblDuration"));
	private final JLabel lblDurationCreateBodies = new JLabel(SimulationView.LOC.getRes("lblDurationCreateBodies"));
	private final JLabel lblDurationCalcForces = new JLabel(SimulationView.LOC.getRes("lblDurationCalcForces"));
	private final JLabel lblDurationMoveBodies = new JLabel(SimulationView.LOC.getRes("lblDurationMoveBodies"));
	private final JLabel lblDurationCalcMinMaxSpeed = new JLabel(
			SimulationView.LOC.getRes("lblDurationCalcMinMaxSpeed"));
	private final JLabel lblDurationCalcPaintings = new JLabel(SimulationView.LOC.getRes("lblDurationCalcPaintings"));
	private final JLabel lblDurationPaintBodies = new JLabel(SimulationView.LOC.getRes("lblDurationPaintBodies"));

	private final List<BodyColor> colors = new ArrayList<>();
	private BufferedImage pnlBodiesImg = null;
	private int refreshRate;

	private Strategy strategy = null;
	private Thread thread = null;
	private volatile boolean tStopped = false;
	private boolean forcePnlBodiesClear = false;
	private boolean debug;

	private volatile long durationCreateBodies;
	private volatile long durationCalcForces;
	private volatile long durationMoveBodies;
	private volatile long durationCalcMinMaxSpeed;
	private volatile long durationCalcPaintings;
	private volatile long durationPaintBodies;

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

				final List<Body> bodies = SimulationView.this.strategy != null
						? SimulationView.this.strategy.getBodies()
						: null;
				if (bodies == null || bodies.isEmpty()) {
					return;
				}

				DurationTracker dt = new DurationTracker("Calculate paintings").start();
				if (SimulationView.this.colors.isEmpty()) {
					for (int i = 0; i < bodies.size(); i++) {
						SimulationView.this.colors.add(new BodyColor());
					}
				}

				final double maxModuleSpeed = bodies.stream().parallel().mapToDouble(b -> b.getSpeed().getModule())
						.max().getAsDouble();

				Body body;
				Position position;
				int size;
				for (int i = 0; i < bodies.size(); i++) {
					body = bodies.get(i);
					position = body.getPosition();
					size = (int) (SimulationView.MIN_BODY_SIZE * body.getMass() / SimulationView.BODY_MIN_MASS);
					g2d.setColor(SimulationView.this.colors.get(i)
							.getNew((float) (body.getSpeed().getModule() / maxModuleSpeed)));
					g2d.fillOval((int) Math.round(position.getX()), (int) Math.round(position.getY()), size, size);
				}
				SimulationView.this.durationCalcPaintings = DurationTracker.toMillsDuration(dt.stop());

				dt = new DurationTracker("Paint bodies").start();
				g2d.dispose();
				super.paintComponent(g);
				((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				((Graphics2D) g).drawImage(SimulationView.this.pnlBodiesImg, null, 0, 0);
				SimulationView.this.durationPaintBodies = DurationTracker.toMillsDuration(dt.stop());
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
		this.pnlControl.add(this.lblStrategy);
		this.pnlControl.add(this.cmbStrategy);
		this.pnlControl.add(this.lblNBodies);
		this.pnlControl.add(this.txtNBodies);
		this.pnlControl.add(this.lblDeltaTime);
		this.pnlControl.add(this.txtDeltaTime);
		this.pnlControl.add(this.lblRefreshRate);
		this.pnlControl.add(this.txtRefreshRate);
		this.pnlControl.add(this.lblKey);
		this.pnlControl.add(this.lblKeySize);
		this.pnlControl.add(this.lblKeyColor);
		this.pnlControl.add(this.lblDuration);
		this.pnlControl.add(this.lblDurationCreateBodies);
		this.pnlControl.add(this.lblDurationCalcForces);
		this.pnlControl.add(this.lblDurationMoveBodies);
		this.pnlControl.add(this.lblDurationCalcMinMaxSpeed);
		this.pnlControl.add(this.lblDurationCalcPaintings);
		this.pnlControl.add(this.lblDurationPaintBodies);

		final int heightComp = 20;
		final int margin = heightComp + 10;
		final int heightButton = 30;
		final int marginButton = heightButton + 10;
		int y = 20;
		this.btnStart.setBounds(10, y, 120, heightButton);
		y += marginButton;
		this.btnStop.setBounds(10, y, 120, heightButton);
		y += marginButton;
		this.btnClear.setBounds(10, y, 120, heightButton);
		y += marginButton;
		this.chkClearTraces.setBounds(10, y, 120, heightButton);
		y += marginButton + 10;
		this.lblParams.setBounds(10, y, 190, heightComp);
		y += margin;
		this.lblStrategy.setBounds(10, y, 60, heightComp);
		this.cmbStrategy.setBounds(70, y, 120, heightComp);
		y += margin;
		this.lblNBodies.setBounds(10, y, 120, heightComp);
		this.txtNBodies.setBounds(130, y, 60, heightComp);
		y += margin;
		this.lblDeltaTime.setBounds(10, y, 120, heightComp);
		this.txtDeltaTime.setBounds(130, y, 60, heightComp);
		y += margin;
		this.lblRefreshRate.setBounds(10, y, 120, heightComp);
		this.txtRefreshRate.setBounds(130, y, 60, heightComp);
		y += marginButton;
		this.lblKey.setBounds(10, y, 190, heightComp);
		y += heightComp;
		this.lblKeySize.setBounds(10, y, 190, heightButton + heightComp);
		y += margin + margin;
		this.lblKeyColor.setBounds(10, y, 190, heightButton + heightComp);
		y += margin + marginButton;
		this.lblDuration.setBounds(10, y, 190, heightComp);
		y += heightComp;
		this.lblDurationCreateBodies.setBounds(10, y, 190, heightComp);
		y += heightComp;
		this.lblDurationCalcForces.setBounds(10, y, 190, heightComp);
		y += heightComp;
		this.lblDurationMoveBodies.setBounds(10, y, 190, heightComp);
		y += heightComp;
		this.lblDurationCalcMinMaxSpeed.setBounds(10, y, 190, heightComp);
		y += heightComp;
		this.lblDurationCalcPaintings.setBounds(10, y, 190, heightComp);
		y += heightComp;
		this.lblDurationPaintBodies.setBounds(10, y, 190, heightComp);

		this.chkClearTraces.setToolTipText(SimulationView.LOC.getRes("chkClearTracesToolTip"));
		this.lblParams.setForeground(new Color(0, 0, 139));
		this.lblParams.setToolTipText(SimulationView.LOC.getRes("lblParamsToolTip"));
		this.lblStrategy.setToolTipText(SimulationView.LOC.getRes("lblStrategyToolTip"));
		this.lblNBodies.setToolTipText(SimulationView.LOC.getRes("lblNBodiesToolTip"));
		this.lblDeltaTime.setToolTipText(SimulationView.LOC.getRes("lblDeltaTimeToolTip"));
		this.lblRefreshRate.setToolTipText(SimulationView.LOC.getRes("lblRefreshRateToolTip"));
		this.lblKey.setForeground(new Color(0, 0, 139));
		this.lblDuration.setForeground(new Color(0, 0, 139));

		this.btnStart.addActionListener(e -> this.btnStartActionPerformed());
		this.btnStop.addActionListener(e -> this.btnStopActionPerformed());
		this.btnClear.addActionListener(e -> this.btnClearActionPerformed());

		this.pack();
		this.setVisible(true);
	}

	private void btnStartActionPerformed() {
		if (this.strategy == null) {
			if (!this.checkParams()) {
				return;
			}

			final DurationTracker dt = new DurationTracker("Create bodies").start();
			this.strategy = StrategyBuilder.buildStrategy(this.cmbStrategy.getSelectedItemKey(),
					this.txtNBodies.getInt(), this.txtDeltaTime.getInt());
			this.strategy.createBodies(SimulationView.BODY_MIN_MASS, SimulationView.BODY_MAX_MASS,
					this.pnlBodies.getSize().getWidth(), this.pnlBodies.getSize().getHeight(),
					SimulationView.BODY_MIN_SPEED, SimulationView.BODY_MAX_SPEED);
			this.durationCreateBodies = DurationTracker.toMillsDuration(dt.stop());
			System.out.println();

			this.refreshRate = this.txtRefreshRate.getInt();
		}
		this.updateGraphics(true, false);
		this.start();
	}

	private void btnStopActionPerformed() {
		this.updateGraphics(false, false);
		if (this.thread != null) {
			this.tStopped = true;
			this.thread.interrupt();
			this.thread = null;
		}
	}

	private void btnClearActionPerformed() {
		this.updateGraphics(false, true);
		this.strategy = null;
		this.colors.clear();
		this.forcePnlBodiesClear = true;
		this.pnlBodies.repaint();
		this.repaint();
	}

	private void updateGraphics(final boolean started, final boolean cleared) {
		this.btnStart.setEnabled(!started);
		this.btnStop.setEnabled(started);
		this.btnClear.setEnabled(!started && !cleared);
		if (!started) {
			this.btnStart.setText(SimulationView.LOC.getRes(cleared ? "btnStart" : "btnResume"));
		}

		this.txtNBodies.setEnabled(!started && cleared);
		this.txtDeltaTime.setEnabled(!started && cleared);
		this.txtRefreshRate.setEnabled(!started && cleared);
		this.updateLabels(cleared);
	}

	private void updateLabels(final boolean cleared) {
		if (cleared) {
			this.lblKeyColor.setText(SimulationView.LOC.getRes("lblKeyColor", SimulationView.BODY_MIN_SPEED,
					SimulationView.BODY_MAX_SPEED));
			this.durationCalcForces = 0;
			this.durationMoveBodies = 0;
			this.durationCalcMinMaxSpeed = 0;
			this.durationCalcPaintings = 0;
			this.durationPaintBodies = 0;
		}
		this.lblDurationCreateBodies
				.setText(SimulationView.LOC.getRes("lblDurationCreateBodies", this.durationCreateBodies));
		this.lblDurationCalcForces.setText(SimulationView.LOC.getRes("lblDurationCalcForces", this.durationCalcForces));
		this.lblDurationMoveBodies.setText(SimulationView.LOC.getRes("lblDurationMoveBodies", this.durationMoveBodies));
		this.lblDurationCalcMinMaxSpeed
				.setText(SimulationView.LOC.getRes("lblDurationCalcMinMaxSpeed", this.durationCalcMinMaxSpeed));
		this.lblDurationCalcPaintings
				.setText(SimulationView.LOC.getRes("lblDurationCalcPaintings", this.durationCalcPaintings));
		this.lblDurationPaintBodies
				.setText(SimulationView.LOC.getRes("lblDurationPaintBodies", this.durationPaintBodies));
	}

	private void init() {
		this.updateGraphics(false, true);
		this.chkClearTraces.setSelected(SimulationView.CLEAR_TRACES);
		this.txtNBodies.setValue(SimulationView.N_BODIES);
		this.txtDeltaTime.setValue(SimulationView.DELTA_TIME);
		this.txtRefreshRate.setValue(SimulationView.REFRESH_RATE);

		this.cmbStrategy.addItems(StrategyBuilder.getComboItems());

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

	private void start() {
		this.tStopped = false;
		this.thread = new Thread(() -> {
			while (!this.tStopped) {
				try {
					DurationTracker dt = new DurationTracker("Calculate forces").start();
					if (this.strategy == null || this.tStopped) {
						return;
					}
					this.strategy.calculateForces();
					this.durationCalcForces = DurationTracker.toMillsDuration(dt.stop());

					dt = new DurationTracker("Move bodies").start();
					if (this.strategy == null || this.tStopped) {
						return;
					}
					this.strategy.moveBodies();
					this.durationMoveBodies = DurationTracker.toMillsDuration(dt.stop());

					this.pnlBodies.repaint();

					dt = new DurationTracker("Calculate bodies min/max speeds").start();
					if (this.strategy == null || this.tStopped) {
						return;
					}
					final List<Body> bodies = this.strategy.getBodies();
					this.lblKeyColor.setText(SimulationView.LOC.getRes("lblKeyColor",
							bodies.stream().mapToDouble(b -> b.getSpeed().getModule()).min().getAsDouble(),
							bodies.stream().mapToDouble(b -> b.getSpeed().getModule()).max().getAsDouble()));
					this.durationCalcMinMaxSpeed = DurationTracker.toMillsDuration(dt.stop());

					this.updateLabels(false);

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
