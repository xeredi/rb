package com.xeredi.canbus.process.canbus;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.UUID;
import javax.obex.ClientSession;

import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.paho.client.mqttv3.MqttException;

import com.xeredi.canbus.bluetooth.BluetoothSearch;
import com.xeredi.canbus.obd.CanbusReader;
import com.xeredi.canbus.process.RbProcess;
import com.xeredi.canbus.util.ConfigurationKey;
import com.xeredi.canbus.util.ConfigurationUtil;

// TODO: Auto-generated Javadoc
/**
 * The Class CanbusReaderProcess.
 */
public final class CanbusReaderProcess extends RbProcess {

	/** The Constant LOG. */
	private static final Log LOG = LogFactory.getLog(CanbusReaderProcess.class);

	/** The Constant CANBUS_UUID. */
	private static final String CANBUS_UUID = ConfigurationUtil.getString(ConfigurationKey.canbus_uuid);

	/** The Constant CANBUS_FILE_CONFIG. */
	private static final String CANBUS_FILE_CONFIG = ConfigurationUtil.getString(ConfigurationKey.canbus_file_config);

	/** The Constant CANBUS_FILE_CONFIG. */
	private static final Long CANBUS_SLEEP_MS = ConfigurationUtil.getLong(ConfigurationKey.canbus_sleep_ms);

	/** The Constant CANBUS_CONFIGURATION. */
	private static final CombinedConfiguration CANBUS_CONFIGURATION = new CombinedConfiguration();

	/**
	 * Instantiates a new canbus reader process.
	 *
	 * @throws MqttException
	 *             the mqtt exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public CanbusReaderProcess() throws MqttException, IOException {
		super();
	}

	/**
	 * Execute.
	 */
	public void execute() {
		final BluetoothSearch bluetoothSearch = new BluetoothSearch();

		if (LOG.isDebugEnabled()) {
			LOG.debug("Bluecove Search");
		}

		bluetoothSearch.setLocalDeviceBluecove();

		if (LOG.isDebugEnabled()) {
			LOG.debug("GIAC");
		}

		bluetoothSearch.searchDevicesBluecove(DiscoveryAgent.GIAC, true);

		// Canbus: 0x1101
		// OBEX Object Push: 0x1105
		// Shit: 0x0100

		final UUID[] uuids = new UUID[] { new UUID(0x0100) }; // OBEX Object Push
		int[] attrIDs = new int[] { 0x0100 }; // Service name

		if (LOG.isDebugEnabled()) {
			LOG.debug("SearchService with UUID: " + uuids[0] + " and attrId: " + attrIDs[0]);
		}

		bluetoothSearch.searchServicesBluecove(uuids, attrIDs);

		final String urlMotoG = "btgoep://24DA9B132084:12;authenticate=false;encrypt=false;master=false";
		final String urlPC = "btgoep://5CF370883424:9;authenticate=false;encrypt=false;master=false";

		bluetoothSearch.sendMessageBluecove(urlMotoG, "Hola, Caracola!!", 10);
		bluetoothSearch.sendMessageBluecove(urlPC, "Hola, Caracola!!", 10);

		try {
			Thread.sleep(5000L);
		} catch (final InterruptedException ex) {
			LOG.fatal(ex, ex);
		}

		LOG.info("Read CAN/BUS");

		ClientSession session = null;

		try {
			session = bluetoothSearch.openConnectionBluecove(urlPC);

			final CanbusReader canbusReader = new CanbusReader(session);

			do {
				final Map<String, List<Byte>> canbusData = canbusReader.read();

				if (LOG.isDebugEnabled()) {
					LOG.debug("CAN/BUS data: " + canbusData.toString());
				}

				// TODO Enviar por MQTT

				Thread.sleep(CANBUS_SLEEP_MS);
			} while (true);
		} catch (final IOException ex) {
			LOG.error(ex, ex);
		} catch (final Throwable ex) {
			LOG.fatal(ex, ex);
		} finally {
			if (session != null) {
				try {
					session.close();
				} catch (final IOException ex) {
					LOG.error(ex, ex);
				}
			}
		}
	}

	/**
	 * The main method.
	 *
	 * @param args
	 *            the arguments
	 */
	public static void main(final String[] args) {
		if (args.length > 0) {
			System.out.println("Reading arguments");

			if ("--version".equals(args[0])) {
				System.out.println("Version: " + ConfigurationUtil.getString(ConfigurationKey.app_version));
			}
		} else {
			LOG.info("Start proccess. Release: " + ConfigurationUtil.getString(ConfigurationKey.app_version));

			try {
				final CanbusReaderProcess process = new CanbusReaderProcess();

				process.execute();
			} catch (final Throwable ex) {
				LOG.fatal(ex, ex);
			}

			LOG.info("End proccess");
		}
	}

}
