/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.prosody.portAuthority.googleMaps.model;

import java.util.List;

/**
 * Legs is a list of paths
 * @author Adithya
 */
    public class Legs {
	
	List<Path> pathsList;//legs: list of paths
	int distance; //total legs distance
	int duration; //duration of travel by legs
	String startAddr; //start address
	String endAddr; //end address

	/**
	 * @param pathsList
	 */
	public Legs(List<Path> pathsList) {
		super();
		this.pathsList = pathsList;
	}

	/**
	 * @return legsList
	 */
	public final List<Path> getPathsList() {
		return pathsList;
	}

	/**
	 * @param pathsList to set
	 */
	public final void setPathsList(List<Path> pathsList) {
		this.pathsList = pathsList;
	}

	/**
	 * @return distance
	 */
	public final int getDistance() {
		return distance;
	}

	/**
	 * @param distance to set
	 */
	public final void setDistance(int distance) {
		this.distance = distance;
	}

	/**
	 * @return duration
	 */
	public final int getDuration() {
		return duration;
	}

	/**
	 * @param duration to set
	 */
	public final void setmDuration(int duration) {
		this.duration = duration;
	}

	/**
	 * @return startAddr
	 */
	public final String getStartAddr() {
		return startAddr;
	}

	/**
	 * @param startAddr to set
	 */
	public final void setStartAddr(String startAddr) {
		this.startAddr = startAddr;
	}

	/**
	 * @return endAddr
	 */
	public final String getEndAddr() {
		return endAddr;
	}

	/**
	 * @param endAddr to set
	 */
	public final void setEndAddr(String endAddr) {
		this.endAddr = endAddr;
	}
        
	@Override
	public String toString() {
		StringBuilder strB=new StringBuilder();
		for(Path path:pathsList) {
			strB.append(path.toString());
                        strB.append(". ");
			strB.append("\r\n");
		}
		return strB.toString();
	}
        
        public String getHTML() {
		StringBuilder strB=new StringBuilder();
		for(Path path:pathsList) {
			strB.append(path.getHtmlText());
                        strB.append(". ");
			strB.append("\r\n");
		}
		return strB.toString();
	}
        
        public String getWayPoints(){
		StringBuilder strB=new StringBuilder();
		for(Path path:pathsList) {
			strB.append(path.printPoints());
		}
		return strB.toString();
        }
}
