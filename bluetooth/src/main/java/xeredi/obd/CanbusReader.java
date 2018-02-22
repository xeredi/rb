package xeredi.obd;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

				final ObdReader obdReader = new ObdReader(is, os);

				for (final String obdCode : obdCodes) {
					obdData.put(obdCode, obdReader.read(obdCode));
				}
			}
		} finally {
			if (putOperation != null) {
				putOperation.close();
			}
		}

		return obdData;
	}
}
