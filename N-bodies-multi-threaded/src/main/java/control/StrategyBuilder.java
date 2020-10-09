package control;

import java.util.ArrayList;
import java.util.List;

import view.component.NBComboBoxItem;

public enum StrategyBuilder {
	STREAM(0), MULTI_THREAD(1);

	private int value;

	private StrategyBuilder(final int value) {
		this.value = value;
	}

	@Override
	public String toString() {
		switch (this) {
		case STREAM:
			return "STREAM";
		case MULTI_THREAD:
			return "MULTI THREAD";
		default:
			return "ERR";
		}
	}

	public static Strategy buildStrategy(final StrategyBuilder strategy, final int nBodies, final int deltaTime) {
		switch (strategy) {
		case STREAM:
			return new StreamStrategy(nBodies, deltaTime);
		case MULTI_THREAD:
			return new MultiThreadStrategy(nBodies, deltaTime);
		default:
			return new StreamStrategy(nBodies, deltaTime);
		}
	}

	public static List<NBComboBoxItem<StrategyBuilder>> getComboItems() {
		final List<NBComboBoxItem<StrategyBuilder>> l = new ArrayList<>();
		l.add(new NBComboBoxItem<>(STREAM, STREAM.toString()));
		l.add(new NBComboBoxItem<>(MULTI_THREAD, MULTI_THREAD.toString()));
		return l;
	}

	public int getValue() {
		return this.value;
	}
}
