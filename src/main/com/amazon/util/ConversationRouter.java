package com.amazon.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.prosody.portAuthority.storage.PaInputData;


public class ConversationRouter {
	private static Logger log = LoggerFactory.getLogger(DataHelper.class);


	public static void checkForAdditionalQuestions(final PaInputData data, SkillContext skillContext) {
		if (data.getRouteID() == null) {
			skillContext.setLastQuestion(OutputHelper.ROUTE_PROMPT);
		} else if (data.getDirection() == null) {
			skillContext.setLastQuestion(OutputHelper.DIRECTION_PROMPT);
		} else if (data.getLocationAddress() == null) {
			skillContext.setLastQuestion(OutputHelper.LOCATION_PROMPT);
		} else {
			skillContext.setAdditionalQuestions(false); //we have everything we need!
		}
	}


	
}