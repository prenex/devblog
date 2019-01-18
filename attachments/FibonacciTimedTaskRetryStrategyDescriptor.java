package hu.grepton.thdocuman.core.persistence.dao.descriptor;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Parameterizable fibonatrix retry strategy which can be used to set a time
 * diff scaler and make a minimal retry interval based on the scaler and the
 * fibonacci sequence and a giving up time after which retry should not happen -
 * actually ONE retry always happens after the give up time, but after that one,
 * no further retry will be tried to make a run!
 * 
 * Rem.: If the interval scalerparameter is 2 hour, it is not guaranteed that
 * the series of minimal intervals will be 2, 4, 6, 10, 16, 26, ... HOURS but
 * the mathematical series of minimal wait intervals between each retry is
 * asymptotically grows the same way as the value of fib(n).
 * 
 * @author rthier
 */
public class FibonacciTimedTaskRetryStrategyDescriptor implements
		TaskRetryStrategyDescriptor {

	/** Constant of the "Golden ratio" */
	private final static double GOLDEN_RATIO = (1 + Math.sqrt(5.0)) / 2.0;

	private final TimeUnit timeUnit;

	/** Giving up after this many time unit */
	private final int giveUpAfter;

	/**
	 * The base multiplier for the fib series that will generate the interval
	 * between retries in this time unit
	 */
	private final int minimalRetryIntervalBaseMultiplier;

	/**
	 * Create a {@link TaskRetryStrategyDescriptor} that implements fibonatrix
	 * parametrized timed strategies.
	 * 
	 * @param giveUpAfterSeconds
	 *            Give up task retry when this int time have been passed after
	 *            the first retry. This is in timeUnits and the system tries at
	 *            one retry after this time but not further ones!
	 * @param minimalRetryIntervalBaseMultiplier
	 *            The minimum time between two retry of a given task in
	 *            timeunits. This will be scaled up by a fibonacci-like growing
	 *            series so each wait will take a little longer at minimum.
	 * @param timeUnit
	 *            The time unit used for the parameters!
	 */
	public FibonacciTimedTaskRetryStrategyDescriptor(int giveUpAfter,
			int minimalRetryIntervalBaseMultiplier, TimeUnit timeUnit) {
		this.giveUpAfter = giveUpAfter;
		this.minimalRetryIntervalBaseMultiplier = minimalRetryIntervalBaseMultiplier;
		this.timeUnit = timeUnit;
	}

	@Override
	public Date calculateNextRetryDate(Date firstRetryTime, Date lastRetryTime) {
		if (firstRetryTime == null || lastRetryTime == null) {
			// In case we say the retry time for this task the first time,
			// return with the time difference multiplier basis...
			return incrementDateWith(new Date(),
					minimalRetryIntervalBaseMultiplier, timeUnit);
		}

		// When zero units are given, it means to never give up!
		if (giveUpAfter != 0) {
			// Calculate the time of giving up
			Date giveUpDate = incrementDateWith(firstRetryTime, giveUpAfter,
					timeUnit);

			// Giving up when the last retry was already after the given
			// time for giving up. We don't care if we are after the last
			// possible time, so even in case of contentions, at least once
			// the system will try to retry AFTER the giveup time provided!!!
			if (lastRetryTime.after(giveUpDate)) {
				// Null means to give up
				return null;
			}
		}

		// Calculate how many time units have passed from the first retry to the
		// last one
		long allUnitsFromFirstRetry = getDateDiff(firstRetryTime,
				lastRetryTime, timeUnit);
		// Add the min-interval to this so we get
		// fib(n)*minimalRetryIntervalBaseMultiplier
		long fibnMultiplied = allUnitsFromFirstRetry
				+ minimalRetryIntervalBaseMultiplier;
		// Calculate the fibn value for the last retry point!
		int fibn = (int) fibnMultiplied / minimalRetryIntervalBaseMultiplier;
		// Get a next position in the fib sequence later than the one that makes
		// fibn
		int nplus = approximateNextNFromFiboVal(fibn);
		// Get the next fibonacci sequence value
		int fibnplus = approxFiboVal(nplus);
		// Scale it with the retry interval
		int fibnplusMultipliedUnits = fibnplus
				* minimalRetryIntervalBaseMultiplier;

		// Increment the last time with the current multiplied incrementer
		Date nextRetryTime = incrementDateWith(lastRetryTime,
				fibnplusMultipliedUnits, timeUnit);
		return nextRetryTime;
	}

	private void incrementCalendarWith(Calendar c, int inc, TimeUnit timeUnit) {
		// Don't even add time in intra-sec cases!
		if (timeUnit == TimeUnit.MILLISECONDS
				|| timeUnit == TimeUnit.MICROSECONDS
				|| timeUnit == TimeUnit.NANOSECONDS) {
			// NOP:
		} else if (timeUnit == TimeUnit.SECONDS) {
			// Use seconds if that is the case
			c.add(Calendar.SECOND, inc);
		} else {
			// Otherwise use minutes at least
			int minutes = (int) TimeUnit.MINUTES.convert(inc, timeUnit);
			c.add(Calendar.MINUTE, minutes);
		}
	}

	private Date incrementDateWith(Date date, int inc, TimeUnit timeUnit) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		incrementCalendarWith(c, inc, timeUnit);
		return c.getTime();
	}

	/**
	 * Tries to approximate a value of ++N by the provided fib(N) value!
	 * 
	 * @param fiboVal
	 *            A value of the fibonacci series (but can be an arbitrary
	 *            number); fiboVal = fib(N)
	 * @return The corresponding appriximation of ++N. That is a position of the
	 *         value in the sequence with added at least one position. Basically
	 *         can be used as a base for getting the next, bigger than current
	 *         fibo securely! It might be that we skip one or two value
	 *         sometimes because of rounding problems...
	 */
	private static int approximateNextNFromFiboVal(int fiboVal) {
		// Inverse fibonacci approximation based on the approxFiboVal method!
		// To invert the rounding, I have just added +1 to the fiboVal to
		// counter the possible rounding flaw. This is of course not correct
		// and this is why this method is an approximation.
		int nextN = (int) logb(GOLDEN_RATIO, Math.sqrt(5.0) * (fiboVal + 1));
		return nextN;
	}

	/**
	 * Gets the nth number in the fibonacci sequence.
	 * 
	 * @param n
	 * @return Approx.: fib(n)
	 */
	private static int approxFiboVal(int n) {
		// Fibonacci "calculation by rounding"
		// Not really an approximation, but this is how it is, however
		// I didn't dare to check everything considering rounding...
		return (int) ((Math.pow(GOLDEN_RATIO, n) / Math.sqrt(5.0)) + 0.5);
	}

	/** log_b(a) == logb(b, a) */
	private static double logb(double b, double a) {
		return Math.log(a) / Math.log(b);
	}

	/**
	 * Get a diff between two dates - doesn't take summer/winter and stuff into
	 * consideration of course!
	 * 
	 * @param date1
	 *            the earlier date
	 * @param date2
	 *            the later date
	 * @param timeUnit
	 *            the unit in which you want the diff
	 * @return the diff value, in the provided unit
	 */
	private static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
		long diffInMillies = date2.getTime() - date1.getTime();
		return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
	}
}
