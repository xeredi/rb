package xeredi.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.obex.ClientSession;
import javax.obex.HeaderSet;
import javax.obex.Operation;
import javax.obex.ResponseCodes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import xeredi.obd.CanbusReader;

// TODO: Auto-generated Javadoc
/**
 * The Class BluetoothSearch.
 */
public final class BluetoothSearch {

	/** The Constant LOG. */
	private static final Log LOG = LogFactory.getLog(BluetoothSearch.class);

	/** The local device. */
	private LocalDevice localDevice;

	/** The remote device map. */
	private Map<String, RemoteDevice> remoteDeviceMap;

	/** The service map. */
	private Map<String, Map<String, ServiceRecord>> serviceMap;

	/**
	 * Instantiates a new bluetooth search.
	 */
	public BluetoothSearch() {
		super();

		remoteDeviceMap = new HashMap<>();
		serviceMap = new HashMap<>();
	}

	/**
	 * Sets the local device bluecove.
	 */
	private void setLocalDeviceBluecove() {
		try {
			localDevice = LocalDevice.getLocalDevice();

			if (LOG.isDebugEnabled()) {
				LOG.debug("localDevice: " + " - BluetoothAddress: " + localDevice.getBluetoothAddress()
						+ " - FriendlyName: " + localDevice.getFriendlyName() + " - Discoverable: "
						+ localDevice.getDiscoverable());
			}
		} catch (final BluetoothStateException ex) {
			LOG.error(ex, ex);
		}
	}

	/**
	 * Search services bluecove.
	 *
	 * @param uuids
	 *            the uuids
	 * @param attrIds
	 *            the attr ids
	 */
	private void searchServicesBluecove(final UUID[] uuids, final int[] attrIds) {
		LOG.info("Search Services");

		for (final String name : remoteDeviceMap.keySet()) {
			final RemoteDevice remoteDevice = remoteDeviceMap.get(name);

			LOG.info("Services from: " + name + " : " + remoteDevice.getBluetoothAddress());

			try {
				final BluetoothServiceDiscoveryListener listener = new BluetoothServiceDiscoveryListener();

				synchronized (listener) {
					final int transactionId = localDevice.getDiscoveryAgent().searchServices(attrIds, uuids,
							remoteDevice, listener);

					if (transactionId > 0) {
						LOG.info("wait for service inquiry to complete...");

						try {
							listener.wait();

							serviceMap.put(name, listener.getServiceMap());
						} catch (final InterruptedException ex) {
							LOG.fatal(ex, ex);
						}

						LOG.info("service inquiry completed");
					}
				}
			} catch (final BluetoothStateException ex) {
				LOG.error(ex, ex);
			}
		}
	}

	/**
	 * Search devices.
	 *
	 * @param accessCode
	 *            the access code
	 * @param retry
	 *            the retry
	 */
	public void searchDevicesBluecove(final int accessCode, final boolean retry) {
		try {
			final DiscoveryAgent agent = localDevice.getDiscoveryAgent();

			do {
				final BluetoothDeviceDiscoveryListener listener = new BluetoothDeviceDiscoveryListener();

				synchronized (listener) {
					final boolean started = agent.startInquiry(accessCode, listener);
					if (started) {
						if (LOG.isDebugEnabled()) {
							LOG.debug("wait for device inquiry to complete...");
						}

						try {
							listener.wait();

							remoteDeviceMap = listener.getDevicesMap();
						} catch (final InterruptedException ex) {
							LOG.fatal(ex, ex);
						}

						if (LOG.isDebugEnabled()) {
							LOG.debug(remoteDeviceMap.size() + " device(s) found");
						}
					}
				}
			} while (remoteDeviceMap.isEmpty() && retry);
		} catch (final BluetoothStateException ex) {
			LOG.error(ex, ex);
		}
	}

	/**
	 * Test connection bluecove.
	 *
	 * @param url
	 *            the url
	 * @return the client session
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public ClientSession openConnectionBluecove(final String url) throws IOException {
		if (LOG.isInfoEnabled()) {
			LOG.info("Connect to URL: " + url);
		}

		final ClientSession clientSession = (ClientSession) Connector.open(url);
		final BluetoothAuthenticator authenticator = new BluetoothAuthenticator();

		clientSession.setAuthenticator(authenticator);

		final HeaderSet serverReply = clientSession.connect(null);

		if (serverReply.getResponseCode() == ResponseCodes.OBEX_HTTP_OK) {
			if (LOG.isInfoEnabled()) {
				LOG.info("Connection OK");
			}
		} else {
			if (LOG.isInfoEnabled()) {
				LOG.error("Connection Error. Code: " + serverReply.getResponseCode());
			}
		}

		return clientSession;
	}

	/**
	 * Send message bluecove.
	 *
	 * @param clientSession
	 *            the client session
	 * @param message
	 *            the message
	 * @param maxMessages
	 *            the max messages
	 */
	public void sendMessageBluecove(final ClientSession clientSession, final String message, final int maxMessages) {
		final HeaderSet request = clientSession.createHeaderSet();

		// request.setHeader(HeaderSet.NAME, "Hello.txt");
		// request.setHeader(HeaderSet.TYPE, "text");

		Operation putOperation = null;

		try {
			putOperation = clientSession.put(request);

			try (final InputStream is = putOperation.openInputStream();
					final OutputStream os = putOperation.openOutputStream()) {
				for (int i = 0; i < maxMessages; i++) {
					if (LOG.isDebugEnabled()) {
						LOG.debug("Write");
					}

					os.write(message.getBytes());
				}

				os.flush();
			}
		} catch (final IOException ex) {
			LOG.error(ex, ex);
		} finally {
			if (putOperation != null) {
				try {
					putOperation.close();
				} catch (final IOException ex) {
					LOG.fatal(ex, ex);
				}
			}
		}
	}

	/**
	 * Send message bluecove.
	 *
	 * @param url
	 *            the url
	 * @param message
	 *            the message
	 * @param maxMessages
	 *            the max messages
	 */
	public void sendMessageBluecove(final String url, final String message, final int maxMessages) {
		try {
			final ClientSession clientSession = openConnectionBluecove(url);

			if (LOG.isInfoEnabled()) {
				LOG.info("Send Message");
			}

			sendMessageBluecove(clientSession, message, maxMessages);

			if (LOG.isInfoEnabled()) {
				LOG.info("Close Connection");
			}

			clientSession.close();
		} catch (final IOException ex) {
			LOG.error(ex);
		}
	}

	/**
	 * The main method.
	 *
	 * @param args
	 *            the arguments
	 */
	public static void main(final String[] args) {
		LOG.info("Start");

		final BluetoothSearch bluetoothSearch = new BluetoothSearch();

		if (LOG.isDebugEnabled()) {
			LOG.debug("Bluecove Search");
		}

		bluetoothSearch.setLocalDeviceBluecove();

		if (LOG.isDebugEnabled()) {
			LOG.debug("GIAC");
		}

		bluetoothSearch.searchDevicesBluecove(DiscoveryAgent.GIAC, true);

		// Canbus: 0x1101
		// OBEX Object Push: 0x1105
		// Shit: 0x0100

		final UUID[] uuids = new UUID[] { new UUID(0x0100) }; // OBEX Object Push
		int[] attrIDs = new int[] { 0x0100 }; // Service name

		if (LOG.isDebugEnabled()) {
			LOG.debug("SearchService with UUID: " + uuids[0] + " and attrId: " + attrIDs[0]);
		}

		bluetoothSearch.searchServicesBluecove(uuids, attrIDs);

		final String urlMotoG = "btgoep://24DA9B132084:12;authenticate=false;encrypt=false;master=false";
		final String urlPC = "btgoep://5CF370883424:9;authenticate=false;encrypt=false;master=false";

		bluetoothSearch.sendMessageBluecove(urlMotoG, "Hola, Caracola!!", 10);
		bluetoothSearch.sendMessageBluecove(urlPC, "Hola, Caracola!!", 10);

		try {
			Thread.sleep(5000L);
		} catch (final InterruptedException ex) {
			LOG.fatal(ex, ex);
		}

		LOG.info("Read CAN/BUS");

		ClientSession session = null;

		try {
			session = bluetoothSearch.openConnectionBluecove(urlPC);

			final CanbusReader canbusReader = new CanbusReader(session);

			for (int i = 0; i < 5; i++) {
				try {
					final Map<String, List<Byte>> canbusData = canbusReader.read();

					if (LOG.isDebugEnabled()) {
						LOG.debug("CAN/BUS data: " + canbusData.toString());
					}
				} catch (final IOException ex) {
					LOG.error(ex, ex);
				}
			}
		} catch (final IOException ex) {
			LOG.error(ex, ex);
		} catch (final Throwable ex) {
			LOG.fatal(ex, ex);
		} finally {
			if (session != null) {
				try {
					session.close();
				} catch (final IOException ex) {
					LOG.error(ex, ex);
				}
			}
		}

		LOG.info("End");
	}
}
