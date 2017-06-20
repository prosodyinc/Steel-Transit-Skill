package com.amazon.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.prosody.portAuthority.GetNextBusSpeechlet;
import co.prosody.portAuthority.ResponseObject;
import co.prosody.portAuthority.api.Message;
import co.prosody.portAuthority.googleMaps.GoogleMaps;
import co.prosody.portAuthority.storage.PaInputData;
import co.prosody.portAuthority.util.Navigation;
import co.prosody.portAuthority.util.Result;


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
	 * Speech fragment for stopping or canceling.
	 */
	public static final String STOP_SPEECH="Oh? OK";
	
	public static ResponseObject getWelcomeResponse(){
		String output=AUDIO_WELCOME+" "+SPEECH_WELCOME + ROUTE_PROMPT;
		return new ResponseObject("", output);
	}
	
	public static ResponseObject getHelpResponse(){
		String output=AUDIO_WELCOME+" "+HELP_SPEECH + " " + ROUTE_PROMPT;
		ResponseObject response = new ResponseObject("", output);
		return response;
	}
	
	public static ResponseObject getStopResponse(){
		String output=STOP_SPEECH;
		ResponseObject response = new ResponseObject("", output);
		return response;
	}
	
	//No Speech Output
	/**
	 * Returns a ResponseObject response that indicates there are no buses (or no particular bus) arriving at the given stop within the next 30 minutes.
	 * @param inputData The data object of the conversation
	 * @param skillContext The context of the conversation
	 * @return The response indicating no buses are due to arrive
	 */
	public static ResponseObject getNoResponse(PaInputData inputData, SkillContext skillContext) {
		String textOutput="";
		if (skillContext.getNeedsLocation()){
			textOutput=String.format(LOCATION_SPEECH, inputData.getLocationName(), inputData.getStopName()); 
		}

		if (skillContext.isAllRoutes()){
			textOutput+=String.format(NO_ALL_ROUTES_SPEECH, inputData.getDirection(), inputData.getStopName());
		} else {
			textOutput+=String.format(NO_SINGLE_ROUTE_SPEECH, inputData.getDirection(), inputData.getRouteID() , inputData.getStopName());
		}

		if ((skillContext.getNeedsMoreHelp())&&(!skillContext.isAllRoutes())){
			textOutput+=HELP_ALL_ROUTES_SPEECH;
			
		}
		return new ResponseObject(textOutput, textOutput);

	}

	/**
	 * Lists off each bus arriving at a given stop.
	 * Builds a response given a list of bus routes and times arriving at a particular stop. 
	 * Formats the response so that the times for each bus route arriving at this stop are listed off.
	 * @param inputData The data object of the conversation
	 * @param results The route number and estimated time of each bus arriving at this stop in the next 30 minutes.
	 * @param skillContext The context of the conversation
	 * @return SSML speechOutput and a regular text output, returned in array 
	 */
	public static ResponseObject generateResponse(PaInputData inputData, ArrayList<Result> results, SkillContext skillContext) {
		String textOutput = "";
		String speechOutput;

		if (skillContext.getNeedsLocation()){
			if (!skillContext.isAllRoutes()){
				textOutput+=skillContext.getFeedbackText();
			}
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
			LOGGER.info(routeID + " " + when);
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
		ResponseObject response;
		response = new ResponseObject(textOutput, speechOutput);
		return response;
		//TODO: maybe the skill context should get these values, instead of having them returned...
	}
        
	/**
	 * Uploads a picture of the closest path from the location provided by the user to the nearest bus stop.
	 * Uploaded to an S3 bucket.
	 * @param locationLat The latitude of the user's location
	 * @param locationLon The longitude of the user's location
	 * @param stopLat The latitude of the nearest stop
	 * @param stopLon The longitude of the nearest stop
	 * @return A navigation object containing the image URL and directions from the user's location to the nearest stop
	 * @throws IOException
	 * @throws JSONException
	 * @throws Exception
	 */
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

    /**
     * Extracts relevant routeID and ETA information from the Messages returned by the TrueTimeAPI.
     * If there are no Messages, or there is an error message, return null.
     * @param messages The Messages returned by the TrueTime API
     * @param skillContext The conversation context
     * @return A List of Results
     */
    public static ArrayList<Result> getResults(List<Message> messages){
    	ArrayList<Result> results = new ArrayList<Result>();
		if (messages.size() == 0 || messages.get(0).getMessageType().equals(Message.ERROR)){
    		return results; //return an empty ArrayList
    	}

		for (int i = 0; i < messages.size(); i++) {
			results.add(new Result(messages.get(i).getRouteID(), messages.get(i).getEstimate()));
		}
		return results;

		
    }
    
    
    /**
     * Returns a response that indicates there has been an issue connecting to one of the APIs
     * @param failureLabel The API that couldn't establish connection
     * @return The failure reponse
     */
	public static ResponseObject getAPIFailureResponse(String failureLabel) {
		String message = ("There has been a problem connecting to " + failureLabel + ". I'll let the developers know.");
		ResponseObject response = new ResponseObject(message, message);
		return response;
	}
}
