package co.prosody.portAuthority;

public class SpokenException extends java.lang.Exception {


	private static final long serialVersionUID = -2463053831658335066L;
	private String speech=null;

	    /**
	     * Constructs a new exception with the specified detail message.  The
	     * cause is not initialized, and may subsequently be initialized by
	     * a call to {@link #initCause}.
	     *
	     * @param   message   the detail message. The detail message is saved for
	     *          later retrieval by the {@link #getMessage()} method.
	     * @param 	speech	the message to be spoken formatted in SSML
	     */
	    public SpokenException(String message, String speech) {
	        super(message);
	        this.speech=speech;
	    }

	    /**
	     * Constructs a new exception with the specified detail message and
	     * cause.  <p>Note that the detail message associated with
	     * {@code cause} is <i>not</i> automatically incorporated in
	     * this exception's detail message.
	     *
	     * @param  message the detail message (which is saved for later retrieval
	     *         by the {@link #getMessage()} method).
	     * @param  cause the cause (which is saved for later retrieval by the
	     *         {@link #getCause()} method).  (A <tt>null</tt> value is
	     *         permitted, and indicates that the cause is nonexistent or
	     *         unknown.)
	     * @param 	speech	the message to be spoken formatted in SSML
	     */
	    public SpokenException(String message, Throwable cause, String speech) {
	        super(message, cause);
	        this.speech=speech;
	    }

	    
	    /**
	     * @return SSML formatted message to be spoken, if it exists- otherwise, just return the plain text message
	     */
	    public String getSpeech(){
	    	String retval;
	    	if (this.speech!=null){
	    		retval = this.speech;
	    	} else {
	    		retval =  getMessage();
	    	}
	    	return retval;
	    }
}
