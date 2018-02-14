package com.xeredi.canbus.job;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.xeredi.canbus.process.ping.PlacaPingProcess;

// TODO: Auto-generated Javadoc
/**
 * The Class PingJob.
 */
@DisallowConcurrentExecution
public final class PlacaPingJob implements Job {

	/** The Constant LOG. */
	private static final Log LOG = LogFactory.getLog(PlacaPingJob.class);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute(final JobExecutionContext context) throws JobExecutionException {
		if (LOG.isInfoEnabled()) {
			LOG.info("Ping Job start");
		}

		try {
			final PlacaPingProcess process = new PlacaPingProcess();

			process.execute();
		} catch (final Throwable ex) {
			LOG.fatal(ex, ex);
		}

		if (LOG.isInfoEnabled()) {
			LOG.info("Ping Job end");
		}
	}

}
