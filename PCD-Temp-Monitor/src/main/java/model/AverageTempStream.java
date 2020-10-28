package model;

import io.reactivex.rxjava3.core.Observable;

public class AverageTempStream {
	private static class Pair {
		private final Double t1;
		private final Double t2;

		public Pair(final Double t1, final Double t2) {
			super();
			this.t1 = t1;
			this.t2 = t2;
		}
	}

	private AverageTempStream() {
		// Do nothing
	}

	@SuppressWarnings("boxing")
	public static Observable<Double> buildAvgTempStream(final Double maxVariation, final Observable<Double> o1,
			final Observable<Double> o2, final Observable<Double> o3) {

		return Observable.combineLatest(AverageTempStream.getFiltered(o1, maxVariation),
				AverageTempStream.getFiltered(o2, maxVariation), AverageTempStream.getFiltered(o3, maxVariation),
				(s1, s2, s3) -> (s1 + s2 + s3) / 3.0);
	}

	@SuppressWarnings("boxing")
	private static Observable<Double> getFiltered(final Observable<Double> temp, final Double maxVariation) {
		return temp.zipWith(temp.skip(1), Pair::new).filter(p -> Math.abs(p.t2 - p.t1) <= maxVariation).map(p -> p.t2);
	}
}
