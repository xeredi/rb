package com.xeredi.canbus.obd;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class ObdReader.
 */
public final class ObdReader {

	/** The Constant LOG. */
	private static final Log LOG = LogFactory.getLog(ObdReader.class);

	/** The is. */
	private final InputStream is;

	/** The os. */
	private final OutputStream os;

	/**
	 * Instantiates a new obd reader.
	 *
	 * @param is
	 *            the is
	 * @param os
	 *            the os
	 */
	public ObdReader(final InputStream is, final OutputStream os) {
		super();
		this.is = is;
		this.os = os;
	}

	/**
	 * Read.
	 *
	 * @param command
	 *            the command
	 * @return the byte[]
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public List<Byte> read(final String command) throws IOException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("command: " + command);
		}

		// Send command
		os.write((command + "\r").getBytes());
		os.flush();

		// FIXME sleep??

		final List<Byte> response = new ArrayList<>();

		byte b = 0;

		while (((b = (byte) is.read()) > -1)) {
			final char c = (char) b;
			if (c == '>') {
				break;
			}

			response.add(b);
		}

		if (LOG.isDebugEnabled()) {
			final StringBuilder res = new StringBuilder();

			for (final byte responseByte : response) {
				res.append((char) responseByte);
			}

			LOG.debug("command: " + command + ", response: " + res.toString());
		}

		return response;
	}

}
