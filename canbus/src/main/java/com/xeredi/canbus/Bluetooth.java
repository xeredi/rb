package com.xeredi.canbus;

import java.io.IOException;

// TODO: Auto-generated Javadoc
/**
 * The Class Main.
 */
public final class Bluetooth {

	/** The bluetooth. */
	private BluetoothListener bluetooth;

	/**
	 * Constructor for objects of class RobotController.
	 */
	public Bluetooth() {
		bluetooth = new BluetoothListener();
		bluetooth.setCallback(new BluetoothListener.Callback() {
			@Override
			public void onData(String data) {
				handleBluetoothData(data);
			}
		});
	}

	/**
	 * Handle bluetooth data.
	 *
	 * @param data
	 *            the data
	 */
	private void handleBluetoothData(String data) {
		System.out.println("handleBluetoothData: " + data);
	}

	/**
	 * The main method.
	 *
	 * @param args
	 *            the arguments
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws InterruptedException
	 *             the interrupted exception
	 */
	public static void main(final String[] args) throws IOException, InterruptedException {
		System.out.println("Hola, caracola!!!");

		final Bluetooth main = new Bluetooth();

		System.out.println("Adios, caracola!!!");
	}

}
