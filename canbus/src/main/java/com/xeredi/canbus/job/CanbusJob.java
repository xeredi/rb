package com.xeredi.canbus.job;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.bluetooth.UUID;

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

import com.xeredi.canbus.bluetooth.BluetoothSearch;
import com.xeredi.canbus.bluetooth.BluetoothServiceInfo;
import com.xeredi.canbus.util.ConfigurationKey;
import com.xeredi.canbus.util.ConfigurationUtil;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

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

	/** The Constant CANBUS_OBDCODES. */
	private static final String[] CANBUS_OBDCODES = ConfigurationUtil.getStringArray(ConfigurationKey.canbus_obdcodes);

	/** The Constant CANBUS_PORT_SPEED. */
	private static final int CANBUS_PORT_SPEED = ConfigurationUtil.getInteger(ConfigurationKey.canbus_port_speed);

	/** The Constant CANBUS_CONFIGURATION. */
	private static PropertiesConfiguration CANBUS_CONFIGURATION = null;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doExecute(final JobExecutionContext context) {
		CommPort commPort = null;
		boolean dataOk = false;

		final Map<String, String> data = new HashMap<>();

		try {
			final File file = new File(CANBUS_PORT_ID);

			if (file.exists()) {
				dataOk = readData(data);
			} else {
				LOG.info(CANBUS_PORT_ID + " not found");
			}

			if (!dataOk) {
				LOG.info("Load Configuration...");
				loadConfiguration();

				if (!CANBUS_CONFIGURATION.isEmpty()) {
					bind(CANBUS_CONFIGURATION.getString(ConfigurationKey.canbus_host.name()),
							CANBUS_CONFIGURATION.getString(ConfigurationKey.canbus_channel.name()));

					dataOk = readData(data);
				}
			}

			if (!dataOk) {
				LOG.info("Search Bluetooth...");

				final BluetoothSearch bluetoothSearch = new BluetoothSearch();
				final UUID[] uuids = new UUID[] { new UUID(CANBUS_UUID, true) };

				final List<BluetoothServiceInfo> serviceInfos = bluetoothSearch.searchServicesBluecove(uuids, null);

				if (serviceInfos.isEmpty()) {
					LOG.error("No services found");
				} else if (serviceInfos.size() > 1) {
					LOG.error("Too many services found: " + serviceInfos.size());
				} else {
					final BluetoothServiceInfo serviceInfo = serviceInfos.get(0);

					LOG.info("serviceInfo: " + serviceInfo);

					try {
						bind(serviceInfo.getAddressNormalized(), serviceInfo.getChannel());

						dataOk = readData(data);

						if (dataOk) {
							CANBUS_CONFIGURATION.setProperty(ConfigurationKey.canbus_host.name(),
									serviceInfo.getAddressNormalized());
							CANBUS_CONFIGURATION.setProperty(ConfigurationKey.canbus_channel.name(),
									serviceInfo.getChannel());

							saveConfiguration();
						}
					} catch (final IOException ex) {
						LOG.error(ex, ex);
					}
				}
			}

			if (dataOk) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Send MQTT");
				}

				mqttWriter.sendCanbusData(data);
			}
		} catch (final Exception ex) {
			LOG.fatal(ex, ex);
		} finally {
			if (commPort != null) {
				commPort.close();
			}
		}
	}

	/**
	 * Read data.
	 *
	 * @param data
	 *            the data
	 * @return true, if successful
	 */
	private boolean readData(final Map<String, String> data) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Read Data");
		}

		CommPort commPort = null;

		try {
			final CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(CANBUS_PORT_ID);

			if (portIdentifier.isCurrentlyOwned()) {
				LOG.warn("Port in use");
			} else {
				commPort = portIdentifier.open(this.getClass().getName(), 2000);

				if (commPort instanceof SerialPort) {
					final SerialPort serialPort = (SerialPort) commPort;

					serialPort.setSerialPortParams(CANBUS_PORT_SPEED, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
							SerialPort.PARITY_NONE);

					try (final InputStream is = serialPort.getInputStream();
							final OutputStream os = serialPort.getOutputStream();) {
						for (int i = 0; i < CANBUS_OBDCODES.length; i++) {
							final String command = CANBUS_OBDCODES[i].trim();

							data.put(command, read(command, is, os));
						}

						return true;
					}
				} else {
					LOG.error("No SerialPort Type");
				}
			}
		} catch (final Exception ex) {
			LOG.error(ex, ex);
		} finally {
			if (commPort != null) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Close commPort");
				}

				commPort.close();
			}
		}

		return false;
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
		if (LOG.isDebugEnabled()) {
			LOG.debug("command: " + command);
		}

		os.write((command + "\r").getBytes());

		// Thread.sleep(CANBUS_SLEEP_MS);

		final StringBuilder buffer = new StringBuilder();

		char c;
		byte b = 0;

		while (((b = (byte) is.read()) > -1)) {
			c = (char) b;
			if (c == '>') {
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

	/**
	 * Bind.
	 *
	 * @param address
	 *            the address
	 * @param channel
	 *            the channel
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void bind(final String address, final String channel) throws IOException {
		final File file = new File(CANBUS_PORT_ID);
		final DefaultExecutor executor = new DefaultExecutor();

		if (file.exists()) {
			LOG.info("Release current: " + CANBUS_PORT_ID);

			int releaseValue = executor.execute(CommandLine.parse("sudo rfcomm release " + CANBUS_PORT_ID));

			LOG.info(releaseValue == 0 ? "Release OK" : "Release fail: " + releaseValue);
		}

		int bindValue = executor
				.execute(CommandLine.parse("sudo rfcomm bind " + CANBUS_PORT_ID + " " + address + " " + channel));

		LOG.info(bindValue == 0 ? "Bind OK" : "Bind fail: " + bindValue);
	}
}
