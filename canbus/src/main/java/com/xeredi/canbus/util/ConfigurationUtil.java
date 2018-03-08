package com.xeredi.canbus.util;

import java.io.File;

import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class ConfigurationUtil.
 */
public final class ConfigurationUtil {

	/** The Constant LOG. */
	private static final Log LOG = LogFactory.getLog(ConfigurationUtil.class);

	/** The Constant CONFIGURATION_FILENAME. */
	public static final String CONFIGURATION_FILENAME = "Configuration.properties";

	/** The configuration. */
	private static final CombinedConfiguration CONFIGURATION = new CombinedConfiguration();

	/**
	 * Instantiates a new configuration util.
	 */
	private ConfigurationUtil() {
		super();
	}

	static {
		if (LOG.isInfoEnabled()) {
			LOG.info("Load Configuration from: " + CONFIGURATION_FILENAME);
		}

		try {
			CONFIGURATION.addConfiguration((new Configurations()).properties(new File(CONFIGURATION_FILENAME)));

			if (LOG.isInfoEnabled()) {
				LOG.info("Load Configuration success");
			}
		} catch (final ConfigurationException ex) {
			LOG.fatal("Error Loading Configuration from: " + CONFIGURATION_FILENAME);
		}
	}

	/**
	 * Gets the string.
	 *
	 * @param key
	 *            the key
	 * @return the string
	 */
	public static String getString(final ConfigurationKey key) {
		return CONFIGURATION.getString(key.name());
	}

	/**
	 * Gets the string array.
	 *
	 * @param key
	 *            the key
	 * @return the string array
	 */
	public static String[] getStringArray(final ConfigurationKey key) {
		return CONFIGURATION.getString(key.name()).split(",");
	}

	/**
	 * Gets the long.
	 *
	 * @param key
	 *            the key
	 * @return the long
	 */
	public static Long getLong(final ConfigurationKey key) {
		return CONFIGURATION.getLong(key.name());
	}

	/**
	 * Gets the integer.
	 *
	 * @param key
	 *            the key
	 * @return the integer
	 */
	public static Integer getInteger(final ConfigurationKey key) {
		return CONFIGURATION.getInt(key.name());
	}

	/**
	 * Gets the boolean.
	 *
	 * @param key
	 *            the key
	 * @return the boolean
	 */
	public static Boolean getBoolean(final ConfigurationKey key) {
		return CONFIGURATION.getBoolean(key.name());
	}

	/**
	 * Gets the character.
	 *
	 * @param key
	 *            the key
	 * @return the character
	 */
	public static Character getCharacter(final ConfigurationKey key) {
		return CONFIGURATION.getString(key.name()).charAt(0);
	}
}
