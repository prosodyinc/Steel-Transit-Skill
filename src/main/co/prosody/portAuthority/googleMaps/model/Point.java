/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.prosody.portAuthority.googleMaps.model;

import co.prosody.portAuthority.util.Location;

/**
 * @author Adithya
 */
public class Point {
	double lat;
	double lng;
        
	private Location coordinates = null;
	
	/*
	 * @param coordinate retrieved by parsing json.
	 */
	public Point(double lat,double lng) {
		super();
		this.lat = lat;
		this.lng=lng;
	}

	/**
	 * @return The Coordinates Object linked to that point
	 */
	public Location getLatLng() {
		if (coordinates == null) {
			coordinates = new Location(lat,lng);
		}
		return coordinates;
	}
        
	@Override
	public String toString() {
		return lat+","+lng;
	}
}
