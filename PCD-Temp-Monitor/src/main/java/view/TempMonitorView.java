package view;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

import control.AvgTempStream;
import control.Chronometer;
import control.TempStream;
import io.reactivex.rxjava3.core.Observable;
import view.component.TMButton;
import view.component.TMDoubleTextField;
import view.component.TMIntTextField;
import view.util.TMColor;
import view.util.TMLocalizator;
import view.util.TMMessage;
import view.util.TMResource;

public class TempMonitorView extends JFrame {
	private static final long serialVersionUID = 846132440578478084L;

	private static final TMLocalizator LOC = new TMLocalizator(TempMonitorView.class);
	private static final boolean DEBUG = false;
	private static final Font INCREASED_FONT;

	private static final Double DEF_THRESHOLD = 70.0;
	private static final int DEF_TIME_THRESHOLD = 2000;  // millis

	static {
		final Font font = new JLabel().getFont();
		INCREASED_FONT = new Font(font.getFamily(), font.getStyle(), 15);
	}

	private final List<TempStreamParams> tempStreamParamss = new ArrayList<>();

	private final TMButton btnStart = new TMButton(TempMonitorView.LOC.getRes("btnStart"), TMResource.getStartImage());
	private final TMButton btnStop = new TMButton(TempMonitorView.LOC.getRes("btnStop"), TMResource.getStopImage());
	private final TMButton btnReset = new TMButton(TempMonitorView.LOC.getRes("btnReset"), TMResource.getResetImage());
	private final JLabel lblThreshold = new JLabel(TempMonitorView.LOC.getRes("lblThreshold"));
	private final TMDoubleTextField txtThreshold = new TMDoubleTextField();
	private final JLabel lblTimeThreshold = new JLabel(TempMonitorView.LOC.getRes("lblTimeThreshold"));
	private final TMIntTextField txtTimeThreshold = new TMIntTextField();

	private final JLabel lblTemperature = new JLabel(TempMonitorView.LOC.getRes("lblTemperature").toUpperCase());
	private final JLabel lblMin = new JLabel(TempMonitorView.LOC.getRes("lblMin"));
	private final JLabel lblMinValue = new JLabel();
	private final JLabel lblCurrent = new JLabel(TempMonitorView.LOC.getRes("lblCurrent"));
	private final JLabel lblCurrentValue = new JLabel();
	private final JLabel lblMax = new JLabel(TempMonitorView.LOC.getRes("lblMax"));
	private final JLabel lblMaxValue = new JLabel();
	private final JLabel lblWrnThreshold = new JLabel(
			TempMonitorView.LOC.getRes("lblWrnThreshold", TempMonitorView.DEF_TIME_THRESHOLD + 1));

	private Thread thread = null;
	private volatile boolean tStopped = false;
	private volatile boolean tPaused = false;

	private Chronometer thresholdChrono = null;
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

		TempStreamParams tsParams;
		for (int i = 0; i < AvgTempStream.N_TEMP_STREAMS; i++) {
			tsParams = new TempStreamParams("" + (i + 1));
			this.tempStreamParamss.add(tsParams);
			this.add(tsParams);
		}

		final int heighParams = 70;
		int y = 10;
		for (int i = 0; i < AvgTempStream.N_TEMP_STREAMS; i++) {
			this.tempStreamParamss.get(i).setBounds(10, y, 775, heighParams);
			y += heighParams;
		}

		this.add(this.btnStart);
		this.add(this.btnStop);
		this.add(this.btnReset);
		this.add(this.lblThreshold);
		this.add(this.txtThreshold);
		this.add(this.lblTimeThreshold);
		this.add(this.txtTimeThreshold);

		final int heighButton = 30;
		final int marginButton = heighButton + 10;
		y = 20;
		this.btnStart.setBounds(850, y, 130, heighButton);
		y += marginButton;
		this.btnStop.setBounds(850, y, 130, heighButton);
		y += marginButton;
		this.btnReset.setBounds(850, y, 130, heighButton);
		y += marginButton;
		this.lblThreshold.setBounds(850, y, 70, heighButton);
		this.txtThreshold.setBounds(930, y, 50, heighButton);
		y += marginButton;
		this.lblTimeThreshold.setBounds(850, y, 70, heighButton);
		this.txtTimeThreshold.setBounds(930, y, 50, heighButton);

		this.btnStart.addActionListener(e -> this.btnStartActionPerformed());
		this.btnStop.addActionListener(e -> this.btnStopActionPerformed());
		this.btnReset.addActionListener(e -> this.btnClearActionPerformed());

		this.lblThreshold.setToolTipText(TempMonitorView.LOC.getRes("lblThresholdToolTip"));
		this.lblTimeThreshold.setToolTipText(TempMonitorView.LOC.getRes("lblTimeThresholdToolTip"));

		this.add(this.lblTemperature);
		this.add(this.lblMin);
		this.add(this.lblMinValue);
		this.add(this.lblCurrent);
		this.add(this.lblCurrentValue);
		this.add(this.lblMax);
		this.add(this.lblMaxValue);
		this.add(this.lblWrnThreshold);

		final int heighComp = 30;
		final int marginComp = heighComp + 10;
		y = heighParams * AvgTempStream.N_TEMP_STREAMS + marginComp;
		this.lblTemperature.setBounds(340, y, 200, heighComp);
		y += marginComp;
		this.lblMin.setBounds(240, y, 120, heighComp);
		this.lblCurrent.setBounds(390, y, 120, heighComp);
		this.lblMax.setBounds(540, y, 120, heighComp);
		y += marginComp;
		this.lblMinValue.setBounds(240, y, 120, heighComp);
		this.lblCurrentValue.setBounds(390, y, 120, heighComp);
		this.lblMaxValue.setBounds(540, y, 120, heighComp);
		y += heighParams;
		this.lblWrnThreshold.setBounds(80, y, 750, heighComp);

		this.lblMin.setToolTipText(TempMonitorView.LOC.getRes("lblMinToolTip"));
		this.lblCurrent.setToolTipText(TempMonitorView.LOC.getRes("lblCurrentToolTip"));
		this.lblMax.setToolTipText(TempMonitorView.LOC.getRes("lblMaxToolTip"));

		this.lblTemperature.setForeground(TMColor.LBL_BLUE);
		this.lblMinValue.setForeground(TMColor.LBL_BLUE);
		this.lblCurrentValue.setForeground(TMColor.LBL_BLUE);
		this.lblMaxValue.setForeground(TMColor.LBL_BLUE);
		this.lblWrnThreshold.setForeground(TMColor.LBL_ORANGE);

		this.lblTemperature.setFont(TempMonitorView.INCREASED_FONT);
		this.lblMin.setFont(TempMonitorView.INCREASED_FONT);
		this.lblMinValue.setFont(TempMonitorView.INCREASED_FONT);
		this.lblCurrent.setFont(TempMonitorView.INCREASED_FONT);
		this.lblCurrentValue.setFont(TempMonitorView.INCREASED_FONT);
		this.lblMax.setFont(TempMonitorView.INCREASED_FONT);
		this.lblMaxValue.setFont(TempMonitorView.INCREASED_FONT);
		this.lblWrnThreshold.setFont(TempMonitorView.INCREASED_FONT);
	}

	private void init() {
		this.txtThreshold.setValue(TempMonitorView.DEF_THRESHOLD);
		this.txtTimeThreshold.setValue(TempMonitorView.DEF_TIME_THRESHOLD);

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
		if (this.thread == null) {
			this.thread = new TMVThread();
			this.thread.start();
		} else {
			synchronized (this.thread) {
				this.thread.notify();
			}
		}

		if (this.thresholdChrono != null) {
			this.thresholdChrono.pauseChrono(false);
		}
	}

	private void btnStopActionPerformed() {
		this.updateGraphics(false, false);

		this.tStopped = false;
		this.tPaused = true;
		if (this.thread != null) {
			synchronized (this.thread) {
				this.thread.notify();
			}
		}

		if (this.thresholdChrono != null) {
			this.thresholdChrono.pauseChrono(true);
		}
	}

	private void btnClearActionPerformed() {
		this.updateGraphics(false, true);

		this.tStopped = true;
		this.tPaused = false;
		if (this.thread != null) {
			this.thread.interrupt();
			this.thread = null;
		}

		if (this.thresholdChrono != null) {
			this.thresholdChrono.stopChrono();
			this.thresholdChrono = null;
		}
	}

	private void updateGraphics(final boolean started, final boolean cleared) {
		this.btnStart.setEnabled(!started);
		this.btnStop.setEnabled(started);
		this.btnReset.setEnabled(!started && !cleared);
		if (!started) {
			this.btnStart.setText(TempMonitorView.LOC.getRes(cleared ? "btnStart" : "btnResume"));
		}

		this.tempStreamParamss.forEach(t -> t.setEnabled(!started && cleared));

		this.updateLabels(cleared, null);
	}

	private synchronized void updateLabels(final boolean cleared, final Double value) {
		if (cleared) {
			this.lblMinValue.setText("-");
			this.lblCurrentValue.setText("-");
			this.lblMaxValue.setText("-");
			this.lblWrnThreshold.setVisible(false);
			this.minValue = null;
			this.maxValue = null;
			return;
		}

		if (value == null) {
			//this.lblCurrentValue.setValue(null);
			return;
		}

		this.lblCurrentValue.setText(TempMonitorView.stringFromDouble(value));
		if (TempMonitorView.DEBUG) {
			System.out.println("VAL: " + value);
		}

		if (this.minValue == null || value < this.minValue) {
			this.minValue = value;
			this.lblMinValue.setText(TempMonitorView.stringFromDouble(this.minValue));
			if (TempMonitorView.DEBUG) {
				System.out.println("MIN: " + this.minValue);
			}
		}

		if (this.maxValue == null || value > this.maxValue) {
			this.maxValue = value;
			this.lblMaxValue.setText(TempMonitorView.stringFromDouble(this.maxValue));
			if (TempMonitorView.DEBUG) {
				System.out.println("MAX: " + this.maxValue);
			}
		}

		if (value > this.txtThreshold.getDouble()) {
			if (this.thresholdChrono == null) {
				this.thresholdChrono = new Chronometer();
				this.thresholdChrono.start();
			} else {
				final long elapsedTime = this.thresholdChrono.getElapsedTime();
				if (elapsedTime > this.txtTimeThreshold.getInt()) {
					this.lblWrnThreshold.setVisible(true);
					this.lblWrnThreshold.setText(TempMonitorView.LOC.getRes("lblWrnThreshold", elapsedTime));
				} else {
					// The user might change the time threshold at runtime
					this.lblWrnThreshold.setVisible(false);
				}
			}
		} else {
			if (this.thresholdChrono != null) {
				this.thresholdChrono.stopChrono();
				this.thresholdChrono = null;
			}
			this.lblWrnThreshold.setVisible(false);
		}
	}

	private boolean checkParams() {
		for (final TempStreamParams tempStreamParams : this.tempStreamParamss) {
			if (!tempStreamParams.checkParams()) {
				return false;
			}
		}

		if (this.txtTimeThreshold.getInt() <= 0) {
			TMMessage.showErrDialog(this,
					this.lblTimeThreshold.getText() + " " + TempMonitorView.LOC.getRes("errNegativeValue"));
			return false;
		}

		return true;
	}

	private static String stringFromDouble(final Double value) {
		return String.format("%.02f", value);
	}

	private class TMVThread extends Thread {
		private final List<TempStream> tss = new ArrayList<>();
		private final List<Observable<Double>> temps = new ArrayList<>();
		private final Observable<Double> combined;

		public TMVThread() {
			for (final TempStreamParams tempStreamParams : TempMonitorView.this.tempStreamParamss) {
				this.tss.add(tempStreamParams.getTempStream());
			}

			for (final TempStream ts : this.tss) {
				this.temps.add(ts.build());
			}

			this.combined = AvgTempStream.buildAvgTempStream(this.temps, TempMonitorView.this.tempStreamParamss.stream()
					.map(TempStreamParams::getMaxVariation).collect(Collectors.toList()));
		}

		@Override
		public void run() {
			this.combined.subscribe(e -> TempMonitorView.this.updateLabels(false, e));
			this.temps.forEach(t -> t.publish().connect());

			while (!TempMonitorView.this.tStopped) {
				try {
					synchronized (this) {
						this.wait();
					}

					this.tss.forEach(t -> t.pauseEmitting(TempMonitorView.this.tPaused));
					if (!TempMonitorView.this.tPaused) {
						// Need to re-subscribe after a pause
						this.combined.subscribe(e -> TempMonitorView.this.updateLabels(false, e));
					}
				} catch (@SuppressWarnings("unused") final InterruptedException e) {
					// Do nothing
				}
			}
		}

		@Override
		public void interrupt() {
			this.tss.forEach(TempStream::stopEmitting);
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
