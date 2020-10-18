package control;

import java.util.ArrayList;
import java.util.List;

import view.component.NBComboBoxItem;

public enum StrategyBuilder {
	STREAM(0), MULTI_THREAD(1), TASK(2), ACTOR(3);

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
		case TASK:
			return "TASK";
		case ACTOR:
			return "ACTOR";
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
		case TASK:
			return new TaskStrategy(nBodies, deltaTime);
		case ACTOR:
			return new ActorStrategy(nBodies, deltaTime);
		default:
			return new StreamStrategy(nBodies, deltaTime);
		}
	}

	public static List<NBComboBoxItem<StrategyBuilder>> getComboItems() {
		final List<NBComboBoxItem<StrategyBuilder>> l = new ArrayList<>();
		l.add(new NBComboBoxItem<>(STREAM, STREAM.toString()));
		l.add(new NBComboBoxItem<>(MULTI_THREAD, MULTI_THREAD.toString()));
		l.add(new NBComboBoxItem<>(TASK, TASK.toString()));
		l.add(new NBComboBoxItem<>(ACTOR, ACTOR.toString()));
		return l;
	}

	public int getValue() {
		return this.value;
	}
}
