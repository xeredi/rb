package com.xeredi.canbus.process.gps;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Enumeration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.paho.client.mqttv3.MqttException;

import com.xeredi.canbus.process.RbProcess;
import com.xeredi.canbus.util.ConfigurationKey;
import com.xeredi.canbus.util.ConfigurationUtil;
import com.xeredi.canbus.util.DateUtil;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

// TODO: Auto-generated Javadoc
/**
 * The Class GpsReader.
 */
public final class GpsReaderProcess extends RbProcess implements SerialPortEventListener {
	/** The Constant LOG. */
	private static final Log LOG = LogFactory.getLog(GpsReaderProcess.class);

	/** The Constant GPS_PORT_ID. */
	private static final String GPS_PORT_ID = ConfigurationUtil.getString(ConfigurationKey.gps_port_id);

	/** The Constant GPS_PORT_SPEED. */
	private static final int GPS_PORT_SPEED = ConfigurationUtil.getInteger(ConfigurationKey.gps_port_speed);

	/** The Constant GPRMC_PREFIX. */
	private static final String SEGMENT_PREFIX = ConfigurationUtil.getString(ConfigurationKey.gps_segment_prefix);

	/** The Constant SEGMENT_SEPARATOR. */
	private static final char SEGMENT_SEPARATOR = ConfigurationUtil
			.getCharacter(ConfigurationKey.gps_segment_separator);

	/** The Constant SEGMENT_MINTOKENS. */
	private static final int SEGMENT_MINTOKENS = ConfigurationUtil.getInteger(ConfigurationKey.gps_segment_mintokens);

	/** The Constant TOKEN_SEPARATOR. */
	private static final String TOKEN_SEPARATOR = ConfigurationUtil.getString(ConfigurationKey.gps_token_separator);

	/** The buffer. */
	private final StringBuffer buffer;

	/** The last data time inms. */
	private long lastDataTimeInms;

	/** The in stream. */
	private InputStream inStream = null;

	/**
	 * Instantiates a new gps reader.
	 *
	 * @throws MqttException
	 *             the mqtt exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public GpsReaderProcess() throws MqttException, IOException {
		super();

		this.buffer = new StringBuffer();
		this.lastDataTimeInms = Calendar.getInstance().getTimeInMillis();
	}

	/**
	 * Ports info.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws InterruptedException
	 *             the interrupted exception
	 */
	public void portsInfo() throws IOException, InterruptedException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Ports Info: ");
		}

		@SuppressWarnings("unchecked")
		final Enumeration<CommPortIdentifier> enumeration = CommPortIdentifier.getPortIdentifiers();

		while (enumeration.hasMoreElements()) {
			final CommPortIdentifier commPortIdentifier = enumeration.nextElement();

			if (LOG.isDebugEnabled()) {
				LOG.debug("\tPort: " + commPortIdentifier.getName() + ", " + commPortIdentifier.getPortType());
			}
		}
	}

	/**
	 * Start serial port.
	 */
	public void startSerialPort() {
		int sleepTime = BASE_SLEEPTIME_MS;

		while (true) {
			if (LOG.isInfoEnabled()) {
				LOG.info("Find Serial Port: " + GPS_PORT_ID);
			}

			SerialPort serialPort = null;

			try {
				final CommPortIdentifier commPortIdentifier = CommPortIdentifier.getPortIdentifier(GPS_PORT_ID);

				serialPort = (SerialPort) commPortIdentifier.open("GPS application", 20000);

				serialPort.setSerialPortParams(GPS_PORT_SPEED, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
						SerialPort.PARITY_NONE);

				serialPort.addEventListener(this);

				serialPort.notifyOnDataAvailable(true);
				serialPort.notifyOnBreakInterrupt(true);
				serialPort.notifyOnCarrierDetect(true);
				serialPort.notifyOnCTS(true);
				serialPort.notifyOnDSR(true);
				serialPort.notifyOnFramingError(true);
				serialPort.notifyOnOutputEmpty(true);
				serialPort.notifyOnOverrunError(true);
				serialPort.notifyOnParityError(true);
				serialPort.notifyOnRingIndicator(true);

				this.inStream = serialPort.getInputStream();

				if (LOG.isInfoEnabled()) {
					LOG.info("Find Serial Port Success");
				}

				while (true) {
					continue;
				}
			} catch (final Exception ex) {
				LOG.error(ex, ex);
			} finally {
				if (serialPort != null) {
					if (LOG.isInfoEnabled()) {
						LOG.info("Close serial port");
					}

					serialPort.close();
				}
			}

			sleepTime = Math.min(sleepTime * SLEEPTIME_MULTIPLIER, MAX_SLEEPTIME_MS);

			if (LOG.isInfoEnabled()) {
				LOG.info("Sleep (msec): " + sleepTime);
			}

			try {
				Thread.sleep(sleepTime);
			} catch (final InterruptedException ex) {
				LOG.error(ex, ex);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void serialEvent(final SerialPortEvent ev) {
		try {
			while (this.inStream.available() > 0) {
				int b = this.inStream.read();
				this.lastDataTimeInms = Calendar.getInstance().getTimeInMillis();

				if (SEGMENT_SEPARATOR == (char) b) {
					if (isValidSegment(buffer.toString())) {
						if (LOG.isDebugEnabled()) {
							LOG.debug(buffer);
						}

						mqttWriter.sendGpsData(buffer.toString());
						// } else {
						// if (LOG.isDebugEnabled()) {
						// LOG.debug("Descartado: " + buffer);
						// }
					}

					buffer.setLength(0);
				} else {
					buffer.append((char) b);
				}
			}

			if (Calendar.getInstance().getTimeInMillis() > (this.lastDataTimeInms + 10000)) {
				LOG.info("So many time without data!!");

				this.lastDataTimeInms = Calendar.getInstance().getTimeInMillis();
			}
		} catch (final IOException ex) {
			LOG.fatal(ex, ex);
		}
	}

	/**
	 * Checks if is valid segment.
	 *
	 * @param segment
	 *            the segment
	 * @return true, if is valid segment
	 */
	private boolean isValidSegment(final String segment) {
		if (!segment.startsWith(SEGMENT_PREFIX)) {
			return false;
		}

		final String[] tokens = segment.split(TOKEN_SEPARATOR);

		if (tokens.length < SEGMENT_MINTOKENS) {
			return false;
		}

		// DateTime validation
		if (tokens[9] == null || tokens[1] == null || tokens[9].isEmpty() || tokens[1].isEmpty()) {
			return false;
		}

		try {
			final Calendar calendar = Calendar.getInstance();

			calendar.setTime(DateUtil.parse(tokens[9] + tokens[1]));

			// if ((calendar.get(Calendar.SECOND) % 2) != 0) {
			// return false;
			// }
		} catch (final ParseException ex) {
			return false;
		}

		return true;
	}

	/**
	 * The main method.
	 *
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {
		if (LOG.isInfoEnabled()) {
			LOG.info("Start");
		}

		try {
			final GpsReaderProcess gpsReader = new GpsReaderProcess();

			gpsReader.portsInfo();
			gpsReader.startSerialPort();
		} catch (final Throwable ex) {
			LOG.error(ex, ex);
		}

		if (LOG.isInfoEnabled()) {
			LOG.info("End");
		}
	}
}