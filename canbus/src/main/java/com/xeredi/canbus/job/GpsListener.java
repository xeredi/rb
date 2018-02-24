package com.xeredi.canbus.job;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataEventListener;
import com.xeredi.canbus.mqtt.MqttWriter;
import com.xeredi.canbus.util.ConfigurationKey;
import com.xeredi.canbus.util.ConfigurationUtil;

// TODO: Auto-generated Javadoc
/**
 * The listener interface for receiving gps events.
 * The class that is interested in processing a gps
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addGpsListener<code> method. When
 * the gps event occurs, that object's appropriate
 * method is invoked.
 *
 * @see GpsEvent
 */
public final class GpsListener implements SerialDataEventListener {

	/** The Constant LOG. */
	private static final Log LOG = LogFactory.getLog(GpsListener.class);

	/** The Constant GPRMC_PREFIX. */
	private static final String SEGMENT_PREFIX = ConfigurationUtil.getString(ConfigurationKey.gps_segment_prefix);

	/** The Constant SEGMENT_SEPARATOR. */
	private static final String SEGMENT_SEPARATOR = ConfigurationUtil.getString(ConfigurationKey.gps_segment_separator);

	/** The Constant SEGMENT_MINTOKENS. */
	private static final int SEGMENT_MINTOKENS = ConfigurationUtil.getInteger(ConfigurationKey.gps_segment_mintokens);

	/** The Constant TOKEN_SEPARATOR. */
	private static final String TOKEN_SEPARATOR = ConfigurationUtil.getString(ConfigurationKey.gps_token_separator);

	/** The buffer. */
	private final StringBuilder buffer;

	/** The mqtt writer. */
	private final MqttWriter mqttWriter;

	/**
	 * Instantiates a new gps listener.
	 *
	 * @param mqttWriter the mqtt writer
	 */
	public GpsListener(final MqttWriter mqttWriter) {
		super();

		this.buffer = new StringBuilder();
		this.mqttWriter = mqttWriter;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dataReceived(final SerialDataEvent event) {
		if (LOG.isTraceEnabled()) {
			LOG.trace("Data received");
		}

		try {
			final String data = event.getAsciiString();

			buffer.append(data);

			final int startSegment = buffer.indexOf(SEGMENT_PREFIX);

			if (startSegment >= 0) {
				final int endSegment = buffer.indexOf(SEGMENT_SEPARATOR, startSegment);

				if (endSegment >= 0) {
					final String gpsSegment = buffer.substring(startSegment, endSegment);

					if (LOG.isTraceEnabled()) {
						LOG.trace("gpsSegment: " + gpsSegment);
					}

					buffer.delete(0, endSegment);

					final String[] tokens = gpsSegment.split(TOKEN_SEPARATOR);

					if (tokens.length >= SEGMENT_MINTOKENS) {
						// DateTime validation
						if (tokens[9] != null && tokens[1] != null && !tokens[9].isEmpty() && !tokens[1].isEmpty()) {
							if (LOG.isDebugEnabled()) {
								LOG.debug("valid gpsSegment: " + gpsSegment);
							}

							mqttWriter.sendGpsData(gpsSegment);
						}
					}

					if (LOG.isTraceEnabled()) {
						LOG.trace("new buffer: " + buffer.toString());
					}
				}
			} else {
				if (LOG.isTraceEnabled()) {
					LOG.trace("data: " + data);
				}
			}
		} catch (final IOException ex) {
			LOG.error(ex);
		}
	}
}
