package control;

import java.util.ArrayList;
import java.util.List;

import view.component.NBComboBoxItem;

/**
 * Enumeration used in combination with the Builder Design Pattern
 * It's used to create different Strategies and populate a ComboBox to choose amongst them
 *
 * @author Andrea Sperandio
 *
 */
public enum StrategyBuilder {
	STREAM(0), MULTI_THREAD(1), TASK(2), ACTOR(3), DISTRIBUTED(4);

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
		case DISTRIBUTED:
			return "DISTRIBUTED";
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
		case DISTRIBUTED:
			return new DistributedStrategy(nBodies, deltaTime);
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
		l.add(new NBComboBoxItem<>(DISTRIBUTED, DISTRIBUTED.toString()));
		return l;
	}

	public int getValue() {
		return this.value;
	}
}
