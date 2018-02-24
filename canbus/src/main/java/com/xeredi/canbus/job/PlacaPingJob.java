package com.xeredi.canbus.job;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;

import com.xeredi.canbus.util.DateUtil;

// TODO: Auto-generated Javadoc
/**
 * The Class PingJob.
 */
@DisallowConcurrentExecution
public final class PlacaPingJob extends AbstractJob {

	/** The Constant LOG. */
	private static final Log LOG = LogFactory.getLog(PlacaPingJob.class);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doExecute(final JobExecutionContext context) {
		try {
			this.mqttWriter.sendPlacaPingData(DateUtil.getDateString());
		} catch (final IOException ex) {
			LOG.error(ex, ex);
		}
	}
}
