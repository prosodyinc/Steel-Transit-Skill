package co.prosody.portAuthority.util;

/**
 *
 * @author Adithya
 */
public class Stop implements Comparable<Stop> {

    private String stopID;//Stop ID
    private String stopName;//Description of stop
    private String direction;//inbound/outbound
    private String mode; //bus
    private double lat; //latitude
    private double lon; //longitude
    private int cleverID; //actual stop ID that shows up on all the apps
    private String routes;//String containing list of comma separated routes
    private String zone; //Zone number
    private String timepoint; //Don't know what this is..just says Yes/ No
    private String shelterOwn; //indicates whether the stop has a shelter
    private String stopType; //indicates type of stop: bus stop/ Train et al.
    private double distance; //in Meters
    private String walkTime; //in Seconds
    
    public Stop(String stopID, String stopName, double lat, double lon){
        this.stopID = stopID;
        this.stopName = stopName;
        this.lat = lat;
        this.lon = lon;
    }

    public Stop() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
	public int compareTo(Stop s){
		return Double.compare(distance, s.distance);
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

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public double getLatitude() {
        return lat;
    }

    public void setLatitude(double latitude) {
        this.lat = latitude;
    }

    public double getLongitude() {
        return lon;
    }

    public void setLongitude(double longitude) {
        this.lon = longitude;
    }

    public int getCleverID() {
        return cleverID;
    }

    public void setCleverID(int cleverID) {
        this.cleverID = cleverID;
    }

    public String getRoutes() {
        return routes;
    }

    public void setRoutes(String routes) {
        this.routes = routes;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getTimepoint() {
        return timepoint;
    }

    public void setTimepoint(String timepoint) {
        this.timepoint = timepoint;
    }

    public String getShelterOwn() {
        return shelterOwn;
    }

    public void setShelterOwn(String shelterOwn) {
        this.shelterOwn = shelterOwn;
    }

    public String getStopType() {
        return stopType;
    }

    public void setStopType(String stopType) {
        this.stopType = stopType;
    }

	public double getDistance() {
		return distance;
	}

	public void setDistance(double d) {
		this.distance = d;
	}

	public String getWalkTime() {
		return walkTime;
	}

	public void setWalkTime(String walkTime) {
		this.walkTime = walkTime;
	}

	@Override
	public String toString() {
		return "Stop [stopID=" + stopID + ", stopName=" + stopName + ", distance=" + distance + ", walkTime=" + walkTime
				+ "]";
	}

}
