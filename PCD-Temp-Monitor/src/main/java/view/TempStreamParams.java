package view;

import javax.swing.JLabel;
import javax.swing.JPanel;

import control.TempStream;
import view.component.TMDoubleTextField;
import view.component.TMIntTextField;
import view.util.TMColor;
import view.util.TMLocalizator;
import view.util.TMMessage;

public class TempStreamParams extends JPanel {
	private static final long serialVersionUID = -722439573105574674L;

	private static final TMLocalizator LOC = new TMLocalizator(TempStreamParams.class);

	private static final int DEF_FREQ = 250;
	private static final Double DEF_MIN = 50.0;
	private static final Double DEF_MAX = 75.0;
	private static final Double DEF_SPIKE_FREQ = 0.01;
	private static final Double DEF_MAX_VARIATION = TempStreamParams.DEF_MAX - TempStreamParams.DEF_MIN;

	private final JLabel lblTitle;
	private final JLabel lblFreq = new JLabel(TempStreamParams.LOC.getRes("lblFreq"));
	private final TMIntTextField txtFreq = new TMIntTextField();
	private final JLabel lblMin = new JLabel(TempStreamParams.LOC.getRes("lblMin"));
	private final TMDoubleTextField txtMin = new TMDoubleTextField();
	private final JLabel lblMax = new JLabel(TempStreamParams.LOC.getRes("lblMax"));
	private final TMDoubleTextField txtMax = new TMDoubleTextField();
	private final JLabel lblSpikeFreq = new JLabel(TempStreamParams.LOC.getRes("lblSpikeFreq"));
	private final TMDoubleTextField txtSpikeFreq = new TMDoubleTextField();
	private final JLabel lblMaxVariation = new JLabel(TempStreamParams.LOC.getRes("lblMaxVariation"));
	private final TMDoubleTextField txtMaxVariation = new TMDoubleTextField();

	public TempStreamParams(final String number) {
		this.lblTitle = new JLabel(TempStreamParams.LOC.getRes("lblTitle", number));

		this.setup();
		this.init();
	}

	private void setup() {
		this.setLayout(null);

		this.add(this.lblTitle);
		this.add(this.lblFreq);
		this.add(this.txtFreq);
		this.add(this.lblMin);
		this.add(this.txtMin);
		this.add(this.lblMax);
		this.add(this.txtMax);
		this.add(this.lblSpikeFreq);
		this.add(this.txtSpikeFreq);
		this.add(this.lblMaxVariation);
		this.add(this.txtMaxVariation);

		final int heightComp = 20;
		final int margin = heightComp + 10;
		int y = 0;
		this.lblTitle.setBounds(0, y, 200, heightComp);
		y += margin;
		this.lblFreq.setBounds(0, y, 65, heightComp);
		this.txtFreq.setBounds(75, y, 50, heightComp);
		this.lblMin.setBounds(145, y, 55, heightComp);
		this.txtMin.setBounds(210, y, 50, heightComp);
		this.lblMax.setBounds(280, y, 55, heightComp);
		this.txtMax.setBounds(345, y, 50, heightComp);
		this.lblSpikeFreq.setBounds(415, y, 100, heightComp);
		this.txtSpikeFreq.setBounds(525, y, 50, heightComp);
		this.lblMaxVariation.setBounds(595, y, 100, heightComp);
		this.txtMaxVariation.setBounds(705, y, 50, heightComp);

		this.lblTitle.setForeground(TMColor.LBL_BLUE);
		this.lblFreq.setToolTipText(TempStreamParams.LOC.getRes("lblFreqToolTip"));
		this.lblMin.setToolTipText(TempStreamParams.LOC.getRes("lblMinToolTip"));
		this.lblMax.setToolTipText(TempStreamParams.LOC.getRes("lblMaxToolTip"));
		this.lblSpikeFreq.setToolTipText(TempStreamParams.LOC.getRes("lblSpikeFreqToolTip"));
		this.lblMaxVariation.setToolTipText(TempStreamParams.LOC.getRes("lblMaxVariationToolTip"));
	}

	private void init() {
		this.txtFreq.setValue(TempStreamParams.DEF_FREQ);
		this.txtMin.setValue(TempStreamParams.DEF_MIN);
		this.txtMax.setValue(TempStreamParams.DEF_MAX);
		this.txtSpikeFreq.setValue(TempStreamParams.DEF_SPIKE_FREQ);
		this.txtMaxVariation.setValue(TempStreamParams.DEF_MAX_VARIATION);
	}

	@Override
	public void setEnabled(final boolean enabled) {
		super.setEnabled(enabled);

		this.txtFreq.setEnabled(enabled);
		this.txtMin.setEnabled(enabled);
		this.txtMax.setEnabled(enabled);
		this.txtSpikeFreq.setEnabled(enabled);
		this.txtMaxVariation.setEnabled(enabled);
	}

	public boolean checkParams() {
		if (this.txtFreq.getInt() <= 0) {
			TMMessage.showErrDialog(this,
					this.lblFreq.getText() + " " + TempStreamParams.LOC.getRes("errNegativeValue"));
			return false;
		}

		if (this.txtMin.getDouble() >= this.txtMax.getDouble()) {
			TMMessage.showErrDialog(this, this.lblMin.getText() + " " + TempStreamParams.LOC.getRes("errGreaterThan")
					+ " " + this.lblMax.getText() + "!");
			return false;
		}

		if (this.txtSpikeFreq.getDouble() < 0 || this.txtSpikeFreq.getDouble() > 1) {
			TMMessage.showErrDialog(this,
					this.lblSpikeFreq.getText() + " " + TempStreamParams.LOC.getRes("errBetween", "0.0", "1.0"));
			return false;
		}

		if (this.txtMaxVariation.getDouble() <= 0) {
			TMMessage.showErrDialog(this,
					this.lblMaxVariation.getText() + " " + TempStreamParams.LOC.getRes("errNegativeValue"));
			return false;
		}

		return true;
	}

	public long getFreq() {
		return this.txtFreq.getInt();
	}

	public Double getMin() {
		return this.txtMin.getDouble();
	}

	public Double getMax() {
		return this.txtMax.getDouble();
	}

	public Double getSpikeFreq() {
		return this.txtSpikeFreq.getDouble();
	}

	public Double getMaxVariation() {
		return this.txtMaxVariation.getDouble();
	}

	public TempStream getTempStream() {
		return new TempStream(this.getFreq(), this.getMin(), this.getMax(), this.getSpikeFreq());
	}
}
