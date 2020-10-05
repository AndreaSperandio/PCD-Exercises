package view.component;

import javax.swing.JCheckBox;
import javax.swing.JLabel;

public class NBCheckBox extends JCheckBox {
	private static final long serialVersionUID = 2645746174084935947L;

	public NBCheckBox(final String text) {
		super(text);
		this.setup();
	}

	private void setup() {
		this.setFont(new JLabel().getFont());
	}

	@Override
	public void setBounds(final int x, final int y, final int width, final int height) {
		super.setBounds(x - 5, y, width, height);
	}
}