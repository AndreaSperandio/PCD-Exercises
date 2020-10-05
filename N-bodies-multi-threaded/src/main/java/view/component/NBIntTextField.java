package view.component;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.JFormattedTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.NumberFormatter;

public class NBIntTextField extends JFormattedTextField {
	private static final long serialVersionUID = 7532741475194030901L;

	public NBIntTextField() {
		super(NBIntTextField.getFormat());

		this.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(final FocusEvent arg0) {
				SwingUtilities.invokeLater(NBIntTextField.this::selectAll); // Select all on focus gained
			}
		});
	}

	public int getInt() {
		final Integer value = (Integer) this.getValue();
		return value != null ? value : 0;
	}

	private static NumberFormatter getFormat() {
		final NumberFormat format = NumberFormat.getIntegerInstance();
		format.setGroupingUsed(false);

		final NumberFormatter formatter = new NumberFormatter(format) {
			private static final long serialVersionUID = 7705727952405130171L;

			@Override
			public Object stringToValue(String text) throws ParseException {
				text = text != null ? text.trim() : null;
				if (text == null || "".equals(text)) { // Allows empty field
					return null;
				}
				return super.stringToValue(text);
			}

		};
		formatter.setValueClass(Integer.class);
		formatter.setAllowsInvalid(false);
		formatter.setMinimum(0);
		formatter.setMaximum(Integer.MAX_VALUE);
		formatter.setCommitsOnValidEdit(true);
		return formatter;
	}
}