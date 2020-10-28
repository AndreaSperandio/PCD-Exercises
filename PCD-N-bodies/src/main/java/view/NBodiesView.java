package view;

import java.awt.BorderLayout;
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
import javax.swing.SwingUtilities;
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
import view.util.NBColor;
import view.util.NBLocalizator;
import view.util.NBMessage;
import view.util.NBResource;

public class NBodiesView extends JFrame {
	private static final long serialVersionUID = 846132440578478084L;

	private static final NBLocalizator LOC = new NBLocalizator(NBodiesView.class);
	private static final boolean DEBUG = true;

	private static final int MIN_BODY_SIZE = 1;
	private static final int N_BODIES = 5000;
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
	private final NBButton btnStart = new NBButton(NBodiesView.LOC.getRes("btnStart"), NBResource.getStartImage());
	private final NBButton btnStop = new NBButton(NBodiesView.LOC.getRes("btnStop"), NBResource.getStopImage());
	private final NBButton btnClear = new NBButton(NBodiesView.LOC.getRes("btnClear"), NBResource.getClearImage());
	private final NBCheckBox chkClearTraces = new NBCheckBox(NBodiesView.LOC.getRes("chkClearTraces"));
	private final JLabel lblParams = new JLabel(NBodiesView.LOC.getRes("lblParams"));
	private final JLabel lblStrategy = new JLabel(NBodiesView.LOC.getRes("lblStrategy"));
	private final NBComboBox<NBComboBoxItem<StrategyBuilder>, StrategyBuilder> cmbStrategy = new NBComboBox<>();
	private final JLabel lblNBodies = new JLabel(NBodiesView.LOC.getRes("lblNBodies"));
	private final NBIntTextField txtNBodies = new NBIntTextField();
	private final JLabel lblDeltaTime = new JLabel(NBodiesView.LOC.getRes("lblDeltaTime"));
	private final NBIntTextField txtDeltaTime = new NBIntTextField();
	private final JLabel lblRefreshRate = new JLabel(NBodiesView.LOC.getRes("lblRefreshRate"));
	private final NBIntTextField txtRefreshRate = new NBIntTextField();
	private final JLabel lblKey = new JLabel(NBodiesView.LOC.getRes("lblKey"));
	private final JLabel lblKeySize = new JLabel(
			NBodiesView.LOC.getRes("lblKeySize", NBodiesView.BODY_MIN_MASS, NBodiesView.BODY_MAX_MASS));
	private final JLabel lblKeyColor = new JLabel(
			NBodiesView.LOC.getRes("lblKeyColor", NBodiesView.BODY_MIN_SPEED, NBodiesView.BODY_MAX_SPEED));
	private final JLabel lblDuration = new JLabel(NBodiesView.LOC.getRes("lblDuration"));
	private final JLabel lblDurationCreateBodies = new JLabel(NBodiesView.LOC.getRes("lblDurationCreateBodies"));
	private final JLabel lblDurationCalcAndMove = new JLabel(NBodiesView.LOC.getRes("lblDurationCalcAndMove"));
	private final JLabel lblDurationCalcMinMaxSpeed = new JLabel(NBodiesView.LOC.getRes("lblDurationCalcMinMaxSpeed"));
	private final JLabel lblDurationCalcPaintings = new JLabel(NBodiesView.LOC.getRes("lblDurationCalcPaintings"));
	private final JLabel lblDurationPaintBodies = new JLabel(NBodiesView.LOC.getRes("lblDurationPaintBodies"));
	private final JLabel lblDurationTotal = new JLabel(NBodiesView.LOC.getRes("lblDurationTotal"));
	private final JLabel lblDurationTotalErr = new JLabel(NBodiesView.LOC.getRes("lblDurationTotalErr"));
	private final JLabel lblDurationMean = new JLabel(NBodiesView.LOC.getRes("lblDurationMean"));
	private final JLabel lblDurationMeanErr = new JLabel(NBodiesView.LOC.getRes("lblDurationMeanErr"));

	private final List<NBColor> colors = new ArrayList<>();
	private BufferedImage pnlBodiesImg = null;
	private int refreshRate;

	private Strategy strategy = null;
	private Thread thread = null;
	private volatile boolean tStopped = false;
	private boolean forcePnlBodiesClear = false;

	private volatile Long durationCreateBodies = null;
	private volatile Long durationCalcAndMove = null;
	private volatile Long durationCalcMinMaxSpeed = null;
	private volatile Long durationCalcPaintings = null;
	private volatile Long durationPaintBodies = null;
	private volatile Long durationTotal = null;
	private volatile Double durationMean = null;
	private volatile int cycles;

	public NBodiesView() {
		this.setup();
		this.init();
	}

	private void setup() {
		this.setTitle(NBodiesView.LOC.getRes("title"));
		final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.setSize(screenSize);
		this.setPreferredSize(screenSize);
		this.setExtendedState(Frame.MAXIMIZED_BOTH);
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			@SuppressWarnings("unused")
			@Override
			public void windowClosing(final WindowEvent e) {
				if (NBodiesView.DEBUG
						|| NBMessage.showConfirmWarnDialog(NBodiesView.this, NBodiesView.LOC.getRes("cnfExit"))) {
					NBodiesView.this.dispose();
					System.exit(0);
				}
			}
		});
		this.setLayout(new BorderLayout());

		this.pnlBodies = new JPanel() {
			private static final long serialVersionUID = -2832881083038073186L;

			@Override
			public void paintComponent(final Graphics g) {
				if (NBodiesView.this.chkClearTraces.isSelected() || NBodiesView.this.forcePnlBodiesClear) {
					((Graphics2D) g).clearRect(0, 0, this.getWidth(), this.getHeight());
					NBodiesView.this.pnlBodiesImg = null;
					NBodiesView.this.forcePnlBodiesClear = false;
				}

				if (NBodiesView.this.pnlBodiesImg == null) {
					NBodiesView.this.pnlBodiesImg = new BufferedImage(this.getWidth(), this.getHeight(),
							BufferedImage.TYPE_INT_ARGB);
				}
				final Graphics2D g2d = (Graphics2D) NBodiesView.this.pnlBodiesImg.getGraphics();
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

				final List<Body> bodies = NBodiesView.this.strategy != null ? NBodiesView.this.strategy.getBodies()
						: null;
				if (bodies == null || bodies.isEmpty()) {
					return;
				}

				DurationTracker dt = new DurationTracker("Calculate paintings").start();
				if (NBodiesView.this.colors.isEmpty()) {
					for (int i = 0; i < bodies.size(); i++) {
						NBodiesView.this.colors.add(new NBColor());
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
					size = (int) (NBodiesView.MIN_BODY_SIZE * body.getMass() / NBodiesView.BODY_MIN_MASS);
					g2d.setColor(NBodiesView.this.colors.get(i)
							.getNew((float) (body.getSpeed().getModule() / maxModuleSpeed)));
					g2d.fillOval((int) Math.round(position.getX()), (int) Math.round(position.getY()), size, size);
				}
				NBodiesView.this.durationCalcPaintings = DurationTracker.toMillsDuration(dt.stop(NBodiesView.DEBUG));

				dt = new DurationTracker("Paint bodies").start();
				g2d.dispose();
				super.paintComponent(g);
				((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				((Graphics2D) g).drawImage(NBodiesView.this.pnlBodiesImg, null, 0, 0);
				NBodiesView.this.durationPaintBodies = DurationTracker.toMillsDuration(dt.stop(NBodiesView.DEBUG));
				if (NBodiesView.DEBUG) {
					System.out.println();
				}
			}
		};
		this.add(this.pnlBodies, BorderLayout.CENTER);
		final Dimension pnlBodiesDim = new Dimension((int) screenSize.getWidth() - NBodiesView.PNL_CONTROL_WIDTH,
				(int) screenSize.getHeight());
		this.pnlBodies.setSize(pnlBodiesDim);
		this.pnlBodies.setPreferredSize(pnlBodiesDim);
		this.pnlBodies.setBackground(NBColor.PNL_WHITE);
		this.pnlBodies.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, NBColor.PNL_BORDER));

		this.add(this.pnlControl, BorderLayout.EAST);
		final Dimension pnlControlDim = new Dimension(NBodiesView.PNL_CONTROL_WIDTH, (int) screenSize.getHeight());
		this.pnlControl.setSize(pnlControlDim);
		this.pnlControl.setPreferredSize(pnlControlDim);
		this.pnlControl.setBorder(BorderFactory.createMatteBorder(1, 1, 0, 0, NBColor.PNL_BORDER));
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
		this.pnlControl.add(this.lblDurationCalcAndMove);
		this.pnlControl.add(this.lblDurationCalcMinMaxSpeed);
		this.pnlControl.add(this.lblDurationCalcPaintings);
		this.pnlControl.add(this.lblDurationPaintBodies);
		this.pnlControl.add(this.lblDurationTotal);
		this.pnlControl.add(this.lblDurationTotalErr);
		this.pnlControl.add(this.lblDurationMean);
		this.pnlControl.add(this.lblDurationMeanErr);

		final int heightComp = 20;
		final int margin = heightComp + 10;
		final int heightButton = 30;
		final int marginButton = heightButton + 10;
		int y = 10;
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
		this.lblDurationCalcAndMove.setBounds(10, y, 190, heightComp);
		y += heightComp;
		this.lblDurationCalcMinMaxSpeed.setBounds(10, y, 190, heightComp);
		y += heightComp;
		this.lblDurationCalcPaintings.setBounds(10, y, 190, heightComp);
		y += heightComp;
		this.lblDurationPaintBodies.setBounds(10, y, 190, heightComp);
		y += heightComp;
		this.lblDurationTotal.setBounds(10, y, 85, heightComp);
		this.lblDurationTotalErr.setBounds(95, y, 90, heightComp);
		y += heightComp;
		this.lblDurationMean.setBounds(10, y, 85, heightComp);
		this.lblDurationMeanErr.setBounds(95, y, 90, heightComp);

		this.chkClearTraces.setToolTipText(NBodiesView.LOC.getRes("chkClearTracesToolTip"));
		this.lblParams.setToolTipText(NBodiesView.LOC.getRes("lblParamsToolTip"));
		this.lblStrategy.setToolTipText(NBodiesView.LOC.getRes("lblStrategyToolTip"));
		this.lblNBodies.setToolTipText(NBodiesView.LOC.getRes("lblNBodiesToolTip"));
		this.lblDeltaTime.setToolTipText(NBodiesView.LOC.getRes("lblDeltaTimeToolTip"));
		this.lblRefreshRate.setToolTipText(NBodiesView.LOC.getRes("lblRefreshRateToolTip"));

		this.lblParams.setForeground(NBColor.LBL_BLUE);
		this.lblKey.setForeground(NBColor.LBL_BLUE);
		this.lblDuration.setForeground(NBColor.LBL_BLUE);
		this.lblDurationCreateBodies.setForeground(NBColor.LBL_LIGHT_GREY);
		this.lblDurationTotalErr.setForeground(NBColor.LBL_RED);
		this.lblDurationMeanErr.setForeground(NBColor.LBL_RED);

		this.btnStart.addActionListener(e -> this.btnStartActionPerformed());
		this.btnStop.addActionListener(e -> this.btnStopActionPerformed());
		this.btnClear.addActionListener(e -> this.btnClearActionPerformed());
	}

	private void init() {
		this.updateGraphics(false, true);
		this.chkClearTraces.setSelected(NBodiesView.CLEAR_TRACES);
		this.txtNBodies.setValue(NBodiesView.N_BODIES);
		this.txtDeltaTime.setValue(NBodiesView.DELTA_TIME);
		this.txtRefreshRate.setValue(NBodiesView.REFRESH_RATE);
		this.cmbStrategy.addItems(StrategyBuilder.getComboItems());

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
			this.strategy.createBodies(NBodiesView.BODY_MIN_MASS, NBodiesView.BODY_MAX_MASS,
					this.pnlBodies.getSize().getWidth(), this.pnlBodies.getSize().getHeight(),
					NBodiesView.BODY_MIN_SPEED, NBodiesView.BODY_MAX_SPEED);
			this.durationCreateBodies = DurationTracker.toMillsDuration(dt.stop(NBodiesView.DEBUG));
			if (NBodiesView.DEBUG) {
				System.out.println();
			}

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
		this.strategy.clear();
		this.strategy = null;
		this.colors.clear();
		this.forcePnlBodiesClear = true;

		// btnClearActionPerformed() is already executed by the EDT -> can't call invokeAndWait
		SwingUtilities.invokeLater(() -> {
			this.pnlBodies.repaint();
			this.repaint();
		});
	}

	private void updateGraphics(final boolean started, final boolean cleared) {
		this.btnStart.setEnabled(!started);
		this.btnStop.setEnabled(started);
		this.btnClear.setEnabled(!started && !cleared);
		if (!started) {
			this.btnStart.setText(NBodiesView.LOC.getRes(cleared ? "btnStart" : "btnResume"));
		}

		this.cmbStrategy.setEnabled(!started && cleared);
		this.txtNBodies.setEnabled(!started && cleared);
		this.txtDeltaTime.setEnabled(!started && cleared);
		this.txtRefreshRate.setEnabled(!started && cleared);
		this.updateLabels(cleared);
	}

	private void updateLabels(final boolean cleared) {
		this.durationTotal = 0L;

		if (cleared) {
			this.lblKeyColor.setText(
					NBodiesView.LOC.getRes("lblKeyColor", NBodiesView.BODY_MIN_SPEED, NBodiesView.BODY_MAX_SPEED));
			this.durationCreateBodies = null;
			this.durationCalcAndMove = null;
			this.durationCalcMinMaxSpeed = null;
			this.durationCalcPaintings = null;
			this.durationPaintBodies = null;
			this.durationMean = 0D;
			this.cycles = 0;
		} else {
			//durationTotal += this.durationCreateBodies != null ? this.durationCreateBodies : 0L;
			this.durationTotal += this.durationCalcAndMove != null ? this.durationCalcAndMove : 0L;
			this.durationTotal += this.durationCalcMinMaxSpeed != null ? this.durationCalcMinMaxSpeed : 0L;
			this.durationTotal += this.durationCalcPaintings != null ? this.durationCalcPaintings : 0L;
			this.durationTotal += this.durationPaintBodies != null ? this.durationPaintBodies : 0L;
			this.durationMean = this.durationMean == 0D ? this.durationTotal
					: (this.durationMean * this.cycles + this.durationTotal) / (this.cycles + 1);
			this.cycles++;
		}

		this.lblDurationCreateBodies
				.setText(NBodiesView.LOC.getRes("lblDurationCreateBodies", this.durationCreateBodies));
		this.lblDurationCalcAndMove.setText(NBodiesView.LOC.getRes("lblDurationCalcAndMove", this.durationCalcAndMove));
		this.lblDurationCalcMinMaxSpeed
				.setText(NBodiesView.LOC.getRes("lblDurationCalcMinMaxSpeed", this.durationCalcMinMaxSpeed));
		this.lblDurationCalcPaintings
				.setText(NBodiesView.LOC.getRes("lblDurationCalcPaintings", this.durationCalcPaintings));
		this.lblDurationPaintBodies.setText(NBodiesView.LOC.getRes("lblDurationPaintBodies", this.durationPaintBodies));
		this.lblDurationTotal.setText(NBodiesView.LOC.getRes("lblDurationTotal",
				this.durationTotal == 0L ? (Long) null : this.durationTotal));
		this.lblDurationMean.setText(NBodiesView.LOC.getRes("lblDurationMean",
				this.durationMean == 0D ? (Long) null : (Long) this.durationMean.longValue()));

		this.lblDurationTotal.setForeground(this.durationTotal > this.refreshRate ? NBColor.LBL_RED : NBColor.LBL_GREY);
		this.lblDurationTotalErr.setVisible(this.durationTotal > this.refreshRate);
		this.lblDurationMean.setForeground(this.durationMean > this.refreshRate ? NBColor.LBL_RED : NBColor.LBL_GREY);
		this.lblDurationMeanErr.setVisible(this.durationMean > this.refreshRate);
	}

	private boolean checkParams() {
		if (this.txtNBodies.getInt() <= 0) {
			NBMessage.showErrDialog(this, this.lblNBodies.getText() + " " + NBodiesView.LOC.getRes("errNegativeValue"));
			return false;
		}

		if (this.txtDeltaTime.getInt() <= 0) {
			NBMessage.showErrDialog(this,
					this.lblDeltaTime.getText() + " " + NBodiesView.LOC.getRes("errNegativeValue"));
			return false;
		}

		if (this.txtRefreshRate.getInt() <= 0) {
			NBMessage.showErrDialog(this,
					this.lblRefreshRate.getText() + " " + NBodiesView.LOC.getRes("errNegativeValue"));
			return false;
		}
		return true;
	}

	private void start() {
		this.tStopped = false;
		this.thread = new NBVThread();
		this.thread.start();
	}

	private class NBVThread extends Thread {
		@Override
		public void run() {
			while (!NBodiesView.this.tStopped) {
				try {
					DurationTracker dt = new DurationTracker("Calculate & Move").start();
					if (NBodiesView.this.strategy == null || NBodiesView.this.tStopped) {
						return;
					}
					NBodiesView.this.strategy.calculateAndMove();
					NBodiesView.this.durationCalcAndMove = DurationTracker.toMillsDuration(dt.stop(NBodiesView.DEBUG));

					dt = new DurationTracker("Calculate bodies min/max speeds").start();
					if (NBodiesView.this.strategy == null || NBodiesView.this.tStopped) {
						return;
					}
					final List<Body> bodies = NBodiesView.this.strategy.getBodies();
					NBodiesView.this.lblKeyColor.setText(NBodiesView.LOC.getRes("lblKeyColor",
							bodies.stream().mapToDouble(b -> b.getSpeed().getModule()).min().getAsDouble(),
							bodies.stream().mapToDouble(b -> b.getSpeed().getModule()).max().getAsDouble()));
					NBodiesView.this.durationCalcMinMaxSpeed = DurationTracker
							.toMillsDuration(dt.stop(NBodiesView.DEBUG));

					// Will cause a EDT call, it might mess the durationTotal value a bit up, but it' not significant
					SwingUtilities.invokeLater(() -> {
						NBodiesView.this.pnlBodies.repaint();
					});
					NBodiesView.this.updateLabels(false);

					if (NBodiesView.this.durationTotal < NBodiesView.this.refreshRate) {
						Thread.sleep(NBodiesView.this.refreshRate - NBodiesView.this.durationTotal);
					}
				} catch (@SuppressWarnings("unused") final InterruptedException e) {
					if (NBodiesView.this.strategy != null) {
						NBodiesView.this.strategy.interrupt();
					}
					continue;
				}
			}
		}

		@Override
		public void interrupt() {
			if (NBodiesView.this.strategy != null) {
				NBodiesView.this.strategy.interrupt();
			}
			super.interrupt();
		}
	}

	public static void main(final String[] args) {
		EventQueue.invokeLater(() -> {
			try {
				final NBodiesView frame = new NBodiesView();
				frame.setVisible(true);
			} catch (final Exception e) {
				e.printStackTrace();
			}
		});
	}
}
