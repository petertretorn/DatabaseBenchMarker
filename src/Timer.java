public class Timer {

	private long startTime;
	private long stopTime;
	private boolean started = false;

	private static class TimerException extends RuntimeException {

		private static final long serialVersionUID = 1L;
		
		private TimerException(String message) {
			super(message);
		}
	}

	public void start() {
		if (started) {
			throw new TimerException("Timer is already started!");
		}
		startTime = System.currentTimeMillis();
		started = true;
	}

	public void stop() {
		if (!started) {
			throw new TimerException("Timer has not been started!");
		}
		stopTime = System.currentTimeMillis();
		started = false;
	}

	public long elapsed() {
		return (stopTime - startTime);
	}
}