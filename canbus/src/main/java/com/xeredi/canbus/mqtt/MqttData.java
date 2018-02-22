package com.xeredi.canbus.mqtt;

import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * Instantiates a new mqtt data.
 */
public class MqttData {

	/** The sender id. */
	private final String senderId;

	/** The message list. */
	private final List<String> messageList;

	/**
	 * Instantiates a new mqtt data.
	 *
	 * @param asenderId
	 *            the asender id
	 * @param amessageList
	 *            the amessage list
	 */
	public MqttData(final String asenderId, final List<String> amessageList) {
		super();
		this.senderId = asenderId;
		this.messageList = amessageList;
	}

	/**
	 * Gets the sender id.
	 *
	 * @return the sender id
	 */
	public String getSenderId() {
		return senderId;
	}

	/**
	 * Gets the message list.
	 *
	 * @return the message list
	 */
	public List<String> getMessageList() {
		return messageList;
	}
}
