package com.amazon.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;

import co.prosody.portAuthority.GetNextBusSpeechlet;
import co.prosody.portAuthority.InvalidInputException;
import co.prosody.portAuthority.storage.PaInputData;


public class ConversationRouter {
	private static Logger log = LoggerFactory.getLogger(DataHelper.class);

	public static SpeechletResponse checkForAdditionalQuestions(Session session) {
		return ConversationRouter.checkForAdditionalQuestions(session, "");
	}

	public static SpeechletResponse checkForAdditionalQuestions(Session session, String feedbackText) {

		// String lastQuestion=DataHelper.getValueFromSession(session,
		// DataHelper.LAST_QUESTION);

		// Need Route, Location, and Direction
		PaInputData data = PaInputData.create(session.getAttribute(DataHelper.SESSION_OBJECT_NAME));
		if (data == null){
			return OutputHelper.newTellResponse("something is wrong with the database object");
		}
		if (data.getRouteID() == null) {
			session.setAttribute(DataHelper.LAST_QUESTION, OutputHelper.ROUTE_PROMPT);
			return OutputHelper.newAskResponse(feedbackText + "," + OutputHelper.ROUTE_PROMPT, OutputHelper.ROUTE_PROMPT);
		}
		if (data.getDirection() == null) {
			session.setAttribute(DataHelper.LAST_QUESTION, OutputHelper.DIRECTION_PROMPT);
			return OutputHelper.newAskResponse(feedbackText + "," + OutputHelper.DIRECTION_PROMPT,
					OutputHelper.DIRECTION_PROMPT);
		}
		if (data.getLocationAddress() == null) {
			session.setAttribute(DataHelper.LAST_QUESTION, OutputHelper.LOCATION_PROMPT);
			return OutputHelper.newAskResponse(feedbackText + "," + OutputHelper.LOCATION_PROMPT,
					OutputHelper.LOCATION_PROMPT);
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

		if (!GetNextBusSpeechlet.isValidIntent(intent.getName())) {
			return false;
		}

		if (!(intent.getName().equals(DataHelper.ROUTE_INTENT_NAME)
				|| intent.getName().equals(DataHelper.DIRECTION_INTENT_NAME)
				|| intent.getName().equals(DataHelper.LOCATION_INTENT_NAME))) {
			return true;
		}

		//should be replaced with skill context
		String lastQuestion = GetNextBusSpeechlet.getValueFromSession(session, DataHelper.LAST_QUESTION);

		switch (lastQuestion) {
		case OutputHelper.ROUTE_PROMPT:
			// route is the first prompt, only route is acceptable
			if (intent.getName() == DataHelper.ROUTE_INTENT_NAME) {
				return true;
			} else {
				return false;
			}

		case OutputHelper.DIRECTION_PROMPT:
			// might specify direction or might be trying to fix route
			if (intent.getName() == DataHelper.DIRECTION_INTENT_NAME) {
				return true;
			} else if (intent.getName() == DataHelper.ROUTE_INTENT_NAME) {
				return true;
			} else {
				return false;
			}
		case OutputHelper.LOCATION_PROMPT:
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
		if (!GetNextBusSpeechlet.isValidIntent(intent.getName())) {
			throw new InvalidInputException("Invalid Intent", "Try Again");
		}

		String lastQuestion = GetNextBusSpeechlet.getValueFromSession(session, DataHelper.LAST_QUESTION);
		// first time through, make it a Route.
		if (lastQuestion == null) {
			lastQuestion = OutputHelper.ROUTE_PROMPT;
		}

		switch (lastQuestion) {
		case OutputHelper.ROUTE_PROMPT:
			if (!DataHelper.ROUTE_INTENT_NAME.equals(intent.getName())) {
				// TODO: send to analytics
				log.error("Forcing {} to be a Route Intent", intent.getName());
			}
			feedback = GetNextBusSpeechlet.putRouteValuesInSession(session, intent);
			break;

		case OutputHelper.DIRECTION_PROMPT:
			// might specify direction or might be trying to fix route
			if (DataHelper.ROUTE_INTENT_NAME.equals(intent.getName())) {
				feedback = GetNextBusSpeechlet.putRouteValuesInSession(session, intent);
				break;
			}
			if (!DataHelper.DIRECTION_INTENT_NAME.equals(intent.getName())) {
				// TODO: send to analytics
				log.error("Forcing {} to be a Direction Intent", intent.getName());
			}
			feedback = GetNextBusSpeechlet.putDirectionValuesInSession(session, intent);
			break;

		case OutputHelper.LOCATION_PROMPT:
			// might be trying to specify location or fix direction
			if (DataHelper.DIRECTION_INTENT_NAME.equals(intent.getName())) {
				feedback = GetNextBusSpeechlet.putDirectionValuesInSession(session, intent);
				break;
			}
			if (!DataHelper.LOCATION_INTENT_NAME.equals(intent.getName())) {
				// TODO: send to analytics
				log.error("Forcing {} to be a Location Intent", intent.getName());
			}
			feedback = GetNextBusSpeechlet.putLocationValuesInSession(session, intent);
			break;
		}
		return feedback;

	}
}