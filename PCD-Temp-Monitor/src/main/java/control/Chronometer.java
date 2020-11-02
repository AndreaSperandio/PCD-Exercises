package control;

/**
 * Chronometer class used to keep track of the elapsed time since the running point.
 *
 * The Chronometer can be paused, resumed or stopped permanently.
 *
 * @author Andrea Sperandio
 *
 */
public class Chronometer extends Thread {
	private volatile boolean paused;
	private volatile boolean stopped;

	private volatile long startTime;
	private volatile Long elapsedTime;

	public Chronometer() {
		this.paused = false;
		this.stopped = false;
		this.startTime = 0;
		this.elapsedTime = null;
	}

	@Override
	public void run() {
		this.startTime = System.currentTimeMillis();
		while (!this.stopped) {
			try {
				synchronized (this) {
					this.wait();
				}
				if (this.paused) {
					this.elapsedTime = System.currentTimeMillis() - this.startTime;
				} else {
					this.startTime = System.currentTimeMillis() - this.elapsedTime;
				}
			} catch (@SuppressWarnings("unused") final InterruptedException e) {
				// Do nothing
			}
		}
	}

	public synchronized long getElapsedTime() {
		// Might happen if a thread calls getElapsedTime() before this Thread started
		if (this.startTime == 0) {
			return 0;
		}
		return System.currentTimeMillis() - this.startTime;
	}

	public void pauseChrono(final boolean pause) {
		this.paused = pause;
		synchronized (this) {
			this.notify();
		}
	}

	public void stopChrono() {
		this.stopped = true;
		this.interrupt();
	}
}
