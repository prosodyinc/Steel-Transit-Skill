package co.prosody.portAuthority;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.BasicConfigurator;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.slu.Intent;
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
		
	}
	
	/**
	 * Called when the user invokes an intent.
	 */
	public SpeechletResponse onIntent(IntentRequest request, Session session) throws SpeechletException {
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
		
		
		log.info("onIntent intent={}, requestId={}, sessionId={}", request.getIntent().getName(),
				request.getRequestId(), session.getSessionId());
		log.info("onIntent sessionValue={}", session.getAttributes().toString());
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
				if (DataHelper.getValueFromIntentSlot(intent, DataHelper.ROUTE_ID)!=null){
					feedbackText = DataHelper.putRouteValuesInSession(session, intent);
				}

				if (DataHelper.getValueFromIntentSlot(intent, DataHelper.LOCATION)!=null){
					feedbackText += DataHelper.putLocationValuesInSession(session, intent);
				}

				if (DataHelper.getValueFromIntentSlot(intent, DataHelper.DIRECTION)!=null){
					feedbackText += DataHelper.putDirectionValuesInSession(session, intent);
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
		PaInputData inputData = makeFromSession(session);
		if (inputData == null){
			log.info("input data is null but should not be at this point.");
		}
		try {
			if (inputData.getStopID() == null) {
				skillContext.setNeedsLocation(true);
				inputData.setStop(getNearestStop(inputData));
			} else {
				skillContext.setNeedsLocation(false);
			}
		} catch (InvalidInputException | IOException | JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return OutputHelper.getFailureResponse("Google Maps");
			
		} finally {
			saveInputToDB(inputData);
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



	private PaInputData makeFromSession(Session session) {
		log.info("User ID" + ": " + session.getUser().getUserId());
		// TODO: Make Session Data be a PaInput
		// Map<String,String> sessionData= getInputValuesFromSession();
		Map<String, Object> sessionData = session.getAttributes();
		PaInputData inputData = PaInputData.newInstance(session.getUser().getUserId());
		inputData.setDirection(sessionData.get(DataHelper.DIRECTION).toString());
		inputData.setRouteID(sessionData.get(DataHelper.ROUTE_ID).toString());
		inputData.setRouteName(sessionData.get(DataHelper.ROUTE_NAME).toString());
		inputData.setLocationName(sessionData.get(DataHelper.LOCATION).toString());
		inputData.setLocationAddress(sessionData.get(DataHelper.ADDRESS).toString());
		inputData.setLocationLat(sessionData.get(DataHelper.LAT).toString());
		inputData.setLocationLong(sessionData.get(DataHelper.LONG).toString());
		return inputData;
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

}

