package com.xeredi.canbus.mqtt;

import java.util.List;

import lombok.Data;
import lombok.NonNull;

// TODO: Auto-generated Javadoc
/**
 * Instantiates a new mqtt data.
 */
@Data
public class MqttData {

	/** The sender id. */
	private String senderId;

	/** The message list. */
	private List<String> messageList;

	/**
	 * Instantiates a new mqtt data.
	 *
	 * @param asenderId
	 *            the asender id
	 * @param amessageList
	 *            the amessage list
	 */
	public MqttData(final @NonNull String asenderId, final @NonNull List<String> amessageList) {
		super();
		this.senderId = asenderId;
		this.messageList = amessageList;
	}

	public MqttData() {
		super();
	}
}
