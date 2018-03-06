package com.xeredi.canbus.job;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;

import com.pi4j.io.serial.Baud;
import com.pi4j.io.serial.DataBits;
import com.pi4j.io.serial.FlowControl;
import com.pi4j.io.serial.Parity;
import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialConfig;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.StopBits;
import com.xeredi.canbus.bluetooth.BluetoothSearch;
import com.xeredi.canbus.bluetooth.BluetoothServiceInfo;
import com.xeredi.canbus.util.ConfigurationKey;
import com.xeredi.canbus.util.ConfigurationUtil;

// TODO: Auto-generated Javadoc
/**
 * The Class CanbusJob.
 */
@DisallowConcurrentExecution
public final class CanbusJob extends AbstractJob {

	/** The Constant LOG. */
	private static final Log LOG = LogFactory.getLog(CanbusJob.class);

	/** The Constant CANBUS_UUID. */
	private static final String CANBUS_UUID = ConfigurationUtil.getString(ConfigurationKey.canbus_uuid);

	/** The Constant CANBUS_FILE_CONFIG. */
	private static final String CANBUS_FILE_CONFIG = ConfigurationUtil.getString(ConfigurationKey.canbus_file_config);

	/** The Constant CANBUS_FILE_CONFIG. */
	private static final Long CANBUS_SLEEP_MS = ConfigurationUtil.getLong(ConfigurationKey.canbus_sleep_ms);

	/** The Constant CANBUS_PORT_ID. */
	private static final String CANBUS_PORT_ID = ConfigurationUtil.getString(ConfigurationKey.canbus_port_id);

	/** The Constant CANBUS_PORT_SPEED. */
	private static final int CANBUS_PORT_SPEED = ConfigurationUtil.getInteger(ConfigurationKey.canbus_port_speed);

	/** The Constant CANBUS_CONFIGURATION. */
	private static PropertiesConfiguration CANBUS_CONFIGURATION = null;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doExecute(final JobExecutionContext context) {
		final Serial serial = findSerial();

		if (serial == null) {
			LOG.error("NO serial found");
		} else {
			LOG.info("Serial found");

			LOG.info(serial.isOpen() ? "Serial opened" : "Serial not opened");

			try {
				try (final InputStream is = serial.getInputStream();
						final OutputStream os = serial.getOutputStream();) {
					// LOG.info("ATZ: " + read("ATZ", is, os));
					LOG.info("ATZ: " + read("ATZ", is, os));

					LOG.info("RPM: " + read("01 0C", is, os));
					LOG.info("Speed: " + read("01 0D", is, os));
					LOG.info("Errors: " + read("03", is, os));
				}

				serial.close();
			} catch (final Exception ex) {
				LOG.error(ex, ex);
			}

			SerialFactory.shutdown();
		}
	}

	/**
	 * Read.
	 *
	 * @param command
	 *            the command
	 * @param is
	 *            the is
	 * @param os
	 *            the os
	 * @return the string
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws InterruptedException
	 *             the interrupted exception
	 */
	private String read(final String command, final InputStream is, final OutputStream os)
			throws IOException, InterruptedException {
		byte b = 0;

		if (LOG.isDebugEnabled()) {
			LOG.debug("command: " + command);
		}

		if (is.available() > 0) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("SKIP data");
			}

			while (((b = (byte) is.read()) > -1)) {
				if (LOG.isDebugEnabled()) {
					LOG.info("skip: " + (char) b);
				}
			}
		}

		os.write((command + "\r").getBytes());

		if (LOG.isDebugEnabled()) {
			LOG.debug("Sleep: " + CANBUS_SLEEP_MS);
		}
		Thread.sleep(CANBUS_SLEEP_MS);

		final StringBuilder buffer = new StringBuilder();

		char c;

		if (LOG.isDebugEnabled()) {
			LOG.debug("Read response");
		}
		while (((b = (byte) is.read()) > -1)) {
			c = (char) b;
			if (c == '>') // read until '>' arrives
			{
				break;
			}
			buffer.append(c);
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug("buffer: " + buffer);
		}

		return buffer.toString();
	}

	/**
	 * Find serial 2.
	 *
	 * @return the serial
	 */
	private Serial findSerial() {
		final Serial serial = SerialFactory.createInstance();
		final SerialConfig config = new SerialConfig();
		final Baud baud = Baud.getInstance(CANBUS_PORT_SPEED);

		config.device(CANBUS_PORT_ID).baud(baud).dataBits(DataBits._8).parity(Parity.NONE).stopBits(StopBits._1)
				.flowControl(FlowControl.NONE);

		try {
			loadConfiguration();

			if (CANBUS_CONFIGURATION.isEmpty()) {
				LOG.info("Find bluetooth");

				final BluetoothSearch bluetoothSearch = new BluetoothSearch();
				final javax.bluetooth.UUID[] uuids = new javax.bluetooth.UUID[] {
						new javax.bluetooth.UUID(CANBUS_UUID, true) };

				final List<BluetoothServiceInfo> serviceInfos = bluetoothSearch.searchServicesBluecove(uuids, null);

				if (serviceInfos.isEmpty()) {
					LOG.warn("No services found");
				} else if (serviceInfos.size() > 1) {
					LOG.warn("Too much services found: " + serviceInfos.size());
				} else {
					final BluetoothServiceInfo serviceInfo = serviceInfos.get(0);

					LOG.info("Service found: " + serviceInfo);

					final String host = normalizeBluetoothAddress(serviceInfo.getDeviceAddress());
					final String channel = serviceInfo.getChannel();

					if (serialAvailable(host, channel)) {
						serial.open(config);

						LOG.info("Serial opened. Save configuration");

						CANBUS_CONFIGURATION.setProperty(ConfigurationKey.canbus_host.name(), host);
						CANBUS_CONFIGURATION.setProperty(ConfigurationKey.canbus_channel.name(), channel);

						saveConfiguration();

						return serial;
					}
				}
			} else {
				final String host = CANBUS_CONFIGURATION.getString(ConfigurationKey.canbus_host.name());
				final String channel = CANBUS_CONFIGURATION.getString(ConfigurationKey.canbus_channel.name());

				if (serialAvailable(host, channel)) {
					serial.open(config);

					LOG.info("Serial opened");

					return serial;
				}
			}
		} catch (final Exception ex) {
			LOG.fatal(ex, ex);
		}

		return null;
	}

	/**
	 * Normalize bluetooth address.
	 *
	 * @param address
	 *            the address
	 * @return the string
	 */
	private String normalizeBluetoothAddress(final String address) {
		LOG.info("address: " + address);

		final StringBuilder sb = new StringBuilder();

		for (int i = 0; i < address.length(); i++) {
			if (i > 0 && i < (address.length() - 1) && (i % 2 == 0)) {
				sb.append(':');
			}

			sb.append(address.charAt(i));
		}

		return sb.toString();
	}

	/**
	 * Open serial.
	 *
	 * @param address
	 *            the address
	 * @param channel
	 *            the channel
	 * @return true, if successful
	 */
	private boolean serialAvailable(final String address, final String channel) {
		final DefaultExecutor executor = new DefaultExecutor();

		final String pingLine = "sudo l2ping -c 1 " + address;

		LOG.info("Ping Command: " + pingLine);

		final CommandLine pingCommand = CommandLine.parse(pingLine);

		try {
			int exitValue = executor.execute(pingCommand);

			if (exitValue == 0) {
				LOG.info("Ping OK: " + exitValue);
			} else {
				LOG.info("Ping Fail: " + exitValue);

				return false;
			}

			return true;
		} catch (final IOException ex) {
			LOG.warn(ex.getMessage());
		}

		final File file = new File(CANBUS_PORT_ID);

		if (!file.exists()) {
			final String bindLine = "sudo rfcomm bind " + CANBUS_PORT_ID + " " + address + " " + channel;

			LOG.info("Bind Command: " + bindLine);

			final CommandLine bindCommand = CommandLine.parse(bindLine);

			try {
				int exitValue = executor.execute(bindCommand);

				if (exitValue == 0) {
					LOG.info("Bind OK: " + exitValue);
				} else {
					LOG.info("Bind Fail: " + exitValue);

					return false;
				}
			} catch (final IOException ex) {
				LOG.warn(ex.getMessage());
			}
		}

		return true;
	}

	/**
	 * Load configuration.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws ConfigurationException
	 *             the configuration exception
	 */
	private void loadConfiguration() throws IOException, ConfigurationException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Load configuration: " + CANBUS_FILE_CONFIG);
		}

		final File file = new File(CANBUS_FILE_CONFIG);

		if (!file.exists()) {
			file.getParentFile().mkdirs();
			file.createNewFile();
		}

		CANBUS_CONFIGURATION = (new Configurations()).properties(file);
	}

	/**
	 * Save configuration.
	 *
	 * @throws ConfigurationException
	 *             the configuration exception
	 */
	private void saveConfiguration() throws ConfigurationException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Save configuration: " + CANBUS_FILE_CONFIG);

			final Iterator<String> iterator = CANBUS_CONFIGURATION.getKeys();

			while (iterator.hasNext()) {
				final String key = iterator.next();

				LOG.debug("key: " + key + ", value: " + CANBUS_CONFIGURATION.getString(key));
			}
		}

		final FileHandler handler = new FileHandler(CANBUS_CONFIGURATION);
		final File out = new File(CANBUS_FILE_CONFIG);

		handler.save(out);
	}
}
