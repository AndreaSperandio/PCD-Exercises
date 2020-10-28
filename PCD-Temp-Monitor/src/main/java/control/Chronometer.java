package control;

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
					//System.out.println("elapsedTime = " + this.elapsedTime);
				} else {
					this.startTime = System.currentTimeMillis() - this.elapsedTime;
					if (this.startTime < 1000) {
						System.err.println("qui " + this.startTime);
					}

					/*System.out.println("startTime = " + this.startTime);
					System.out.println("elapsedTimeNow = " + this.getElapsedTime());*/
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
