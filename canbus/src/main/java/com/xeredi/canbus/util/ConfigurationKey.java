package com.xeredi.canbus.util;

// TODO: Auto-generated Javadoc
/**
 * The Enum ConfigurationKey.
 */
public enum ConfigurationKey {
	/** The app version. */
	app_version,

	/** The mqtt server url. */
	mqtt_server_url,
	/** The mqtt offline folder. */
	mqtt_offline_folder,
	/** The mqtt retry timeinmillis. */
	mqtt_retry_timeinmillis,
	/** The mqtt message batchsize. */
	mqtt_message_batchsize,

	/** The rb base sleeptime. */
	rb_base_sleeptime,
	/** The rb max sleeptime. */
	rb_max_sleeptime,
	/** The rb sleeptime multiplier. */
	rb_sleeptime_multiplier,

	/** The gps port id. */
	gps_port_id,
	/** The gps port speed. */
	gps_port_speed,
	/** The gps segment prefix. */
	gps_segment_prefix,
	/** The gps segment separator. */
	gps_segment_separator,
	/** The gps segment mintokens. */
	gps_segment_mintokens,
	/** The gps token separator. */
	gps_token_separator,

	/** The canbus uuid. */
	canbus_uuid,
	/** The canbus port id. */
	canbus_port_id,
	/** The canbus port speed. */
	canbus_port_speed,
	/** The canbus file config. */
	canbus_file_config,
	/** The canbus host. */
	canbus_host,
	/** The canbus channel. */
	canbus_channel,
	/** The canbus sleep ms. */
	canbus_sleep_ms,
	canbus_obdcodes,

	;
}
