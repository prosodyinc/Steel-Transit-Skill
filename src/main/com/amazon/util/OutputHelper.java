package com.amazon.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.Card;
import com.amazon.speech.ui.Image;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import com.amazon.speech.ui.SsmlOutputSpeech;
import com.amazon.speech.ui.StandardCard;

import co.prosody.portAuthority.GetNextBusSpeechlet;
import co.prosody.portAuthority.googleMaps.GoogleMaps;
import co.prosody.portAuthority.googleMaps.Instructions;
import co.prosody.portAuthority.googleMaps.NearestStopLocator;
import co.prosody.portAuthority.storage.PaInputData;
import co.prosody.portAuthority.util.Navigation;
import co.prosody.portAuthority.util.Result;

import org.json.JSONObject;


public class OutputHelper {
	
	// CONFIGURE ME!
	public static final String AUDIO_WELCOME = "<audio src=\"https://s3.amazonaws.com/maya-audio/ppa_welcome.mp3\" />";
	private static final String AUDIO_FAILURE = "<audio src=\"https://s3.amazonaws.com/maya-audio/ppa_failure.mp3\" />";
	private static final String AUDIO_SUCCESS = "<audio src=\"https://s3.amazonaws.com/maya-audio/ppa_success.mp3\" />";
    private static final String S3_BUCKET = System.getenv("S3_BUCKET"); //S3 Bucket name
    private static final String IMG_FOLDER = "image"; //S3 Folder name
    
    
	private final static Logger LOGGER = LoggerFactory.getLogger("OutputHelper");
	
	private static String SPEECH_WELCOME = "Welcome to "+GetNextBusSpeechlet.INVOCATION_NAME;
	
	//TODO: add markers into conversation
	private static final String CHANGE_MARKER=" , by the way, ";
	private static final String SUCCESS_MARKER="okay, ";
	private static final String FAILED_MARKER="oh, ";
	
	public static final String ROUTE_PROMPT = " Which bus line would you like arrival information for?";
	public static final String HELP_ROUTE= "The Bus Line is usually a number, like sixty-seven, or a number and a letter, "
			+ "like the seventy-one B , If you don't know what bus line you want, say, cancel, and go look it up on Google Maps";
	
	public static final String LOCATION_PROMPT = "Where are you now?";
	public static final String HELP_LOCATION= "You can say a street address where you are, or a landmark near your bus stop , "
			+ GetNextBusSpeechlet.INVOCATION_NAME+ " will figure out the closest stop to the location you give.";
	
	
	public static final String DIRECTION_PROMPT = "In which direction are you <w role=\"ivona:NN\">traveling</w>?";
	public static final String HELP_DIRECTION= "For busses headed <emphasis>towards</emphasis> "
			+ "<phoneme alphabet=\"x-sampa\" ph=\"dAn tAn\">downtown</phoneme> ,"
			+ "you can say, <phoneme alphabet=\"x-sampa\" ph=\"InbaUnd\">Inbound</phoneme> ,"
			+ "or, for busses headed <emphasis>away</emphasis> from the city, say, Outbound";
	
	public static final String HELP_INTENT = "Use a complete sentence, like ,  I am currently outside Gateway Three";
	
	/**
	 * Location Name, StopName
	 */
	private static final String LOCATION_SPEECH="The nearest stop to %s is %s. ";
	/**
	 * StopName
	 */
	private static final String BUSSTOP_SPEECH=" At %s, ";
	
	
	////RESULTS////
	/**
	 * Speech fragment if there are no prediction results for an "All Routes" request
	 * Format with Direction, BusStopName
	 */
	private static final String NO_ALL_ROUTES_SPEECH=" No %s busses are expected at %s in the next 30 minutes. ";
	
	/**
	 * Speech fragment if there are no prediction results for an "All Routes" request
	 * Format with Direction, RouteID, and BusStopName
	 */
	private static final String NO_SINGLE_ROUTE_SPEECH=" No %s, %s is expected at %s in the next 30 minutes. ";
	
	/**
	 * Speech fragment for first prediction result
	 * Format with RouteID, Prediction Time
	 */
	private static final String FIRST_RESULT_SPEECH=" The %s will be arriving in %s minutes ";
	
	/**
	 * Speech fragment for additional prediction result
	 * Format with Prediction Time
	 */
	private static final String MORE_RESULTS_SPEECH=" and %s minutes ";
	
	/**
	 * Speech fragment with instructions to hear all routes.
	 */
	private static final String HELP_ALL_ROUTES_SPEECH=CHANGE_MARKER+"to hear predictions for all routes that stop there, say , Alexa, ask "+GetNextBusSpeechlet.INVOCATION_NAME+" for All Routes";

	/**
	 * Speech fragment with generic instructions .
	 */
	private static final String HELP_SPEECH=GetNextBusSpeechlet.INVOCATION_NAME+" will tell you when the next bus is coming if you provide it a bus line, direction, and location near your bus stop.";
	/**
	 * Speech fragment for stopping or cancelling.
	 */
	private static final String STOP_SPEECH="Oh? OK";


	//	public static SpeechletResponse getNoResponse(PaInputData inputData) {
	//		return getNoResponse(inputData, "");
	//	}

	public static SpeechletResponse getWelcomeResponse(){
		String output=AUDIO_WELCOME+" "+SPEECH_WELCOME + ROUTE_PROMPT;
		
		return newAskResponse(output, ROUTE_PROMPT);
	}
	
	public static SpeechletResponse getHelpResponse(){
		String output=AUDIO_WELCOME+" "+HELP_SPEECH + " " + ROUTE_PROMPT;
		
		return newAskResponse(output, ROUTE_PROMPT);
	}
	
	public static SpeechletResponse getStopResponse(){
		
		return newTellResponse(STOP_SPEECH);
	}

	public static SpeechletResponse getNoResponse(PaInputData inputData, SkillContext c) {
		SsmlOutputSpeech outputSpeech=new SsmlOutputSpeech();
		String textOutput="";
		if (c.getNeedsLocation()){
			textOutput=String.format(LOCATION_SPEECH, inputData.getLocationName(), inputData.getStopName()); 
		}

		if (c.isAllRoutes()){
			textOutput+=String.format(NO_ALL_ROUTES_SPEECH, inputData.getDirection(), inputData.getStopName());
		} else {
			textOutput+=String.format(NO_SINGLE_ROUTE_SPEECH, inputData.getDirection(), inputData.getRouteID() , inputData.getStopName());
		}

		if ((c.getNeedsMoreHelp())&&(!c.isAllRoutes())){
			textOutput+=HELP_ALL_ROUTES_SPEECH;
			
		}

		
		outputSpeech.setSsml("<speak> " + AUDIO_FAILURE + textOutput + "</speak>");
		return SpeechletResponse.newTellResponse(outputSpeech, buildCard(textOutput));

	}

	//	public static SpeechletResponse getResponse(PaInputData inputData, ArrayList<Result> results) {	
	//		return getResponse(inputData, results, "");
	//	}

	public static SpeechletResponse getResponse(PaInputData inputData, ArrayList<Result> results, SkillContext c) {	
		SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
		String textOutput;
		String speechOutput;

		//final String locationOutput="The nearest stop to "+  inputData.getLocationName() +" is " + inputData.getStopName()+". ";
		//final String stopOutput=" At " +inputData.getStopName()+", ";
		//final String allRoutesHelpText=" <break time=\"0.25s\" />  to hear predictions for all routes that stop "+BUSSTOP_SPEECH+  ", say <break time=\"0.25s\" /> Alexa, ask "+GetNextBusSpeechlet.INVOCATION_NAME+" for All Routes";


		if (c.getNeedsLocation()){
			textOutput=String.format(LOCATION_SPEECH, inputData.getLocationName(), inputData.getStopName());
		} else {
			textOutput=String.format(BUSSTOP_SPEECH, inputData.getStopName()) ;
		}
		speechOutput = textOutput+"<break time=\"0.1s\" />";


		int when;
		String routeID;
		String prevRouteID=null;
		//TODO: Collect Route responses together, but Return the first bus first. 
		Collections.sort(results);

		for (int i = 0; i < results.size(); i++) {

			routeID=results.get(i).getRoute();
			when = results.get(i).getEstimate();

			if (i==0){
				textOutput += String.format(FIRST_RESULT_SPEECH,routeID,when);
				speechOutput += String.format(FIRST_RESULT_SPEECH,routeID,when);
			} else if (routeID.equals(prevRouteID)){
				textOutput += String.format(MORE_RESULTS_SPEECH, when);
				speechOutput += String.format(MORE_RESULTS_SPEECH, when);
			} else {
				textOutput += ".\n "+String.format(FIRST_RESULT_SPEECH,routeID,when);
				speechOutput += "<break time=\"0.25s\" /> "+String.format(FIRST_RESULT_SPEECH,routeID,when);
			}
			prevRouteID=routeID;
		}

//		if ((c.needsMoreHelp())&&(!c.isAllRoutes())){
//			speechOutput+=HELP_ALL_ROUTES_SPEECH;
//		}
		outputSpeech.setSsml("<speak> " + AUDIO_SUCCESS + speechOutput + "</speak>");
		Card card;
		
		try {
			card = buildCard(textOutput, inputData.getLocationLat(), inputData.getLocationLong(), inputData.getStopLat(), inputData.getStopLon());
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			card= buildCard(textOutput);
		}
		
		return SpeechletResponse.newTellResponse(outputSpeech, card);
	}
        
	//card for error output
	private static SimpleCard buildCard(String s){
		SimpleCard card=new SimpleCard();
		card.setTitle(GetNextBusSpeechlet.INVOCATION_NAME);
		card.setContent(s);
		return card;
	}
        //card with image for successful output
	private static StandardCard buildCard(String text, String locationLat, String locationLong, double stopLat, double stopLon) throws IOException, JSONException, Exception {
            StandardCard card = new StandardCard();
            Navigation navigation = buildNavigation(locationLat, locationLong, stopLat, stopLon);
            card.setTitle(GetNextBusSpeechlet.INVOCATION_NAME);
            card.setText(text+"\n"+navigation.getInstructions());
            Image image = new Image();
            image.setLargeImageUrl(navigation.getImage());
            LOGGER.info("LARGE IMAGE URL: "+navigation.getImage());
            card.setImage(image);
            return card;
        }
    private static Navigation buildNavigation(String locationLat, String locationLon, double stopLat, double stopLon) throws IOException, JSONException, Exception{	
    	Navigation navigation = new Navigation();
        
    	String directions = GoogleMaps.generateDirections(locationLat, locationLon, stopLat, stopLon);
    	
    	String image = GoogleMaps.generateImageURL(locationLat, locationLon, stopLat, stopLon);
        image = image.substring(0, image.length() -1); //Remove the last '|'
        
        //Set image Name
        String imageName = locationLat+locationLon+stopLat+stopLon;
        imageName = imageName.replaceAll("\\.", "");
        
        //Upload image on S3
        ImageUploader.uploadImage(image, imageName, IMG_FOLDER, S3_BUCKET);
        LOGGER.info("UPLOAD IMAGE SUCCESSFUL WITH NAME: "+imageName);
        
        //Set instructions and S3 image link to navigation object
        navigation.setInstructions(directions);
        navigation.setImage("https://s3.amazonaws.com/"+S3_BUCKET+"/"+IMG_FOLDER+"/"+ imageName+".png");
        LOGGER.info("SET IMAGE SUCCESSFUL");
        //LOGGER.info("IMAGE URL={}",image);
        return navigation;
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
	public static SpeechletResponse newAskResponse(String stringOutput, String repromptText) {
		// PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
		// outputSpeech.setText(stringOutput);
		SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
		outputSpeech.setSsml("<speak> " + stringOutput + " </speak>");

		PlainTextOutputSpeech repromptOutputSpeech = new PlainTextOutputSpeech();
		repromptOutputSpeech.setText(repromptText);
		// SsmlOutputSpeech repromptOutputSpeech = new SsmlOutputSpeech();
		// repromptOutputSpeech.setSsml(repromptText);

		Reprompt reprompt = new Reprompt();
		reprompt.setOutputSpeech(repromptOutputSpeech);
		return SpeechletResponse.newAskResponse(outputSpeech, reprompt);
	}

	public static SpeechletResponse newTellResponse(String message) {
		SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
		outputSpeech.setSsml("<speak> " + message + " </speak>");
		return SpeechletResponse.newTellResponse(outputSpeech);
	}

	public static SpeechletResponse getFailureResponse(String failureLabel) {
		String message = ("There has been a problem connecting to " + failureLabel + ". I'll let the developers know."); 
		SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
		outputSpeech.setSsml("<speak> " + message + " </speak>");
		return SpeechletResponse.newTellResponse(outputSpeech);
	}
}
