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
	public static final String AUDIO_FAILURE = "<audio src=\"https://s3.amazonaws.com/maya-audio/ppa_failure.mp3\" />";
	public static final String AUDIO_SUCCESS = "<audio src=\"https://s3.amazonaws.com/maya-audio/ppa_success.mp3\" />";
    public static final String S3_BUCKET = System.getenv("S3_BUCKET"); //S3 Bucket name
    public static final String IMG_FOLDER = "image"; //S3 Folder name
    
    
	public final static Logger LOGGER = LoggerFactory.getLogger("OutputHelper");
	
	public static String SPEECH_WELCOME = "Welcome to "+GetNextBusSpeechlet.INVOCATION_NAME;
	
	//TODO: add markers into conversation
	public static final String CHANGE_MARKER=" , by the way, ";
	public static final String SUCCESS_MARKER="okay, ";
	public static final String FAILED_MARKER="oh, ";
	
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
	public static final String LOCATION_SPEECH="The nearest stop to %s is %s. ";
	/**
	 * StopName
	 */
	public static final String BUSSTOP_SPEECH=" At %s, ";
	
	
	////RESULTS////
	/**
	 * Speech fragment if there are no prediction results for an "All Routes" request
	 * Format with Direction, BusStopName
	 */
	public static final String NO_ALL_ROUTES_SPEECH=" No %s busses are expected at %s in the next 30 minutes. ";
	
	/**
	 * Speech fragment if there are no prediction results for an "All Routes" request
	 * Format with Direction, RouteID, and BusStopName
	 */
	public static final String NO_SINGLE_ROUTE_SPEECH=" No %s, %s is expected at %s in the next 30 minutes. ";
	
	/**
	 * Speech fragment for first prediction result
	 * Format with RouteID, Prediction Time
	 */
	public static final String FIRST_RESULT_SPEECH=" The %s will be arriving in %s minutes ";
	
	/**
	 * Speech fragment for additional prediction result
	 * Format with Prediction Time
	 */
	public static final String MORE_RESULTS_SPEECH=", %s minutes ";
	
	/**
	 * Speech fragment for additional prediction result
	 * Format with Prediction Time
	 */
	public static final String FINAL_RESULTS_SPEECH=", and %s minutes ";
	
	/**
	 * Speech fragment with instructions to hear all routes.
	 */
	public static final String HELP_ALL_ROUTES_SPEECH=CHANGE_MARKER+"to hear predictions for all routes that stop there, say , Alexa, ask "+GetNextBusSpeechlet.INVOCATION_NAME+" for All Routes";

	/**
	 * Speech fragment with generic instructions .
	 */
	public static final String HELP_SPEECH=GetNextBusSpeechlet.INVOCATION_NAME+" will tell you when the next bus is coming if you provide it a bus line, direction, and location near your bus stop.";
	/**
	 * Speech fragment for stopping or cancelling.
	 */
	public static final String STOP_SPEECH="Oh? OK";


	//	public static SpeechletResponse getNoResponse(PaInputData inputData) {
	//		return getNoResponse(inputData, "");
	//	}

	

	public static String getNoResponse(PaInputData inputData, SkillContext c) {
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

		return textOutput;

	}

	//	public static SpeechletResponse getResponse(PaInputData inputData, ArrayList<Result> results) {	
	//		return getResponse(inputData, results, "");
	//	}

	public static String[] getResponse(PaInputData inputData, ArrayList<Result> results, SkillContext c) {	
		String[] output = new String[2];
		String textOutput = "";
		String speechOutput;

		//final String locationOutput="The nearest stop to "+  inputData.getLocationName() +" is " + inputData.getStopName()+". ";
		//final String stopOutput=" At " +inputData.getStopName()+", ";
		//final String allRoutesHelpText=" <break time=\"0.25s\" />  to hear predictions for all routes that stop "+BUSSTOP_SPEECH+  ", say <break time=\"0.25s\" /> Alexa, ask "+GetNextBusSpeechlet.INVOCATION_NAME+" for All Routes";

		
		if (c.getNeedsLocation()){
			textOutput+=String.format(LOCATION_SPEECH, inputData.getLocationName(), inputData.getStopName());
		} else {
			textOutput+=String.format(BUSSTOP_SPEECH, inputData.getStopName()) ;
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
			} else if (i == results.size() - 1){
				textOutput += String.format(FINAL_RESULTS_SPEECH, when);
				speechOutput += String.format(FINAL_RESULTS_SPEECH, when);
			} else if (routeID.equals(prevRouteID)){
				if (i < results.size() - 1 && !results.get(i + 1).getRoute().equals(routeID)){
					textOutput += String.format(FINAL_RESULTS_SPEECH, when);
					speechOutput += String.format(FINAL_RESULTS_SPEECH, when);
				} else {
					textOutput += String.format(MORE_RESULTS_SPEECH, when);
					speechOutput += String.format(MORE_RESULTS_SPEECH, when);
				}
			} else {
				textOutput += ".\n "+String.format(FIRST_RESULT_SPEECH,routeID,when);
				speechOutput += "<break time=\"0.25s\" /> "+String.format(FIRST_RESULT_SPEECH,routeID,when);
			}
			prevRouteID=routeID;
		}
		output[0] = textOutput;
		output[1] = speechOutput;
		return output;
		//TODO: maybe the skill context should get these values, instead of having them returned...
	}
        
	
    public static Navigation buildNavigation(String locationLat, String locationLon, double stopLat, double stopLon) throws IOException, JSONException, Exception{	
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

	public static String getFailureResponse(String failureLabel) {
		String message = ("There has been a problem connecting to " + failureLabel + ". I'll let the developers know."); 
		return message;
	}
}
