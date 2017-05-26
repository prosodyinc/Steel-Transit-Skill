
package co.prosody.portAuthority.util;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Adithya
 */
public class DirectionCorrector {

    private static final Map<String, String> expectedInputs = new HashMap<>(200);

    static {
        expectedInputs.put("INBOUND", "INBOUND");
        expectedInputs.put("INBONE", "INBOUND");
        expectedInputs.put("INBALL", "INBOUND");
        expectedInputs.put("OUTBOUND", "OUTBOUND");
        expectedInputs.put("ALBUM", "OUTBOUND");
        expectedInputs.put("ALBON", "OUTBOUND");
        expectedInputs.put("TOWARDS", "INBOUND");
        expectedInputs.put("AWAY", "OUTBOUND");
    }

    public static String getDirection(String inputDirection) throws Exception {
    	String output=expectedInputs.get(inputDirection.toUpperCase());
        if (output != null) {
            return output;
        } else {
            throw new Exception ("Cannot understand the direction " + inputDirection);
        }
    }
}
