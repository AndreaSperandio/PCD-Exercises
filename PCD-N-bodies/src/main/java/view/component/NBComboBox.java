package view.component;

import java.util.List;

import javax.swing.JComboBox;

public class NBComboBox<T extends NBComboBoxItem<I>, I> extends JComboBox<T> {
	private static final long serialVersionUID = 1105083489692804419L;

	public NBComboBox() {
		super();
	}

	public void addItems(final List<T> items) {
		for (final T t : items) {
			this.addItem(t);
		}
	}

	public void setSelectedItemByKey(final I key) {
		if (key == null) {
			return;
		}

		T item;
		for (int i = 0; i < this.getItemCount(); i++) {
			item = this.getItemAt(i);
			if (item != null && key.equals(item.getKey())) {
				this.setSelectedIndex(i);
				return;
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public T getSelectedItem() {
		return (T) super.getSelectedItem();
	}

	public T getItemByKey(final I key) {
		if (key == null) {
			return null;
		}

		T item;
		for (int i = 0; i < this.getItemCount(); i++) {
			item = this.getItemAt(i);
			if (item != null && key.equals(item.getKey())) {
				return item;
			}
		}

		return null;
	}

	public I getSelectedItemKey() {
		final T selectedItem = this.getSelectedItem();
		return selectedItem == null ? null : selectedItem.getKey();
	}

	public String getSelectedItemValue() {
		final T selectedItem = this.getSelectedItem();
		return selectedItem == null ? null : selectedItem.getValue();
	}

	@Override
	public void setEditable(final boolean editable) {
		this.setEnabled(editable);
	}
}
