package view;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

import control.AvgTempStream;
import control.TempStream;
import io.reactivex.rxjava3.core.Observable;
import view.component.TMButton;
import view.util.TMColor;
import view.util.TMLocalizator;
import view.util.TMMessage;
import view.util.TMResource;

public class TempMonitorView extends JFrame {
	private static final long serialVersionUID = 846132440578478084L;

	private static final TMLocalizator LOC = new TMLocalizator(TempMonitorView.class);
	private static final boolean DEBUG = true;
	private static final Font INCREASED_FONT;

	static {
		final Font font = new JLabel().getFont();
		INCREASED_FONT = new Font(font.getFamily(), font.getStyle(), 15);
	}

	private final TempStreamParams tempStreamParams1 = new TempStreamParams("1");
	private final TempStreamParams tempStreamParams2 = new TempStreamParams("2");
	private final TempStreamParams tempStreamParams3 = new TempStreamParams("3");

	private final TMButton btnStart = new TMButton(TempMonitorView.LOC.getRes("btnStart"), TMResource.getStartImage());
	private final TMButton btnStop = new TMButton(TempMonitorView.LOC.getRes("btnStop"), TMResource.getStopImage());
	private final TMButton btnReset = new TMButton(TempMonitorView.LOC.getRes("btnReset"), TMResource.getResetImage());

	private final JLabel lblTemperature = new JLabel(TempMonitorView.LOC.getRes("lblTemperature").toUpperCase());
	private final JLabel lblMin = new JLabel(TempMonitorView.LOC.getRes("lblMin"));
	private final JLabel lblMinValue = new JLabel();
	private final JLabel lblCurrent = new JLabel(TempMonitorView.LOC.getRes("lblCurrent"));
	private final JLabel lblCurrentValue = new JLabel();
	private final JLabel lblMax = new JLabel(TempMonitorView.LOC.getRes("lblMax"));
	private final JLabel lblMaxValue = new JLabel();

	private Thread thread = null;
	private volatile boolean tStopped = false;
	private volatile boolean tPaused = false;

	private volatile Double minValue = null;
	private volatile Double maxValue = null;

	public TempMonitorView() {
		this.setup();
		this.init();
	}

	private void setup() {
		this.setTitle(TempMonitorView.LOC.getRes("title"));
		final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.setSize(screenSize);
		this.setPreferredSize(screenSize);
		this.setExtendedState(Frame.MAXIMIZED_BOTH);
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			@SuppressWarnings("unused")
			@Override
			public void windowClosing(final WindowEvent e) {
				if (TempMonitorView.DEBUG || TMMessage.showConfirmWarnDialog(TempMonitorView.this,
						TempMonitorView.LOC.getRes("cnfExit"))) {
					TempMonitorView.this.dispose();
					System.exit(0);
				}
			}
		});
		this.setLayout(null);

		this.add(this.tempStreamParams1);
		this.add(this.tempStreamParams2);
		this.add(this.tempStreamParams3);

		final int heighParams = 70;
		int y = 10;
		this.tempStreamParams1.setBounds(10, y, 775, heighParams);
		y += heighParams;
		this.tempStreamParams2.setBounds(10, y, 775, heighParams);
		y += heighParams;
		this.tempStreamParams3.setBounds(10, y, 775, heighParams);

		this.add(this.btnStart);
		this.add(this.btnStop);
		this.add(this.btnReset);

		final int heighButton = 30;
		final int marginButton = heighButton + 10;
		y = 20;
		this.btnStart.setBounds(850, y, 120, heighButton);
		y += marginButton;
		this.btnStop.setBounds(850, y, 120, heighButton);
		y += marginButton;
		this.btnReset.setBounds(850, y, 120, heighButton);

		this.btnStart.addActionListener(e -> this.btnStartActionPerformed());
		this.btnStop.addActionListener(e -> this.btnStopActionPerformed());
		this.btnReset.addActionListener(e -> this.btnClearActionPerformed());

		this.add(this.lblTemperature);
		this.add(this.lblMin);
		this.add(this.lblMinValue);
		this.add(this.lblCurrent);
		this.add(this.lblCurrentValue);
		this.add(this.lblMax);
		this.add(this.lblMaxValue);

		final int heighComp = 30;
		final int marginComp = heighComp + 10;
		y = heighParams * 3 + marginComp;
		this.lblTemperature.setBounds(340, y, 200, heighComp);
		y += marginComp;
		this.lblMin.setBounds(240, y, 120, heighComp);
		this.lblCurrent.setBounds(390, y, 120, heighComp);
		this.lblMax.setBounds(540, y, 120, heighComp);
		y += marginComp;
		this.lblMinValue.setBounds(240, y, 120, heighComp);
		this.lblCurrentValue.setBounds(390, y, 120, heighComp);
		this.lblMaxValue.setBounds(540, y, 120, heighComp);

		this.lblMin.setToolTipText(TempMonitorView.LOC.getRes("lblMinToolTip"));
		this.lblCurrent.setToolTipText(TempMonitorView.LOC.getRes("lblCurrentToolTip"));
		this.lblMax.setToolTipText(TempMonitorView.LOC.getRes("lblMaxToolTip"));

		this.lblTemperature.setForeground(TMColor.LBL_BLUE);
		this.lblMinValue.setForeground(TMColor.LBL_BLUE);
		this.lblCurrentValue.setForeground(TMColor.LBL_BLUE);
		this.lblMaxValue.setForeground(TMColor.LBL_BLUE);

		this.lblTemperature.setFont(TempMonitorView.INCREASED_FONT);
		this.lblMin.setFont(TempMonitorView.INCREASED_FONT);
		this.lblMinValue.setFont(TempMonitorView.INCREASED_FONT);
		this.lblCurrent.setFont(TempMonitorView.INCREASED_FONT);
		this.lblCurrentValue.setFont(TempMonitorView.INCREASED_FONT);
		this.lblMax.setFont(TempMonitorView.INCREASED_FONT);
		this.lblMaxValue.setFont(TempMonitorView.INCREASED_FONT);
	}

	private void init() {
		this.updateGraphics(false, true);

		this.pack();
		this.setVisible(true);
	}

	private void btnStartActionPerformed() {
		if (!this.checkParams()) {
			return;
		}

		this.updateGraphics(true, false);

		this.tStopped = false;
		this.tPaused = false;
		this.thread = new TMVThread();
		this.thread.start();
	}

	private void btnStopActionPerformed() {
		this.updateGraphics(false, false);

		if (this.thread != null) {
			this.tStopped = false;
			this.tPaused = true;
			synchronized (this.thread) {
				this.thread.notify();
			}
		}
	}

	private void btnClearActionPerformed() {
		this.updateGraphics(false, true);

		if (this.thread != null) {
			this.tStopped = true;
			this.tPaused = false;
			this.thread.interrupt();
			this.thread = null;
		}

		//TODO
	}

	private void updateGraphics(final boolean started, final boolean cleared) {
		this.btnStart.setEnabled(!started);
		this.btnStop.setEnabled(started);
		this.btnReset.setEnabled(!started && !cleared);
		if (!started) {
			this.btnStart.setText(TempMonitorView.LOC.getRes(cleared ? "btnStart" : "btnResume"));
		}

		this.tempStreamParams1.setEnabled(!started && cleared);
		this.tempStreamParams2.setEnabled(!started && cleared);
		this.tempStreamParams3.setEnabled(!started && cleared);

		this.updateLabels(cleared, null);
	}

	private synchronized void updateLabels(final boolean cleared, final Double value) {
		if (cleared) {
			this.lblMinValue.setText("-");
			this.lblCurrentValue.setText("-");
			this.lblMaxValue.setText("-");
			this.minValue = null;
			this.maxValue = null;
			return;
		}

		if (value == null) {
			//this.lblCurrentValue.setValue(null);
			return;
		}

		this.lblCurrentValue.setText(TempMonitorView.fromDouble(value));
		if (TempMonitorView.DEBUG) {
			System.out.println("VAL: " + value);
		}

		if (this.minValue == null || value < this.minValue) {
			this.minValue = value;
			this.lblMinValue.setText(TempMonitorView.fromDouble(this.minValue));
			if (TempMonitorView.DEBUG) {
				System.out.println("MIN: " + this.minValue);
			}
		}

		if (this.maxValue == null || value > this.maxValue) {
			this.maxValue = value;
			this.lblMaxValue.setText(TempMonitorView.fromDouble(this.maxValue));
			if (TempMonitorView.DEBUG) {
				System.out.println("MAX: " + this.maxValue);
			}
		}
	}

	private boolean checkParams() {
		if (!this.tempStreamParams1.checkParams() || !this.tempStreamParams2.checkParams()
				|| !this.tempStreamParams3.checkParams()) {
			return false;
		}
		//TODO

		return true;
	}

	private static String fromDouble(final Double value) {
		return String.format("%.02f", value);
	}

	private class TMVThread extends Thread {
		private final TempStream ts1;
		private final TempStream ts2;
		private final TempStream ts3;

		private final Observable<Double> temp1;
		private final Observable<Double> temp2;
		private final Observable<Double> temp3;

		private final Observable<Double> combined;

		public TMVThread() {
			this.ts1 = TempMonitorView.this.tempStreamParams1.getTempStream();
			this.ts2 = TempMonitorView.this.tempStreamParams2.getTempStream();
			this.ts3 = TempMonitorView.this.tempStreamParams3.getTempStream();

			this.temp1 = this.ts1.build();
			this.temp2 = this.ts2.build();
			this.temp3 = this.ts3.build();

			this.combined = AvgTempStream.buildAvgTempStream(this.temp1,
					TempMonitorView.this.tempStreamParams1.getMaxVariation(), this.temp2,
					TempMonitorView.this.tempStreamParams2.getMaxVariation(), this.temp3,
					TempMonitorView.this.tempStreamParams3.getMaxVariation());
		}

		@Override
		public void run() {
			this.combined.subscribe(e -> TempMonitorView.this.updateLabels(false, e));

			this.temp1.publish().connect();
			this.temp2.publish().connect();
			this.temp3.publish().connect();

			while (!TempMonitorView.this.tStopped) {
				try {
					synchronized (this) {
						this.wait();
					}

					this.ts1.pauseEmitting(TempMonitorView.this.tPaused);
					this.ts2.pauseEmitting(TempMonitorView.this.tPaused);
					this.ts3.pauseEmitting(TempMonitorView.this.tPaused);
				} catch (@SuppressWarnings("unused") final InterruptedException e) {
					// Do nothing
				}
			}
		}

		@Override
		public void interrupt() {
			this.ts1.stopEmitting();
			this.ts2.stopEmitting();
			this.ts3.stopEmitting();

			super.interrupt();
		}

	}

	public static void main(final String[] args) {
		EventQueue.invokeLater(() -> {
			try {
				final TempMonitorView frame = new TempMonitorView();
				frame.setVisible(true);
			} catch (final Exception e) {
				e.printStackTrace();
			}
		});
	}
}
