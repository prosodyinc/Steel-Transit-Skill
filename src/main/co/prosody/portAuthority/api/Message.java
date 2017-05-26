package co.prosody.portAuthority.api;

import java.text.SimpleDateFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Message implements Comparable<Message>{

	private final static Logger LOGGER = LoggerFactory.getLogger("Message");
	//Message Parameters
	static final String TIMESTAMP="tmstmp";
	static final String TYPE ="typ";
	static final String STOP_ID ="stpid";
	static final String STOP_NAME ="stpnm";
	static final String VEHICLE_ID ="vid";
	static final String DISTANCE_TO_STOP ="dstp";
	static final String ROUTE_ID ="rt";
	static final String ROUTE_NAME ="rtnm";
	static final String ROUTE_COLOR ="rtnm";
	static final String ROUTE_DESIGNATOR ="rtnm";
	static final String DIRECTION ="rtdir";
	static final String DESTINATION ="des";
	static final String PREDICTION_TIME ="prdtm";
	static final String IS_DELAYED ="dly";
	static final String TA_BLOCK_ID ="tablockid";
	static final String TA_TRIP_ID ="tatripid"  ;   
	static final String ZONE ="zone";
	
	//Message Types
	public static final String PREDICTION="prd";
	public static final String STOP="stop";
	public static final String ROUTE="route";
	public static final String ERROR = "error";
	
    static SimpleDateFormat FORMATTER = new SimpleDateFormat("YYYYMMDD HH:MM");

    /**
     * static final String PREDICTION="prd";
     * static final String STOP="stop";
     * static final String ERROR = "error";
     * static final String ROUTE = "route"
     */
    	private String messageType;
    	
	/**
	 * Date and time (local) the prediction was generated. Date and time is
	 * represented in the following format: YYYYMMDD HH:MM. Month is represented
	 * as two digits where January is equal to "01" and December is equal to
	 * "12". Time is represented using a 24-hour clock.
	 */
    private String timestamp; //<xs:element name="tmstmp" type="xs:string" minOccurs="1" maxOccurs="1"/>
    
    /**
	 * Type of prediction. 'A' for an arrival
	 * prediction (prediction of when the vehicle will arrive at this stop). 'D'
	 * for a departure prediction (prediction of when the vehicle will depart
	 * this stop, if applicable). Predictions made for first stops of a route or
	 * layovers are examples of departure predictions.
	 */
    private String type; //<xs:element name="typ" type="xs:string" minOccurs="1" maxOccurs="1"/>
    
    /**
     * Unique identifier representing the stop for which this prediction was generated.
     */
    private String stopID; //<xs:element name="stpid" type="xs:int" minOccurs="1" maxOccurs="1"/>
    
    /**
     * Display name of the stop for which this prediction was generated.
     */
    private String stopName;//<xs:element name="stpnm" type="xs:string" minOccurs="1" maxOccurs="1"/>
    
    /**
     * Unique ID of the vehicle for which this prediction was generated.
     */
    private int vehicleID;//<xs:element name="vid" type="xs:int" minOccurs="1" maxOccurs="1"/>
    
    /**
     * Linear distance (feet) left to be traveled by the vehicle before it reaches the stop associated with this prediction. 
     */
    private int distanceToStop;//<xs:element name="dstp" type="xs:int" minOccurs="1" maxOccurs="1"/>
     
    /**
     * Alphanumeric designator of the route (ex. "20" or "X20") for which this prediction was generated.
     */
    private String routeID;//<xs:element name="rt" type="xs:string" minOccurs="1" maxOccurs="1"/>
    
    /**
     * Common name of the route (ex. "Madison" for the 20 route).
     */
    private String routeName; //<xs:element name="rtnm" type="xs:string" minOccurs="1" maxOccurs="1"/>
   
	 /**
	  * Color of the route line used in map (ex. "#ffffff")
	  */
	 private String routeColor;
	 
	 /**
	  *  Language-specific route designator meant for display.
	  */
	 private String routeDesignator;
			 
	/**
     * Direction of travel of the route associated with this prediction (ex. "East Bound").
     */
    private String direction;//<xs:element name="rtdir" type="xs:string" minOccurs="1" maxOccurs="1"/>
    
    /**
     * Final destination of the vehicle associated with this prediction.
     */
    private String destination;//<xs:element name="des" type="xs:string" minOccurs="1" maxOccurs="1"/>
    
	/**
	 * Predicted date and time (local) of a vehicle's arrival or departure to
	 * the stop associated with this prediction. Date and time is represented in
	 * the following format: YYYYMMDD HH:MM. Month is represented as two digits
	 * where January is equal to "01" and December is equal to "12". Time is
	 * represented using a 24-hour clock.
	 */
    private String predictionTime; //<xs:element name="prdtm" type="xs:string" minOccurs="1" maxOccurs="1"/>
    
    /**
     * Only present if the vehicle that generated this prediction is delayed.
     */
    private boolean isDelayed;//<xs:element name="dly " type="xs:boolean" minOccurs="0" maxOccurs="1"/>
    
    /**
     * TA's version of the scheduled block identifier for the work currently being performed by the vehicle.
     */
    private String taBLockID; //<xs:element name="tablockid" type="xs:string" minOccurs="1" maxOccurs="1"/>
    
    /**
     * TA's version of the scheduled trip identifier for the vehicles current trip.
     */
    private String taTripID; //<xs:element name="tatripid" type="xs:string" minOccurs="1" maxOccurs="1"/>      
    
    /**
     * The zone name if the vehicle has entered a defined zones, otherwise blank.
     */
    private String zone; //<xs:element name="zone" type="xs:string" minOccurs="1" maxOccurs="1"/>     
    
    /**
     * The calculated ETA
     */
    private int estimate; 
    
    /**
     * The error returned by the API
     */
    private String error; 

    ////////////////	
    public boolean equals(Object obj) {
    	//TODO: write equals code
    	return false;
    }
    
    // sort by predictionTime
    public int compareTo(Message another) {
    	if (another == null) return 1;
    	// sort descending, most recent first
    	return another.predictionTime.compareTo(predictionTime);
    }
    

    //////////////////
    public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getStopID() {
		return stopID;
	}

	public void setStopID(String stopID) {
		this.stopID = stopID;
	}

	public String getStopName() {
		return stopName;
	}

	public void setStopName(String stopName) {
		this.stopName = stopName;
	}

	public int getVehicleID() {
		return vehicleID;
	}

	public void setVehicleID(int vehicleID) {
		this.vehicleID = vehicleID;
	}

	public int getDistanceToStop() {
		return distanceToStop;
	}

	public void setDistanceToStop(int distanceToStop) {
		this.distanceToStop = distanceToStop;
	}

	public String getRouteID() {
		return routeID;
	}

	public void setRouteID(String routeID) {
		this.routeID = routeID;
	}
	
    public String getRouteName() {
		return routeName;
	}

	public void setRouteName(String routeName) {
		this.routeName = routeName;
	}

	public String getRouteColor() {
		return routeColor;
	}

	public void setRouteColor(String routeColor) {
		this.routeColor = routeColor;
	}

	public String getRouteDesignator() {
		return routeDesignator;
	}

	public void setRouteDesignator(String routeDesignator) {
		this.routeDesignator = routeDesignator;
	}

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}
	
	public void setEstimate(int estimate){
		this.estimate=estimate;
	}
	
	public int getEstimate(){
		return estimate;
	}

	public String getPredictionTime() {
		return predictionTime;
	}

	public void setPredictionTime(String predictionTime) {
		this.predictionTime = predictionTime;
	}

	public boolean isDelayed() {
		return isDelayed;
	}

	public void setDelayed(boolean isDelayed) {
		this.isDelayed = isDelayed;
	}

	public String getTaBLockID() {
		return taBLockID;
	}

	public void setTaBLockID(String taBLockID) {
		this.taBLockID = taBLockID;
	}

	public String getTaTripID() {
		return taTripID;
	}

	public void setTaTripID(String taTripID) {
		this.taTripID = taTripID;
	}

	public String getZone() {
		return zone;
	}

	public void setZone(String zone) {
		this.zone = zone;
	}
	
	public String getMessageType(){
		return messageType;
	}
	
	public void setMessageType(String msgType){
		this.messageType=msgType;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		LOGGER.warn("Error detected:"+error);
		this.error = error;
	}

}
