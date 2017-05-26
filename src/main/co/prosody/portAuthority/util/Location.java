package co.prosody.portAuthority.util;

import java.util.List;

/**
 *
 * @author Adithya
 */
public class Location {

    
    private double lat;
    private double lng;
    private String address;
    private String name;
    private List<String> types;
    
    public Location(){	
    }
    
    public Location(double lat, double lng){	
        this.lat = lat;
        this.lng = lng;
    }
    
    public Location(double lat, double lng, String address) {
		this.lat=lat;
		this.lng=lng;
		this.address=address;
	}
    
    public Location(String name, double lat, double lng, String address, List<String> types) {
		this.name=name;
		this.lat=lat;
		this.lng=lng;
		this.address=address;
		this.types=types;
	}
    
    public String getStreetAddress(){
    	if (address==null){
    		return null;
    	}
    	
    	String output;
    	String[] addressLines = address.split(",");
    	output= addressLines[0];
    	return output;
    }
    
    public boolean isAddress(){
    	if (types!=null&&types.contains("street_address")){
    		return true;
    	} else{
    		return false;
    	}
    }

    //////////Getters/Setters////////////
	public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getTypes() {
		return types;
	}

	public void setTypes(List<String> types) {
		this.types = types;
	}

	@Override
	public String toString() {
		return "Coordinates [lat=" + lat + ", lng=" + lng + ", address=" + address + "]";
	}


    
}
