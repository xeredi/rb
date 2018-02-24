package com.xeredi.canbus.obd;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.obex.ClientSession;
import javax.obex.HeaderSet;
import javax.obex.Operation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class CanbusReader.
 */
public final class CanbusReader {

	/** The Constant LOG. */
	private static final Log LOG = LogFactory.getLog(CanbusReader.class);

	/** The session. */
	private final ClientSession session;

	/** The obd codes. */
	private final List<String> obdCodes;

	/**
	 * Instantiates a new canbus reader.
	 *
	 * @param session
	 *            the session
	 */
	public CanbusReader(final ClientSession session) {
		super();

		this.session = session;
		this.obdCodes = Arrays.asList("01 0C", "01 0D", "03"); // FIXME Leer de configuracion
	}

	/**
	 * Read.
	 *
	 * @return the map
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public Map<String, List<Byte>> read() throws IOException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Read codes from CAN/BUS: " + this.obdCodes.toString());
		}

		final HeaderSet request = session.createHeaderSet();

		final Map<String, List<Byte>> obdData = new HashMap<>();

		Operation putOperation = null;

		try {
			putOperation = session.put(request);

			try (final InputStream is = putOperation.openInputStream();
					final OutputStream os = putOperation.openOutputStream()) {
				for (final String obdCode : obdCodes) {
					obdData.put(obdCode, read(is, os, obdCode));
				}
			}
		} finally {
			if (putOperation != null) {
				putOperation.close();
			}
		}

		return obdData;
	}

	/**
	 * Read.
	 *
	 * @param is
	 *            the is
	 * @param os
	 *            the os
	 * @param command
	 *            the command
	 * @return the list
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private List<Byte> read(final InputStream is, final OutputStream os, final String command) throws IOException {
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
