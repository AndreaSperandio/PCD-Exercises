package control;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;

/**
 * Class that returns a stream of AvgTempSensor, using 3 different TempStreams
 *
 * Each time a sensor emits a valid value (non spiky one), this stream evaluates the average value
 * of it and the last valid ones emitted by the other 2 sensors and emits it.
 *
 * @author Andrea Sperandio
 *
 */
public class AvgTempStream {
	public static final int N_TEMP_STREAMS = 3;

	private static class Pair {
		private final Double t1;
		private final Double t2;

		public Pair(final Double t1, final Double t2) {
			super();
			this.t1 = t1;
			this.t2 = t2;
		}
	}

	private AvgTempStream() {
		// Do nothing
	}

	private static Observable<Double> buildAvgTempStream(final Observable<Double> o1, final Double maxVariation1,
			final Observable<Double> o2, final Double maxVariation2, final Observable<Double> o3,
			final Double maxVariation3) {

		return Observable.combineLatest(AvgTempStream.getFiltered(o1, maxVariation1),
				AvgTempStream.getFiltered(o2, maxVariation2), AvgTempStream.getFiltered(o3, maxVariation3),
				(s1, s2, s3) -> (s1 + s2 + s3) / AvgTempStream.N_TEMP_STREAMS);
	}

	/* A value is considered a spike if it differs from the previous one by more than maxVariation */
	private static Observable<Double> getFiltered(final Observable<Double> temp, final Double maxVariation) {
		return temp.zipWith(temp.skip(1), Pair::new).filter(p -> Math.abs(p.t2 - p.t1) <= maxVariation).map(p -> p.t2);
	}

	public static Observable<Double> buildAvgTempStream(final List<Observable<Double>> temps,
			final List<Double> maxVariations) {
		if (temps.size() != maxVariations.size() || temps.size() != AvgTempStream.N_TEMP_STREAMS) {
			return null;
		}

		int i = 0;
		return AvgTempStream.buildAvgTempStream(temps.get(i), maxVariations.get(i++), temps.get(i),
				maxVariations.get(i++), temps.get(i), maxVariations.get(i++));
	}
}
