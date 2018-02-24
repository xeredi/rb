package com.xeredi.canbus;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Scheduler;
import org.quartz.impl.StdSchedulerFactory;

import com.pi4j.system.SystemInfo;
import com.xeredi.canbus.mqtt.MqttWriter;
import com.xeredi.canbus.util.ConfigurationKey;
import com.xeredi.canbus.util.ConfigurationUtil;
import com.xeredi.canbus.util.DateUtil;

// TODO: Auto-generated Javadoc
/**
 * The Class Main.
 */
public final class Main {

	/** The Constant LOG. */
	private static final Log LOG = LogFactory.getLog(Main.class);

	/**
	 * The main method.
	 *
	 * @param args
	 *            the arguments
	 */
	public static void main(final String[] args) {
		if (args.length > 0) {
			System.out.println("Reading arguments");

			if ("--version".equals(args[0])) {
				System.out.println("Version: " + ConfigurationUtil.getString(ConfigurationKey.app_version));
			}
		} else {
			try {
				if (LOG.isInfoEnabled()) {
					LOG.info("Start Scheduler. Release: " + ConfigurationUtil.getString(ConfigurationKey.app_version));
				}

				final MqttWriter mqttWriter = MqttWriter.getInstance(SystemInfo.getSerial());

				mqttWriter.sendPlacaArranqueData(DateUtil.getDateString());

				final Scheduler scheduler = new StdSchedulerFactory().getScheduler();

				scheduler.start();

				if (LOG.isInfoEnabled()) {
					LOG.info("Start Scheduler SUCCESS");
				}
			} catch (final Throwable ex) {
				LOG.fatal("Start Scheduler FAIL", ex);
			}
		}
	}
}
