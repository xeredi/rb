package com.xeredi.canbus.bluetooth;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.bluetooth.DataElement;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// TODO: Auto-generated Javadoc
/**
 * The listener interface for receiving bluetoothServiceDiscovery events. The
 * class that is interested in processing a bluetoothServiceDiscovery event
 * implements this interface, and the object created with that class is
 * registered with a component using the component's
 * <code>addBluetoothServiceDiscoveryListener<code> method. When the
 * bluetoothServiceDiscovery event occurs, that object's appropriate method is
 * invoked.
 *
 * @see BluetoothServiceDiscoveryEvent
 */
public final class BluetoothServiceDiscoveryListener implements DiscoveryListener {

	/** The Constant LOG. */
	private static final Log LOG = LogFactory.getLog(BluetoothServiceDiscoveryListener.class);

	/** The services. */
	private final Map<String, ServiceRecord> serviceMap = new HashMap<>();

	/** The result type. */
	private int resultType;

	/**
	 * {@inheritDoc}
	 */
	@Override
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

			serviceMap.put(uuid, record);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void serviceSearchCompleted(final int transID, final int respCode) {
		this.resultType = respCode;

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

		synchronized (this) {
			this.notifyAll();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deviceDiscovered(final RemoteDevice btDevice, final DeviceClass cod) {
		throw new Error("Non implemented!");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void inquiryCompleted(final int discType) {
		throw new Error("Non implemented!");
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
	 * Gets the services.
	 *
	 * @return the services
	 */
	public Map<String, ServiceRecord> getServiceMap() {
		return serviceMap;
	}

	/**
	 * Gets the result type.
	 *
	 * @return the result type
	 */
	public int getResultType() {
		return resultType;
	}
}
