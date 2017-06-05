package co.prosody.portAuthority;

import java.util.ArrayList;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.util.DataHelper;

class OneShotFetcher extends InformationFetcher {

	@Override
	public String getValueFromIntentSlot(Intent intent, String name) {
		Slot slot = intent.getSlot(name);
		if (slot == null) {
			slot = intent.getSlot(getSlotNameForIntentName(intent.getName()));
		}
		return (slot != null) ? slot.getValue() : null;
	}
	
}

class SingleIntentFetcher extends InformationFetcher {

	@Override
	public String getValueFromIntentSlot(Intent intent, String name) throws InvalidInputException {
		Slot slot = intent.getSlot(name);
		if (slot == null) {
			//if the slot doesn't exist for this intent, check the intent's name
			slot = intent.getSlot(getSlotNameForIntentName(intent.getName()));
		}
		if (slot == null){
			throw new InvalidInputException("No " + name + " in intent.", "Please repeat your " + name);
		}
		return slot.getValue();
	}
	
}

public abstract class InformationFetcher {
	public abstract String getValueFromIntentSlot(Intent intent, String name) throws InvalidInputException;
	
	protected ArrayList<String> getValidIntents() {
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
	
	protected boolean isValidIntent(String intentName) {
		return (getValidIntents().contains(intentName));
	}

	protected String getSlotNameForIntentName(String intentName) {
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
}
