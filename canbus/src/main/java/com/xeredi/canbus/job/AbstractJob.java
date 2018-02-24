package com.xeredi.canbus.job;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.pi4j.system.SystemInfo;
import com.xeredi.canbus.mqtt.MqttWriter;

// TODO: Auto-generated Javadoc
/**
 * The Class AbstractJob.
 */
public abstract class AbstractJob implements Job {

	/** The Constant LOG. */
	private static final Log LOG = LogFactory.getLog(AbstractJob.class);

	/** The serial id. */
	protected String serialId;

	/** The mqtt writer. */
	protected MqttWriter mqttWriter;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void execute(JobExecutionContext context) throws JobExecutionException {
		if (LOG.isInfoEnabled()) {
			LOG.info("Start: " + context.getJobDetail().getJobClass().getName());
		}

		try {
			this.serialId = SystemInfo.getSerial();
			this.mqttWriter = MqttWriter.getInstance(serialId);

			doExecute(context);
		} catch (final Throwable ex) {
			LOG.fatal(ex, ex);
		}

		if (LOG.isInfoEnabled()) {
			LOG.info("End: " + context.getJobDetail().getJobClass().getName());
		}
	}

	/**
	 * Do execute.
	 *
	 * @param context
	 *            the context
	 */
	protected abstract void doExecute(final JobExecutionContext context);
}
