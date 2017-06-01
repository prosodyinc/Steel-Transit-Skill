package co.prosody.portAuthority;

import java.util.LinkedHashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SsmlOutputSpeech;
import com.amazon.util.ConversationRouter;
import com.amazon.util.DataHelper;
import com.amazon.util.OutputHelper;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

import co.prosody.portAuthority.api.Message;
import co.prosody.portAuthority.storage.PaDao;
import co.prosody.portAuthority.storage.PaDynamoDbClient;
import co.prosody.portAuthority.storage.PaInputData;
import co.prosody.portAuthority.util.AnalyticsManager;

public class TestSpeechlet implements Speechlet{

	private AmazonDynamoDBClient amazonDynamoDBClient;
	private PaDynamoDbClient dynamoDbClient;
	private PaDao inputDao;
	
	private static Logger log = LoggerFactory.getLogger(TestSpeechlet.class);
	
	public TestSpeechlet(){
		
	}
	
	@Override
	public void onSessionStarted(SessionStartedRequest request, Session session) throws SpeechletException {
		log.info("onSessionStarted called");
		
	}

	@Override
	public SpeechletResponse onLaunch(LaunchRequest request, Session session) throws SpeechletException {
		log.info("onLaunch called");
		return null;
	}

	@Override
	public SpeechletResponse onIntent(IntentRequest request, Session session) throws SpeechletException {
		//session.removeAttribute("InputData");
		log.info("onIntent called {}", request.getIntent().getName());
		log.info("onIntent sessionValue={}", session.getAttributes().toString());
		String response  = "";
		
		response = session.isNew() ? "New Session " : "Not a new session ";
		
		PaInputData data = null;
		
		if (session.getAttribute("DataBase") == null){
			data = getPaDao().getPaInputData(session.getUser().getUserId());
			if (data == null){
				data = PaInputData.newInstance(session.getUser().getUserId());
				
				/* This should be taken out later */
				log.info("Data object is null, adding one now");
				data = PaInputData.newInstance(session.getUser().getUserId());
				data.setDirection("INBOUND");
				
				data.setLocationLat("80");
				data.setLocationLong("72");
				
				data.setRouteID("1");
				data.setRouteName("the route");
				
				data.setStopID("the stop");
				data.setStopName("Stop Name!");
				data.setLocationName("Starbucks");
				data.setStopLat(1);
				data.setStopLon(4.4);
				saveInputToDB(data);
				
			}
			session.setAttribute("DataBase", data);
		} else {
			try {
				data = PaInputData.create(session.getAttribute("DataBase"));
			} catch (ClassCastException e){
				log.info(e.getMessage());
			}
		}
		
		/*
		//If this attribute is null, we need to fetch the data from the database, or make a new database object.
		//There will be no needed conversion from LinkedHashMap to PaInputData
		if (session.getAttribute("DataBase") == null){
			data = getPaDao().getPaInputData(session.getUser().getUserId());
		
			if (data == null){
				log.info("Data object is null, adding one now");
				data = PaInputData.newInstance(session.getUser().getUserId());
				data.setDirection("INBOUND");
				
				data.setLocationLat("80");
				data.setLocationLong("72");
				
				data.setRouteID("1");
				data.setRouteName("the route");
				
				data.setStopID("the stop");
				data.setStopName("Stop Name!");
				data.setLocationName("Starbucks");
				data.setStopLat(1);
				data.setStopLon(4.4);
				saveInputToDB(data);
			
			} else {
				boolean hasAll = data.hasAllData();
				log.info("Data object is not null {}", hasAll);
			}
			session.setAttribute("DataBase", data);
			}
		
		//If this attribute already exists, it needs to be converted from a LinkedHashMap to a PaInputData object,
		//as per the way Speechlets work.
		else{
			data = convertToData((LinkedHashMap<String, Object>)session.getAttribute("DataBase"), session);
		}*/
		
		log.info("onIntent sessionValue={}", session.getAttributes().toString());
		
		log.info("From the data object {}", data.getDirection());
		
		
		switch (request.getIntent().getName()) {
		case "AMAZON.StopIntent":
			return OutputHelper.getStopResponse();
		case "AMAZON.CancelIntent":
			return OutputHelper.getStopResponse();
		case "AMAZON.HelpIntent":
			return OutputHelper.getHelpResponse();

		case DataHelper.RESET_INTENT_NAME:
			this.getPaDao().deletePaInput(session.getUser().getUserId());
			PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
			outputSpeech.setText(response + "Reset History");
			return SpeechletResponse.newTellResponse(outputSpeech);

		case DataHelper.ALL_ROUTES_INTENT_NAME:
			PlainTextOutputSpeech outputSpeech1 = new PlainTextOutputSpeech();
			outputSpeech1.setText(response + "AllRoutes");
			return SpeechletResponse.newTellResponse(outputSpeech1);

		case DataHelper.ONE_SHOT_INTENT_NAME:
			PlainTextOutputSpeech outputSpeech11 = new PlainTextOutputSpeech();
			outputSpeech11.setText(response + "OneShot");
			return SpeechletResponse.newTellResponse(outputSpeech11);

		default:
			PlainTextOutputSpeech outputSpeech111 = new PlainTextOutputSpeech();
			outputSpeech111.setText(response + "Default");
			Reprompt reprompt = new Reprompt();
			PlainTextOutputSpeech replySpeech = new PlainTextOutputSpeech();
			replySpeech.setText("...keep going");
			reprompt.setOutputSpeech(replySpeech);
			return SpeechletResponse.newAskResponse(outputSpeech111, reprompt);
		}
	}
    /*
	//This is used to convert a LinkedHashMap to a PaInputData object.
	public PaInputData convertToData(LinkedHashMap<String, Object> map, Session session){
		log.info("Conversion from LinkedHashMap to PaInputData was called");
		PaInputData data = PaInputData.newInstance(session.getUser().getUserId());
		data.setLocationName((String)map.get("locationName"));
		data.setLocationAddress((String)map.get("locationAddress"));
		data.setLocationLat((String)map.get("locationLat"));
		data.setLocationLong((String)map.get("locationLong"));
		
		data.setStopID((String)map.get("stopID"));
		data.setStopName((String)map.get("stopName"));
		
		try{
			data.setStopLat((Double)map.get("stopLat"));
		}
		catch (Exception e){
			data.setStopLat((Integer)map.get("stopLat"));
		}
		try{
			data.setStopLon((Double)map.get("stopLon"));
		} catch (Exception e){
			data.setStopLon((Integer)map.get("stopLon"));
		}
		
		data.setRouteID((String)map.get("routeID"));
		data.setRouteName((String)map.get("routeName"));
		
		data.setDirection((String)map.get("direction"));
		return data;
	}
	*/
	
	@Override
	public void onSessionEnded(SessionEndedRequest request, Session session) throws SpeechletException {
		log.info("onSessionEnded called");
		
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
