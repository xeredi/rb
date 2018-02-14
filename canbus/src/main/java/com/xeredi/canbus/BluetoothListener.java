package com.xeredi.canbus;

import java.io.IOException;

import com.pi4j.io.serial.Baud;
import com.pi4j.io.serial.DataBits;
import com.pi4j.io.serial.FlowControl;
import com.pi4j.io.serial.Parity;
import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialConfig;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataEventListener;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.StopBits;

// TODO: Auto-generated Javadoc
/**
 * The listener interface for receiving bluetooth events.
 * The class that is interested in processing a bluetooth
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addBluetoothListener<code> method. When
 * the bluetooth event occurs, that object's appropriate
 * method is invoked.
 *
 * @see BluetoothEvent
 */
public final class BluetoothListener {

	/** The serial. */
	private Serial serial;

	/** The callback. */
	private Callback callback;

	/**
	 * Constructor for objects of class BluetoothListener.
	 */
	public BluetoothListener() {
		serial = SerialFactory.createInstance();
		serial.addListener(new SerialDataEventListener() {
			@Override
			public void dataReceived(SerialDataEvent event) {
				try {
					System.out.println("Data Received!!");
					System.out.println(event.getAsciiString());

					if (callback != null) {
						System.out.println("Callback not null!!");
						callback.onData(event.getAsciiString());
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Sets the callback.
	 *
	 * @param callback the new callback
	 */
	public void setCallback(Callback callback) {
		this.callback = callback;
	}

	/**
	 * Start.
	 *
	 * @return true, if successful
	 */
	public boolean start() {
		try {
			final SerialConfig config = new SerialConfig();

			config.device("/dev/rfcomm0").baud(Baud._38400).dataBits(DataBits._8).parity(Parity.NONE)
					.stopBits(StopBits._1).flowControl(FlowControl.NONE);

			serial.open(config);
			return true;
		} catch (IOException e) {
			System.err.println("====> SERIAL SETUP FAILED: " + e.getMessage());
			return false;
		}
	}

	/**
	 * Stop.
	 */
	public void stop() {
		if (serial != null) {
			try {
				serial.close();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * The Interface Callback.
	 */
	public static interface Callback {

		/**
		 * On data.
		 *
		 * @param data the data
		 */
		void onData(String data);
	}
}
