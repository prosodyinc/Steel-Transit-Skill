/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.prosody.portAuthority.googleMaps;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.util.OutputHelper;

import co.prosody.portAuthority.InvalidInputException;
import co.prosody.portAuthority.api.TrueTimeAPI;
import co.prosody.portAuthority.util.JsonUtils;
import co.prosody.portAuthority.util.Location;
import co.prosody.portAuthority.util.Stop;

/**
 *
 * @author Adithya
 */
public class LocationTracker {

	private static Logger log = LoggerFactory.getLogger(LocationTracker.class);

    /**
     * Case 1: Request returns list of Coordinates
     * Proceed to Step 2
     * 
     * Case 2: Unable to understand source location
     * Ask user to try again.

     * 
     * @param json returned by striking the Google maps API
     * @param limit set limit to the number of places returned by the API
     * @return
     * @throws JSONException 
     */
	protected static List<Location> getLatLngDetails(JSONObject json, int limit) throws JSONException, InvalidInputException {
    	List<Location> output = new ArrayList<>();
    	
        JSONArray results = json.getJSONArray("results");
        log.debug("JSON Results Size={}",results.length());
        if (results.length() == 0) {
            throw new InvalidInputException("No results from JSON","I did not understand the source location, " + OutputHelper.LOCATION_PROMPT);
        }
        int numResultsToReturn=Math.min(limit, results.length());
        
        
        JSONObject result;
       	JSONObject location;

        for (int i = 0; i < numResultsToReturn; i++) {
        	result = results.getJSONObject(i);
        	
        	location = result.getJSONObject("geometry").getJSONObject("location");
        	Location c = new Location(
        			result.getString("name"),
        			location.getDouble("lat"),
        			location.getDouble("lng"),
        			result.getString("formatted_address"),
        			makeList(result.getJSONArray("types")));

        	output.add(c);
        }
        return output;
    }
    
	protected static List<String> makeList(JSONArray array) throws JSONException{
    	List<String>  output = new ArrayList<String>();
    	for (int i=0;i<array.length();i++){
    		output.add(array.getString(i));
    	}
    	return output;
    }
    
	/* MOVED FROM TRUETIMEAPI
	 * Note: this makes this method dependent on existence of TrueTimeAPI.
	 * Someday if needed, the URL constants can be passed in as parameters to workaround this*/
	
	/**
     * Gets list of stops for a route#
     * @param route
     * @param direction
     * @return
     * @throws IOException
     * @throws JSONException 
     */
	protected static List<Stop> getStopsAsJson(String route, String direction) throws IOException, JSONException{
    	log.trace("getStopsAsJson: route={}, direction={}", route, direction);
    	//String url =  "http://truetime.portauthority.org/bustime/api/v2/getstops?key=929FvbAPSEeyexCex5a7aDuus&rt="+routeID+"&dir="+direction.toUpperCase()+"&format=json";
    	String url= TrueTimeAPI.TRUETIME_URL+TrueTimeAPI.VERSION+
    			TrueTimeAPI.CMD_STOPS+"?key="+TrueTimeAPI.TRUETIME_ACCESS_ID+"&rtpidatafeed="+
    			TrueTimeAPI.AGENCY+"&rt="+route+"&dir="+direction.toUpperCase()+"&format=json";
    	JSONObject stopsJSON = null;
       List<Stop> listOfStops = null;
       stopsJSON = JsonUtils.readJsonFromUrl(url);
       
       listOfStops = getStopDetails(stopsJSON);
       //listOfStops = GoogleMaps.generateStops(stopsJSON);
       
       return listOfStops;
    }
	
	protected static List<Stop> getStopDetails(JSONObject json) throws JSONException {
    	List<Stop> stops = new ArrayList<>();
        JSONArray stopsResponse = json.getJSONObject("bustime-response").getJSONArray("stops");

        if (stopsResponse != null) {
            for (int i = 0; i < stopsResponse.length(); i++) {
                JSONObject stop = stopsResponse.getJSONObject(i);
                double lat = stop.getDouble("lat");
                double lon = stop.getDouble("lon");
                String stopID = stop.getString("stpid");
                String stpnm = stop.getString("stpnm");
                Stop s = new Stop(stopID, stpnm, lat, lon);
                stops.add(s);
            }
        }
        return stops;
    }
}
