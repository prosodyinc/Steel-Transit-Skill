package co.prosody.portAuthority.api;

//import GetNextBusSpeechlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import co.prosody.portAuthority.googleMaps.GoogleMaps;
import co.prosody.portAuthority.googleMaps.LocationTracker;
import co.prosody.portAuthority.util.JsonUtils;
import co.prosody.portAuthority.util.Stop;


public class TrueTimeAPI extends BaseAPIParser {
	private static  Logger log = LoggerFactory.getLogger(TrueTimeAPI.class);
	public static final String TRUETIME_URL="http://truetime.portauthority.org/bustime/api/";
	public static final String VERSION="v3/";
	public static final String AGENCY="Port%20Authority%20Bus";
	public static final String CMD_PREDICTION="getpredictions";
	public static final String CMD_STOPS="getstops";
	public static final String CMD_ROUTES="getroutes";
	//moved to lambda configuration properties
	public static final String TRUETIME_ACCESS_ID=System.getenv("TRUETIME_ACCESS_ID");

	protected TrueTimeAPI() {
		log.trace("constructor");
	}

	/**
	 * Use the getstops request to retrieve the set of stops for the specified route and direction.
	 * Stop lists are only available for a valid route/direction pair. In other words, a list of all stops that service a particular route (regardless of direction) cannot be requested.
	 *  rt- single route designator (required). Alphanumeric designator of the route (ex. “20” or “X20”) for which a list of available stops is to be returned.
	 *  dir- single route direction (required). Direction of the route (ex. “East Bound”) for which a list of available stops is to be returned. This needs to match the direction in the getdirections call. When using multiple languages, it must match the direction for that language.
	 *  stpid - single stop id (required if rt and dir are not provided) -  Numeric ID number for a specific stop (ex. "305") for which a single stop is to be returned. Can send up to 10 stop parameters.
	 * @return
	 * error- Message if the processing of the request resulted in an error.
	 * stop- Encapsulates all descriptive information about a particular stop.
	 * stpid- Unique identifier representing this stop.
	 * stpnm- Display name of this stop (ex.“Madison and Clark”)
	 * lat- Latitude position of the stop in decimal degrees (WGS 84).
	 * lon- Longitude position of the stop in decimal degrees (WGS 84).
	 */
	protected static List<Message> getStops (String route, String direction){
		//http://truetime.portauthority.org/bustime/api/v3/getdirections?key=2UfTSDVsRQTSWCke23DVcyviW&rtpidatafeed=Port%20Authority%20Bus&rt=67
		String apiString= TRUETIME_URL+VERSION+CMD_STOPS+"?key="+TRUETIME_ACCESS_ID+"&rtpidatafeed="+AGENCY+"&rt="+route+"&dir="+direction;
		log.debug("getStops:apiString="+apiString);
		List<Message> messages=new ArrayList<Message>();
		try {
			messages= TrueTimeAPI.parse(apiString);
		} catch (IOException | SAXException | ParserConfigurationException e) {
			messages.clear();
			Message errorMsg=new Message();
			errorMsg.setError(e.getMessage());
			messages.add(errorMsg);
		}
		log.debug("getStops:messages size"+messages.size());
		return messages;

		
	}
	/* This was moved to LocationTracker
    /** 
     * Gets list of stops for a route#
     * @param route
     * @param direction
     * @return
     * @throws IOException
     * @throws JSONException 
     */
	/*
	protected static List<Stop> getStopsAsJson(String route, String direction) throws IOException, JSONException{
    	log.trace("getStopsAsJson: route={}, direction={}", route, direction);
    	//String url =  "http://truetime.portauthority.org/bustime/api/v2/getstops?key=929FvbAPSEeyexCex5a7aDuus&rt="+routeID+"&dir="+direction.toUpperCase()+"&format=json";
    	String url= TRUETIME_URL+VERSION+CMD_STOPS+"?key="+TRUETIME_ACCESS_ID+"&rtpidatafeed="+AGENCY+"&rt="+route+"&dir="+direction.toUpperCase()+"&format=json";
    	JSONObject stopsJSON = null;
       List<Stop> listOfStops = null;
       stopsJSON = JsonUtils.readJsonFromUrl(url);
       
       //TODO: should this be here?
       //listOfStops = LocationTracker.getStopDetails(stopsJSON);
       listOfStops = GoogleMaps.generateStops(stopsJSON);
       
       return listOfStops;
    }*/
	
	

	/**
	 * Use the getstops request to retrieve the set of stops for the specified route and direction.
	 * Stop lists are only available for a valid route/direction pair. In other words, a list of all stops that service a particular route (regardless of direction) cannot be requested.
	 *  rt- single route designator (required). Alphanumeric designator of the route (ex. “20” or “X20”) for which a list of available stops is to be returned.
	 *  dir- single route direction (required). Direction of the route (ex. “East Bound”) for which a list of available stops is to be returned. This needs to match the direction in the getdirections call. When using multiple languages, it must match the direction for that language.
	 *  stpid - single stop id (required if rt and dir are not provided) -  Numeric ID number for a specific stop (ex. "305") for which a single stop is to be returned. Can send up to 10 stop parameters.
	 * @return
	 * error- Message if the processing of the request resulted in an error.
	 * stop- Encapsulates all descriptive information about a particular stop.
	 * stpid- Unique identifier representing this stop.
	 * stpnm- Display name of this stop (ex.“Madison and Clark”)
	 * lat- Latitude position of the stop in decimal degrees (WGS 84).
	 * lon- Longitude position of the stop in decimal degrees (WGS 84).
	 */
	protected List<Message> getStops (long stpid){
		List<Message> retval= new ArrayList<Message>();
		Message msg=new Message();
		msg.setError("TrueTimeMessageParser method notImplemented");
		retval.add(msg);
		return retval;
	}

	/**
	 * Use the getpredictions request to retrieve predictions for one or more stops or one or more vehicles. Predictions are always returned in ascending order according to prdtm.
	 * Use the vid parameter to retrieve predictions for one or more vehicles currently being tracked. A maximum of 10 vehicles can be specified.
	 * Use the stpid parameter to retrieve predictions for one or more stops. A maximum of 10 stops can be specified.
	 * Note: The vid and stpid parameters cannot be combined in one request. If both parameters are specified on a request to getpredictions, only the first parameter specified on the request will be processed.
	 * Calls to getpredictions without specifying the vid or stpid parameters are not allowed. 
	 * Use the top parameter to specify the maximum number of predictions to return. If top is not specified, then all predictions matching the specified parameters will be returned.
	 * @param stpid - comma-delimited list of stop IDs (not available with vid parameter) Set of one or more stop IDs whose predictions are to be returned. For example: 5029,1392,2019,4367 will return predictions for the four stops. A maximum of 10 identifiers can be specified.
	 * @param rt - comma-delimited list of route designators (optional, available with stpid parameter) Set of one or more route designators for which matching predictions are to be returned.
	 * @param vid - comma-delimited list of vehicle IDs (not available with stpid parameter) Set of one or more vehicle IDs whose predictions should be returned. For example: 509,392,201,4367 will return predictions for four vehicles. A maximum of 10 identifiers can be specified. 
	 * @param top - number (optional) Maximum number of predictions to be returned.
	 * @param tmres - string(optional) - Resolution of time stamps. Set to “s” to get time resolution to the second. Set to “m” to get time resolution to the minute. If omitted, defaults to “m”. Date and time is represented in the following format: If specified as “s” YYYYMMDD HH:MM:SS If specified as “m” YYYYMMDD HH:MM Month is represented as two digits where January is equal to “01” and December is equal to “12”. Time is represented using a 24-hour clock.
	 * @return
	 * error - Message if the processing of the request resulted in an error.
	 * prd - Encapsulates a predicted arrival or departure time for the specified set of stops or vehicles.
	 * tmstmp - Date and time (local) the prediction was generated. Date and time is represented based on the tmres parameter.
	 * typ - Type of prediction. ‘A’ for an arrival prediction (prediction of when the vehicle will arrive at this stop). ‘D’ for a departure prediction (prediction of when the vehicle will depart this stop, if applicable). Predictions made for first stops of a route or layovers are examples of departure predictions.
	 * stpid -  Unique identifier representing the stop for which this prediction was generated.
	 * stpnm - Display name of the stop for which this prediction was generated.
	 * vid - Unique ID of the vehicle for which this prediction was generated.
	 * dstp- Linear distance (feet) left to be traveled by the vehicle before it reaches the stop associated with this prediction.
	 * rt - Alphanumeric designator of the route (ex. “20” or “X20”) for which this prediction was generated.
	 * rtdd - Language-specific route designator meant for display.
	 * rtdir - Direction of travel of the route associated with this prediction (ex. “East Bound”). 
	 * des - Final destination of the vehicle associated with this prediction.
	 * prdtm - Predicted date and time (local) of a vehicle’s arrival or departure to the stop associated with this prediction. Date and time is represented based on the tmres parameter.
	 * dly - “true” if the vehicle is delayed. The dly element is only present if the vehicle that generated this prediction is delayed. 
	 * tablockid - TA’s version of the scheduled block identifier for the work currently being performed by the vehicle.
	 * tatripid -TA’s version of the scheduled trip identifier for the vehicle’s current trip.
	 * zone- The zone name if the vehicle has entered a defined zones, otherwise blank.
	 */
	protected static List<Message> getPredictions (String route, String stationID){
		List<Message> messages= new ArrayList<Message>();
		String apiString= TRUETIME_URL+VERSION+CMD_PREDICTION+"?key="+TRUETIME_ACCESS_ID+"&rtpidatafeed="+AGENCY+"&rt="+route+"&stpid="+stationID;
		log.info("getPredictions:apiString="+apiString);

		//TrueTimeMessageParser tester = new TrueTimeMessageParser(apiString);
		try {
			messages=TrueTimeAPI.parse(apiString);
		} catch (IOException e) {
			log.info("getPredictions:IOE:messages size"+messages.size());
			messages.clear();
			Message errorMsg=new Message();
			errorMsg.setError("IOException:"+e.getMessage());
			messages.add(errorMsg);
		} catch (SAXException saxe) {
			log.info("getPredictions:SAXE:messages size"+messages.size());
			messages.clear();
			Message errorMsg=new Message();
			errorMsg.setError("SAXException:"+saxe.getMessage());
			messages.add(errorMsg);
		} catch (ParserConfigurationException pce) {
			log.info("getPredictions:PCE:messages size"+messages.size());
			messages.clear();
			Message errorMsg=new Message();
			errorMsg.setError("ParserConfiguration:"+pce.getMessage());
			messages.add(errorMsg);
		}
		
		return messages;
	}
	
	/**
	 * get predictions for all routes that stop at this stop.
	 * @param stationID
	 * @return
	 */
	protected static List<Message> getPredictions (String stationID){
		return getPredictions(stationID, 10);
	}
	
	protected static List<Message> getPredictions (String stationID, int maxValues){
		List<Message> messages= new ArrayList<Message>();
		String apiString= TRUETIME_URL+VERSION+CMD_PREDICTION+"?key="+TRUETIME_ACCESS_ID+"&rtpidatafeed="+AGENCY+"&stpid="+stationID+"&top="+maxValues;
		log.debug("getPredictions:apiString="+apiString);

		//TrueTimeMessageParser tester = new TrueTimeMessageParser(apiString);
		try {
			messages=TrueTimeAPI.parse(apiString);
		} catch (IOException | SAXException | ParserConfigurationException e) {
			messages.clear();
			Message errorMsg=new Message();
			errorMsg.setError(e.getMessage());
			messages.add(errorMsg);
		}
		log.debug("getPredictions:messages size"+messages.size());
		return messages;
	}


	protected static List<Message> parse(String apiString) 
			throws IOException, SAXException, ParserConfigurationException {

		//Create a "parser factory" for creating SAX parsers
		SAXParserFactory spfac = SAXParserFactory.newInstance();

		//Now use the parser factory to create a SAXParser object
		SAXParser sp = spfac.newSAXParser();

		//Create an instance of this class; it defines all the handler methods
		TrueTimeHandler handler = new TrueTimeHandler();

		//Finally, tell the parser to parse the input and notify the handler
		sp.parse(apiString, handler);

		return handler.getMessages();

	}

	/**
	 * 
	 * @return List of a single element, either an error or tm
	 * Date and time are represented in the following format: YYYYMMDD HH:MM:SS. Month is represented as two digits where January is “01” and December is “12”. Time is represented using a 24-hour clock. 
	 * 
	 */
	protected List<Message> getTime(){
		List<Message> retval= new ArrayList<Message>();
		Message msg=new Message();
		msg.setError("TrueTimeMessageParser method notImplemented");
		retval.add(msg);
		return retval;
	}

	/**
	 * Use the getvehicles request to retrieve vehicle information (i.e., locations) of all or a subset of vehicles currently being tracked by BusTime.
	 * Use the vid parameter to retrieve information for one or more vehicles currently being tracked.
	 * Use the rt parameter to retrieve information for vehicles currently running one or more of the specified routes.
	 * Note: The vid and rt parameters cannot be combined in one request. If both parameters are specified on a request to getvehicles, only the first parameter specified on the request will be processed.
	 * @param vid- Set of one or more vehicle IDs whose location should be returned. For example: 509,392,201,4367 will return information for four vehicles (if available). A maximum of 10 identifiers can be specified. 
	 * @param rt- A set of one or more route designators for which matching vehicles should be returned. For example: X3,4,20 will return information for all vehicles currently running on those three routes (if available). A maximum of 10 identifiers can be specified. 
	 * @param tmres- Resolution of time stamps. Set to “s” to get time resolution to the second. Set to “m” to get time resolution to the minute. If omitted, defaults to “m”.  Date and time is represented in the following format:  If specified as “s” YYYYMMDD HH:MM:SS If specified as “m” YYYYMMDD HH:MM Month is represented as two digits where January is equal to “01” and December is equal to “12”. Time is represented using a 24-hour clock.
	 * @param format- The format of the response.  Legal values are xml and json.  XML is the default format, and will be used if this parameter is not present in the request. 
	 * @return 
	 * error- Message if the processing of the request resulted in an error.
	 * vehicle- Encapsulates all information available for a single vehicle in the response.
	 * vid- Alphanumeric string representing the vehicle ID (ie. bus number)
	 * tmstmp- Date and local time of the last positional update of the vehicle. Date and time is represented in the following format: YYYYMMDD HH:MM. Month is represented as two digits where January is equal to “01” and December is equal to “12”. Time is represented using a 24-hour clock.
	 * lat- Latitude position of the vehicle in decimal degrees (WGS 84).
	 * lon- Longitude position of the vehicle in decimal degrees (WGS 84).
	 * hdg- Heading of vehicle as a 360o value, where 0o is North, 90o is East, 180o is South and 270o is West.
	 * pid- Pattern ID of trip currently being executed.
	 * pdist- Linear distance in feet that the vehicle has traveled into the pattern currently being executed.
	 * rt- Route that is currently being executed by the vehicle (ex. “20”).
	 * des- Destination of the trip being executed by the vehicle (ex. “Austin”).
	 * dly- The value is “true” if the vehicle is delayed. The dly element is only present if the vehicle is delayed.
	 * spd- Speed as reported from the vehicle expressed in miles per hour (MPH).
	 * tablockid- TA’s version of the scheduled block identifier for the work currently being performed by the vehicle.
	 * tatripid- TA’s version of the scheduled trip identifier for the vehicle’s current trip.
	 * zone- The zone name if the vehicle has entered a defined zone, otherwise blank.
	 */
	protected List<Message> getVehicles(){
		List<Message> retval= new ArrayList<Message>();
		Message msg=new Message();
		msg.setError("TrueTimeMessageParser method notImplemented");
		retval.add(msg);
		return retval;
	}

	/**
	 * 
	 * @return
	 * error	Contains a message if the processing of the request resulted in an error.
	 * route	Encapsulates a route serviced by the system.
	 * rt	Child element of the route element. Alphanumeric designator of a route (ex. "20" or "X20").
	 * rtnm	Child element of the route element. Common name of the route (ex. "Madison" for the 20 route).
	 * rtclr	Child element of the route element. Color of the route line used in map (ex. "#ffffff")
	 * rtdd	Child element of the route element. Language-specific route designator meant for display.
	 */
	protected static List<Message> getRoutes(){
		////http://truetime.portauthority.org/bustime/api/v3/getroutes?key=2UfTSDVsRQTSWCke23DVcyviW&rtpidatafeed=Port%20Authority%20Bus&rt=67
		String apiString= TRUETIME_URL+VERSION+CMD_ROUTES+"?key="+TRUETIME_ACCESS_ID+"&rtpidatafeed="+AGENCY;
		log.debug("getRoutes:apiString="+apiString);
		List<Message> messages=new ArrayList<Message>();
		try {
			messages= TrueTimeAPI.parse(apiString);
		} catch (IOException | SAXException | ParserConfigurationException e) {
			messages.clear();
			Message errorMsg=new Message();
			errorMsg.setError(e.getMessage());
			messages.add(errorMsg);
		}
		log.debug("getRoutes:messages size"+messages.size());
		return messages;
	}

	/**
	 * 
	 * @return
	 */
	protected List<Message> getDirections(){
		List<Message> retval= new ArrayList<Message>();
		Message msg=new Message();
		msg.setError("TrueTimeMessageParser method notImplemented");
		retval.add(msg);
		return retval;
	}

	/**
	 * Use the getpatterns request to retrieve the set of geo-positional points and stops that when connected can be used to construct the geo-positional layout of a pattern (i.e., route variation).
	 * Use pid to specify one or more identifiers of patterns whose points are to be returned. A maximum of 10 patterns can be specified.
	 * Use rt to specify a route identifier where all active patterns are returned. The set of active patterns returned includes: one or more patterns marked as “default” patterns for the specified route and all patterns that are currently being executed by at least one vehicle on the specified route.
	 * Note: The pid and rt parameters cannot be combined in one request. If both parameters are specified on a request to getpatterns, only the first parameter specified on the request will be processed.
	 * @param pid- comma-delimited list of pattern IDs (not available with rt parameter) Set of one or more pattern IDs whose points should be returned. For example: 56,436,1221 will return points from three (3) patterns. A maximum of 10 identifiers can be specified.
	 * @param rt- single route designator (not available with pid parameter) Route designator for which all active patterns should be returned.
	 * @return
	 * error- Message if the processing of the request resulted in an error.
	 * ptr- Encapsulates a set of points which define a pattern.
	 * pid- ID of pattern.
	 * ln- Length of the pattern in feet.
	 * rtdir- Direction that is valid for the specified route designator. For example, “East Bound”.
	 * pt- Child element of the root element. Encapsulates one a set of geo-positional points (including stops) that when connected define a pattern.
	 * seq- Position of this point in the overall sequence of points.
	 * typ- ‘S’ if the point represents a Stop, ‘W’ if the point represents a waypoint along the route.
	 * stpid- If the point represents a stop, the unique identifier of the stop.
	 * stpnm- If the point represents a stop, the display name of the stop.
	 * pdist- If the point represents a stop, the linear distance of this point (feet) into the requested pattern.
	 * lat- Latitude position of the point in decimal degrees (WGS 84).
	 * lon- Longitude position of the point in decimal degrees (WGS 84).
	 */
	protected List<Message> getPatterns(){
		List<Message> retval= new ArrayList<Message>();
		Message msg=new Message();
		msg.setError("TrueTimeMessageParser method notImplemented");
		retval.add(msg);
		return retval;
	}

	/**
	 * 
	 * @return
	 */
	protected List<Message> getLocaleList(){
		List<Message> retval= new ArrayList<Message>();
		Message msg=new Message();
		msg.setError("TrueTimeMessageParser method notImplemented");
		retval.add(msg);
		return retval;
	}

	/**
	 * 
	 * @return
	 */
	protected List<Message> getServiceBulletins(){
		List<Message> retval= new ArrayList<Message>();
		Message msg=new Message();
		msg.setError("TrueTimeMessageParser method notImplemented");
		retval.add(msg);
		return retval;
	}

}
