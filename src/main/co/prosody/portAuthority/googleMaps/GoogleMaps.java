package co.prosody.portAuthority.googleMaps;

import java.io.IOException;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import co.prosody.portAuthority.InvalidInputException;
import co.prosody.portAuthority.util.Location;
import co.prosody.portAuthority.util.Stop;

/**
 * Exposes methods of classes that use the Google Maps API
 * @author adambarson
 *
 */
public class GoogleMaps {
	
	public static Stop findNearestStop(Location source, String routeID, String direction) throws InvalidInputException, IOException, JSONException {
		return NearestStopLocator.process(source, routeID, direction);
	}
	
	public static Location findSourceLocation(String location) throws InvalidInputException, IOException, JSONException{
		return NearestStopLocator.getSourceLocation(location);
	}
	
	public static String generateInstructions(String locationLat, String locationLon, double stopLat, double stopLon) throws Exception {
		JSONObject json = NearestStopLocator.getDirections(locationLat, locationLon, stopLat, stopLon);
        return Instructions.getInstructions(json);
	}
	
	public static String generateImageURL(String locationLat, String locationLon, double stopLat, double stopLon) throws Exception {
		JSONObject json = NearestStopLocator.getDirections(locationLat, locationLon, stopLat, stopLon);
		return NearestStopLocator.buildImage(locationLat, locationLon, stopLat, stopLon) + Instructions.printWayPoints(json);
	}
	
	// Maybe this should generate the json object from a route and direction instead of having one passed in
	public static List<Stop> generateStops(JSONObject json) throws JSONException{
		return LocationTracker.getStopDetails(json);
	}
}
