/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.prosody.portAuthority.googleMaps.model;

import java.util.List;

import co.prosody.portAuthority.util.Location;


/**
 * Direction is a list of legs
 * example:
 * https://maps.googleapis.com/maps/api/directions/json?origin=40.4413962,-80.0035603&destination=40.4332551,-79.9257867&mode=walk&transit_mode=walking&key=API_KEY
 * @author Adithya

 */
public class Direction {
	List<Legs> legsList; //direction: list of legs
	Location northEastBound; //north east direction
	Location southWestBound;// south west direction
	String copyrights; //copyrights

	/**
	 * @param legsList
	 */
	public Direction(List<Legs> legsList) {
		super();
		this.legsList = legsList;
	}

	/**
	 * @return legsList
	 */
	public final List<Legs> getLegsList() {
		return legsList;
	}

	/**
	 * @param legsList
	 */
	public final void setPathsList(List<Legs> legsList) {
		this.legsList = legsList;
	}

	/**
	 * @return northEastBound
	 */
	public final Location getNorthEastBound() {
		return northEastBound;
	}

	/**
	 * @param northEastBound
	 */
	public final void setNorthEastBound(Location northEastBound) {
		this.northEastBound = northEastBound;
	}

	/**
	 * @return southWestBound
	 */
	public final Location getSouthWestBound() {
		return southWestBound;
	}

	/**
	 * @param southWestBound
	 */
	public final void setmSouthWestBound(Location southWestBound) {
		this.southWestBound = southWestBound;
	}
	/**
	 * @return copyrights
	 */
	public final String getCopyrights() {
		return copyrights;
	}

	/**
	 * @param copyrights
	 */
	public final void setCopyrights(String copyrights) {
		this.copyrights = copyrights;
	}
        
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Legs legs : legsList) {
			sb.append(legs.toString());
			sb.append("\r\n");
		}
                String formattedResult = sb.toString().trim().replaceAll(" +", " ");
		return formattedResult;
	}
        
        public String getHTML() {
		StringBuilder sb = new StringBuilder();
		for (Legs legs : legsList) {
			sb.append(legs.getHTML());
			sb.append("\r\n");
		}
		return sb.toString();
	}
        
         public String getPoints() {
		StringBuilder sb = new StringBuilder();
		for (Legs legs : legsList) {
			sb.append(legs.getWayPoints());
		}
		return sb.toString();
	}

}
