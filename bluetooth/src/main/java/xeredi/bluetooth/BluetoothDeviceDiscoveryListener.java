package xeredi.bluetooth;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// TODO: Auto-generated Javadoc
/**
 * The listener interface for receiving bluetoothDeviceDiscovery events. The
 * class that is interested in processing a bluetoothDeviceDiscovery event
 * implements this interface, and the object created with that class is
 * registered with a component using the component's
 * <code>addBluetoothDeviceDiscoveryListener<code> method. When the
 * bluetoothDeviceDiscovery event occurs, that object's appropriate method is
 * invoked.
 *
 * @see BluetoothDeviceDiscoveryEvent
 */
public final class BluetoothDeviceDiscoveryListener implements DiscoveryListener {

	/** The Constant LOG. */
	private static final Log LOG = LogFactory.getLog(BluetoothDeviceDiscoveryListener.class);

	/** The Constant MAX_NAME_ATTEMPTS. */
	private static final int MAX_NAME_ATTEMPTS = 5;

	/** The Constant NAME_ATTEMPT_WAIT_MS. */
	private static final int NAME_ATTEMPT_WAIT_MS = 500;

	/** The devices map. */
	private final Map<String, RemoteDevice> devicesMap = new HashMap<>();

	/** The result type. */
	private int resultType;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deviceDiscovered(final RemoteDevice btDevice, final DeviceClass cod) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("DeviceDiscovered: " + btDevice.getBluetoothAddress());
		}

		String name = null;
		int nameAttempts = 0;

		do {
			try {
				name = btDevice.getFriendlyName(true);
			} catch (final IOException ex) {
				nameAttempts++;

				if (LOG.isDebugEnabled()) {
					LOG.info(ex);
				}

				try {
					Thread.sleep(NAME_ATTEMPT_WAIT_MS);
				} catch (final InterruptedException e) {
					LOG.fatal(e, e);
				}
			}
		} while ((name == null) && (nameAttempts < MAX_NAME_ATTEMPTS));

		if (name == null) {
			LOG.warn("Couldn't find name for address: " + btDevice.getBluetoothAddress());

			name = btDevice.getBluetoothAddress();
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug(" - Address: " + btDevice.getBluetoothAddress() + " - Name: " + name);
		}

		devicesMap.put(name, btDevice);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void inquiryCompleted(final int discType) {
		this.resultType = discType;

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

		synchronized (this) {
			this.notifyAll();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void servicesDiscovered(final int transID, final ServiceRecord[] servRecord) {
		throw new Error("Non implemented!!");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void serviceSearchCompleted(final int transID, final int respCode) {
		throw new Error("Non implemented!!");
	}

	/**
	 * Gets the devices map.
	 *
	 * @return the devices map
	 */
	public Map<String, RemoteDevice> getDevicesMap() {
		return devicesMap;
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
