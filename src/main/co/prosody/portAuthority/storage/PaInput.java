package co.prosody.portAuthority.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.speechlet.Session;

/**
 * Represents a user input to Port Authority Skill
 */
public final class PaInput {
	private static Logger log = LoggerFactory.getLogger(PaInput.class);
    private PaInputData data;
    private String id;

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
    public static PaInput newInstance(String id, PaInputData data) {
        PaInput input = new PaInput();
        input.setID(id);
        input.setData(data);
        return input;
    }
    
    protected String getID(){
    	return id;
    }
    
    protected void setID(String id){
    	this.id = id;
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
