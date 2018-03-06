package xeredi.bluetooth;

import javax.bluetooth.RemoteDevice;

import org.apache.commons.lang3.builder.ToStringBuilder;

// TODO: Auto-generated Javadoc
/**
 * The Class BluetoothDeviceInfo.
 */
public final class BluetoothDeviceInfo {

	/** The device. */
	private final RemoteDevice device;

	/** The name. */
	private final String name;

	/**
	 * Instantiates a new bluetooth device info.
	 *
	 * @param device
	 *            the device
	 * @param name
	 *            the name
	 */
	public BluetoothDeviceInfo(RemoteDevice device, String name) {
		super();
		this.device = device;
		this.name = name;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	/**
	 * Gets the device.
	 *
	 * @return the device
	 */
	public RemoteDevice getDevice() {
		return device;
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}
}
