
package co.prosody.portAuthority.util;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.prosody.portAuthority.UnexpectedInputException;


/**
 *
 * @author Adithya
 */
public class RouteCorrector {

	private static Logger log = LoggerFactory.getLogger(RouteCorrector.class);
	
    private static final Map<String, String> expectedInputs = new HashMap<>(200);

    static {
        expectedInputs.put("1", "1");
        expectedInputs.put("on", "1");
        expectedInputs.put("won", "1");
        expectedInputs.put("2", "2");
        expectedInputs.put("do", "2");
        expectedInputs.put("4", "4");
        expectedInputs.put("fork", "4");
        expectedInputs.put("6", "6");
        expectedInputs.put("sick", "6");
        expectedInputs.put("7", "7");
        expectedInputs.put("8", "8");
        expectedInputs.put("ate", "8");
        expectedInputs.put("11", "11");
        expectedInputs.put("12", "12");
        expectedInputs.put("13", "13");
        expectedInputs.put("14", "14");
        expectedInputs.put("15", "15");
        expectedInputs.put("16", "16");
        expectedInputs.put("17", "17");
        expectedInputs.put("18", "18");;
        expectedInputs.put("19L", "19L");
        expectedInputs.put("20", "20");
        expectedInputs.put("21", "21");
        expectedInputs.put("22", "22");;
        expectedInputs.put("24", "24");
        expectedInputs.put("26", "26");
        expectedInputs.put("27", "27");
        expectedInputs.put("28X", "28X");
        expectedInputs.put("29", "29");
        expectedInputs.put("31", "31");
        expectedInputs.put("301", "31");
        expectedInputs.put("36", "36");;
        expectedInputs.put("38", "38");
        expectedInputs.put("39", "39");
        expectedInputs.put("40", "40");
        expectedInputs.put("41", "41");
        expectedInputs.put("401", "41");
        expectedInputs.put("43", "43");
        expectedInputs.put("44", "44");
        expectedInputs.put("48", "48");;
        expectedInputs.put("51", "51");
        expectedInputs.put("501", "51");
        expectedInputs.put("51L", "51L");
        expectedInputs.put("501L", "51L");
        expectedInputs.put("52L", "52L");
        expectedInputs.put("53", "53");
        expectedInputs.put("53L", "53L");
        expectedInputs.put("54", "54");;
        expectedInputs.put("55", "55");
        expectedInputs.put("56", "56");
        expectedInputs.put("57", "57");
        expectedInputs.put("58", "58");
        expectedInputs.put("59", "59");
        expectedInputs.put("60", "60");
        expectedInputs.put("61A", "61A");
        expectedInputs.put("61B", "61B");
        expectedInputs.put("61C", "61C");
        expectedInputs.put("61D", "61D");
        expectedInputs.put("601A", "61A");
        expectedInputs.put("601B", "61B");;
        expectedInputs.put("601C", "61C");
        expectedInputs.put("601D", "61D");
        expectedInputs.put("64", "64");
        expectedInputs.put("65", "65");
        expectedInputs.put("67", "67");
        expectedInputs.put("68", "68");
        expectedInputs.put("69", "69");
        expectedInputs.put("71", "71");
        expectedInputs.put("701", "71");
        expectedInputs.put("71A", "71A");
        expectedInputs.put("71B", "71B");
        expectedInputs.put("71C", "71C");
        expectedInputs.put("71D", "71D");
        expectedInputs.put("701A", "71A");
        expectedInputs.put("701B", "71B");
        expectedInputs.put("701C", "71C");
        expectedInputs.put("701D", "71D");
        expectedInputs.put("74", "74");
        expectedInputs.put("75", "75");
        expectedInputs.put("77", "77");
        expectedInputs.put("78", "78");
        expectedInputs.put("79", "79");
        expectedInputs.put("81", "81");
        expectedInputs.put("82", "82");
        expectedInputs.put("83", "83");
        expectedInputs.put("86", "86");
        expectedInputs.put("87", "87");
        expectedInputs.put("88", "88");
        expectedInputs.put("89", "89");
        expectedInputs.put("91", "91");
        expectedInputs.put("901", "91");
        expectedInputs.put("93", "93");;
        expectedInputs.put("BLLB", "BLLB");
        expectedInputs.put("BLSV", "BLSV");
        expectedInputs.put("G2", "G2");
        expectedInputs.put("G3", "G3");
        expectedInputs.put("G31", "G31");
        expectedInputs.put("O1", "O1");
        expectedInputs.put("O12", "O12");
        expectedInputs.put("O5", "O5");
        expectedInputs.put("P1", "P1");
        expectedInputs.put("P10", "P10");
        expectedInputs.put("P12", "P12");
        expectedInputs.put("P13", "P13");
        expectedInputs.put("P16", "P16");
        expectedInputs.put("P17", "P17");
        expectedInputs.put("P2", "P2");
        expectedInputs.put("P3", "P3");
        expectedInputs.put("P67", "P67");
        expectedInputs.put("P68", "P68");
        expectedInputs.put("P69", "P69");
        expectedInputs.put("P7", "P7");
        expectedInputs.put("P71", "P71");
        expectedInputs.put("P76", "P76");
        expectedInputs.put("P78", "P78");
        expectedInputs.put("RED", "RED");
        expectedInputs.put("Y1", "Y1");
        expectedInputs.put("Y45", "Y45");
        expectedInputs.put("Y46", "Y46");
        expectedInputs.put("Y47", "Y47");
        expectedInputs.put("Y49", "Y49");
        expectedInputs.put("DQI", "DQI");
        expectedInputs.put("MI", "MI");
    }

    public static String getRoute(String inputRoute) throws UnexpectedInputException{
    	inputRoute = inputRoute.toUpperCase();
    	inputRoute = removeIs(inputRoute);
    	inputRoute = inputRoute.replaceAll("\\s+", "");
    	
    	String output = expectedInputs.get(inputRoute);
        if (output != null) {
        	return output;
        } else {
            throw new UnexpectedInputException ("Cannot find route " + inputRoute);
        }
    }
    
    public static String removeIs (String inputRoute) {
    	inputRoute = inputRoute.replace("YEARS", "");
    	inputRoute = inputRoute.replace("NEWS", "");
    	inputRoute = inputRoute.replace("S", "");
    	inputRoute = inputRoute.replace("AND", "");
    	inputRoute = inputRoute.replace("IN", "");
    	return inputRoute;
    }
}
