package com.xeredi.canbus.job;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;

import com.pi4j.io.serial.Baud;
import com.pi4j.io.serial.DataBits;
import com.pi4j.io.serial.FlowControl;
import com.pi4j.io.serial.Parity;
import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialConfig;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.StopBits;
import com.xeredi.canbus.util.ConfigurationKey;
import com.xeredi.canbus.util.ConfigurationUtil;

// TODO: Auto-generated Javadoc
/**
 * The Class GpsJob.
 */
@DisallowConcurrentExecution
public final class GpsJob extends AbstractJob {

	/** The Constant LOG. */
	private static final Log LOG = LogFactory.getLog(GpsJob.class);

	/** The Constant GPS_PORT_ID. */
	private static final String GPS_PORT_ID = ConfigurationUtil.getString(ConfigurationKey.gps_port_id);

	/** The Constant GPS_PORT_SPEED. */
	private static final int GPS_PORT_SPEED = ConfigurationUtil.getInteger(ConfigurationKey.gps_port_speed);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doExecute(final JobExecutionContext context) {
		final GpsListener gpsListener = new GpsListener(mqttWriter);
		final Serial serial = SerialFactory.createInstance();

		serial.addListener(gpsListener);

		final SerialConfig config = new SerialConfig();
		final Baud baud = Baud.getInstance(GPS_PORT_SPEED);

		config.device(GPS_PORT_ID).baud(baud).dataBits(DataBits._8).parity(Parity.NONE).stopBits(StopBits._1)
				.flowControl(FlowControl.NONE);

		while (true) {
			try {
				serial.open(config);
			} catch (IOException ex) {
				LOG.fatal(ex, ex);

				if (serial.isOpen()) {
					try {
						serial.close();
					} catch (final IOException e) {
						LOG.fatal(e, e);
					}
				}

				try {
					Thread.sleep(30000L);
				} catch (final Exception e) {
					LOG.fatal(e, e);
				}
			}
		}
	}
}
