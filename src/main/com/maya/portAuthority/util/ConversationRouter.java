package com.maya.portAuthority.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.maya.portAuthority.InvalidInputException;

public class ConversationRouter {
	private static Logger log = LoggerFactory.getLogger(DataHelper.class);

	public static SpeechletResponse checkForAdditionalQuestions(Session session) {
		return ConversationRouter.checkForAdditionalQuestions(session, "");
	}

	public static SpeechletResponse checkForAdditionalQuestions(Session session, String feedbackText) {

		// String lastQuestion=DataHelper.getValueFromSession(session,
		// DataHelper.LAST_QUESTION);

		// Need Route, Location, and Direction
		
		// maybe replace with PaInput.hasAllData()
		if (DataHelper.getValueFromSession(session, DataHelper.ROUTE_ID) == null) {
			session.setAttribute(DataHelper.LAST_QUESTION, DataHelper.ROUTE_PROMPT);
			return OutputHelper.newAskResponse(feedbackText + "," + DataHelper.ROUTE_PROMPT, DataHelper.ROUTE_PROMPT);
		}
		if (DataHelper.getValueFromSession(session, DataHelper.DIRECTION) == null) {
			session.setAttribute(DataHelper.LAST_QUESTION, DataHelper.DIRECTION_PROMPT);
			return OutputHelper.newAskResponse(feedbackText + "," + DataHelper.DIRECTION_PROMPT,
					DataHelper.DIRECTION_PROMPT);
		}
		if (DataHelper.getValueFromSession(session, DataHelper.LOCATION) == null) {
			session.setAttribute(DataHelper.LAST_QUESTION, DataHelper.LOCATION_PROMPT);
			return OutputHelper.newAskResponse(feedbackText + "," + DataHelper.LOCATION_PROMPT,
					DataHelper.LOCATION_PROMPT);
		}

		return null;
	}

	/**
	 * 
	 * @param session
	 * @param intent
	 * @return
	 */
	public static boolean isIntentValidForQuestion(Session session, Intent intent) {
		// throw new UnsupportedOperationException("Not supported yet.");

		if (!DataHelper.isValidIntent(intent.getName())) {
			return false;
		}

		if (!(intent.getName().equals(DataHelper.ROUTE_INTENT_NAME)
				|| intent.getName().equals(DataHelper.DIRECTION_INTENT_NAME)
				|| intent.getName().equals(DataHelper.LOCATION_INTENT_NAME))) {
			return true;
		}

		String lastQuestion = DataHelper.getValueFromSession(session, DataHelper.LAST_QUESTION);

		switch (lastQuestion) {
		case DataHelper.ROUTE_PROMPT:
			// route is the first prompt, only route is acceptable
			if (intent.getName() == DataHelper.ROUTE_INTENT_NAME) {
				return true;
			} else {
				return false;
			}

		case DataHelper.DIRECTION_PROMPT:
			// might specify direction or might be trying to fix route
			if (intent.getName() == DataHelper.DIRECTION_INTENT_NAME) {
				return true;
			} else if (intent.getName() == DataHelper.ROUTE_INTENT_NAME) {
				return true;
			} else {
				return false;
			}
		case DataHelper.LOCATION_PROMPT:
			// might be trying to specify location or fix direction
			if (intent.getName() == DataHelper.LOCATION_INTENT_NAME) {
				return true;
			} else if (intent.getName() == DataHelper.DIRECTION_INTENT_NAME) {
				return true;
			} else {
				return false;
			}
		default:
			if (intent.getName() == DataHelper.ROUTE_INTENT_NAME) {
				return true;
			} else {
				return false;
			}
		}

	}

	public static String putValuesInSession(Session session, Intent intent) throws InvalidInputException {
		// throw new UnsupportedOperationException("Not supported yet.");
		String feedback = "";
		if (!DataHelper.isValidIntent(intent.getName())) {
			throw new InvalidInputException("Invalid Intent", "Try Again");
		}

		String lastQuestion = DataHelper.getValueFromSession(session, DataHelper.LAST_QUESTION);
		// first time through, make it a Route.
		if (lastQuestion == null) {
			lastQuestion = DataHelper.ROUTE_PROMPT;
		}

		switch (lastQuestion) {
		case DataHelper.ROUTE_PROMPT:
			if (!DataHelper.ROUTE_INTENT_NAME.equals(intent.getName())) {
				// TODO: send to analytics
				log.error("Forcing {} to be a Route Intent", intent.getName());
			}
			feedback = DataHelper.putRouteValuesInSession(session, intent);
			break;

		case DataHelper.DIRECTION_PROMPT:
			// might specify direction or might be trying to fix route
			if (DataHelper.ROUTE_INTENT_NAME.equals(intent.getName())) {
				feedback = DataHelper.putRouteValuesInSession(session, intent);
				break;
			}
			if (!DataHelper.DIRECTION_INTENT_NAME.equals(intent.getName())) {
				// TODO: send to analytics
				log.error("Forcing {} to be a Direction Intent", intent.getName());
			}
			feedback = DataHelper.putDirectionValuesInSession(session, intent);
			break;

		case DataHelper.LOCATION_PROMPT:
			// might be trying to specify location or fix direction
			if (DataHelper.DIRECTION_INTENT_NAME.equals(intent.getName())) {
				feedback = DataHelper.putDirectionValuesInSession(session, intent);
				break;
			}
			if (!DataHelper.LOCATION_INTENT_NAME.equals(intent.getName())) {
				// TODO: send to analytics
				log.error("Forcing {} to be a Location Intent", intent.getName());
			}
			feedback = DataHelper.putLocationValuesInSession(session, intent);
			break;
		}
		return feedback;

	}
}