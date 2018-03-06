package xeredi.bluetooth;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

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
	private final List<BluetoothServiceInfo> serviceInfos = new ArrayList<>();

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
			final String channel = getChannel(record);

			final BluetoothServiceInfo serviceInfo = new BluetoothServiceInfo(address, address, connectionURL, name,
					uuid, channel, record);

			if (LOG.isDebugEnabled()) {
				LOG.debug("ServiceInfo: " + serviceInfo);
			}

			serviceInfos.add(serviceInfo);
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

		final DataElement element = record.getAttributeValue(0x0001);

		if (element == null) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("No UUID");
			}
		} else {
			final Enumeration enumeration = (Enumeration) element.getValue();

			if (enumeration == null || !enumeration.hasMoreElements()) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("No UUID");
				}
			} else {
				final DataElement serviceUUID = (DataElement) enumeration.nextElement();

				if (serviceUUID == null) {
					if (LOG.isDebugEnabled()) {
						LOG.debug("No UUID");
					}
				} else {
					uuid = serviceUUID.getValue().toString();
				}
			}
		}

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
	 * Gets the channel.
	 *
	 * @param record
	 *            the record
	 * @return the channel
	 */
	private String getChannel(final ServiceRecord record) {
		String name = null;

		final DataElement element = record.getAttributeValue(0x0004);

		LOG.debug("element: " + element);

		if (element == null) {
			LOG.debug("No Channel");

			return null;
		}

		LOG.debug("channel: " + element);

		if (element.getDataType() == DataElement.DATSEQ) {
			final Enumeration enumeration = (Enumeration) element.getValue();

			if (enumeration == null) {
				LOG.debug("No Channel");

				return null;
			}

			enumeration.nextElement();

			final DataElement elementChannel = (DataElement) enumeration.nextElement();

			if (elementChannel.getDataType() == DataElement.DATSEQ) {
				final Enumeration enumerationChannel = (Enumeration) elementChannel.getValue();

				// LOG.debug("enumerationChannel: " + enumerationChannel);

				enumerationChannel.nextElement();

				return String.valueOf(((DataElement) enumerationChannel.nextElement()).getLong());
			}
		}

		return name;
	}

	/*
	 * 0x4:DATSEQ
	 *
	 * { DATSEQ { UUID 0000010000001000800000805f9b34fb } DATSEQ { UUID
	 * 0000000300001000800000805f9b34fb U_INT_1 0x8 } }
	 */
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
	public List<BluetoothServiceInfo> getServiceInfos() {
		return serviceInfos;
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
