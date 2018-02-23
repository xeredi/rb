package com.xeredi.canbus.process.gps;

import java.io.IOException;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.paho.client.mqttv3.MqttException;

import com.pi4j.io.serial.Baud;
import com.pi4j.io.serial.DataBits;
import com.pi4j.io.serial.FlowControl;
import com.pi4j.io.serial.Parity;
import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialConfig;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataEventListener;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.SerialPort;
import com.pi4j.io.serial.StopBits;
import com.pi4j.util.CommandArgumentParser;
import com.pi4j.util.Console;
import com.xeredi.canbus.process.RbProcess;
import com.xeredi.canbus.util.ConfigurationKey;
import com.xeredi.canbus.util.ConfigurationUtil;

// TODO: Auto-generated Javadoc
/**
 * The Class GpsPi4jProcess.
 */
public final class GpsPi4jProcess extends RbProcess implements SerialDataEventListener {

	/** The Constant LOG. */
	private static final Log LOG = LogFactory.getLog(GpsPi4jProcess.class);

	/** The Constant GPS_PORT_ID. */
	private static final String GPS_PORT_ID = ConfigurationUtil.getString(ConfigurationKey.gps_port_id);

	/** The Constant GPS_PORT_SPEED. */
	private static final int GPS_PORT_SPEED = ConfigurationUtil.getInteger(ConfigurationKey.gps_port_speed);

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

	/**
	 * Instantiates a new gps pi 4 j process.
	 *
	 * @throws MqttException
	 *             the mqtt exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public GpsPi4jProcess() throws MqttException, IOException {
		super();

		buffer = new StringBuilder();
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

	/**
	 * Execute.
	 */
	public void execute() {
		LOG.info("Start");

		final Serial serial = SerialFactory.createInstance();

		serial.addListener(this);

		try {
			final SerialConfig config = new SerialConfig();
			final Baud baud = Baud.getInstance(GPS_PORT_SPEED);

			config.device(GPS_PORT_ID).baud(baud).dataBits(DataBits._8).parity(Parity.NONE).stopBits(StopBits._1)
					.flowControl(FlowControl.NONE);

			serial.open(config);
		} catch (IOException ex) {
			LOG.fatal(ex, ex);

			if (serial.isOpen()) {
				try {
					serial.close();
				} catch (final IOException e) {
					LOG.fatal(e, e);
				}
			}

			try {
				Thread.sleep(MAX_SLEEPTIME_MS);
			} catch (final InterruptedException e) {
				LOG.fatal(e, e);
			}
		}
	}
}
