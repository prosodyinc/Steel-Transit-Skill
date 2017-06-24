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
import com.amazon.speech.ui.Card;
import com.amazon.speech.ui.Image;
import com.amazon.speech.ui.OutputSpeech;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.SsmlOutputSpeech;
import com.amazon.speech.ui.StandardCard;
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

	private AmazonDynamoDBClient amazonDynamoDBClient;
	private PaDynamoDbClient dynamoDbClient;
	private PaDao inputDao;

	private AnalyticsManager analytics;
	
	private SkillContext skillContext;
	private PaInputData data;
	private InformationFetcher fetcher;
	
	private ResponseObject response;

	/** PUBLIC METHODS ******************************/
	/**
	 * called when the skill is first requested and no intent is provided return
	 */
	public SpeechletResponse onLaunch(LaunchRequest request, Session session) throws SpeechletException {
		BasicConfigurator.configure();
		log.info("onLaunch requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());
		
		
		
		skillContext = SkillContext.create(session.getAttribute(DataHelper.SKILL_CONTEXT_NAME));
		
		//Try to fetch the user's history
		PaInputData storedInput = loadData(session.getUser().getUserId());
		if (storedInput != null) {
			data = storedInput;
			analytics.postEvent(AnalyticsManager.CATEGORY_LAUNCH, "Return Saved");
			skillContext.setNeedsLocation(false);
			session.setAttribute(DataHelper.SKILL_CONTEXT_NAME, skillContext);
			List<Message> predictions = getPredictions();
			return buildResponse(predictions);
		} else {
			analytics.postEvent(AnalyticsManager.CATEGORY_LAUNCH, "Welcome");
			//by default, the lastQuestion of skill context is Route_prompt
			return createWelcomeResponse();
		}
		
		
	}

	/**
	 * Called when an intent is first received, before handing to onIntent.
	 */
	public void onSessionStarted(SessionStartedRequest request, Session session) throws SpeechletException {
		log.info("onSessionStarted requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());
		
		analytics = new AnalyticsManager();
		analytics.setUserId(session.getUser().getUserId());
		analytics.postSessionEvent(AnalyticsManager.ACTION_SESSION_START);
	
		// create a new data object and context object and add it to the session.
		data = PaInputData.newInstance(session.getUser().getUserId());
		skillContext = SkillContext.newInstance();
		session.setAttribute(DataHelper.SESSION_OBJECT_NAME, data);
		session.setAttribute(DataHelper.SKILL_CONTEXT_NAME, skillContext);
		
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
		
		switch (request.getIntent().getName()){
		case(DataHelper.ONE_SHOT_INTENT_NAME):
			fetcher = new OneShotFetcher();
			break;
		case(DataHelper.ROUTE_INTENT_NAME):
			fetcher = new SingleIntentFetcher();
			break;
		case(DataHelper.DIRECTION_INTENT_NAME):
			fetcher = new SingleIntentFetcher();
			break;
		case(DataHelper.LOCATION_INTENT_NAME):
			fetcher = new SingleIntentFetcher();
			break;
		}
		
		if (session.getAttribute(DataHelper.SKILL_CONTEXT_NAME) == null){
			log.info("Skill Context object is null, adding one now");
			skillContext = SkillContext.newInstance();
			session.setAttribute(DataHelper.SKILL_CONTEXT_NAME, skillContext);
		} else {
			skillContext = SkillContext.create(session.getAttribute(DataHelper.SKILL_CONTEXT_NAME));
			skillContext.setFeedbackText("");
		}
		
		if (session.getAttribute(DataHelper.SESSION_OBJECT_NAME) == null){
			log.info("Data object is null, adding one now");
			data = PaInputData.newInstance(session.getUser().getUserId());
			session.setAttribute(DataHelper.SESSION_OBJECT_NAME, data);
		}  else {
			data = PaInputData.create(session.getAttribute(DataHelper.SESSION_OBJECT_NAME));
		}
		
		/* ------  */
		
		
		try {
			Intent intent = request.getIntent();
			
			analytics.postEvent(AnalyticsManager.CATEGORY_INTENT, intent.getName());
			switch (intent.getName()) {
			case "AMAZON.StopIntent":
				return createStopResponse();
			case "AMAZON.CancelIntent":
				return createStopResponse();
			case "AMAZON.HelpIntent":
				return createHelpResponse();

			case DataHelper.RESET_INTENT_NAME:
				// Delete current record for this user
				this.getPaDao().deletePaInput(session.getUser().getUserId());
				// Notify the user of success
				PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
				outputSpeech.setText("Reset History");
				return SpeechletResponse.newTellResponse(outputSpeech);

			case DataHelper.ALL_ROUTES_INTENT_NAME:
				// try to retrieve current record for this user
				PaInputData input = loadData(session.getUser().getUserId());
				
				if (input != null) { // if record found
																// and the all
																// necessary
																// data was
																// found therein
					data = input;
					analytics.postEvent(AnalyticsManager.CATEGORY_LAUNCH, "Return Saved");

					// get predictions for all routes for this stop
				
					skillContext.setAllRoutes(true);
					skillContext.setNeedsLocation(false);
					
					// TODO: Make this part of the normal conversation
					List<Message> predictions = getPredictions();
					return buildResponse(predictions);

				} else { // if there is not enough information retrieved
							// continue with conversation
					log.debug("AllRoutesIntent was unable to retreive all saved data");
				}
				break;

			case DataHelper.ONE_SHOT_INTENT_NAME:
				skillContext.setAllRoutes(false);
				// collect all the information provided by the user
				extractRoute(session, intent);
				extractDirection(session, intent);
				extractLocation(session, intent);
				break;
				
			//Either a route intent, direction intent, or location intent
			default:
				handleSingleIntent(session, intent);
				break;
			}
		} catch (InvalidInputException e) {
			analytics.postException(e.getMessage(), false);
			return newAskResponse(e.getSpeech(), e.getSpeech());
		}

		//check to see if we're missing any information
		ConversationRouter.checkForAdditionalQuestions(data, skillContext);
		saveAttributes(session);
		//if there are additional questions to be asked, we need to save the attributes for the next go around of the session.
		if (skillContext.getAdditionalQuestions()){
			saveAttributes(session);
			return newAskResponse(skillContext.getFeedbackText() + skillContext.getLastQuestion(), skillContext.getLastQuestion());
		} else if (log.isInfoEnabled()) {
			logSession(session, "Returning response for:");
		}

		// OK, the user has entered everything, save their entries
		analytics.postEvent(AnalyticsManager.CATEGORY_INTENT, "Collected all input");

		try {
			if (data.getStopID() == null) {
				skillContext.setNeedsLocation(true);
				Stop stop = getNearestStop();
				data.setStop(stop);
				if (stop.getDistance() > 1609.34){
					log.info("Stop is over a mile away.");
					data.setStopID(null);
					skillContext.setLastQuestion(OutputHelper.LOCATION_PROMPT);
					return newAskResponse(stop.getStopName() + " is over a mile away from the "
							+ data.getLocationName() + " I found. Please say the location again and be more specific.",
							"Please say the location again and be more specific.");
				}
				log.info("Distance from location to the stop: {}", stop.getDistance());
			} else {
				skillContext.setNeedsLocation(false);
			}
			//we only want to save the input if we successfully fetched all the data
			if (data.hasAllData()){
				saveInputToDB(data);
			}
		} catch (InvalidInputException | IOException | JSONException e) {
			e.printStackTrace();
			return getFailureResponse("Google Maps");
			
		} finally {
			//saveInputToDB(inputData);
		}
		//TODO: can getPredictions and buildResponse be combined?
		List<Message> predictions = getPredictions();
		log.info(predictions.toString());
		// get speech response
		return buildResponse(predictions);

	}
	
	/**
	 * If the session must go on, and the user is re-prompted for more input, this method can be called to save the context and data
	 * of the conversation, so that it persists to the next onIntent call.
	 * @param session The current session
	 */
	private void saveAttributes(Session session){
		session.setAttribute(DataHelper.SKILL_CONTEXT_NAME, skillContext);
		session.setAttribute(DataHelper.SESSION_OBJECT_NAME, data);
	}
	
	/**
	 * Attempt to pull a routeID value out of the user's speech, and add it to the conversation.
	 * If successful, passes the value on to DataHelper for processing, which updates data and skillContext.
	 * @param session The current session
	 * @param intent The intent obtained from the user's speech
	 * @throws InvalidInputException
	 */
	private void extractRoute(Session session, Intent intent) throws InvalidInputException{
		String routeID = fetcher.getValueFromIntentSlot(intent, DataHelper.ROUTE_ID);
		if (routeID != null){
			log.info("putting value in session Slot " + DataHelper.ROUTE_ID +" : "+routeID);
			DataHelper.addRouteToConversation(data, skillContext, routeID);
		}
		
	}
	
	/**
	 * Attempt to pull a direction value out of the user's speech, and add it to the conversation.
	 * If successful, passes the value on to DataHelper for processing, which updates data and skillContext.
	 * @param session The current session
	 * @param intent The intent obtained from the user's speech
	 * @throws InvalidInputException
	 */
	private void extractDirection(Session session, Intent intent) throws InvalidInputException{
		String direction = fetcher.getValueFromIntentSlot(intent, DataHelper.DIRECTION);
		if (direction != null){
			log.info("retreivedSlot " + DataHelper.DIRECTION+" : "+direction);
			DataHelper.addDirectionToConversation(data, skillContext, direction);
		}
		
	}
	
	
	/**
	 * Attempt to pull a location value out of the user's speech, and add it to the conversation.
	 * If successful, passes the value on to DataHelper for processing, which updates data and skillContext.
	 * The location held in the intent's slot might contain an address or a
	 * landmark or business name. DataHelper calls the Google Maps API to translate
	 * that to a street address and put it in the data object.
	 * @param session The current session
	 * @param intent The intent obtained from the user's speech
	 * @throws InvalidInputException
	 */
	private void extractLocation(Session session, Intent intent) throws InvalidInputException{
		String location = fetcher.getValueFromIntentSlot(intent, DataHelper.LOCATION);
		//fetcher will throw an exception if the location is null and the intent is a single intent,
		//a single intenet being that the user tries to provide one piece of information (a location in this case)
		//e.g. "Highmark"
		if (location != null){
			log.info("putting value in session Slot " + DataHelper.LOCATION +" : "+location);
			DataHelper.addLocationToConversation(data, skillContext, location);
		} 
		
	}
	
	/**
	 * This method is called when the user's intent is detected to be singular,
	 * either a route, direction, or location. Checks to make sure the intent
	 * makes sense given the context's last question asked.
	 * @param session The current session
	 * @param intent The intent obtained from the user's speech
	 * @throws InvalidInputException
	 */
	private void handleSingleIntent(Session session, Intent intent) throws InvalidInputException {
		skillContext.setAllRoutes(false);
		switch(skillContext.getLastQuestion()){
		case OutputHelper.ROUTE_PROMPT:
			if (!DataHelper.ROUTE_INTENT_NAME.equals(intent.getName())) {
				// TODO: send to analytics
				log.error("Forcing {} to be a Route Intent", intent.getName());
			}
			extractRoute(session, intent);
			break;

		case OutputHelper.DIRECTION_PROMPT:
			// might specify direction or might be trying to fix route
			if (DataHelper.ROUTE_INTENT_NAME.equals(intent.getName())) {
				extractRoute(session, intent);
				break;
			}
			if (!DataHelper.DIRECTION_INTENT_NAME.equals(intent.getName())) {
				// TODO: send to analytics
				log.error("Forcing {} to be a Direction Intent", intent.getName());
			}
			extractDirection(session, intent);
			break;

		case OutputHelper.LOCATION_PROMPT:
			// might be trying to specify location or fix direction
			if (DataHelper.DIRECTION_INTENT_NAME.equals(intent.getName())) {
				extractDirection(session, intent);
				break;
			}
			if (!DataHelper.LOCATION_INTENT_NAME.equals(intent.getName())) {
				// TODO: send to analytics
				log.error("Forcing {} to be a Location Intent", intent.getName());
			}
			extractLocation(session, intent);
			break;

		}
	}
	
	public void onSessionEnded(SessionEndedRequest request, Session session) throws SpeechletException {
		log.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());
		analytics.postSessionEvent(AnalyticsManager.ACTION_SESSION_END);
	}

	
	
	/**
	 * Wrapper for creating the Ask response from the input strings.

	 * @param stringOutput
	 *            the output to be spoken
	 * @param repromptText
	 *            the reprompt for if the user doesn't reply or is
	 *            misunderstood.
	 * @return SpeechletResponse the speechlet response
	 */
	private static SpeechletResponse newAskResponse(String stringOutput, String repromptText) {
		SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
		outputSpeech.setSsml("<speak> " + stringOutput + " </speak>");

		PlainTextOutputSpeech repromptOutputSpeech = new PlainTextOutputSpeech();
		repromptOutputSpeech.setText(repromptText);
		Reprompt reprompt = new Reprompt();
		reprompt.setOutputSpeech(repromptOutputSpeech);
		return SpeechletResponse.newAskResponse(outputSpeech, reprompt);
	}
	
	/**
	 * Wrapper for creating the Ask tell from the input string.

	 * @param message
	 *            the output to be spoken
	 * @return SpeechletResponse the speechlet response
	 */
	private static SpeechletResponse newTellResponse(String message) {
		SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
		outputSpeech.setSsml("<speak> " + message + " </speak>");
		return SpeechletResponse.newTellResponse(outputSpeech);
	}
	
	/**
	 * Creates a welcome response and then prompts the user for a route.
	 * @return SpeechletResponse a welcome response
	 */
	private SpeechletResponse createWelcomeResponse(){
		response = OutputHelper.getWelcomeResponse();
		return newAskResponse(response.getSpeechOutput(), OutputHelper.ROUTE_PROMPT);
	}
	
	/**
	 * Creates a response that is designed to help the user understand how to use the app, and then prompts the user for a route.
	 * @return SpeechletResponse a help response
	 */
	private SpeechletResponse createHelpResponse(){
		response = OutputHelper.getHelpResponse();
		return newAskResponse(response.getSpeechOutput(), OutputHelper.ROUTE_PROMPT);
	}
	
	/**
	 * Creates a response for when the user ends the session abruptly (i.e "Alexa, stop")
	 * @return SpeechletResponse a stop response
	 */
	private SpeechletResponse createStopResponse(){
		response = OutputHelper.getStopResponse();
		return newTellResponse(response.getSpeechOutput());
	}
	
	/**
	 * Creates a response indicating that there are no buses (or no particular bus) arriving at 
	 * the user defined stop within the next 30 minutes.
	 * @return SpeechletResponse a failure response
	 */
	private SpeechletResponse createNoResponse() {
		SsmlOutputSpeech outputSpeech=new SsmlOutputSpeech();
		response = OutputHelper.getNoResponse(data, skillContext);
		outputSpeech.setSsml("<speak> " + OutputHelper.AUDIO_FAILURE + response.getSpeechOutput() + "</speak>");
		return SpeechletResponse.newTellResponse(outputSpeech, buildCard(response.getTextOutput()));
	}
	
	//TODO: A lot of this logic could be separated from the Speechlet.
	/**
	 * Given a list of messages (TrueTimeAPI values) that are obtained by making calls to the TrueTime API, 
	 * attempt to extract the route ID and ETA of each message and create a response with them by passing these values to readResults. 
	 * If there are 0 messages or 1 message that is an error message, call getNoResponse to indicate failure.
	 * @param messages
	 * @return SpeechletResponse A response indicating the arrival times for buses coming to the user defined stop.
	 */
	
	private SpeechletResponse buildResponse(List<Message> messages) {
		//create an empty ArrayList of Results and try to get results for the bus route
		ArrayList<Result> results = new ArrayList<Result>();
		try { //why would an exception be thrown?
			results= OutputHelper.getResults(messages);
		} catch (Exception e){
			analytics.postException(e.getMessage(), true);
			e.printStackTrace();
		}
		
		if (!results.isEmpty()){ //if there were results returned
			if (skillContext.isAllRoutes()) {
				analytics.postEvent(AnalyticsManager.CATEGORY_RESPONSE, "Success",
						"All routes at " + data.getStopName(), messages.size());
			} else {
				analytics.postEvent(AnalyticsManager.CATEGORY_RESPONSE, "Success",
						data.getRouteName() + " at " + data.getStopName(), messages.size());
			}
			return readResults(results);
		} else {
			return createNoResponse();
		}
	}
	
	/**
	 * Given a list of results (routeIDs and ETAs), create a response listing off information for
	 * each route arriving at the user defined stop. The actual formatting of the response takes place
	 * in generateResponse of OutputHelper.
	 * @param results The list of results
	 * @return SpeechletResponse A response of arrival information for the user defined stop
	 */
	public SpeechletResponse readResults(ArrayList<Result> results) {	
		SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
		response = OutputHelper.generateResponse(data, results, skillContext);
		outputSpeech.setSsml("<speak> " + OutputHelper.AUDIO_SUCCESS + response.getSpeechOutput() + "</speak>");
		Card card;
		
		try {
			card = buildCard(response.getTextOutput(), data.getLocationLat(), data.getLocationLong(), data.getStopLat(), data.getStopLon());
		} catch (Exception e) {
			log.error(e.getMessage());
			card= buildCard(response.getTextOutput());
		}
		return SpeechletResponse.newTellResponse(outputSpeech, card);
	}

	/**
	 * Creates a response that indicates a failure occurred.
	 * @param failureLabel The failure message
	 * @return SpeechletResponse the failure response
	 */
	public SpeechletResponse getFailureResponse(String failureLabel) {
	    response = OutputHelper.getAPIFailureResponse(failureLabel);
		SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
		outputSpeech.setSsml("<speak> " + response.getSpeechOutput() + " </speak>");
		return SpeechletResponse.newTellResponse(outputSpeech);
	}
	
	//card for error output
	/**
	 * Creates a card that indicates an error occurred.
	 * @param s The failure message
	 * @return SimpleCard a failure card
	 */
	public SimpleCard buildCard(String s){
		SimpleCard card=new SimpleCard();
		card.setTitle(GetNextBusSpeechlet.INVOCATION_NAME);
		card.setContent(s);
		return card;
	}
        //card with image for successful output
	
	/**
	 * Creates a card that indicates route information was successfully gathered.
	 * Includes a picture of a directions from the user's location to the nearest stop.
	 * @param text List of buses and prediction times arriving at this stop
	 * @param locationLat The latitude of the location provided by the user
	 * @param locationLong The longitude of the location provided by the user
	 * @param stopLat The latitude of the stop
	 * @param stopLon The longitude of the stop
	 * @return StandardCard A card indicating success
	 * @throws IOException
	 * @throws JSONException
	 * @throws Exception
	 */
	public StandardCard buildCard(String text, String locationLat, String locationLong, double stopLat, double stopLon) throws IOException, JSONException, Exception {
            StandardCard card = new StandardCard();
            Navigation navigation = OutputHelper.buildNavigation(locationLat, locationLong, stopLat, stopLon);
            card.setTitle(GetNextBusSpeechlet.INVOCATION_NAME);
            card.setText(text+"\n"+navigation.getInstructions());
            Image image = new Image();
            image.setLargeImageUrl(navigation.getImage());
            log.info("LARGE IMAGE URL: "+navigation.getImage());
            card.setImage(image);
            return card;
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
	
	
	//TODO: This can become independent of Amazon Speechlet
	/**
	 * Attempt to load the PaInputData associated with this user ID.
	 * @param id The user ID
	 * @return If the data exists and has all the necessary data, return it. Otherwise, return null.
	 */
	private PaInputData loadData(String id){
		PaInputData input = getPaDao().getPaInputData(id);
		if (input != null && input.hasAllData()){
			return input;
		}
		return null;
	}
		
	//TODO: This can become independent of Amazon Speechlet
	/**
	 * Helper method that takes location data from the data object and passes it along to the Google Maps API.
	 * @param in The conversation data object
	 * @return The nearest stop to the location given by the user in the conversation
	 * @throws InvalidInputException
	 * @throws IOException
	 * @throws JSONException
	 */
	private Stop getNearestStop() throws InvalidInputException, IOException, JSONException {
		Location c = new Location();
		c.setAddress(data.getLocationAddress());
		c.setLat(new Double(data.getLocationLat()).doubleValue());
		c.setLng(new Double(data.getLocationLong()).doubleValue());
		return GoogleMaps.findNearestStop(c, data.getRouteID(), data.getDirection());
	}

	//TODO: This can become independent of Amazon Speechlet
	/**
	 * Helper method that makes a call to the TrueTime API to obtain predictions for all buses (or one particular bus, depending on the skill context)
	 * arriving at a given stop.
	 * @return Predictions of buses arriving at the stop (that was found during the conversation)
	 */
	private List<Message> getPredictions() {
		List<Message> messages = new ArrayList<Message>();
		if (skillContext.isAllRoutes()) {
			messages = TrueTime.generatePredictions(data.getStopID());
		} else {
			messages = TrueTime.generatePredictions(data.getRouteID(), data.getStopID());
		}
		return messages;
	}
	
	//TODO: These can all become independent of Amazon Speechlet
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
	
}

