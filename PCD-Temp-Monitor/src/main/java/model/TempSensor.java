package model;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Class implementing a simulated temperature sensor
 *
 * @author aricci
 *
 */
public class TempSensor {
	private volatile double currentValue;
	private final double spikeFreq;
	private final Random gen;
	private final BaseTimeValue time;
	private final double zero;
	private final double range;
	private final double spikeVar;
	private final UpdateTask updateTask;
	private final ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);

	/**
	 * Create a sensor producing values in a (min,max) range, with possible spikes
	 *
	 * @param min
	 *            range min
	 * @param max
	 *            range max
	 * @param spikeFreq
	 *            - probability to read a spike (0 = no spikes, 1 = always spikes)
	 */
	public TempSensor(final double min, final double max, final double spikeFreq) {
		this.gen = new Random(System.nanoTime());
		this.time = BaseTimeValue.getInstance();
		this.zero = (max + min) * 0.5;
		this.range = (max - min) * 0.5;
		this.spikeFreq = spikeFreq;
		this.spikeVar = this.range * 10;
		this.updateTask = new UpdateTask();
		this.exec.scheduleAtFixedRate(this.updateTask, 0, 100, java.util.concurrent.TimeUnit.MILLISECONDS);

		/* initialize currentValue */
		this.updateTask.run();
	}

	/**
	 * Reading the current sensor value
	 *
	 * @return sensor value
	 */
	public double getCurrentValue() {
		synchronized (this.updateTask) {
			return this.currentValue;
		}
	}

	public void shutdown() {
		this.exec.shutdownNow();
	}

	class UpdateTask implements Runnable {
		@Override
		public void run() {
			double newValue;

			final double delta = (-0.5 + TempSensor.this.gen.nextDouble()) * TempSensor.this.range * 0.2;
			newValue = TempSensor.this.zero
					+ Math.sin(TempSensor.this.time.getCurrentValue()) * TempSensor.this.range * 0.8 + delta;

			final boolean newSpike = TempSensor.this.gen.nextDouble() <= TempSensor.this.spikeFreq;
			if (newSpike) {
				newValue = TempSensor.this.currentValue + TempSensor.this.spikeVar;
			}

			synchronized (this) {
				TempSensor.this.currentValue = newValue;
			}
		}
	}

}

class BaseTimeValue {
	static BaseTimeValue instance;

	static BaseTimeValue getInstance() {
		synchronized (BaseTimeValue.class) {
			if (BaseTimeValue.instance == null) {
				BaseTimeValue.instance = new BaseTimeValue();
			}
			return BaseTimeValue.instance;
		}
	}

	private double time;
	private final ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);

	private BaseTimeValue() {
		this.time = 0;
		this.exec.scheduleAtFixedRate(() -> {
			synchronized (this.exec) {
				this.time += 0.01;
			}
		}, 0, 100, java.util.concurrent.TimeUnit.MILLISECONDS);
	}

	public double getCurrentValue() {
		synchronized (this.exec) {
			return this.time;
		}
	}
}
