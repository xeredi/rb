package com.xeredi.canbus.process.canbus;

import java.io.IOException;
import java.util.Enumeration;

import javax.bluetooth.DataElement;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class CanbusUtil.
 */
public final class CanbusUtil {

	/** The Constant LOG. */
	private static final Log LOG = LogFactory.getLog(CanbusUtil.class);

	/**
	 * Gets the name.
	 *
	 * @param device
	 *            the device
	 * @return the name
	 */
	public static String getName(final RemoteDevice device) {
		String name = null;

		int i = 0;
		final int max = 10;

		do {
			try {
				name = device.getFriendlyName(true);
			} catch (final IOException ex) {
				LOG.warn(ex.getMessage() + ", address: " + device.getBluetoothAddress());

				i++;

				try {
					Thread.sleep(500);
				} catch (final InterruptedException e) {
					LOG.fatal(e, e);
				}
			}
		} while (name == null && i < max);

		return name == null ? device.getBluetoothAddress() : name;
	}

	/**
	 * Gets the uuid.
	 *
	 * @param record
	 *            the record
	 * @return the uuid
	 */
	public static String getUUID(final ServiceRecord record) {
		String uuid = null;

		final DataElement serviceUUID = (DataElement) ((Enumeration) record.getAttributeValue(0x0001).getValue())
				.nextElement();

		uuid = serviceUUID.getValue().toString();

		return uuid;
	}

	/**
	 * Gets the connection URL.
	 *
	 * @param record
	 *            the record
	 * @return the connection URL
	 */
	public static String getConnectionURL(final ServiceRecord record) {
		return record.getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
	}

	/**
	 * Prints the remote device.
	 *
	 * @param device
	 *            the device
	 */
	public static void printRemoteDevice(final RemoteDevice device) {
		final String address = device.getBluetoothAddress();
		final boolean authenticated = device.isAuthenticated();
		final boolean trusted = device.isTrustedDevice();

		final String name = getName(device);

		if (LOG.isInfoEnabled()) {
			LOG.info("RemoteDevice. name: " + name + ", address: " + address + ", authenticated: " + authenticated
					+ ", trusted: " + trusted);
		}
	}

	/**
	 * Prints the service.
	 *
	 * @param transactionId
	 *            the transaction id
	 * @param records
	 *            the records
	 */
	public static void printService(final int transactionId, final ServiceRecord[] records) {
		if (LOG.isInfoEnabled()) {
			LOG.info("Service. transactionId: " + transactionId);

			for (int i = 0; i < records.length; i++) {
				final ServiceRecord record = records[i];
				final DataElement serviceName = record.getAttributeValue(0x1105);
				final String serviceUUID = getUUID(record);
				final String serviceURL = getConnectionURL(record);

				LOG.info(" - ServiceRecord. Name: " + serviceName + ", URL:" + serviceURL + " , UUID: " + serviceUUID);
				// LOG.info(" -- Attributes");
				//
				// for (final int attributeId : record.getAttributeIDs()) {
				// LOG.info(" --- ID: " + String.format("0x%04X", attributeId & 0xFFFF) + " -
				// value: "
				// + record.getAttributeValue(attributeId));
				// }
			}
		}
	}
}
