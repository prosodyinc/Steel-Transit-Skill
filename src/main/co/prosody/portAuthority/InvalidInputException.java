/**
 * 
 */
package co.prosody.portAuthority;

/**
 * @author jonathanbrown
 *
 */
public class InvalidInputException extends SpokenException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2463053831658335066L;

	public InvalidInputException(String message, Throwable cause, String speech) {
		super(message, cause, speech);
	}

	public InvalidInputException(String message, String speech) {
		super(message, speech);
	}



}
