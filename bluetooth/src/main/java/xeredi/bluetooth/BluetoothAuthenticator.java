package xeredi.bluetooth;

import javax.obex.Authenticator;
import javax.obex.PasswordAuthentication;

public class BluetoothAuthenticator implements Authenticator {

	public BluetoothAuthenticator() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public PasswordAuthentication onAuthenticationChallenge(String description, boolean isUserIdRequired,
			boolean isFullAccess) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] onAuthenticationResponse(byte[] userName) {
		// TODO Auto-generated method stub
		return null;
	}

}
