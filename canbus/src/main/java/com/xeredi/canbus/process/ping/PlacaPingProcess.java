package com.xeredi.canbus.process.ping;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.paho.client.mqttv3.MqttException;

import com.xeredi.canbus.process.RbProcess;
import com.xeredi.canbus.util.DateUtil;

// TODO: Auto-generated Javadoc
/**
 * The Class PlacaPingProcess.
 */
public final class PlacaPingProcess extends RbProcess {

	/** The Constant LOG. */
	private static final Log LOG = LogFactory.getLog(PlacaPingProcess.class);

	/**
	 * Instantiates a new placa ping process.
	 *
	 * @throws MqttException
	 *             the mqtt exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public PlacaPingProcess() throws MqttException, IOException {
		super();
	}

	/**
	 * Execute.
	 */
	public void execute() {
		try {
			this.mqttWriter.sendPlacaPingData(DateUtil.getDateString());
		} catch (final IOException ex) {
			LOG.error(ex, ex);
		}
	}
}
