package control;

import io.reactivex.rxjava3.core.Observable;
import model.TempSensor;

public class TempStream {
	private TempStream() {
		// Do nothing
	}

	public static Observable<Double> buildTempStream(final long freq, final double min, final double max,
			final double spikeFreq) {
		return Observable.create(emitter -> {
			new Thread(() -> {
				final TempSensor tempSensor = new TempSensor(min, max, spikeFreq);
				while (true) {
					try {
						emitter.onNext(Double.valueOf(tempSensor.getCurrentValue()));
						Thread.sleep(freq);
					} catch (@SuppressWarnings("unused") final Exception ex) {
						// Do nothing
					}
				}
			}).start();
		});
	}
}
