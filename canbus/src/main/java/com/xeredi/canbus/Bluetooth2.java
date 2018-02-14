package com.xeredi.canbus;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DataElement;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.obex.ClientSession;
import javax.obex.HeaderSet;
import javax.obex.Operation;
import javax.obex.ResponseCodes;

// TODO: Auto-generated Javadoc
/**
 * The Class Main.
 */
public final class Bluetooth2 {

	/**
	 * Test device discoverer.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws InterruptedException
	 *             the interrupted exception
	 */
	public void testDeviceDiscoverer() throws IOException, InterruptedException {
		System.out.println("testDeviceDiscoverer Start");

		final Vector<RemoteDevice> devicesDiscovered = new Vector<>();
		final Vector<String> serviceFound = new Vector<>();

		final Object inquiryCompletedEvent = new Object();
		final Object serviceSearchCompletedEvent = new Object();

		devicesDiscovered.clear();
		serviceFound.clear();

		final UUID OBEX_OBJECT_PUSH = new UUID(0x1105);
		final UUID OBEX_FILE_TRANSFER = new UUID(0x1106);
		final UUID serviceUUID = OBEX_OBJECT_PUSH;

		// FIXME
		// if ((args != null) && (args.length > 0)) {
		// serviceUUID = new UUID(args[0], false);
		// }

		final UUID[] searchUuidSet = new UUID[] { serviceUUID };
		final int[] attrIDs = new int[] { 0x0100 // Service name
		};

		final DiscoveryListener listener = new DiscoveryListener() {

			public void deviceDiscovered(final RemoteDevice btDevice, final DeviceClass cod) {
				System.out.println("Device " + btDevice.getBluetoothAddress() + " found");
				devicesDiscovered.addElement(btDevice);
				try {
					System.out.println("     name " + btDevice.getFriendlyName(false));
				} catch (IOException cantGetDeviceName) {
				}

				synchronized (serviceSearchCompletedEvent) {
					try {
						System.out.println("search services on " + btDevice.getBluetoothAddress() + " "
								+ btDevice.getFriendlyName(false));
						LocalDevice.getLocalDevice().getDiscoveryAgent().searchServices(attrIDs, searchUuidSet,
								btDevice, this);
						serviceSearchCompletedEvent.wait();
					} catch (final IOException ex) {
						ex.printStackTrace(System.err);
					} catch (final InterruptedException ex) {
						ex.printStackTrace(System.err);
					}
				}
			}

			public void inquiryCompleted(int discType) {
				System.out.println("Device Inquiry completed!");
				synchronized (inquiryCompletedEvent) {
					inquiryCompletedEvent.notifyAll();
				}
			}

			public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
				for (int i = 0; i < servRecord.length; i++) {
					String url = servRecord[i].getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
					if (url == null) {
						continue;
					}
					serviceFound.add(url);
					DataElement serviceName = servRecord[i].getAttributeValue(0x0100);
					if (serviceName != null) {
						System.out.println("service " + serviceName.getValue() + " found " + url);
					} else {
						System.out.println("service found " + url);
					}
				}
			}

			public void serviceSearchCompleted(int transID, int respCode) {
				System.out.println("service search completed!");
				synchronized (serviceSearchCompletedEvent) {
					serviceSearchCompletedEvent.notifyAll();
				}
			}
		};

		synchronized (inquiryCompletedEvent) {
			final boolean started = LocalDevice.getLocalDevice().getDiscoveryAgent().startInquiry(DiscoveryAgent.GIAC,
					listener);
			if (started) {
				System.out.println("wait for device inquiry to complete...");
				inquiryCompletedEvent.wait();
				System.out.println(devicesDiscovered.size() + " device(s) found");
			}
		}

		System.out.println("testDeviceDiscoverer End");
	}

	/**
	 * Search devices.
	 *
	 * @return the list
	 * @throws InterruptedException
	 *             the interrupted exception
	 * @throws BluetoothStateException
	 *             the bluetooth state exception
	 */
	public List<RemoteDevice> searchDevices() throws InterruptedException, BluetoothStateException {
		System.out.println("searchDevices Start");

		final List<RemoteDevice> remoteDevices = new ArrayList<>();
		final Object lock = new Object();

		final DiscoveryListener listener = new DiscoveryListener() {
			public void deviceDiscovered(final RemoteDevice btDevice, final DeviceClass cod) {
				System.out.println("Device " + btDevice.getBluetoothAddress() + " found");
				remoteDevices.add(btDevice);

				try {
					System.out.println("     name " + btDevice.getFriendlyName(false));
				} catch (final IOException ex) {
					System.out.println("     name " + btDevice.toString());
				}
			}

			public void inquiryCompleted(int discType) {
				System.out.println("Device Inquiry completed!");
				synchronized (lock) {
					lock.notifyAll();
				}
			}

			public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
				throw new Error("Non implemented!");
			}

			public void serviceSearchCompleted(int transID, int respCode) {
				throw new Error("Non implemented!");
			}
		};

		synchronized (lock) {
			final boolean started = LocalDevice.getLocalDevice().getDiscoveryAgent().startInquiry(DiscoveryAgent.GIAC,
					listener);
			if (started) {
				System.out.println("wait for device inquiry to complete...");
				lock.wait();
				System.out.println(remoteDevices.size() + " device(s) found");
			}
		}

		System.out.println("searchDevices End");

		return remoteDevices;
	}

	/**
	 * Search services.
	 *
	 * @param remoteDevice
	 *            the remote device
	 * @return the map
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws InterruptedException
	 *             the interrupted exception
	 */
	public Map<Integer, ServiceRecord[]> searchServices(final RemoteDevice remoteDevice)
			throws IOException, InterruptedException {
		System.out.println("searchServices Start");

		final Map<Integer, ServiceRecord[]> servicesMap = new HashMap<>();
		final Object lock = new Object();

		final DiscoveryListener listener = new DiscoveryListener() {
			public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
				throw new Error("Non implemented!");
			}

			public void inquiryCompleted(int discType) {
				throw new Error("Non implemented!");
			}

			public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
				System.out.println("service discovered!");
				if (servicesMap.containsKey(transID)) {
					System.out.println("transID duplicated!!: " + transID);
				} else {
					servicesMap.put(transID, servRecord);
				}
			}

			public void serviceSearchCompleted(int transID, int respCode) {
				System.out.println("service search completed!");
				synchronized (lock) {
					lock.notifyAll();
				}
			}
		};

		final UUID OBEX_OBJECT_PUSH = new UUID(0x1105);
		final UUID OBEX_FILE_TRANSFER = new UUID(0x1106);

		final UUID serviceUUID = OBEX_OBJECT_PUSH;
		// if ((args != null) && (args.length > 0)) {
		// serviceUUID = new UUID(args[0], false);
		// }

		final UUID[] searchUuidSet = new UUID[] { serviceUUID };
		final int[] attrIDs = new int[] { 0x0100 // Service name
		};

		synchronized (lock) {
			System.out.println("search services on " + remoteDevice.getBluetoothAddress());
			LocalDevice.getLocalDevice().getDiscoveryAgent().searchServices(attrIDs, searchUuidSet, remoteDevice,
					listener);
			lock.wait();
		}

		System.out.println("searchServices End");

		return servicesMap;
	}

	/**
	 * Test retrieved device info.
	 *
	 * @param option
	 *            the option
	 */
	public void testRetrievedDeviceInfo(final int option) {
		System.out.println("testRetrievedDeviceInfo Start with option: " + option);

		try {
			final LocalDevice localDevice = LocalDevice.getLocalDevice();

			final RemoteDevice[] remoteDevices = localDevice.getDiscoveryAgent().retrieveDevices(option);

			if (remoteDevices == null) {
				System.out.println("\nNo devices found!");
			} else {
				for (final RemoteDevice remoteDevice : remoteDevices) {
					System.out.println("\nRemoteDevice");
					System.out.println("\tBluetoothAddress: " + remoteDevice.getBluetoothAddress());
				}
			}
		} catch (final Exception ex) {
			ex.printStackTrace(System.err);
		}

		System.out.println("testRetrievedDeviceInfo End");
	}

	/**
	 * Test local device info.
	 *
	 * @param device
	 *            the device
	 */
	private void printLocalDeviceInfo(final LocalDevice device) {
		System.out.println("\nLOCAL DEVICE INFO");

		System.out.println("\tBluetoothAddress: " + device.getBluetoothAddress());
		System.out.println("\tFriendlyName: " + device.getFriendlyName());
		System.out.println("\tPowerOn: " + device.isPowerOn());
		System.out.println("\tDiscoverable: " + device.getDiscoverable());
		System.out.println("\tDeviceClass: " + device.getDeviceClass().toString());
		System.out.println("\tDiscoveryAgent: " + device.getDiscoveryAgent().toString());
	}

	/**
	 * Prints the remote device.
	 *
	 * @param device
	 *            the device
	 */
	private void printRemoteDevice(final RemoteDevice device) {
		System.out.println("\nREMOTE DEVICE INFO");
		System.out.println("\tBluetoothAddress: " + device.getBluetoothAddress());

		try {
			System.out.println("\tFriendlyName: " + device.getFriendlyName(true));
		} catch (final IOException ex) {
			System.out.println("\tFriendlyName: Unable to get!!");
		}

		System.out.println("\tAuthenticated: " + device.isAuthenticated());
		System.out.println("\tEncrypted: " + device.isEncrypted());
		System.out.println("\tTrustedDevice: " + device.isTrustedDevice());
	}

	/**
	 * Prints the device services.
	 *
	 * @param map
	 *            the map
	 */
	private void printDeviceServices(final Map<Integer, ServiceRecord[]> map) {
		System.out.println("\nDEVICE SERVICES INFO");

		for (final Integer transID : map.keySet()) {
			System.out.println("\ntransID: " + transID);

			for (final ServiceRecord record : map.get(transID)) {
				System.out.println("\tservice: " + record.toString());
				System.out.println("\turl: " + record.getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false));
				System.out.println("\tAttributeIDs: ");

				for (final Integer attributeID : record.getAttributeIDs()) {
					final DataElement attributeValue = record.getAttributeValue(attributeID);

					String dataType = null;
					Object dataValue = null;

					switch (attributeValue.getDataType()) {
					case DataElement.BOOL:
						dataValue = attributeValue.getBoolean();

						break;
					case DataElement.INT_1:
					case DataElement.INT_2:
					case DataElement.INT_4:
					case DataElement.INT_8:
					case DataElement.U_INT_1:
					case DataElement.U_INT_2:
					case DataElement.U_INT_4:
						dataValue = attributeValue.getLong();

						break;
					case DataElement.INT_16:
					case DataElement.U_INT_16:
					case DataElement.U_INT_8:
					case DataElement.DATALT:
					case DataElement.DATSEQ:
					case DataElement.STRING:
					case DataElement.URL:
					case DataElement.UUID:
						dataValue = attributeValue.getValue();

						break;
					case DataElement.NULL:
						dataValue = null;

						break;
					default:
						throw new Error("Invalid datatype: " + attributeValue.getDataType());
					}

					switch (attributeValue.getDataType()) {
					case DataElement.BOOL:
						dataType = "BOOL";

						break;
					case DataElement.DATALT:
						dataType = "DATALT";

						break;
					case DataElement.DATSEQ:
						dataType = "DATSEQ";

						break;
					case DataElement.INT_1:
						dataType = "INT_1";

						break;
					case DataElement.INT_16:
						dataType = "INT_16";

						break;
					case DataElement.INT_2:
						dataType = "INT_2";

						break;
					case DataElement.INT_4:
						dataType = "INT_4";

						break;
					case DataElement.INT_8:
						dataType = "INT_8";

						break;
					case DataElement.U_INT_1:
						dataType = "U_INT_1";

						break;
					case DataElement.U_INT_16:
						dataType = "U_INT_16";

						break;
					case DataElement.U_INT_2:
						dataType = "U_INT_2";

						break;
					case DataElement.U_INT_4:
						dataType = "U_INT_4";

						break;
					case DataElement.U_INT_8:
						dataType = "U_INT_8";

						break;
					case DataElement.NULL:
						dataType = "NULL";

						break;
					case DataElement.STRING:
						dataType = "STRING";

						break;
					case DataElement.URL:
						dataType = "URL";

						break;
					case DataElement.UUID:
						dataType = "UUID";

						break;
					default:
						break;
					}

					System.out.println("\t\tID: " + attributeID + ", dataType: " + dataType + ", value: " + dataValue);
				}
			}
		}
	}

	public void testDirectConnect(final String url) throws IOException {
		System.out.println("Connecting to " + url);

		ClientSession clientSession = null;

		try {
			clientSession = (ClientSession) Connector.open(url);

			HeaderSet hsConnectReply = clientSession.connect(null);
			if (hsConnectReply.getResponseCode() != ResponseCodes.OBEX_HTTP_OK) {
				System.out.println("Failed to connect");
				return;
			}

			HeaderSet hsOperation = clientSession.createHeaderSet();
			hsOperation.setHeader(HeaderSet.NAME, "Hello.txt");
			hsOperation.setHeader(HeaderSet.TYPE, "text");

			// Create PUT Operation
			Operation putOperation = clientSession.put(hsOperation);

			// Send some text to server
			byte data[] = "Hello world!".getBytes("iso-8859-1");
			OutputStream os = putOperation.openOutputStream();
			os.write(data);
			os.close();

			putOperation.close();
			clientSession.disconnect(null);
		} finally {
			if (clientSession != null) {
				clientSession.close();
			}
		}
	}

	private RemoteDevice searchDeviceByName(final String name) throws IOException, InterruptedException {
		final List<RemoteDevice> remoteDevices = searchDevices();

		for (final RemoteDevice remoteDevice : remoteDevices) {
			if (name.equals(remoteDevice.getFriendlyName(true))) {
				System.out.println("Found: " + name);

				return remoteDevice;
			}
		}

		System.out.println("Not Found: " + name);

		return null;
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

		final Bluetooth2 main = new Bluetooth2();

		// main.testDirectConnect("btgoep://24DA9B132084:12;authenticate=false;encrypt=false;master=false");

		System.out.println("Search MotoG3");
		final RemoteDevice device = main.searchDeviceByName("MotoG3");

		if (device != null) {
			System.out.println("Create session");
			final ClientSession session = (ClientSession) Connector
					.open("btgoep://24DA9B132084:12;authenticate=false;encrypt=false;master=false");

			System.out.println("Connect");
			final HeaderSet hsConnectReply = session.connect(null);

			if (hsConnectReply.getResponseCode() != ResponseCodes.OBEX_HTTP_OK) {
				System.out.println("Failed to connect");
			} else {
				System.out.println("Connect success");
			}

			if (device.isTrustedDevice()) {
				System.out.println("Already Trusted");
			}
			if (device.isAuthenticated()) {
				System.out.println("Already Authenticated");
			} else {
				System.out.println("Authenticate");
				device.authenticate();
			}
		}

		main.printLocalDeviceInfo(LocalDevice.getLocalDevice());

		final List<RemoteDevice> remoteDevices = main.searchDevices();

		for (final RemoteDevice remoteDevice : remoteDevices) {
			main.printRemoteDevice(remoteDevice);

			final Map<Integer, ServiceRecord[]> mapServices = main.searchServices(remoteDevice);

			main.printDeviceServices(mapServices);
		}

		System.out.println("Adios, caracola!!!");
	}

}
