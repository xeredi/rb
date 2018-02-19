package xeredi.obd;

public abstract class ObdCommand<T> {

	private final String code;

	protected T value;

	public ObdCommand(final String code) {
		super();
		this.code = code;
	}

	public final T getValue() {
		return value;
	}

}
