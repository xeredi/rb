package com.xeredi.canbus.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
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
	public void setLocalDeviceBluecove() {
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
	public void searchServicesBluecove(final UUID[] uuids, final int[] attrIds) {
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
}
