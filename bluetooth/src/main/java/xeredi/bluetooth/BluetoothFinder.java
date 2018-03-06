package xeredi.bluetooth;

import java.util.ArrayList;
import java.util.List;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class BluetoothFinder {
	private static final Log LOG = LogFactory.getLog(BluetoothFinder.class);

	public List<BluetoothServiceInfo> searchServicesBluecove(final UUID[] uuids, final int[] attrIds) {
		LOG.info("Search Services");

		final List<BluetoothServiceInfo> serviceInfos = new ArrayList<>();

		final List<BluetoothDeviceInfo> deviceInfos = searchDevicesBluecove(DiscoveryAgent.GIAC, true);

		for (final BluetoothDeviceInfo deviceInfo : deviceInfos) {
			LOG.info("Services from: " + deviceInfo.getName() + " : " + deviceInfo.getDevice().getBluetoothAddress());

			try {
				final BluetoothServiceDiscoveryListener listener = new BluetoothServiceDiscoveryListener();

				synchronized (listener) {
					final int transactionId = LocalDevice.getLocalDevice().getDiscoveryAgent().searchServices(attrIds,
							uuids, deviceInfo.getDevice(), listener);

					if (transactionId > 0) {
						LOG.info("wait for service inquiry to complete...");

						try {
							listener.wait();

							for (final BluetoothServiceInfo serviceInfo : listener.getServiceInfos()) {
								serviceInfos.add(serviceInfo);
							}
						} catch (final InterruptedException ex) {
							LOG.fatal(ex, ex);
						}

						LOG.info("service inquiry completed");
					}
				}
			} catch (final BluetoothStateException ex) {
				LOG.error(ex, ex);
			}
		}

		return serviceInfos;
	}

	/**
	 * Search devices.
	 *
	 * @param accessCode
	 *            the access code
	 * @param retry
	 *            the retry
	 */
	private List<BluetoothDeviceInfo> searchDevicesBluecove(final int accessCode, final boolean retry) {
		final List<BluetoothDeviceInfo> deviceInfos = new ArrayList<>();

		try {
			final DiscoveryAgent agent = LocalDevice.getLocalDevice().getDiscoveryAgent();

			do {
				final BluetoothDeviceDiscoveryListener listener = new BluetoothDeviceDiscoveryListener();

				synchronized (listener) {
					final boolean started = agent.startInquiry(accessCode, listener);
					if (started) {
						if (LOG.isDebugEnabled()) {
							LOG.debug("wait for device inquiry to complete...");
						}

						try {
							listener.wait();

							deviceInfos.addAll(listener.getDeviceInfos());
						} catch (final InterruptedException ex) {
							LOG.fatal(ex, ex);
						}

						if (LOG.isDebugEnabled()) {
							LOG.debug(deviceInfos.size() + " device(s) found");
						}
					}
				}
			} while (deviceInfos.isEmpty() && retry);
		} catch (final BluetoothStateException ex) {
			LOG.error(ex, ex);
		}

		return deviceInfos;
	}

}
