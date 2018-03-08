package com.xeredi.canbus.bluetooth;

import javax.bluetooth.ServiceRecord;

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

	/** The channel. */
	private final String channel;

	/** The record. */
	private final ServiceRecord record;

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
	 * @param channel
	 *            the channel
	 * @param record
	 *            the record
	 */
	public BluetoothServiceInfo(final String deviceAddress, final String deviceNane, final String url,
			final String name, final String uuid, final String channel, final ServiceRecord record) {
		super();

		this.deviceAddress = deviceAddress;
		this.deviceNane = deviceNane;
		this.url = url;
		this.name = name;
		this.uuid = uuid;
		this.channel = channel;
		this.record = record;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	/**
	 * Gets the address normalized.
	 *
	 * @return the address normalized
	 */
	public String getAddressNormalized() {
		if (deviceAddress == null) {
			return null;
		}

		final StringBuilder sb = new StringBuilder();

		for (int i = 0; i < deviceAddress.length(); i++) {
			if (i > 0 && i < (deviceAddress.length() - 1) && (i % 2 == 0)) {
				sb.append(':');
			}

			sb.append(deviceAddress.charAt(i));
		}

		return sb.toString();
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

	/**
	 * Gets the channel.
	 *
	 * @return the channel
	 */
	public String getChannel() {
		return channel;
	}

	/**
	 * Gets the record.
	 *
	 * @return the record
	 */
	public ServiceRecord getRecord() {
		return record;
	}
}
