package com.amazon.util;

import com.amazon.speech.speechlet.Session;

import co.prosody.portAuthority.APIException;
import co.prosody.portAuthority.InvalidInputException;
import co.prosody.portAuthority.UnexpectedInputException;
import co.prosody.portAuthority.api.Message;
import co.prosody.portAuthority.api.TrueTime;
import co.prosody.portAuthority.api.TrueTimeAPI;
import co.prosody.portAuthority.googleMaps.GoogleMaps;
import co.prosody.portAuthority.googleMaps.NearestStopLocator;
import co.prosody.portAuthority.storage.PaInputData;
import co.prosody.portAuthority.util.DirectionCorrector;
import co.prosody.portAuthority.util.Location;
import co.prosody.portAuthority.util.LocationCorrector;
import co.prosody.portAuthority.util.Route;
import co.prosody.portAuthority.util.RouteCorrector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;

/**
 * @author brown
 *
 */
public class DataHelper {
	private static Logger log = LoggerFactory.getLogger(DataHelper.class);

	public static final String SESSION_OBJECT_NAME = "InputData";

	// INTENTS
	public static final String ONE_SHOT_INTENT_NAME = "OneshotBusIntent";
	public static final String RESET_INTENT_NAME = "ResetBusIntent";
	public static final String ALL_ROUTES_INTENT_NAME = "AllBusRoutesIntent";
	public static final String ROUTE_INTENT_NAME = "RouteBusIntent";
	public static final String LOCATION_INTENT_NAME = "LocationBusIntent";
	public static final String DIRECTION_INTENT_NAME = "DirectionBusIntent";

	// SLOTS
	public static final String ROUTE_NAME = "Route_Name";
	public static final String ROUTE_ID = "Route";
	// public static final String NAME = "Route";
	public static final String LOCATION = "Location";
	public static final String LAT = "lat";
	public static final String LONG = "long";
	public static final String ADDRESS = "address";
	public static final String DIRECTION = "Direction";

	public static final String LAST_QUESTION = "LastQuestion";

	// private ArrayList<String> validIntents = null;

	/*
	public static ArrayList<String> getValidIntents() {
		// if (validIntents!=null){
		// return validIntents;
		// } else {
		ArrayList<String> validIntents = new ArrayList<String>();
		validIntents.add(ONE_SHOT_INTENT_NAME);
		validIntents.add(RESET_INTENT_NAME);
		validIntents.add(ROUTE_INTENT_NAME);
		validIntents.add(LOCATION_INTENT_NAME);
		validIntents.add(DIRECTION_INTENT_NAME);
		validIntents.add("AMAZON.StopIntent");
		validIntents.add("AMAZON.CancelIntent");
		validIntents.add("AMAZON.HelpIntent");
		return validIntents;
	}

	public static boolean isValidIntent(String intentName) {
		return (getValidIntents().contains(intentName));
	}

	public static String getValueFromIntentSlot(Intent intent, String name) {
		log.trace("getValueFromIntentSlot" + intent.getName());
		Slot slot = intent.getSlot(name);
		if (slot == null) {
			log.error("Cannot get Slot={} for Intent={} ", name, intent.getName());
			// if we can't return the requested slot from this intent
			// return the default slot for the intent
			slot = intent.getSlot(getSlotNameForIntentName(intent.getName()));
		}
		return (slot != null) ? slot.getValue() : null;
	}

	private static String getSlotNameForIntentName(String intentName) {
		if (intentName == null) {
			return null;
		}

		String output = null;
		switch (intentName) {
		case ROUTE_INTENT_NAME:
			output = ROUTE_ID;
			break;
		case LOCATION_INTENT_NAME:
			output = LOCATION;
			break;
		case DIRECTION_INTENT_NAME:
			output = DIRECTION;
			break;
		}
		return output;
	}

	public static String getValueFromSession(Session session, String name) {
		log.info("getValuesFromSession name={}",name);
		if (session.getAttributes().containsKey(name)) {
			return (String) session.getAttribute(name);
		} else {
			return null;
		}
	}
	*/
//	public static String putDirectionValuesInSession(Session session, Intent intent) throws InvalidInputException {
//		log.trace("putDirectionValuesInSession" + intent.getName());
//
//		String direction = getValueFromIntentSlot(intent, DIRECTION);
//		log.info("retreivedSlot " + DIRECTION+" : "+direction);
//		if (direction == null) {
//			if (intent.getName().equals(ONE_SHOT_INTENT_NAME)) {
//				// For OneShotBusIntent, this is an acceptable condition.
//				log.info("Intent:" + intent.getName() + " direction is null");
//				return "";
//			} else {
//				log.info("Intent:" + intent.getName() + " direction is null");
//				throw new InvalidInputException("No Direction in Intent",
//						"Please repeat your direction. " + OutputHelper.DIRECTION_PROMPT);
//			}
//		}
//
//		try {
//			direction=DirectionCorrector.getDirection(direction);
//			log.info("putting value in session Slot " + DIRECTION+" : "+direction);
//			session.setAttribute(DIRECTION, direction);
//		} catch (Exception e) {
//			throw new InvalidInputException(e.getMessage(), e, "Please repeat your direction. " + OutputHelper.DIRECTION_PROMPT);
//		}
//
//		return "";
//	}
	
	public static void addDirectionToConversation(PaInputData data, String direction){
		if (direction != null)
		data.setDirection(direction);
	}
	
	public static void addLocationToConversation(PaInputData data, Location loc){
		if (loc != null){
		data.setLocation(loc);
		data.setLocationName(loc.getName());
		data.setLocationLat("" + loc.getLat());
		data.setLocationLong("" + loc.getLng());
		data.setLocationAddress(loc.getAddress());
		}
	}
	
	public static void addRouteToConversation(PaInputData data, Route route){
		if (route != null){
		data.setRouteID(route.getId());
		data.setRouteName(route.getName());
		}
	}
	
	
	/*
	*//**
	 * The location held in the intent's slot might contain an address or a
	 * landmark or business name. Here we call the Google Maps API to translate
	 * that to a street address and put it in session.
	 *//*
	public static String putLocationValuesInSession(Session session, Intent intent) throws InvalidInputException {
		log.info("putLocationValuesInSession" + intent.getName());
		String location = getValueFromIntentSlot(intent, LOCATION);
		log.info("retreivedSlot " + LOCATION+" : "+location);

		// Handle Null Location
		if (location == null) {
			if (intent.getName().equals(ONE_SHOT_INTENT_NAME)) {
				// For OneShotBusIntent, this is an acceptable condition.
				log.info("Intent:" + intent.getName() + " location is null");
				return "";
			} else {
				log.info("Intent:" + intent.getName() + " location is null");
				throw new InvalidInputException("No Location in Intent",
						"Please repeat your location. " + OutputHelper.LOCATION_PROMPT);
			}
		}

		// Find address for location
		try {
			location=LocationCorrector.getLocation(location);
			log.info("putting value in session Slot Location:" + location);
			session.setAttribute(LOCATION, location.toUpperCase());
			//Location c = NearestStopLocator.getSourceLocation(location);
			Location c = GoogleMaps.findSourceLocation(location);
			
			//String streetAddress = simplifyAddress(c.getAddress());
			session.setAttribute(LAT, c.getLat());
			session.setAttribute(LONG, c.getLng());
			session.setAttribute(ADDRESS, c.getStreetAddress());
			//TODO: add this to skill context and interpret in OutputHelper
			if (!c.isAddress()) {
				return "I found " + location + " at " + c.getStreetAddress() + ".";
			}

		} catch (JSONException jsonE) {
			throw new InvalidInputException("No Location in Intent", jsonE,
					"Please repeat your location. " + OutputHelper.LOCATION_PROMPT);
		} catch (IOException ioE) {
			throw new InvalidInputException("Cannot reach Google Maps ", ioE,
					"Please repeat your location. " + OutputHelper.LOCATION_PROMPT);
		} catch (UnexpectedInputException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}*/
	
	
	
	/// Route
	/*
	public static String putRouteValuesInSession(Session session, Intent intent) throws InvalidInputException {
		log.info("putRouteValuesInSession" + intent.getName());
		Route route;

		String routeID = getValueFromIntentSlot(intent, ROUTE_ID);
		log.info("retreivedSlot " + ROUTE_ID+" : "+routeID);

		// Handle Null routeID
		if (routeID == null) {
			if (intent.getName().equals(ONE_SHOT_INTENT_NAME)) {
				// For OneShotBusIntent, this is an acceptable condition.
				log.info("Intent:" + intent.getName() + " routeID is null");
				return "";
			} else {
				log.info("Intent:" + intent.getName() + " routeID is null");
				throw new InvalidInputException("No routeID in Intent", "Please repeat your bus line. " + OutputHelper.ROUTE_PROMPT);
			}
		}

		try {
			routeID = RouteCorrector.getRoute(routeID);

			route = getMatchedRoute(routeID);

			log.info("putting value in session Slot " + ROUTE_ID+" : "+route.getId());
			session.setAttribute(ROUTE_ID, route.getId());
			session.setAttribute(ROUTE_NAME, route.getName());

		} catch (UnexpectedInputException e) {
			//TODO: Rephrase if question different.
			String lastQuestion=DataHelper.getValueFromSession(session, DataHelper.LAST_QUESTION);
			log.error("UnexpectedInputException:Message={}:LastQuestion={}",e.getMessage(),lastQuestion);
			
			if ((lastQuestion!=null)&&(lastQuestion.equals(OutputHelper.LOCATION_PROMPT))){
				throw new InvalidInputException(e.getMessage(), e, OutputHelper.HELP_INTENT);
			}
			throw new InvalidInputException(e.getMessage(), e, "Please repeat your bus line. " + OutputHelper.ROUTE_PROMPT);
			
		} catch (APIException apiE) {
			throw new InvalidInputException("Route does not match API",
					"Could not find the bus line " + routeID + "." + OutputHelper.ROUTE_PROMPT);
		}

		return route.getId() + "," + route.getName();

	}
*/
	public static Route getMatchedRoute(String routeID) throws APIException {
		Route output = null;
		//List<Message> routes = TrueTimeAPI.getRoutes();
		List<Message> routes = TrueTime.generateRoutes();
		
		Iterator<Message> iterator = routes.iterator();
		while (iterator.hasNext()) {
			Message element = (Message) iterator.next();
			if (element.getMessageType().equalsIgnoreCase(Message.ERROR)) {
				throw new APIException("Error from API:"+element.getError());
			}
			if (element.getMessageType().equalsIgnoreCase(Message.ROUTE)) {
				if (routeID.equalsIgnoreCase(element.getRouteID())) {
					output = Route.createRoute(element);
				}
			}
		}
		if (output==null){
			throw new APIException("Route does not match API");
		}
		
		return output;
	}
	
}

