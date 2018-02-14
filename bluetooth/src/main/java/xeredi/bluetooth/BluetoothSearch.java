package xeredi.bluetooth;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DataElement;
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
import javax.obex.ResponseCodes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class BluetoothSearch.
 */
public final class BluetoothSearch implements DiscoveryListener {

	/** The Constant LOG. */
	private static final Log LOG = LogFactory.getLog(BluetoothSearch.class);

	/** The local device. */
	private LocalDevice localDevice;

	/** The remote device map. */
	private Map<String, RemoteDevice> remoteDeviceMap;

	/** The service map. */
	private Map<String, Map<String, ServiceRecord>> serviceMap;

	/** The lock. */
	private Object lock;

	/**
	 * Instantiates a new bluetooth search.
	 */
	public BluetoothSearch() {
		super();

		lock = new Object();
		remoteDeviceMap = new HashMap<>();
		serviceMap = new HashMap<>();
	}

	/**
	 * {@inheritDoc}
	 */
	public void deviceDiscovered(final RemoteDevice btDevice, final DeviceClass cod) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("DeviceDiscovered: " + btDevice.getBluetoothAddress());
		}

		String name = null;
		int nameAttempts = 0;
		int maxNameAttempts = 5;

		do {
			try {
				name = btDevice.getFriendlyName(true);
			} catch (final IOException ex) {
				nameAttempts++;

				LOG.error(ex);

				try {
					Thread.sleep(500);
				} catch (final InterruptedException e) {
					LOG.fatal(e, e);
				}
			}
		} while ((name == null) && (nameAttempts < maxNameAttempts));

		if (name == null) {
			LOG.warn("Couldn't find name for address: " + btDevice.getBluetoothAddress());

			name = btDevice.getBluetoothAddress();
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug(" - Address: " + btDevice.getBluetoothAddress() + " - Name: " + name);
		}

		if (btDevice.isAuthenticated()) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Already authenticated");
			}
		} else {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Authenticate");
			}

			try {
				btDevice.authenticate();

				LOG.debug("Authentication OK");
			} catch (final IOException ex) {
				LOG.error(ex, ex);
			}

		}

		remoteDeviceMap.put(name, btDevice);
	}

	/**
	 * {@inheritDoc}
	 */
	public void servicesDiscovered(final int transID, final ServiceRecord[] servRecords) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("servicesDiscovered");
		}

		for (int i = 0; i < servRecords.length; i++) {
			final ServiceRecord record = servRecords[i];

			final String address = record.getHostDevice().getBluetoothAddress();
			final String uuid = getUUID(record);
			final String name = getName(record);
			final String connectionURL = getConnectionURL(record);

			if (LOG.isDebugEnabled()) {
				LOG.debug("ServiceRecord: " + " - address: " + address + " - uuid: " + uuid + " - connectionURL: "
						+ connectionURL + " - name: " + name);
			}

			if (!serviceMap.containsKey(address)) {
				serviceMap.put(address, new HashMap<>());
			}

			serviceMap.get(address).put(uuid, record);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void serviceSearchCompleted(final int transID, final int respCode) {
		String type = null;

		switch (respCode) {
		case DiscoveryListener.SERVICE_SEARCH_COMPLETED:
			type = "SERVICE_SEARCH_COMPLETED";
			break;
		case DiscoveryListener.SERVICE_SEARCH_DEVICE_NOT_REACHABLE:
			type = "SERVICE_SEARCH_DEVICE_NOT_REACHABLE";
			break;
		case DiscoveryListener.SERVICE_SEARCH_ERROR:
			type = "SERVICE_SEARCH_ERROR";
			break;
		case DiscoveryListener.SERVICE_SEARCH_NO_RECORDS:
			type = "SERVICE_SEARCH_NO_RECORDS";
			break;
		case DiscoveryListener.SERVICE_SEARCH_TERMINATED:
			type = "SERVICE_SEARCH_TERMINATED";
			break;
		default:
			type = "UNKNOWN_SERVICE_SEARCH";
			break;
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug("serviceSearchCompleted." + " - Type: " + type);
		}

		synchronized (lock) {
			lock.notifyAll();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void inquiryCompleted(final int discType) {
		String type = null;

		switch (discType) {
		case DiscoveryListener.INQUIRY_COMPLETED:
			type = "INQUIRY_COMPLETED";
			break;
		case DiscoveryListener.INQUIRY_ERROR:
			type = "INQUIRY_ERROR";
			break;
		case DiscoveryListener.INQUIRY_TERMINATED:
			type = "INQUIRY_TERMINATED";
			break;

		default:
			type = "UNKNOWN_SERVICE_SEARCH";
			break;
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug("inquiryCompleted." + " - Type: " + type);
		}

		synchronized (lock) {
			lock.notifyAll();
		}
	}

	/**
	 * Gets the connection URL.
	 *
	 * @param record
	 *            the record
	 * @return the connection URL
	 */
	private String getConnectionURL(final ServiceRecord record) {
		return record.getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
	}

	/**
	 * Gets the uuid.
	 *
	 * @param record
	 *            the record
	 * @return the uuid
	 */
	private String getUUID(final ServiceRecord record) {
		String uuid = null;

		final DataElement serviceUUID = (DataElement) ((Enumeration) record.getAttributeValue(0x0001).getValue())
				.nextElement();

		uuid = serviceUUID.getValue().toString();

		return uuid;
	}

	/**
	 * Gets the name.
	 *
	 * @param record
	 *            the record
	 * @return the name
	 */
	private String getName(final ServiceRecord record) {
		String name = null;

		final DataElement element = record.getAttributeValue(0x0100);

		if (element != null) {
			name = (String) element.getValue();
		}

		return name;
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
				synchronized (lock) {
					final int transactionId = localDevice.getDiscoveryAgent().searchServices(attrIds, uuids,
							remoteDevice, this);

					if (transactionId > 0) {
						LOG.info("wait for service inquiry to complete...");

						try {
							lock.wait();
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
				synchronized (lock) {
					final boolean started = agent.startInquiry(accessCode, this);
					if (started) {
						if (LOG.isDebugEnabled()) {
							LOG.debug("wait for device inquiry to complete...");
						}

						try {
							lock.wait();
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

		// if (LOG.isDebugEnabled()) {
		// LOG.debug("LIAC");
		// }
		//
		// bluetoothSearch.searchDevicesBluecove(DiscoveryAgent.LIAC, true);

		final UUID[] uuids = new UUID[] { new UUID(0x0100 /* 0x1105 */) }; // OBEX Object Push
		int[] attrIDs = new int[] { 0x0100 }; // Service name

		if (LOG.isDebugEnabled()) {
			LOG.debug("SearchService with UUID: " + uuids[0] + " and attrId: " + attrIDs[0]);
		}

		bluetoothSearch.searchServicesBluecove(uuids, attrIDs);

		{
			final String url = "btgoep://24DA9B132084:12;authenticate=false;encrypt=false;master=false"; // MotoG

			try {
				final ClientSession clientSession = bluetoothSearch.openConnectionBluecove(url);

				if (LOG.isInfoEnabled()) {
					LOG.info("Close Connection");
				}

				clientSession.close();
			} catch (final IOException ex) {
				LOG.error(ex);
			}
		}

		{
			final String url = "btspp://5CF370883424:12;authenticate=false;encrypt=false;master=false"; // PCG

			try {
				final ClientSession clientSession = bluetoothSearch.openConnectionBluecove(url);

				if (LOG.isInfoEnabled()) {
					LOG.info("Close Connection");
				}

				clientSession.close();
			} catch (final IOException ex) {
				LOG.error(ex);
			}
		}

		LOG.info("End");
	}
}
