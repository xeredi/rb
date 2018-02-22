package com.xeredi.canbus.mqtt;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.google.gson.Gson;
import com.xeredi.canbus.util.ConfigurationKey;
import com.xeredi.canbus.util.ConfigurationUtil;

// TODO: Auto-generated Javadoc
/**
 * The Class MqttWriter.
 */
public final class MqttWriter {

	/** The Constant LOG. */
	private static final Log LOG = LogFactory.getLog(MqttWriter.class);

	/**
	 * The Enum TOPIC.
	 */
	private enum TOPIC {

		/** The gps data. */
		gps_data,
		/** The placa ping data. */
		placa_ping_data,
		/** The placa arranque data. */
		placa_arranque_data,

		;
	};

	/** The Constant MQTT_SERVER_URL. */
	private static final String MQTT_SERVER_URL = ConfigurationUtil.getString(ConfigurationKey.mqtt_server_url);

	/** The Constant MQTT_RETRY_TIMEINMILLIS. */
	private static final int MQTT_RETRY_TIMEINMILLIS = ConfigurationUtil
			.getInteger(ConfigurationKey.mqtt_retry_timeinmillis);

	private static final int MQTT_MESSAGE_BATCHSIZE = ConfigurationUtil
			.getInteger(ConfigurationKey.mqtt_message_batchsize);

	/** The Constant MQTT_GPS_TOPIC. */
	private static final String MQTT_OFFLINE_FOLDER = ConfigurationUtil.getString(ConfigurationKey.mqtt_offline_folder);

	/** The Constant MQTT_TOPIC_LIST. */
	private static final List<TOPIC> MQTT_TOPIC_LIST = Arrays.asList(TOPIC.gps_data, TOPIC.placa_ping_data,
			TOPIC.placa_arranque_data);

	/** The Constant OBJECT_MAPPER. */
	private static final Gson OBJECT_MAPPER = new Gson();

	/** The Constant TOPIC_PENDING_MAP. */
	private static final Map<TOPIC, Boolean> TOPIC_PENDING_MAP = new HashMap<>();

	/** The Constant TOPIC_FILE_MAP. */
	private static final Map<TOPIC, File> TOPIC_FILE_MAP = new HashMap<>();

	/** The mqtt writer. */
	private static MqttWriter MQTT_WRITER;

	/** The mqtt client. */
	private MqttClient mqttClient;

	/** The next connection try. */
	private Calendar nextConnectionTry;

	/**
	 * Instantiates a new mqtt writer.
	 *
	 * @param aclientId
	 *            the aclient id
	 * @throws MqttException
	 *             the mqtt exception
	 */
	private MqttWriter(final String aclientId) throws MqttException {
		super();

		this.nextConnectionTry = Calendar.getInstance();
		this.mqttClient = new MqttClient(MQTT_SERVER_URL, aclientId);
	}

	/**
	 * Gets the single instance of MqttWriter.
	 *
	 * @param clientId
	 *            the client id
	 * @return single instance of MqttWriter
	 * @throws MqttException
	 *             the mqtt exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static MqttWriter getInstance(final String clientId) throws MqttException, IOException {
		if (MQTT_WRITER == null) {
			if (LOG.isInfoEnabled()) {
				LOG.info("MqttWriter Initialize");
			}

			MQTT_WRITER = new MqttWriter(clientId);

			for (final TOPIC topic : MQTT_TOPIC_LIST) {
				final File file = new File(new File(MQTT_OFFLINE_FOLDER), topic.name());

				if (!file.exists()) {
					file.getParentFile().mkdirs();
					file.createNewFile();
				}

				TOPIC_FILE_MAP.put(topic, file);
				TOPIC_PENDING_MAP.put(topic, FileUtils.sizeOf(file) > 0);
			}

			if (LOG.isInfoEnabled()) {
				LOG.info("TOPIC_FILE_MAP: " + TOPIC_FILE_MAP);
				LOG.info("TOPIC_PENDING_MAP: " + TOPIC_PENDING_MAP);
			}
		}

		return MQTT_WRITER;
	}

	/**
	 * Send gps data.
	 *
	 * @param message
	 *            the message
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void sendGpsData(final String message) throws IOException {
		sendMessage(TOPIC.gps_data, message);
	}

	/**
	 * Send placa ping data.
	 *
	 * @param message
	 *            the message
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void sendPlacaPingData(final String message) throws IOException {
		sendMessage(TOPIC.placa_ping_data, message);
	}

	/**
	 * Send placa arranque data.
	 *
	 * @param message
	 *            the message
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void sendPlacaArranqueData(final String message) throws IOException {
		sendMessage(TOPIC.placa_arranque_data, message);
	}

	/**
	 * Send message.
	 *
	 * @param topic
	 *            the topic
	 * @param message
	 *            the message
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void sendMessage(final TOPIC topic, final String message) throws IOException {
		final File topicFile = TOPIC_FILE_MAP.get(topic);

		try {
			if (!mqttClient.isConnected()) {
				if (Calendar.getInstance().after(nextConnectionTry)) {
					if (LOG.isDebugEnabled()) {
						LOG.debug("Connect MQTT Client");
					}

					try {
						mqttClient.connect();
					} catch (final MqttException ex) {
						this.nextConnectionTry.add(Calendar.MILLISECOND, MQTT_RETRY_TIMEINMILLIS);

						LOG.info("No connection to MQTT Server, retry in: " + this.nextConnectionTry.getTime());
					}
				}
			}

			if (mqttClient.isConnected()) {
				if (TOPIC_PENDING_MAP.get(topic)) {
					int i = 0;
					final List<String> messages = new ArrayList<>();

					final LineIterator lineIterator = FileUtils.lineIterator(topicFile);

					while (lineIterator.hasNext()) {
						i++;

						messages.add(lineIterator.next());

						if ((i == MQTT_MESSAGE_BATCHSIZE) || !lineIterator.hasNext()) {
							if (LOG.isDebugEnabled()) {
								LOG.debug("Send batch of messages. Size: " + i);
							}

							final MqttMessage mqttMessage = new MqttMessage();

							mqttMessage.setQos(0);
							mqttMessage.setPayload(
									OBJECT_MAPPER.toJson(new MqttData(mqttClient.getClientId(), messages)).getBytes());

							mqttClient.publish(topic.name(), mqttMessage);

							messages.clear();
							i = 0;
						}
					}

					FileUtils.writeLines(topicFile, new ArrayList<>(), false);
					TOPIC_PENDING_MAP.put(topic, false);
				}

				if (LOG.isDebugEnabled()) {
					LOG.debug("message: " + message);
				}

				final MqttMessage mqttMessage = new MqttMessage();

				mqttMessage.setQos(0);
				mqttMessage.setPayload(OBJECT_MAPPER
						.toJson(new MqttData(mqttClient.getClientId(), Arrays.asList(message))).getBytes());

				mqttClient.publish(topic.name(), mqttMessage);

				this.nextConnectionTry = Calendar.getInstance();
			} else {
				FileUtils.writeLines(topicFile, Arrays.asList(message), true);
				TOPIC_PENDING_MAP.put(topic, true);
			}
		} catch (final IOException ex) {
			throw ex;
		} catch (final Throwable ex) {
			FileUtils.writeLines(topicFile, Arrays.asList(message), true);
			TOPIC_PENDING_MAP.put(topic, true);

			this.nextConnectionTry.add(Calendar.MILLISECOND, MQTT_RETRY_TIMEINMILLIS);

			LOG.info("No connection to MQTT Server, retry in: " + this.nextConnectionTry.getTime());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void finalize() throws Throwable {
		super.finalize();

		if (LOG.isDebugEnabled()) {
			LOG.debug("Finalize MQTT Client");
		}

		if (mqttClient != null) {
			if (mqttClient.isConnected()) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Disconnect MQTT Client");
				}

				try {
					mqttClient.disconnect();
				} catch (final MqttException ex) {
					LOG.error("Error disconnecting MQTT Client", ex);
				}
			}

			try {
				mqttClient.close();
			} catch (final MqttException ex) {
				LOG.error("Error closing MQTT Client", ex);
			}
		}
	}
}
