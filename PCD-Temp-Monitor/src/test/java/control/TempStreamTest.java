package control;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;

public class TempStreamTest {

	private class Pair {
		private final Double t1;
		private final Double t2;

		public Pair(final Double t1, final Double t2) {
			super();
			this.t1 = t1;
			this.t2 = t2;
		}
	}

	private class Worker extends Thread {
		private final ObservableEmitter<Double> emitter;
		private final List<Double> list;
		private final long sleepTime;

		public Worker(final ObservableEmitter<Double> emitter, final List<Double> list, final long sleepTime) {
			this.emitter = emitter;
			this.list = list;
			this.sleepTime = sleepTime;
		}

		@Override
		public void run() {
			for (int i = 0; i < this.list.size(); i++) {
				try {
					this.emitter.onNext(this.list.get(i));
					Thread.sleep(this.sleepTime);
				} catch (@SuppressWarnings("unused") final Exception ex) {
					// Do nothing
				}
			}
		}
	}

	private double maxVariation;

	private Observable<Double> temp1;
	private Observable<Double> temp2;
	private Observable<Double> temp3;

	private List<Double> validTemp1;
	private List<Double> validTemp2;
	private List<Double> validTemp3;

	private List<Double> expected;

	@Before
	public void initialize() {
		this.maxVariation = 30.0;
	}

	@Test
	public void testValidFilter() {
		this.temp1 = Observable.just(10.0, 20.0, 50.0, 70.0, 20.0, 40.0);
		this.validTemp1 = Arrays.asList(20.0, 50.0, 70.0, 40.0);

		this.temp2 = Observable.just(10.0, 20.0, 30.0, 10.0, 50.0, 30.0);
		this.validTemp2 = Arrays.asList(20.0, 30.0, 10.0, 30.0);

		this.temp3 = Observable.just(20.0, 30.0, 70.0, 60.0, 50.0, 0.0);
		this.validTemp3 = Arrays.asList(30.0, 60.0, 50.0);

		int i = 1;
		TempStreamTest.testBlockingObservable(this.getFiltered(this.temp1), this.validTemp1, "" + i++);
		TempStreamTest.testBlockingObservable(this.getFiltered(this.temp2), this.validTemp2, "" + i++);
		TempStreamTest.testBlockingObservable(this.getFiltered(this.temp3), this.validTemp3, "" + i++);
	}

	@Test
	public void testValidCombinedOutput() {
		//Time		0	25	50	75	100	125	150	175	200	225	250	275	300
		//o1		(10)			20				(80)			70
		//o2		(10)		30			(70)		50
		//o3		(30)				40					60
		//Comb							30				37	43		60

		final Observable<Double> o1 = Observable
				.create(emitter -> new Worker(emitter, Arrays.asList(10.0, 20.0, 80.0, 70.0), 100).start());
		final Observable<Double> o2 = Observable
				.create(emitter -> new Worker(emitter, Arrays.asList(10.0, 30.0, 70.0, 50.0), 75).start());
		final Observable<Double> o3 = Observable
				.create(emitter -> new Worker(emitter, Arrays.asList(30.0, 40.0, 60.0), 125).start());

		final Observable<Double> combined = Observable.combineLatest(this.getFiltered(o1), this.getFiltered(o2),
				this.getFiltered(o3), (s1, s2, s3) -> (s1 + s2 + s3) / 3);

		final List<Double> combinedList = new ArrayList<>();
		combined.subscribe(combinedList::add);

		this.expected = new ArrayList<>();
		this.expected.add((20.0 + 30.0 + 40.0) / 3.0);
		this.expected.add((20.0 + 50.0 + 40.0) / 3.0);
		this.expected.add((20.0 + 50.0 + 60.0) / 3.0);
		this.expected.add((70.0 + 50.0 + 60.0) / 3.0);

		o1.publish().connect();
		o2.publish().connect();
		o3.publish().connect();

		try {
			Thread.sleep(310); // Force a join
		} catch (@SuppressWarnings("unused") final InterruptedException e) {
			// Do nothing
		}

		for (int i = 0; i < this.expected.size(); i++) {
			Assert.assertEquals("Combined elem " + i + " is valid.", this.expected.get(i), combinedList.get(i),
					0.0000000000001);
		}
	}

	private static void testBlockingObservable(final Observable<Double> observable, final List<Double> expected,
			final String name) {
		final List<Double> observableList = observable.toList().blockingGet();

		for (int i = 0; i < expected.size(); i++) {
			Assert.assertEquals("Temp" + name + " elem " + i + " is valid.", expected.get(i), observableList.get(i),
					0.0000000000001);
		}
	}

	private Observable<Double> getFiltered(final Observable<Double> temp) {
		return temp.zipWith(temp.skip(1), Pair::new).filter(p -> Math.abs(p.t2 - p.t1) <= this.maxVariation)
				.map(p -> p.t2);
	}
}
