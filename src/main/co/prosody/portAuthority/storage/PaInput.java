package co.prosody.portAuthority.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.speechlet.Session;

/**
 * Represents a user input to Port Authority Skill
 */
public final class PaInput {
	private static Logger log = LoggerFactory.getLogger(PaInput.class);
    private Session session;
    private PaInputData data;

    private PaInput() {
    }

    /**
     * Creates a new instance of {@link PaInput} with the provided {@link Session} and
     * {@link PaInputData}.
     * <p>
     * To create a new instance of {@link PaInputData}, see
     * {@link PaInputData#newInstance()}
     * 
     * @param session
     * @param data
     * @return
     * @see PaInputData#newInstance()
     */
    public static PaInput newInstance(Session session, PaInputData data) {
        PaInput input = new PaInput();
        input.setSession(session);
        input.setData(data);
        return input;
    }

    protected void setSession(Session session) {
        this.session = session;
    }

    protected Session getSession() {
        return session;
    }

    public PaInputData getData() {
        return data;
    }

    protected void setData(PaInputData data) {
        this.data = data;
    }

    /**
     */
    public boolean hasAllData() {
        return (hasLocation()&&hasDirection()&&hasRouteID());
    }
    
    public boolean hasStopName() {
        return !(data.getStopName()==null);
    }
    
    public boolean hasDirection() {
        return !(data.getDirection()==null);
    }
    
    public boolean hasLocation() {
        return !( data.getLocationName()==null);
    }
    
    public boolean hasRouteID() {
        return !(data.getRouteID()==null);
    }

}
