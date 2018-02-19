package com.xeredi.canbus.process.canbus;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.obex.ClientSession;
import javax.obex.HeaderSet;
import javax.obex.Operation;
import javax.obex.ResponseCodes;

import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.paho.client.mqttv3.MqttException;

import com.github.pires.obd.commands.engine.RPMCommand;
import com.xeredi.canbus.process.RbProcess;
import com.xeredi.canbus.util.ConfigurationKey;
import com.xeredi.canbus.util.ConfigurationUtil;

// TODO: Auto-generated Javadoc
/**
 * The Class CanbusReaderProcess.
 */
public final class CanbusReaderProcess extends RbProcess implements DiscoveryListener {

	/** The Constant LOG. */
	private static final Log LOG = LogFactory.getLog(CanbusReaderProcess.class);

	/** The Constant CANBUS_UUID. */
	private static final String CANBUS_UUID = ConfigurationUtil.getString(ConfigurationKey.canbus_uuid);

	/** The Constant CANBUS_FILE_CONFIG. */
	private static final String CANBUS_FILE_CONFIG = ConfigurationUtil.getString(ConfigurationKey.canbus_file_config);

	/** The Constant CANBUS_CONFIGURATION. */
	private static final CombinedConfiguration CANBUS_CONFIGURATION = new CombinedConfiguration();

	/** The client session. */
	private ClientSession clientSession;

	/** The remote devices. */
	private final Map<String, RemoteDevice> remoteDevices = new HashMap<>();

	/** The remote services. */
	private final Map<String, Map<String, ServiceRecord>> remoteServices = new HashMap<>();

	/** The lock. */
	private final Object lock = new Object();

	/**
	 * Instantiates a new canbus reader process.
	 *
	 * @throws MqttException
	 *             the mqtt exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public CanbusReaderProcess() throws MqttException, IOException {
		super();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deviceDiscovered(final RemoteDevice btDevice, final DeviceClass cod) {
		// CanbusUtil.printRemoteDevice(btDevice);

		final String name = CanbusUtil.getName(btDevice);
		final String address = btDevice.getBluetoothAddress();

		remoteDevices.put(name, btDevice);
		remoteServices.put(address, new HashMap<>());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void servicesDiscovered(final int transID, final ServiceRecord[] servRecord) {
		LOG.info("Service Discovered!");

		CanbusUtil.printService(transID, servRecord);

		for (int i = 0; i < servRecord.length; i++) {
			final ServiceRecord record = servRecord[i];

			final String address = record.getHostDevice().getBluetoothAddress();
			final String uuid = CanbusUtil.getUUID(record);
			final String connectionURL = CanbusUtil.getConnectionURL(record);

			remoteServices.get(address).put(uuid, record);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void serviceSearchCompleted(final int transID, final int respCode) {
		LOG.info("Service search completed!");

		switch (respCode) {
		case DiscoveryListener.SERVICE_SEARCH_COMPLETED:
			LOG.info("SERVICE_SEARCH_COMPLETED");
			break;
		case DiscoveryListener.SERVICE_SEARCH_DEVICE_NOT_REACHABLE:
			LOG.info("SERVICE_SEARCH_DEVICE_NOT_REACHABLE");
			break;
		case DiscoveryListener.SERVICE_SEARCH_ERROR:
			LOG.info("SERVICE_SEARCH_ERROR");
			break;
		case DiscoveryListener.SERVICE_SEARCH_NO_RECORDS:
			LOG.info("SERVICE_SEARCH_NO_RECORDS");
			break;
		case DiscoveryListener.SERVICE_SEARCH_TERMINATED:
			LOG.info("SERVICE_SEARCH_TERMINATED");
			break;
		default:
			LOG.error("UNKNOWN_SERVICE_SEARCH");
			break;
		}

		synchronized (lock) {
			lock.notifyAll();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void inquiryCompleted(final int discType) {
		LOG.info("Device Inquiry completed!");

		switch (discType) {
		case DiscoveryListener.INQUIRY_COMPLETED:
			LOG.info("INQUIRY_COMPLETED");
			break;
		case DiscoveryListener.INQUIRY_ERROR:
			LOG.info("INQUIRY_ERROR");
			break;
		case DiscoveryListener.INQUIRY_TERMINATED:
			LOG.info("INQUIRY_TERMINATED");
			break;

		default:
			LOG.info("UNKNOWN_SERVICE_SEARCH");
			break;
		}

		synchronized (lock) {
			lock.notifyAll();
		}
	}

	private boolean connect(final String url) {
		try {
			LOG.info("Create session. url: " + url);
			clientSession = (ClientSession) Connector.open(url);

			LOG.info("Connect");
			final HeaderSet serverReply = clientSession.connect(null);

			if (serverReply.getResponseCode() == ResponseCodes.OBEX_HTTP_OK) {
				LOG.info("Connection OK");

				return true;
			} else {
				LOG.error("Connection Error. Code: " + serverReply.getResponseCode());

				clientSession.close();
				clientSession = null;
			}
		} catch (final IOException ex) {
			LOG.fatal(ex, ex);
		}

		return false;
	}

	/**
	 * Find url.
	 *
	 * @return the string
	 */
	private void findCanbusUrl() {
		while (clientSession == null) {
			LOG.info("Find canbus url in: " + CANBUS_FILE_CONFIG);

			final File file = new File(CANBUS_FILE_CONFIG);

			if (file.exists()) {
				try {
					CANBUS_CONFIGURATION
							.addConfiguration((new Configurations()).properties(new File(CANBUS_FILE_CONFIG)));

					final String url = CANBUS_CONFIGURATION.getString(ConfigurationKey.canbus_url.name());

					if (connect(url)) {
						LOG.info("Valid URL from configuration: " + url);

						return;
					}
				} catch (final ConfigurationException ex) {
					LOG.fatal(ex, ex);
				}
			}

			LOG.info("Search canbus device");

			try {
				final LocalDevice localDevice = LocalDevice.getLocalDevice();

				if (LOG.isInfoEnabled()) {
					LOG.info("LocalDevice. Name: " + localDevice.getFriendlyName() + ", address: "
							+ localDevice.getBluetoothAddress());
				}

				final DiscoveryAgent agent = localDevice.getDiscoveryAgent();

				synchronized (lock) {
					final boolean started = agent.startInquiry(DiscoveryAgent.GIAC, this);
					if (started) {
						LOG.info("wait for device inquiry to complete...");
						lock.wait();
						LOG.info(remoteDevices.size() + " device(s) found");
					}

					if (!remoteDevices.isEmpty()) {
						LOG.info("Search Services");

						final UUID[] uuids = new UUID[] { new UUID(0x0100)/* , new UUID(0x1106) , new UUID(0x110c) */ };

						for (final RemoteDevice remoteDevice : remoteDevices.values()) {
							LOG.info("Services from: " + remoteDevice.getBluetoothAddress());

							final int transactionId = localDevice.getDiscoveryAgent().searchServices(null, uuids,
									remoteDevice, this);

							if (transactionId > 0) {
								LOG.info("wait for service inquiry to complete...");
								lock.wait();
								LOG.info("service inquiry completed");
							}
						}
					}
				}

				for (final RemoteDevice remoteDevice : remoteDevices.values()) {
					final ServiceRecord serviceRecord = remoteServices.get(remoteDevice.getBluetoothAddress())
							.get(CANBUS_UUID);

					if (serviceRecord != null) {
						LOG.info(CANBUS_UUID + " found in host: " + remoteDevice.getBluetoothAddress());

						final String url = CanbusUtil.getConnectionURL(serviceRecord);

						if (connect(url)) {
							LOG.info("Valid URL from service search: " + url);

							// TODO Save URL

							return;
						}
					}
				}
			} catch (final BluetoothStateException ex) {
				LOG.fatal(ex, ex);
			} catch (final InterruptedException ex) {
				LOG.fatal(ex, ex);
			}
		}
	}

	/**
	 * Read data.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void readData() throws IOException {
		LOG.info("Write Message");

		final HeaderSet request = clientSession.createHeaderSet();

		request.setHeader(HeaderSet.NAME, "Hello.txt");
		request.setHeader(HeaderSet.TYPE, "text");

		LOG.info("Operation");
		final Operation putOperation = clientSession.put(request);

		try (final InputStream is = putOperation.openInputStream();
				final OutputStream os = putOperation.openOutputStream()) {
			for (int i = 0; i < 6; i++) {
				LOG.info("Write");
				os.write("Test".getBytes());
			}

			try {
				LOG.info("RPM read");

				final RPMCommand command = new RPMCommand();

				command.run(is, os);

				LOG.info("RMP: " + command.getRPM());
			} catch (final InterruptedException ex) {
				LOG.fatal(ex, ex);
			}

			os.flush();
		}

		LOG.info("Close");
		putOperation.close();

	}

	/**
	 * Execute.
	 */
	public void execute() {
		try {
			for (int i = 0; i < 4; i++) {
				findCanbusUrl();
				readData();
			}
		} catch (final IOException ex) {
			LOG.fatal(ex, ex);
		}
	}

	/**
	 * The main method.
	 *
	 * @param args
	 *            the arguments
	 */
	public static void main(final String[] args) {
		if (args.length > 0) {
			System.out.println("Reading arguments");

			if ("--version".equals(args[0])) {
				System.out.println("Version: " + ConfigurationUtil.getString(ConfigurationKey.app_version));
			}
		} else {
			LOG.info("Start proccess. Release: " + ConfigurationUtil.getString(ConfigurationKey.app_version));

			try {
				final CanbusReaderProcess process = new CanbusReaderProcess();

				process.execute();
			} catch (final Throwable ex) {
				LOG.fatal(ex, ex);
			}

			LOG.info("End proccess");
		}
	}

}
