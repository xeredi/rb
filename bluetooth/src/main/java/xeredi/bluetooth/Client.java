package xeredi.bluetooth;

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

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

// TODO: Auto-generated Javadoc
/**
 * The Class Client.
 */
public class Client {

	/** The Constant LOG. */
	private static final Log LOG = LogFactory.getLog(Client.class);

	/** The Constant GPS_PORT_ID. */
	// private static final String CANBUS_PORT_ID = "/dev/ttyS81";
	private static final String CANBUS_PORT_ID = "/dev/rfcomm0";

	/** The Constant CANBUS_UUID. */
	private static final String CANBUS_UUID = "1101";

	/** The Constant CANBUS_CONFIGURATION_FILE. */
	private static final String CANBUS_CONFIGURATION_FILE = "conf/bluetooth.conf";

	/** The Constant CANBUS_ADDRESS. */
	private static final String CANBUS_ADDRESS = "address";

	/** The Constant CANBUS_CHANNEL_NO. */
	private static final String CANBUS_CHANNEL_NO = "channel";

	/** The Constant GPS_PORT_SPEED. */
	private static final int CANBUS_PORT_SPEED = 115200;

	/** The canbus configuration. */
	private static PropertiesConfiguration CANBUS_CONFIGURATION = null;

	/**
	 * Start.
	 */
	private void start() {
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
					bind(CANBUS_CONFIGURATION.getString(CANBUS_ADDRESS),
							CANBUS_CONFIGURATION.getString(CANBUS_CHANNEL_NO));

					dataOk = readData(data);
				}
			}

			if (!dataOk) {
				LOG.info("Search Bluetooth...");

				final BluetoothFinder bluetoothFinder = new BluetoothFinder();
				final UUID[] uuids = new UUID[] { new UUID(CANBUS_UUID, true) };

				final List<BluetoothServiceInfo> serviceInfos = bluetoothFinder.searchServicesBluecove(uuids, null);

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
							CANBUS_CONFIGURATION.setProperty(CANBUS_ADDRESS, serviceInfo.getAddressNormalized());
							CANBUS_CONFIGURATION.setProperty(CANBUS_CHANNEL_NO, serviceInfo.getChannel());

							saveConfiguration();
						}
					} catch (final IOException ex) {
						LOG.error(ex, ex);
					}
				}
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
		CommPort commPort = null;

		try {
			final CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(CANBUS_PORT_ID);

			if (portIdentifier.isCurrentlyOwned()) {
				LOG.warn("Port in use");
			} else {
				LOG.info("Open");
				commPort = portIdentifier.open(this.getClass().getName(), 2000);

				if (commPort instanceof SerialPort) {
					final SerialPort serialPort = (SerialPort) commPort;

					serialPort.setSerialPortParams(CANBUS_PORT_SPEED, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
							SerialPort.PARITY_NONE);

					try (final InputStream is = serialPort.getInputStream();
							final OutputStream os = serialPort.getOutputStream();) {
						LOG.info("Streams opened");

						data.put("ATZ", read("ATZ", is, os));
						data.put("01 0C", read("01 0C", is, os));
						data.put("01 0D", read("01 0D", is, os));
						data.put("03", read("03", is, os));

						for (final String key : data.keySet()) {
							LOG.info("key: " + key + " - value: " + data.get(key));
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
				LOG.info("Close commPort");
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
			LOG.debug("Load configuration: " + CANBUS_CONFIGURATION_FILE);
		}

		final File file = new File(CANBUS_CONFIGURATION_FILE);

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
			LOG.debug("Save configuration: " + CANBUS_CONFIGURATION_FILE);

			final Iterator<String> iterator = CANBUS_CONFIGURATION.getKeys();

			while (iterator.hasNext()) {
				final String key = iterator.next();

				LOG.debug("key: " + key + ", value: " + CANBUS_CONFIGURATION.getString(key));
			}
		}

		final FileHandler handler = new FileHandler(CANBUS_CONFIGURATION);
		final File out = new File(CANBUS_CONFIGURATION_FILE);

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

	/**
	 * The main method.
	 *
	 * @param args
	 *            the arguments
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws InterruptedException
	 *             the interrupted exception
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		Client client = new Client();

		LOG.info("Start");

		client.start();

		LOG.info("End");
	}
}