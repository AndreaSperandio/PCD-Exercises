package control;

import io.reactivex.rxjava3.core.Observable;
import model.TempSensor;

public class TempStream {
	private final long freq;
	private final double min;
	private final double max;
	private final double spikeFreq;

	private volatile boolean stopped;
	private volatile boolean paused;
	private Thread thread;

	public TempStream(final long freq, final Double min, final Double max, final Double spikeFreq) {
		this.freq = freq;
		this.min = min;
		this.max = max;
		this.spikeFreq = spikeFreq;

		this.stopped = false;
		this.paused = false;
	}

	public Observable<Double> build() {
		return Observable.create(emitter -> {
			this.thread = new Thread() {
				@Override
				public void run() {
					final TempSensor tempSensor = new TempSensor(TempStream.this.min, TempStream.this.max,
							TempStream.this.spikeFreq);
					while (!TempStream.this.stopped) {
						try {
							if (TempStream.this.paused) {
								//synchronized (this) forces the AvgTempStream to stop listening and need to re-subscribe
								synchronized (this) {
									this.wait();
								}
								continue;
							}

							emitter.onNext(Double.valueOf(tempSensor.getCurrentValue()));
							Thread.sleep(TempStream.this.freq);
						} catch (@SuppressWarnings("unused") final Exception ex) {
							// Do nothing
						}
					}
				}
			};
			this.thread.start();
		});
	}

	public synchronized void stopEmitting() {
		this.stopped = true;
		this.thread.interrupt();
	}

	public synchronized void pauseEmitting(final boolean pause) {
		this.paused = pause;
		synchronized (this.thread) {
			this.thread.notify();
		}
	}
}
