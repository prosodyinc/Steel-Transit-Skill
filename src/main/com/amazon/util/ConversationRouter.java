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


	public static SpeechletResponse checkForAdditionalQuestions(PaInputData data, SkillContext skillContext) {

		// String lastQuestion=DataHelper.getValueFromSession(session,
		// DataHelper.LAST_QUESTION);

		// Need Route, Location, and Direction
		String feedbackText = skillContext.getFeedbackText();
		
		if (data.getRouteID() == null) {
			skillContext.setLastQuestion(OutputHelper.ROUTE_PROMPT);
			return OutputHelper.newAskResponse(feedbackText + "," + OutputHelper.ROUTE_PROMPT, OutputHelper.ROUTE_PROMPT);
		}
		if (data.getDirection() == null) {
			skillContext.setLastQuestion(OutputHelper.DIRECTION_PROMPT);
			return OutputHelper.newAskResponse(feedbackText + "," + OutputHelper.DIRECTION_PROMPT,
					OutputHelper.DIRECTION_PROMPT);
		}
		if (data.getLocationAddress() == null) {
			skillContext.setLastQuestion(OutputHelper.LOCATION_PROMPT);
			return OutputHelper.newAskResponse(feedbackText + "," + OutputHelper.LOCATION_PROMPT,
					OutputHelper.LOCATION_PROMPT);
		}

		return null;
	}


	
}