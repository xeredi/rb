package xeredi.bluetooth;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.pi4j.io.serial.Baud;
import com.pi4j.io.serial.DataBits;
import com.pi4j.io.serial.FlowControl;
import com.pi4j.io.serial.Parity;
import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialConfig;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.StopBits;

// TODO: Auto-generated Javadoc
/**
 * The Class Client.
 */
public class Client {

	/** The Constant LOG. */
	private static final Log LOG = LogFactory.getLog(Client.class);

	/** The Constant GPS_PORT_ID. */
	// private static final String CANBUS_PORT_ID = "/dev/ttyS81";
	private static final String CANBUS_PORT_ID = "/dev/rfcomm0";

	/** The Constant CANBUS_ADDRESS. */
	private static final String CANBUS_ADDRESS = "5C:F3:70:88:34:24";

	/** The Constant CANBUS_CHANNEL_NO. */
	private static final String CANBUS_CHANNEL_NO = "8";

	/** The Constant GPS_PORT_SPEED. */
	private static final int CANBUS_PORT_SPEED = 9600;

	/**
	 * Start.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws InterruptedException
	 *             the interrupted exception
	 */
	public void start() throws IOException, InterruptedException {
		final Serial serial = SerialFactory.createInstance();
		final SerialConfig config = new SerialConfig();
		final Baud baud = Baud.getInstance(CANBUS_PORT_SPEED);

		config.device(CANBUS_PORT_ID).baud(baud).dataBits(DataBits._8).parity(Parity.NONE).stopBits(StopBits._1)
				.flowControl(FlowControl.NONE);

		serial.open(config);

		try (final InputStream is = serial.getInputStream(); final OutputStream os = serial.getOutputStream();) {
			// LOG.info("RPM Command");
			//
			// final RPMCommand command = new RPMCommand();
			//
			// command.run(is, os);
			//
			// LOG.info("RPM: " + command.getRPM());

			read("ATZ", is, os);
			// read("ATZ", is, os);
			// read("ATH1", is, os);
			// read("ATL1", is, os);
			// read("ATS1", is, os);
			// read("ATSP0", is, os);
			for (int i = 0; i < 5; i++) {
				LOG.info("RPM: " + read("01 0C", is, os));
				LOG.info("Speed: " + read("01 0D", is, os));
				LOG.info("Errors: " + read("03", is, os));
			}
		}

		serial.close();
	}

	/**
	 * Read.
	 *
	 * @param command
	 *            the command
	 * @param is
	 *            the is
	 * @param os
	 *            the os
	 * @return the string
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws InterruptedException
	 *             the interrupted exception
	 */
	private String read(final String command, final InputStream is, final OutputStream os)
			throws IOException, InterruptedException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("command: " + command);
		}

		os.write((command + "\r").getBytes());

		// Thread.sleep(500L);

		final StringBuilder buffer = new StringBuilder();

		char c;
		byte b = 0;

		while (((b = (byte) is.read()) > -1)) {
			c = (char) b;
			if (c == '>') // read until '>' arrives
			{
				break;
			}
			buffer.append(c);
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug("buffer: " + buffer);
		}

		return buffer.toString();
	}

	/**
	 * Configure.
	 */
	private void configure() throws IOException {
		LOG.info("Chanel configuration");

		final File file = new File(CANBUS_PORT_ID);

		if (file.exists()) {
			LOG.info(CANBUS_PORT_ID + " already configured");
		} else {
			LOG.info(CANBUS_PORT_ID + " configure");

			final String line = "sudo rfcomm bind " + CANBUS_PORT_ID + " " + CANBUS_ADDRESS + " " + CANBUS_CHANNEL_NO;

			LOG.info("Command: " + line);

			final CommandLine cmdLine = CommandLine.parse(line);
			final DefaultExecutor executor = new DefaultExecutor();

			int exitValue = executor.execute(cmdLine);

			LOG.info("exitValue: " + exitValue);
		}
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
	public static void main(String[] args) throws IOException, InterruptedException {
		Client client = new Client();

		LOG.info("Start");

		client.configure();
		client.start();

		LOG.info("End");
	}// main
}