package control;

import io.reactivex.rxjava3.core.Observable;

public class AvgTempStream {
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

	public static Observable<Double> buildAvgTempStream(final Observable<Double> o1, final Double maxVariation1,
			final Observable<Double> o2, final Double maxVariation2, final Observable<Double> o3,
			final Double maxVariation3) {

		return Observable.combineLatest(AvgTempStream.getFiltered(o1, maxVariation1),
				AvgTempStream.getFiltered(o2, maxVariation2), AvgTempStream.getFiltered(o3, maxVariation3),
				(s1, s2, s3) -> (s1 + s2 + s3) / 3.0);
	}

	private static Observable<Double> getFiltered(final Observable<Double> temp, final Double maxVariation) {
		return temp.zipWith(temp.skip(1), Pair::new).filter(p -> Math.abs(p.t2 - p.t1) <= maxVariation).map(p -> p.t2);
	}
}
