package com.amazon.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.prosody.portAuthority.storage.PaInputData;


public class ConversationRouter {
	private static Logger log = LoggerFactory.getLogger(DataHelper.class);

	/**
	 * This method checks to see what data still needs to be collected. Starting with route, whatever
	 * piece of data is missing will be marked in the skill context. If nothing is missing,
	 * skill context will be marked as having no additional questions.
	 * @param data The conversation data
	 * @param skillContext The context of the conversation
	 */
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