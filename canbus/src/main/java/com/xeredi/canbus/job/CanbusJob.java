package com.xeredi.canbus.job;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.xeredi.canbus.process.canbus.CanbusReaderProcess;

// TODO: Auto-generated Javadoc
/**
 * The Class CanbusJob.
 */
@DisallowConcurrentExecution
public final class CanbusJob implements Job {

	/** The Constant LOG. */
	private static final Log LOG = LogFactory.getLog(CanbusJob.class);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute(final JobExecutionContext context) throws JobExecutionException {
		if (LOG.isInfoEnabled()) {
			LOG.info("Canbus Job start");
		}

		try {
			final CanbusReaderProcess process = new CanbusReaderProcess();

			process.execute();
		} catch (final Throwable ex) {
			LOG.fatal(ex, ex);
		}

		if (LOG.isInfoEnabled()) {
			LOG.info("Canbus Job end");
		}
	}

}
