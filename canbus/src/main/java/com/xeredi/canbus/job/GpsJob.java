package com.xeredi.canbus.job;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.xeredi.canbus.process.gps.GpsPi4jProcess;

// TODO: Auto-generated Javadoc
/**
 * The Class GpsJob.
 */
@DisallowConcurrentExecution
public final class GpsJob implements Job {

	/** The Constant LOG. */
	private static final Log LOG = LogFactory.getLog(GpsJob.class);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute(final JobExecutionContext context) throws JobExecutionException {
		if (LOG.isInfoEnabled()) {
			LOG.info("GPS Job start");
		}

		try {
			// final GpsReaderProcess gpsReader = new GpsReaderProcess();
			//
			// gpsReader.portsInfo();
			// gpsReader.startSerialPort();

			final GpsPi4jProcess process = new GpsPi4jProcess();

			process.execute();
		} catch (final Throwable ex) {
			LOG.fatal(ex, ex);
		}

		if (LOG.isInfoEnabled()) {
			LOG.info("GPS Job end");
		}
	}

}
