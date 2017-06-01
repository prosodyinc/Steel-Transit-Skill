package co.prosody.portAuthority;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.BasicConfigurator;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.OutputSpeech;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.SsmlOutputSpeech;
import com.amazon.util.ConversationRouter;
import com.amazon.util.DataHelper;
import com.amazon.util.OutputHelper;
import com.amazon.util.SkillContext;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import co.prosody.portAuthority.api.Message;
import co.prosody.portAuthority.api.TrueTime;
import co.prosody.portAuthority.api.TrueTimeAPI;
import co.prosody.portAuthority.storage.PaDao;
import co.prosody.portAuthority.storage.PaDynamoDbClient;
import co.prosody.portAuthority.storage.PaInputData;
import co.prosody.portAuthority.util.*;
import co.prosody.portAuthority.googleMaps.*;

public class GetNextBusSpeechlet implements Speechlet {

	private static Logger log = LoggerFactory.getLogger(GetNextBusSpeechlet.class);
	
	public static ObjectMapper mapper = new ObjectMapper();

	public static final String INVOCATION_NAME = "Steel Transit";

	private SkillContext skillContext;

	private AmazonDynamoDBClient amazonDynamoDBClient;
	private PaDynamoDbClient dynamoDbClient;
	private PaDao inputDao;

	private AnalyticsManager analytics;

	/** PUBLIC METHODS ******************************/
	/**
	 * called when the skill is first requested and no intent is provided return
	 */
	public SpeechletResponse onLaunch(LaunchRequest request, Session session) throws SpeechletException {
		BasicConfigurator.configure();
		log.info("onLaunch requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());
		// TODO: Pull the Skill Context out of history, too.
		PaInputData storedInput = (PaInputData) session.getAttribute(DataHelper.SESSION_OBJECT_NAME);
		
		if ((storedInput != null) && storedInput.hasAllData()) {
			analytics.postEvent(AnalyticsManager.CATEGORY_LAUNCH, "Return Saved");
			skillContext.setNeedsLocation(false);
			List<Message> predictions = getPredictions(storedInput);
			return buildResponse(storedInput, predictions);
		} else {
			analytics.postEvent(AnalyticsManager.CATEGORY_LAUNCH, "Welcome");
			//TODO: review whether this value should be placed in session by someone else. 
			session.setAttribute(DataHelper.LAST_QUESTION, OutputHelper.ROUTE_PROMPT);
			return OutputHelper.getWelcomeResponse();
		}
	}

	/**
	 * Called when an intent is first received, before handing to onIntent.
	 * Establishes which
	 */
	public void onSessionStarted(SessionStartedRequest request, Session session) throws SpeechletException {
		log.info("onSessionStarted requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());
		
		analytics = new AnalyticsManager();
		analytics.setUserId(session.getUser().getUserId());
		analytics.postSessionEvent(AnalyticsManager.ACTION_SESSION_START);

		skillContext = new SkillContext();
		
		// create a new data object and add it to the session.
		session.setAttribute(DataHelper.SESSION_OBJECT_NAME, PaInputData.newInstance(session.getUser().getUserId()));
		
		
		
		log.info("Called from onSessionStarted {}", session.getAttribute(DataHelper.SESSION_OBJECT_NAME).getClass().toString());
		
	}
	
	/**
	 * Called when the user invokes an intent.
	 */
	public SpeechletResponse onIntent(IntentRequest request, Session session) throws SpeechletException {
		log.info("onIntent intent={}, requestId={}, sessionId={}", request.getIntent().getName(),
				request.getRequestId(), session.getSessionId());
		log.info("onIntent sessionValue={}", session.getAttributes().toString());
		
		/* It appears that onSessionStarted is not being called, at least when I test online. Don't know why that would be.. 
		 * Maybe when you test online, onSessionStarted isn't called */
		
		if (analytics == null){
			analytics = new AnalyticsManager();
			analytics.setUserId(session.getUser().getUserId());
			analytics.postSessionEvent(AnalyticsManager.ACTION_SESSION_START);
		} 
		if (skillContext == null){
			skillContext = new SkillContext();
		}
		
		PaInputData data;
		if (session.getAttribute(DataHelper.SESSION_OBJECT_NAME) == null){
			log.info("Data object is null, adding one now");
			data = PaInputData.newInstance(session.getUser().getUserId());
			session.setAttribute(DataHelper.SESSION_OBJECT_NAME, data);
		} else {
			data = PaInputData.create(session.getAttribute(DataHelper.SESSION_OBJECT_NAME));
		}
		
		
		
		String feedbackText = "";
		try {
			Intent intent = request.getIntent();
			analytics.postEvent(AnalyticsManager.CATEGORY_INTENT, intent.getName());
			switch (intent.getName()) {
			case "AMAZON.StopIntent":
				return OutputHelper.getStopResponse();
			case "AMAZON.CancelIntent":
				return OutputHelper.getStopResponse();
			case "AMAZON.HelpIntent":
				return OutputHelper.getHelpResponse();

			case DataHelper.RESET_INTENT_NAME:
				
				// Delete current record for this user
				this.getPaDao().deletePaInput(session.getUser().getUserId());

				// Notify the user of success
				PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
				outputSpeech.setText("Reset History");
				return SpeechletResponse.newTellResponse(outputSpeech);

			case DataHelper.ALL_ROUTES_INTENT_NAME:
				// try to retrieve current record for this user
				//PaInputData input = PaInputData.create(session.getAttribute(DataHelper.SESSION_OBJECT_NAME), session.getUser().getUserId());
				PaInputData input = getPaDao().getPaInputData(session.getUser().getUserId());
				
				if ((input != null) && input.hasAllData()) { // if record found
																// and the all
																// necessary
																// data was
																// found therein
					analytics.postEvent(AnalyticsManager.CATEGORY_LAUNCH, "Return Saved");

					// get predictions for all routes for this stop
					skillContext.setAllRoutes(true);
					skillContext.setNeedsLocation(false);
					// TODO: Make this part of the normal conversation
					List<Message> predictions = getPredictions(input);
					return buildResponse(input, predictions);

				} else { // if there is not enough information retrieved
							// continue with conversation
					log.debug("AllRoutesIntent was unable to retreive all saved data");
				}
				break;

			case DataHelper.ONE_SHOT_INTENT_NAME:
				// collect all the information provided by the user
				
				skillContext.setAllRoutes(false);
				if (getValueFromIntentSlot(intent, DataHelper.ROUTE_ID)!=null){
					feedbackText = putRouteValuesInSession(session, intent);
				}

				if (getValueFromIntentSlot(intent, DataHelper.LOCATION)!=null){
					feedbackText += putLocationValuesInSession(session, intent);
				}

				if (getValueFromIntentSlot(intent, DataHelper.DIRECTION)!=null){
					feedbackText += putDirectionValuesInSession(session, intent);
				}

				break;

//			case DataHelper.DIRECTION_INTENT_NAME:
//				// collect the direction information
//				feedbackText = DataHelper.putDirectionValuesInSession(session, intent);
//				break;
//
//			case DataHelper.LOCATION_INTENT_NAME:
//				// collect the location information
//				feedbackText = DataHelper.putLocationValuesInSession(session, intent);
//				break;
//
//			case DataHelper.ROUTE_INTENT_NAME:
//				// collect the route information
//				feedbackText = DataHelper.putRouteValuesInSession(session, intent);
//				break;
			
			//TODO: figure out the life cycle of skill context
			default:
				skillContext.setAllRoutes(false);
				feedbackText= ConversationRouter.putValuesInSession(session, intent);
			}

		} catch (InvalidInputException e) {
			analytics.postException(e.getMessage(), false);
			return OutputHelper.newAskResponse(e.getSpeech(), e.getSpeech());
		}

		// if we don't have everything we need to create predictions, continue
		// the conversation
		SpeechletResponse furtherQuestions;
		if ((furtherQuestions = ConversationRouter.checkForAdditionalQuestions(session, feedbackText)) != null) {
			return furtherQuestions;
		} else if (log.isInfoEnabled()) {
			logSession(session, "Returning response for:");
		}

		// OK, the user has entered everything, save their entries
		analytics.postEvent(AnalyticsManager.CATEGORY_INTENT, "Collected all input");

		// TODO: use input data from the get go.
		PaInputData inputData = PaInputData.create(session.getAttribute(DataHelper.SESSION_OBJECT_NAME));
		try {
			if (inputData.getStopID() == null) {
				skillContext.setNeedsLocation(true);
				inputData.setStop(getNearestStop(inputData));
			} else {
				skillContext.setNeedsLocation(false);
			}
			//we only want to save the input if we successfully fetched all the data
			if (inputData.hasAllData()){
				saveInputToDB(inputData);
			}
		} catch (InvalidInputException | IOException | JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return OutputHelper.getFailureResponse("Google Maps");
			
		} finally {
			//saveInputToDB(inputData);
		}

		List<Message> predictions = getPredictions(inputData);
		log.info(predictions.toString());
		// get speech response
		return buildResponse(inputData, predictions);

	}
	
	public void onSessionEnded(SessionEndedRequest request, Session session) throws SpeechletException {
		log.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());
		analytics.postSessionEvent(AnalyticsManager.ACTION_SESSION_END);
	}

	private Stop getNearestStop(PaInputData in) throws InvalidInputException, IOException, JSONException {
		Location c = new Location();
		c.setAddress(in.getLocationAddress());
		c.setLat(new Double(in.getLocationLat()).doubleValue());
		c.setLng(new Double(in.getLocationLong()).doubleValue());

		//return NearestStopLocator.process(c, in.getRouteID(), in.getDirection());
		return GoogleMaps.findNearestStop(c, in.getRouteID(), in.getDirection());
	}

	private List<Message> getPredictions(PaInputData inputData) {
		List<Message> messages = new ArrayList<Message>();
		if (skillContext.isAllRoutes()) {
			//messages = TrueTimeAPI.getPredictions(inputData.getStopID());
			messages = TrueTime.generatePredictions(inputData.getStopID());
		} else {
			//messages = TrueTimeAPI.getPredictions(inputData.getRouteID(), inputData.getStopID());
			messages = TrueTime.generatePredictions(inputData.getRouteID(), inputData.getStopID());
		}
		return messages;
	}

	private SpeechletResponse buildResponse(PaInputData inputData, List<Message> messages) {
		SpeechletResponse output;
		try {
			if (messages.size() == 0) {
				log.info("No Messages");
				output = OutputHelper.getNoResponse(inputData, skillContext);
				analytics.postEvent(AnalyticsManager.CATEGORY_RESPONSE, "No Result", "Null", messages.size());
				return output;
			}

			if ((messages.size() == 1) && (messages.get(0).getMessageType().equals(Message.ERROR))) {
				log.error("1 error message:" + messages.get(0) + ":" + messages.get(0).getError());
				analytics.postEvent(AnalyticsManager.CATEGORY_RESPONSE, "No Result", messages.get(0).getError(),
						messages.size());
				return OutputHelper.getNoResponse(inputData, skillContext);

			}

			ArrayList<Result> results = new ArrayList<Result>();
			for (int i = 0; i < messages.size(); i++) {
				results.add(new Result(messages.get(i).getRouteID(), messages.get(i).getEstimate()));
			}
			
			if (skillContext.isAllRoutes()) {
				analytics.postEvent(AnalyticsManager.CATEGORY_RESPONSE, "Success",
						"All routes at " + inputData.getStopName(), messages.size());
			} else {
				analytics.postEvent(AnalyticsManager.CATEGORY_RESPONSE, "Success",
						inputData.getRouteName() + " at " + inputData.getStopName(), messages.size());
			}
			
			return OutputHelper.getResponse(inputData, results, skillContext);

		} catch (Exception e) {
			analytics.postException(e.getMessage(), true);
			e.printStackTrace();
		}
		return null;
	}


	/**
	 * Helper method to log the data currently stored in session.
	 * 
	 * @param session
	 * @param intro
	 */
	private void logSession(Session session, String intro) {
		Map<String, Object> attributes = session.getAttributes();
		Set<String> set = attributes.keySet();
		Iterator<String> itr = set.iterator();
		while (itr.hasNext()) {
			String element = itr.next();
			log.info(intro + "Session:" + element + ":" + session.getAttribute(element));
		}
	}

	/**
	 * Matches numerics to Strings, too.
	 * 
	 * @return
	 */
	private boolean match(String s1, String s2) {
		if (s1.toUpperCase().contains(s2.toUpperCase())) {
			return true;
		}
		// replace numbers with words
		if (StringUtils.isAlphanumericSpace(s1) && !StringUtils.isAlphaSpace(s1)) {
			s1 = replaceNumWithOrdinalWord(s1);
		}
		if (StringUtils.isAlphanumericSpace(s2) && !StringUtils.isAlphaSpace(s2)) {
			s2 = replaceNumWithOrdinalWord(s2);
		}
		if (s1.toUpperCase().contains(s2.toUpperCase())) {
			return true;
		}
		return false;
	}

	private String replaceNumWithOrdinalWord(String inputString) {
		log.debug("replaceNumWithOrdinalWord input:" + inputString);
		StringBuffer output = new StringBuffer(inputString.length());
		String digitStr = "";

		for (int i = 0; i < inputString.length(); i++) {
			if (Character.isDigit(inputString.charAt(i))) {
				digitStr += inputString.charAt(i);
			} else if (Character.isAlphabetic(inputString.charAt(i)) && !digitStr.isEmpty()) {
				// ignore alphabetics that are juxtaposed with digits
			} else if (digitStr.isEmpty()) {
				output.append(inputString.charAt(i));
			} else {
				// translate the digits and move them over
				output.append(NumberMaps.num2OrdWordMap.get(Integer.parseInt(digitStr)));
				digitStr = "";
			}
		}
		if (!digitStr.isEmpty()) {
			// translate the digits and move them over
			output.append(NumberMaps.num2OrdWordMap.get(Integer.parseInt(digitStr)));
			digitStr = "";
		}
		String returnValue = new String(output);
		log.debug("replaceNumWithOrdinalWord returning:" + returnValue);
		return returnValue;
	}

	private SpeechletResponse handleFatalExcpetion(Session s, Exception e) {
		e.printStackTrace();
		analytics.postException(e.getMessage(), true);
		return OutputHelper.newTellResponse(e.getMessage());
	}

	private SpeechletResponse handleExcpetion(Session s, Exception e, boolean fatal) {
		if (fatal) {
			return handleFatalExcpetion(s, e);
		} else {
			e.printStackTrace();
			analytics.postException(e.getMessage(), false);
			return OutputHelper.newAskResponse(e.getMessage(), e.getMessage());
		}
	}

	private AmazonDynamoDBClient getAmazonDynamoDBClient() {
		if (this.amazonDynamoDBClient == null) {
			this.amazonDynamoDBClient = new AmazonDynamoDBClient();
		}
		return this.amazonDynamoDBClient;
	}

	private PaDynamoDbClient getPaDynamoDbClient() {
		if (this.dynamoDbClient == null) {
			this.dynamoDbClient = new PaDynamoDbClient(getAmazonDynamoDBClient());
		}
		return this.dynamoDbClient;
	}

	private PaDao getPaDao() {
		if (this.inputDao == null) {
			this.inputDao = new PaDao(getPaDynamoDbClient());
		}
		return this.inputDao;
	}

	private void saveInputToDB(PaInputData input) {
		getPaDao().savePaInput(input);

	}
	
	
	
	
	/* Moved from DataHelper */
	
	public static ArrayList<String> getValidIntents() {
		// if (validIntents!=null){
		// return validIntents;
		// } else {
		ArrayList<String> validIntents = new ArrayList<String>();
		validIntents.add(DataHelper.ONE_SHOT_INTENT_NAME);
		validIntents.add(DataHelper.RESET_INTENT_NAME);
		validIntents.add(DataHelper.ROUTE_INTENT_NAME);
		validIntents.add(DataHelper.LOCATION_INTENT_NAME);
		validIntents.add(DataHelper.DIRECTION_INTENT_NAME);
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
		case DataHelper.ROUTE_INTENT_NAME:
			output = DataHelper.ROUTE_ID;
			break;
		case DataHelper.LOCATION_INTENT_NAME:
			output = DataHelper.LOCATION;
			break;
		case DataHelper.DIRECTION_INTENT_NAME:
			output = DataHelper.DIRECTION;
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
	
	public static String putDirectionValuesInSession(Session session, Intent intent) throws InvalidInputException {
		log.trace("putDirectionValuesInSession" + intent.getName());

		String direction = getValueFromIntentSlot(intent, DataHelper.DIRECTION);
		log.info("retreivedSlot " + DataHelper.DIRECTION+" : "+direction);
		if (direction == null) {
			if (intent.getName().equals(DataHelper.ONE_SHOT_INTENT_NAME)) {
				// For OneShotBusIntent, this is an acceptable condition.
				log.info("Intent:" + intent.getName() + " direction is null");
				return "";
			} else {
				log.info("Intent:" + intent.getName() + " direction is null");
				throw new InvalidInputException("No Direction in Intent",
						"Please repeat your direction. " + OutputHelper.DIRECTION_PROMPT);
			}
		}

		try {
			direction=DirectionCorrector.getDirection(direction);
			log.info("putting value in session Slot " + DataHelper.DIRECTION +" : "+direction);
			//session.setAttribute(DataHelper.DIRECTION, direction);
			PaInputData data = PaInputData.create(session.getAttribute(DataHelper.SESSION_OBJECT_NAME));
			DataHelper.addDirectionToConversation(data, direction);
			session.setAttribute(DataHelper.SESSION_OBJECT_NAME, data);
		} catch (Exception e) {
			throw new InvalidInputException(e.getMessage(), e, "Please repeat your direction. " + OutputHelper.DIRECTION_PROMPT);
		}

		return "";
	}
	
	
	/**
	 * The location held in the intent's slot might contain an address or a
	 * landmark or business name. Here we call the Google Maps API to translate
	 * that to a street address and put it in session.
	 */
	public static String putLocationValuesInSession(Session session, Intent intent) throws InvalidInputException {
		log.info("putLocationValuesInSession" + intent.getName());
		String location = getValueFromIntentSlot(intent, DataHelper.LOCATION);
		log.info("retreivedSlot " + DataHelper.LOCATION+" : "+location);

		// Handle Null Location
		if (location == null) {
			if (intent.getName().equals(DataHelper.ONE_SHOT_INTENT_NAME)) {
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
			
			Location c = GoogleMaps.findSourceLocation(location);
			
			PaInputData data = PaInputData.create(session.getAttribute(DataHelper.SESSION_OBJECT_NAME));
			DataHelper.addLocationToConversation(data, c);
			session.setAttribute(DataHelper.SESSION_OBJECT_NAME, data);
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
	}
	
	
	/// Route
		public static String putRouteValuesInSession(Session session, Intent intent) throws InvalidInputException {
			log.info("putRouteValuesInSession" + intent.getName());
			Route route;

			String routeID = getValueFromIntentSlot(intent, DataHelper.ROUTE_ID);
			log.info("retreivedSlot " + DataHelper.ROUTE_ID+" : "+routeID);

			// Handle Null routeID
			if (routeID == null) {
				if (intent.getName().equals(DataHelper.ONE_SHOT_INTENT_NAME)) {
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

				route = DataHelper.getMatchedRoute(routeID);

				PaInputData data = PaInputData.create(session.getAttribute(DataHelper.SESSION_OBJECT_NAME));
				log.info("putting value in session Slot " + DataHelper.ROUTE_ID+" : "+route.getId());

				DataHelper.addRouteToConversation(data, route);
				session.setAttribute(DataHelper.SESSION_OBJECT_NAME, data);

			} catch (UnexpectedInputException e) {
				//TODO: Rephrase if question different.
				//TODO: use skill context instead
				String lastQuestion=getValueFromSession(session, DataHelper.LAST_QUESTION);
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
		
		
		
}

