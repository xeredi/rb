package com.xeredi.canbus.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

// TODO: Auto-generated Javadoc
/**
 * The Class DateUtil.
 */
public final class DateUtil {

	/** The Constant TIME_ZONE. */
	private static final TimeZone TIME_ZONE = TimeZone.getTimeZone("UTC");

	/** The Constant DATE_FORMAT. */
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("ddMMyyHHmmss.SSS");

	/**
	 * Gets the date.
	 *
	 * @return the date
	 */
	public static final Date getDate() {
		final Calendar calendar = Calendar.getInstance(TIME_ZONE);

		calendar.set(Calendar.MILLISECOND, 0);

		return calendar.getTime();
	}

	/**
	 * Gets the date string.
	 *
	 * @return the date string
	 */
	public static final String getDateString() {
		return DATE_FORMAT.format(getDate());
	}

	/**
	 * Format.
	 *
	 * @param date
	 *            the date
	 * @return the string
	 */
	public static final String format(final Date date) {
		return DATE_FORMAT.format(date);
	}

	/**
	 * Parses the.
	 *
	 * @param date
	 *            the date
	 * @return the date
	 * @throws ParseException
	 *             the parse exception
	 */
	public static final Date parse(final String date) throws ParseException {
		return DATE_FORMAT.parse(date);
	}

}
