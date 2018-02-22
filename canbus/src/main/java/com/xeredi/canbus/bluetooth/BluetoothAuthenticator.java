package com.xeredi.canbus.bluetooth;

import javax.obex.Authenticator;
import javax.obex.PasswordAuthentication;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class BluetoothAuthenticator.
 */
public class BluetoothAuthenticator implements Authenticator {

	/** The Constant LOG. */
	private static final Log LOG = LogFactory.getLog(BluetoothAuthenticator.class);

	/**
	 * {@inheritDoc}
	 */
	public PasswordAuthentication onAuthenticationChallenge(final String description, final boolean isUserIdRequired,
			boolean isFullAccess) {
		LOG.info("onAuthenticationChallenge!!!!: " + " - description: " + description + " - isUserIdRequired: "
				+ isUserIdRequired);

		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public byte[] onAuthenticationResponse(final byte[] userName) {
		LOG.info("onAuthenticationResponse!!!!");

		// TODO Auto-generated method stub
		return null;
	}
}
