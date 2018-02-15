package xeredi.bluetooth;

import javax.obex.ClientSession;

// TODO: Auto-generated Javadoc
/**
 * The Class BluetoothReaderProcess.
 */
public abstract class BluetoothReaderProcess {

	/** The uuid string. */
	private final String uuidString;

	/** The pin. */
	private final String pin;

	/** The conf filename. */
	private final String confFilename;

	/** The session. */
	private ClientSession session;

	/**
	 * Instantiates a new bluetooth reader process.
	 *
	 * @param auuidString
	 *            the auuid string
	 * @param apin
	 *            the apin
	 * @param aconfFilename
	 *            the aconf filename
	 */
	public BluetoothReaderProcess(final String auuidString, final String apin, final String aconfFilename) {
		super();

		this.uuidString = auuidString;
		this.pin = apin;
		this.confFilename = aconfFilename;
	}

	/**
	 * Gets the session.
	 *
	 * @return the session
	 */
	private final ClientSession getSession() {
		throw new Error("Non implemented");
	}

	/**
	 * Process.
	 */
	public final void process() {
		do {
			if (session == null) {
				session = getSession();
			}

			if (session == null) {
				// ERROR
			} else {
				readData(session);
			}
		} while (true);
	}

	/**
	 * Read data.
	 *
	 * @param session
	 *            the session
	 */
	protected abstract void readData(final ClientSession session);

}
