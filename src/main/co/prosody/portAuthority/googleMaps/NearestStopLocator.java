/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.prosody.portAuthority.googleMaps;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.prosody.portAuthority.InvalidInputException;
import co.prosody.portAuthority.api.TrueTime;
import co.prosody.portAuthority.api.TrueTimeAPI;
import co.prosody.portAuthority.util.JsonUtils;
import co.prosody.portAuthority.util.Location;
import co.prosody.portAuthority.util.Stop;

import java.util.ArrayList;

/**
 */
//TODO Change Name of this class
public class NearestStopLocator {

    private static Logger log = LoggerFactory.getLogger(NearestStopLocator.class);

    private static final String TEXT_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/textsearch/json";
    private static final String DISTANCE_MATRIX_URL = "https://maps.googleapis.com/maps/api/distancematrix/json";
    private static final String DIRECTIONS_URL = "https://maps.googleapis.com/maps/api/directions/json?origin=%s,%s&destination=%f,%f&mode=walking&key=%s";

    private static final String GOOGLE_MAPS_KEY = System.getenv("GOOGLE_MAPS_KEY");
    private static final String STATIC_MAPS_KEY = System.getenv("STATIC_MAPS_KEY");

    /**
     * TODO Change Name of this method
     *
     * @param source
     * @param routeID
     * @param direction
     * @return
     * @throws IOException
     * @throws JSONException
     * @throws InvalidInputException
     */
    protected static Stop process(Location source, String routeID, String direction) throws IOException, JSONException, InvalidInputException {
        log.trace("Process: source={}, routeId={}, direction={}", source, routeID, direction);
        if ((source == null) || (routeID == null) || (direction == null)) {
            log.error("Bad Input");
            throw new InvalidInputException("Null input: source=" + source + ",routeID=" + routeID + ",direction=" + direction, "I forgot something");
        }

        //Get list of stops of the route# returned by truetime:
        //List<Stop> listOfStops = TrueTimeAPI.getStopsAsJson(routeID, direction);
        List<Stop> listOfStops = LocationTracker.getStopsAsJson(routeID, direction);
        //List<Stop> listOfStops = TrueTime.getStopsAsJson(routeID, direction);
        
        //find nearest stop from the source location:
        Stop nearestStop = findNearestStop(source.getLat(), source.getLng(), listOfStops);

        return nearestStop;

    }

    protected static Location getSourceLocation(String source) throws IOException, JSONException, InvalidInputException {
        log.trace("getSourceLocation: source={}", source);
        List<Location> sourceLocation = getSourceLocationCoordinates(source, 1);

        //get the first location returned:
        return (sourceLocation.get(0));

    }

    /**
     * Pads "Pittsburgh" to input and requests the Google location API which
     * returns list of location details.
     *
     * @param source name / Landmark / Any keyword/s to identify source location
     * @return List of top 5 location details. (1st being the most relevant)
     * @throws IOException
     * @throws JSONException
     */
    protected static List<Location> getSourceLocationCoordinates(String source, int numResults) throws IOException, JSONException, InvalidInputException {
        log.info("getSourceLocationCoordinates: source={}", source);
        JSONObject currentLocationDetails = null;
        List<Location> listOfLocationCoordinates = null;
        String currLocation = (source + " Pittsburgh").replaceAll("\\s", "+");
        String currLocationURL = TEXT_SEARCH_URL + "?query=" + currLocation + "&sensor=false&key=" + GOOGLE_MAPS_KEY;
        currentLocationDetails = JsonUtils.readJsonFromUrl(currLocationURL);
        if (currentLocationDetails != null) {
            listOfLocationCoordinates = LocationTracker.getLatLngDetails(currentLocationDetails, numResults);
        }
        return listOfLocationCoordinates;
    }

    protected static JSONObject getDirections(String locationLat, String locationLng, double stopLat, double stopLng) throws IOException, JSONException {
        return JsonUtils.readJsonFromUrl(String.format(DIRECTIONS_URL, locationLat, locationLng, stopLat, stopLng, GOOGLE_MAPS_KEY));
    }

    /**
     * @deprecated Gets list of stops for a route#
     * @param routeID
     * @param direction
     * @return
     * @throws IOException
     * @throws JSONException
     *
     */
    //    public static List<Stop> getStops(String routeID, String direction) throws IOException, JSONException{
    //    	log.info("getStops: routeId={}, direction={}", routeID, direction);
    //    	//TODO: Move to TrueTimeMessageParser
    //    	String url =  "http://truetime.portauthority.org/bustime/api/v2/getstops?key=929FvbAPSEeyexCex5a7aDuus&rt="+routeID+"&dir="+direction.toUpperCase()+"&format=json";
    //       JSONObject stopsJSON = null;
    //       List<Stop> listOfStops = null;
    //       stopsJSON = JsonUtils.readJsonFromUrl(url);
    //       listOfStops = LocationTracker.getStopDetails(stopsJSON);
    //       return listOfStops;
    //    }
    /**
     * Finds nearest bus stop by determining the shortest distance between
     * source location and list of bus stop locations returned by truetime API.
     *
     * @param sourceLat
     * @param sourceLon
     * @param stops
     * @return
     * @throws JSONException
     * @throws IOException
     */
    protected static Stop findNearestStop(double sourceLat, double sourceLon, List<Stop> stops) throws JSONException, IOException {
		//TODO Improve efficiency of findNearestStop

		//        "rows": [ {
        //            "elements": [ {
        //              "status": "OK",
        //              "duration": {
        //                "value": 340110,
        //                "text": "3 jours 22 heures"
        //              },
        //              "distance": {
        //                "value": 1734542,
        //                "text": "1 735 km"
        //              }
        //            }, 
        //  
        List<Stop> nearestStops = new ArrayList<>();
        int numberOfBatches = 0;
        if (stops.size() % 25 == 0) {
            numberOfBatches = stops.size() / 25;
        } else {
            numberOfBatches = stops.size() / 25 + 1;
        }
        log.info("NUMBER OF STOPS: " + stops.size());

        /*
         index 0
         index 25
         index 50
	
         = (batch = 0)*25;
         (batch = 1)* 25 
         (batch = 2)*25
         */
        int currentbatch = 0;
        while (currentbatch < numberOfBatches) {
            List<Stop> currentBatchStops = new ArrayList<>();
            String destinationString = "";
            for (int index = currentbatch * 25; index < (currentbatch + 1) * 25 && index < stops.size(); index++) {
                destinationString += "|" + stops.get(index).getLatitude() + "," + stops.get(index).getLongitude();
                currentBatchStops.add(stops.get(index));
            }
            String url = DISTANCE_MATRIX_URL + "?origins=" + sourceLat + "," + sourceLon + "&destinations=" + destinationString.substring(1) + "&key=" + GOOGLE_MAPS_KEY + "&units=imperial&mode=walk&transit_mode=walking";
            log.info("DISTANCE MATRIX URL: " + url);
            Stop batchNearest = calculateShortestDistance(url, currentBatchStops);
            log.info("{},{},{}:{},{}:{},{}", batchNearest.getStopName(), batchNearest.getDistance(), sourceLat, sourceLon, batchNearest.getLatitude(), batchNearest.getLongitude());
            nearestStops.add(batchNearest);
            currentbatch++;
        }

        Collections.sort(nearestStops);

        return nearestStops.get(0);
    }

    protected static Stop calculateShortestDistance(String url, List<Stop> currentBatchStops) throws JSONException, IOException {
        //log.info("DISTANCE_MATRIX_URL:"+url);
        JSONArray distanceJSON = null;
        distanceJSON = JsonUtils.readJsonFromUrl(url)
                .getJSONArray("rows")
                .getJSONObject(0)
                .getJSONArray("elements");

        for (int j = 0; j < distanceJSON.length(); j++) {
            //log.info("JSON Elements at "+j+":"+distanceJSON.getJSONObject(j));
            Stop stop = currentBatchStops.get(j);
            stop.setDistance(distanceJSON.getJSONObject(j).getJSONObject("distance").getDouble("value"));
            stop.setWalkTime(distanceJSON.getJSONObject(j).getJSONObject("duration").getString("value"));
            // log.info("{},{},{}:{},{}:{},{}", stop.getStopName(), stop.getDistance(), sourceLat, sourceLon, stop.getLatitude(), stop.getLongitude());
        }

        Collections.sort(currentBatchStops);
        return currentBatchStops.get(0);
    }

    /**
     * @deprecated Calculates walking distance (in ft) between source and
     * destination locations using Google distancematrix API
     * @param sourceLat
     * @param sourceLon
     * @param destLat
     * @param destLon
     * @return
     */
//	public static double calculateDistance(double sourceLat, double sourceLon, double destLat, double destLon) throws JSONException, IOException{
//		//log.info("calculateDistance: sourceLat={}, sourceLon={}, destLat={}, destLon={}", sourceLat, sourceLon, destLat,destLon );
//		String url =  DISTANCE_MATRIX_URL+"?origins="+sourceLat+","+sourceLon+"&destinations="+destLat+","+destLon+"&key="+GOOGLE_MAPS_KEY+"&units=imperial&mode=walk&transit_mode=walking";
//		JSONObject distanceJSON = null;
//		String distance = "";
//		distanceJSON = JsonUtils.readJsonFromUrl(url);
//		distance = distanceJSON.getJSONArray("rows").getJSONObject(0).getJSONArray("elements").getJSONObject(0).getJSONObject("distance").getString("text");
//		return convertMileToFeet(distance);
//	}
    /**
     * *************************************Util
     * methods**********************************************
     */
    protected static double convertMileToFeet(String distance) {
        log.info("convertMileToFeet: distance={}", distance);

        double result = 0.0;
        if (distance.contains("mi")) {
            result = Double.parseDouble(distance.substring(0, distance.length() - 2)) * 5280.0;
        } else if (distance.contains("ft")) {
            result = Double.parseDouble(distance.substring(0, distance.length() - 2));
        } else {
            result = 0.0;
        }
        return result;
    }

    protected static String buildImage(String locationLat, String locationLon, double stopLat, double stopLon) {
        String url = "https://maps.googleapis.com/maps/api/staticmap?size=1200x800&maptype=roadmap&key=" + STATIC_MAPS_KEY + "&format=png&visual_refresh=true&markers=size:mid%7Ccolor:0xff0000%7Clabel:1%7C" + locationLat + "," + locationLon + "&markers=size:mid%7Ccolor:0xff0000%7Clabel:2%7C" + stopLat + "," + stopLon + "&path=color:0x0000ff|weight:5|";
        log.info("buildImage url={}",url);
        return url;
    }
}
