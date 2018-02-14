package com.xeredi.canbus.process;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.paho.client.mqttv3.MqttException;

import com.pi4j.system.SystemInfo;
import com.xeredi.canbus.mqtt.MqttWriter;
import com.xeredi.canbus.util.ConfigurationKey;
import com.xeredi.canbus.util.ConfigurationUtil;

// TODO: Auto-generated Javadoc
/**
 * The Class RbProcess.
 */
public abstract class RbProcess {

	/** The Constant LOG. */
	private static final Log LOG = LogFactory.getLog(RbProcess.class);

	/** The Constant BASE_SLEEPTIME_MS. */
	protected static final int BASE_SLEEPTIME_MS = ConfigurationUtil.getInteger(ConfigurationKey.rb_base_sleeptime);

	/** The Constant MAX_SLEEPTIME_MS. */
	protected static final int MAX_SLEEPTIME_MS = ConfigurationUtil.getInteger(ConfigurationKey.rb_max_sleeptime);

	/** The Constant SLEEPTIME_MULTIPLIER. */
	protected static final int SLEEPTIME_MULTIPLIER = ConfigurationUtil
			.getInteger(ConfigurationKey.rb_sleeptime_multiplier);

	/** The serial id. */
	protected final String serialId;

	/** The mqtt writer. */
	protected final MqttWriter mqttWriter;

	/**
	 * Instantiates a new rb process.
	 *
	 * @throws MqttException
	 *             the mqtt exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public RbProcess() throws MqttException, IOException {
		super();

		this.serialId = findRaspberrySerialNo();
		this.mqttWriter = MqttWriter.getInstance(serialId);
	}

	/**
	 * Raspberry info.
	 *
	 * @return the string
	 */
	private final String findRaspberrySerialNo() {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Raspberry Serial");
		}

		int sleepTime = BASE_SLEEPTIME_MS;

		while (true) {
			try {
				final String serialNo = SystemInfo.getSerial();

				if (LOG.isDebugEnabled()) {
					LOG.debug("serialNo: " + serialNo);
				}

				return serialNo;
			} catch (final Exception ex) {
				LOG.error(ex, ex);
			}

			sleepTime = Math.min(sleepTime * SLEEPTIME_MULTIPLIER, MAX_SLEEPTIME_MS);

			if (LOG.isInfoEnabled()) {
				LOG.info("Sleep ms: " + sleepTime);
			}

			try {
				Thread.sleep(sleepTime);
			} catch (final InterruptedException ex) {
				LOG.error(ex, ex);
			}
		}
	}

}
