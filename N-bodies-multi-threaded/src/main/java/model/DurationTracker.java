package model;

public class DurationTracker {
	private static final int NANOS_TO_MILLIS = 1000000;

	private String name = "";
	private long startTime = 0L;
	private long endTime = 0L;

	public DurationTracker(final String name) {
		this.name = name;
	}

	/**
	 * Starts the time tracking and returns itself.
	 */
	public DurationTracker start() {
		this.startTime = System.nanoTime();
		return this;
	}

	/**
	 * Stops the time tracking, prints and returns the duration.
	 */
	public long stop() {
		return this.stop(true);
	}

	/**
	 * Stops the time tracking and returns the duration.
	 */
	public long stop(final boolean print) {
		this.endTime = System.nanoTime();
		final long duration = this.getDuration();
		if (print) {
			System.out.println(this.name.toUpperCase() + " - Time elapsed (millis): "
					+ duration / DurationTracker.NANOS_TO_MILLIS);
		}
		return duration;
	}

	public long getDuration() {
		return this.endTime - this.startTime;
	}

	public static long toMillsDuration(final long nanoDuratiom) {
		return nanoDuratiom / DurationTracker.NANOS_TO_MILLIS;
	}
}
