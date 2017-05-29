
package co.prosody.portAuthority.util;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.prosody.portAuthority.UnexpectedInputException;


/**
 *
 * @author Jonathan Brown jonathan.h.brown@gmail.com
 */
public class LocationCorrector {

	private static Logger log = LoggerFactory.getLogger(LocationCorrector.class);
	
    private static final Map<String, String> expectedInputs = new HashMap<>(20);

    static {
        expectedInputs.put("WHEN 25TH AVENUE", "120 FIFTH AVENUE");
    }

    public static String getLocation(String inputLocation) throws UnexpectedInputException{
    	//inputRoute = inputRoute.replaceAll("\\s+", "");
    	String output = expectedInputs.get(inputLocation.toUpperCase());
        if (output != null) {
        	return output;
        } else {
            return inputLocation;
        }
    }
}
