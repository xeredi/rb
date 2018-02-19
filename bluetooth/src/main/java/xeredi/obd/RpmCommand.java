package xeredi.obd;

public final class RpmCommand extends ObdCommand<Long> {

	public RpmCommand() {
		super("01 01");
	}

}
