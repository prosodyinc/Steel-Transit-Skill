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
	
	/**
     * Finds nearest bus stop by determining the shortest distance between
     * source location and list of bus stop locations returned by TrueTime API.
	 * @param source The source location
	 * @param routeID The route ID
	 * @param direction The direction
	 * @return The nearest stop
	 * @throws InvalidInputException
	 * @throws IOException
	 * @throws JSONException
	 */
	public static Stop findNearestStop(Location source, String routeID, String direction) throws InvalidInputException, IOException, JSONException {
		return NearestStopLocator.process(source, routeID, direction);
	}
	
	/** 
     * Pads "Pittsburgh" to input and requests the Google location API. Returns first (most relevant) result returned by Google Maps.
	 * @param location The provided location
	 * @return Location The location found by Google Maps.
	 * @throws InvalidInputException
	 * @throws IOException
	 * @throws JSONException
	 */
	public static Location findSourceLocation(String location) throws InvalidInputException, IOException, JSONException{
		return NearestStopLocator.getSourceLocation(location);
	}
	
	/**
	 * Generates directions from the location provided by the user to the calculated stop.
	 * @param locationLat The latitude of the location
	 * @param locationLon The longitude of the location
	 * @param stopLat The latitude of the stop
	 * @param stopLon The longitude of the stop
	 * @return Directions from the location to the stop
	 * @throws Exception
	 */
	public static String generateDirections(String locationLat, String locationLon, double stopLat, double stopLon) throws Exception {
		JSONObject json = NearestStopLocator.getDirections(locationLat, locationLon, stopLat, stopLon);
        return Instructions.getInstructions(json);
	}
	
	/**
	 * Generates an image URL for directions from the location provided by the user to the calculated stop.
	 * Used for cards.
	 * @param locationLat The latitude of the location
	 * @param locationLon The longitude of the location
	 * @param stopLat The latitude of the stop
	 * @param stopLon The longitude of the stop
	 * @return URL for image
	 * @throws Exception
	 */
	public static String generateImageURL(String locationLat, String locationLon, double stopLat, double stopLon) throws Exception {
		JSONObject json = NearestStopLocator.getDirections(locationLat, locationLon, stopLat, stopLon);
		return NearestStopLocator.buildImage(locationLat, locationLon, stopLat, stopLon) + Instructions.printWayPoints(json);
	}
	
}
