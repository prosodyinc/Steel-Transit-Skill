package co.prosody.portAuthority.api;

import java.util.ArrayList;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class TrueTimeHandler extends DefaultHandler {
	private final static Logger LOGGER = LoggerFactory.getLogger("TrueTimeHandler");

       private Message message;
       private String temp;
       private ArrayList<Message> messageList = new ArrayList<Message>();

       public ArrayList<Message> getMessages(){
    	   return messageList;
       }
       
       public TrueTimeHandler(){
    	   super();
    	   LOGGER.trace("constructor");
       }
       
       /*
        * When the parser encounters plain text (not XML elements),
        * it calls(this method, which accumulates them in a string buffer
        */
       public void characters(char[] buffer, int start, int length) {
              temp = new String(buffer, start, length);
       }
      

       /*
        * Every time the parser encounters the beginning of a new element,
        * it calls this method, which resets the string buffer
        */ 
       public void startElement(String uri, String localName,
                     String qName, Attributes attributes) throws SAXException {
    	   LOGGER.trace("startElement:"+uri+","+localName+","+qName);
              temp = "";
              if (qName.equalsIgnoreCase(Message.PREDICTION)||
            		  qName.equalsIgnoreCase(Message.STOP)||
            		  qName.equalsIgnoreCase(Message.ROUTE)||
            		  qName.equalsIgnoreCase(Message.ERROR)) {
          		
                     message = new Message();
                     message.setMessageType(qName);
                     //acct.setType(attributes.getValue("type"));  
              }
       }

       /*
        * When the parser encounters the end of an element, it calls this method
        */
       public void endElement(String uri, String localName, String qName)
    		   throws SAXException {
    	   LOGGER.trace("endElement="+uri+","+localName+","+qName+","+temp);

    	   if (qName.equalsIgnoreCase(Message.PREDICTION)) {
    		   // message.setEstimate(message.getPredictionTime()-message.getTimestamp());
    		   DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyymmdd HH:mm");
    		   DateTime predTime = formatter.parseDateTime(message.getPredictionTime());
    		   DateTime nowTime = formatter.parseDateTime(message.getTimestamp());
    		   int ETA=predTime.getMinuteOfDay()-nowTime.getMinuteOfDay();
    		   LOGGER.debug("estimate:"+ETA);
    		   message.setEstimate(ETA);
    		   // add it to the list
    		   messageList.add(message);
    	   } else if (qName.equalsIgnoreCase(Message.STOP)) {
    		   messageList.add(message);   
    	   } else if (qName.equalsIgnoreCase(Message.ERROR)) {
    		   messageList.add(message);   
    	   } else if (qName.equalsIgnoreCase(Message.ROUTE)) {
    		   messageList.add(message);   
    	   } else {

    		   if (qName.equalsIgnoreCase(Message.TIMESTAMP)) {

    			   LOGGER.debug("endElement:timestamp="+temp);
    			   message.setTimestamp(temp);
    		   } else if (qName.equalsIgnoreCase(Message.PREDICTION_TIME)) {
    			   LOGGER.debug("endElement:predictionTime="+temp);
    			   message.setPredictionTime(temp);
    		   } else if (qName.equalsIgnoreCase(Message.TYPE)) {
    			   message.setType(temp);
    		   } else if (qName.equalsIgnoreCase(Message.STOP_ID)) {
    			   message.setStopID(temp);  
    		   } else if (qName.equalsIgnoreCase(Message.STOP_NAME)) {
    			   message.setStopName(temp);  
    		   } else if (qName.equalsIgnoreCase(Message.VEHICLE_ID)) {
    			   //    		   TODO: message.setVehicleID(VID);
    		   } else if (qName.equalsIgnoreCase(Message.DISTANCE_TO_STOP)) {
    			   //    		   TODO: message.setDistanceToStop(distanceToStop);
    		   } else if (qName.equalsIgnoreCase(Message.ROUTE_ID)) {
    			   message.setRouteID(temp);
    		   } else if (qName.equalsIgnoreCase(Message.ROUTE_NAME)) {
    			   message.setRouteName(temp);
    		   } else if (qName.equalsIgnoreCase(Message.ROUTE_COLOR)) {
    			   message.setRouteColor(temp);
    		   } else if (qName.equalsIgnoreCase(Message.ROUTE_DESIGNATOR)) {
    			   message.setRouteDesignator(temp);
    		   } else if (qName.equalsIgnoreCase(Message.DIRECTION)) {
    			   message.setDirection(temp);
    		   } else if (qName.equalsIgnoreCase(Message.DESTINATION)) {
    			   message.setDestination(temp);
    		   } else if (qName.equalsIgnoreCase(Message.IS_DELAYED)) {
    			   //  		   TODO: message.setDelayed(isDelayed);
    		   } else if (qName.equalsIgnoreCase(Message.TA_BLOCK_ID)) {
    			   message.setTaBLockID(temp);
    		   } else if (qName.equalsIgnoreCase(Message.TA_TRIP_ID)) {
    			   message.setTaTripID(temp);
    		   } else if (qName.equalsIgnoreCase(Message.ZONE)) {
    			   message.setZone(temp);
    		   }
    	   }
       }
}

