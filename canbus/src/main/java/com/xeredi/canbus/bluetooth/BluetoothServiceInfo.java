package com.xeredi.canbus.bluetooth;

import org.apache.commons.lang3.builder.ToStringBuilder;

// TODO: Auto-generated Javadoc
/**
 * The Class BluetoothServiceInfo.
 */
public final class BluetoothServiceInfo {

	/** The device address. */
	private final String deviceAddress;

	/** The device nane. */
	private final String deviceNane;

	/** The url. */
	private final String url;

	/** The name. */
	private final String name;

	/** The uuid. */
	private final String uuid;

	/**
	 * Instantiates a new bluetooth service info.
	 *
	 * @param deviceAddress
	 *            the device address
	 * @param deviceNane
	 *            the device nane
	 * @param url
	 *            the url
	 * @param name
	 *            the name
	 * @param uuid
	 *            the uuid
	 */
	public BluetoothServiceInfo(final String deviceAddress, final String deviceNane, final String url,
			final String name, final String uuid) {
		super();

		this.deviceAddress = deviceAddress;
		this.deviceNane = deviceNane;
		this.url = url;
		this.name = name;
		this.uuid = uuid;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	/**
	 * Gets the device address.
	 *
	 * @return the device address
	 */
	public String getDeviceAddress() {
		return deviceAddress;
	}

	/**
	 * Gets the device nane.
	 *
	 * @return the device nane
	 */
	public String getDeviceNane() {
		return deviceNane;
	}

	/**
	 * Gets the url.
	 *
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the uuid.
	 *
	 * @return the uuid
	 */
	public String getUuid() {
		return uuid;
	}
}
