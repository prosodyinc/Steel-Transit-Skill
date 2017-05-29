package co.prosody.portAuthority.api;

import java.io.IOException;
import java.util.List;

import org.json.JSONException;

import co.prosody.portAuthority.util.Stop;

/**
 * Exposes methods of classes that use the True Time API
 * @author adambarson
 *
 */
public class TrueTime {
	
	protected static List<Message> generatePredictions(String route, String stationID ){
		return TrueTimeAPI.getPredictions(route, stationID);
	}
	
	protected static List<Message> generatePredictions(String stationID, int maxValues){
		return TrueTimeAPI.getPredictions(stationID, maxValues);
	}
	
	protected static List<Message> generatePredictions(String stationID){
		return TrueTimeAPI.getPredictions(stationID);
	}
	
	//similar to the GoogleMaps generateStops
	protected static List<Stop> getStopsAsJson(String route, String direction) throws IOException, JSONException{
		return TrueTimeAPI.getStopsAsJson(route, direction);
	}
	
	protected static List<Message> generateRoutes() {
		return TrueTimeAPI.getRoutes();
	}
	
	
}