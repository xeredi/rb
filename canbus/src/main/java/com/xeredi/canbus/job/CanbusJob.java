package com.xeredi.canbus.job;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.bluetooth.UUID;
import javax.obex.ClientSession;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;

import com.xeredi.canbus.bluetooth.BluetoothSearch;
import com.xeredi.canbus.bluetooth.BluetoothServiceInfo;
import com.xeredi.canbus.obd.CanbusReader;
import com.xeredi.canbus.util.ConfigurationKey;
import com.xeredi.canbus.util.ConfigurationUtil;

// TODO: Auto-generated Javadoc
/**
 * The Class CanbusJob.
 */
@DisallowConcurrentExecution
public final class CanbusJob extends AbstractJob {

	/** The Constant LOG. */
	private static final Log LOG = LogFactory.getLog(CanbusJob.class);

	/** The Constant CANBUS_UUID. */
	private static final String CANBUS_UUID = ConfigurationUtil.getString(ConfigurationKey.canbus_uuid);

	/** The Constant CANBUS_FILE_CONFIG. */
	private static final String CANBUS_FILE_CONFIG = ConfigurationUtil.getString(ConfigurationKey.canbus_file_config);

	/** The Constant CANBUS_FILE_CONFIG. */
	private static final Long CANBUS_SLEEP_MS = ConfigurationUtil.getLong(ConfigurationKey.canbus_sleep_ms);

	/** The Constant CANBUS_CONFIGURATION. */
	private static PropertiesConfiguration CANBUS_CONFIGURATION = null;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doExecute(final JobExecutionContext context) {
		ClientSession clientSession = null;

		try {
			final BluetoothSearch bluetoothSearch = new BluetoothSearch();

			boolean saveConfiguration = true;

			loadConfiguration();

			String serviceUrl = CANBUS_CONFIGURATION.getString(ConfigurationKey.canbus_url.name());

			if (serviceUrl != null) {
				clientSession = bluetoothSearch.openConnectionBluecove(serviceUrl);

				saveConfiguration = (clientSession == null);
			}

			if (clientSession == null) {
				serviceUrl = searchCanbusUrl(bluetoothSearch);

				LOG.info("serviceUrl: " + serviceUrl);

				clientSession = bluetoothSearch.openConnectionBluecove(serviceUrl);

				if (clientSession != null) {
					CANBUS_CONFIGURATION.setProperty(ConfigurationKey.canbus_url.name(), serviceUrl);

					if (saveConfiguration) {
						saveConfiguration();
					}
				}
			}

			bluetoothSearch.sendMessageBluecove(clientSession, "Hola, Caracola!!", 10);

			if (LOG.isDebugEnabled()) {
				LOG.debug("Read CAN/BUS");
			}

			final CanbusReader canbusReader = new CanbusReader(clientSession);

			canbusReader.read();

		} catch (final IOException ex) {
			LOG.fatal(ex, ex);
		} catch (final ConfigurationException ex) {
			LOG.fatal(ex, ex);
		} finally {
			if (clientSession != null) {
				try {
					clientSession.close();
				} catch (final IOException ex) {
					LOG.fatal(ex, ex);
				}
			}
		}

		// final String urlMotoG =
		// "btgoep://24DA9B132084:12;authenticate=false;encrypt=false;master=false";
		// final String urlPC =
		// "btgoep://5CF370883424:9;authenticate=false;encrypt=false;master=false";
		//
		// bluetoothSearch.sendMessageBluecove(urlMotoG, "Hola, Caracola!!", 10);
		// bluetoothSearch.sendMessageBluecove(urlPC, "Hola, Caracola!!", 10);
		//
		// try {
		// Thread.sleep(5000L);
		// } catch (final InterruptedException ex) {
		// LOG.fatal(ex, ex);
		// }
		//
		// LOG.info("Read CAN/BUS");
		//
		// ClientSession session = null;
		//
		// try {
		// session = bluetoothSearch.openConnectionBluecove(urlPC);
		//
		// final CanbusReader canbusReader = new CanbusReader(session);
		//
		// do {
		// final Map<String, List<Byte>> canbusData = canbusReader.read();
		//
		// if (LOG.isDebugEnabled()) {
		// LOG.debug("CAN/BUS data: " + canbusData.toString());
		// }
		//
		// // TODO Enviar por MQTT
		//
		// Thread.sleep(CANBUS_SLEEP_MS);
		// } while (true);
		// } catch (final IOException ex) {
		// LOG.error(ex, ex);
		// } catch (final Throwable ex) {
		// LOG.fatal(ex, ex);
		// } finally {
		// if (session != null) {
		// try {
		// session.close();
		// } catch (final IOException ex) {
		// LOG.error(ex, ex);
		// }
		// }
		// }
	}

	/**
	 * Load configuration.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws ConfigurationException
	 *             the configuration exception
	 */
	private void loadConfiguration() throws IOException, ConfigurationException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Load configuration: " + CANBUS_FILE_CONFIG);
		}

		final File file = new File(CANBUS_FILE_CONFIG);

		if (!file.exists()) {
			file.getParentFile().mkdirs();
			file.createNewFile();
		}

		CANBUS_CONFIGURATION = (new Configurations()).properties(file);
	}

	/**
	 * Save configuration.
	 *
	 * @throws ConfigurationException
	 *             the configuration exception
	 */
	private void saveConfiguration() throws ConfigurationException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Save configuration: " + CANBUS_FILE_CONFIG);

			final Iterator<String> iterator = CANBUS_CONFIGURATION.getKeys();

			while (iterator.hasNext()) {
				final String key = iterator.next();

				LOG.debug("key: " + key + ", value: " + CANBUS_CONFIGURATION.getString(key));
			}
		}

		final FileHandler handler = new FileHandler(CANBUS_CONFIGURATION);
		final File out = new File(CANBUS_FILE_CONFIG);

		handler.save(out);
	}

	/**
	 * Search canbus url.
	 *
	 * @param bluetoothSearch
	 *            the bluetooth search
	 * @return the string
	 */
	private String searchCanbusUrl(final BluetoothSearch bluetoothSearch) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Search CANBUS url");
		}

		final UUID[] uuids = new UUID[] { new UUID(CANBUS_UUID, true) };
		int[] attrIDs = new int[] { 0x0100 }; // Service name

		if (LOG.isDebugEnabled()) {
			LOG.debug("SearchService with UUID: " + uuids[0] + " and attrId: " + attrIDs[0]);
		}

		final List<BluetoothServiceInfo> serviceInfos = bluetoothSearch.searchServicesBluecove(uuids, attrIDs);

		if (LOG.isDebugEnabled()) {
			LOG.debug("Services found");

			for (final BluetoothServiceInfo serviceInfo : serviceInfos) {
				LOG.debug("service: " + serviceInfo);
			}
		}

		if (serviceInfos.size() == 0) {
			LOG.warn("Ningun servicio encontrado");

			return null;
		}

		if (serviceInfos.size() > 1) {
			LOG.warn("Demasiados servicios encontrados: " + serviceInfos.size());

			return serviceInfos.get(0).getUrl();
		}

		return serviceInfos.get(0).getUrl();
	}
}
