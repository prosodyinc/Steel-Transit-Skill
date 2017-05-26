/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.prosody.portAuthority.googleMaps.model;

import java.util.List;

/**
 *
 * @author Adithya
 */
public class Path {

    List<Point> path; //path: list of points
    int distance; //length of path
    int duration; //duration to traverse the path
    String travelMode; //travel mode of the path
    String htmlText;//HTML instruction associated with the path.

    /**
     * @param path The list of points that make the path
     */
    public Path(List<Point> path) {
        super();
        this.path = path;
    }

    /**
     * @return path
     */
    public final List<Point> getPath() {
        return path;
    }

    /**
     * @param path to set
     */
    public final void setPath(List<Point> path) {
        this.path = path;
    }

    /**
     * @return the mDistance
     */
    public final int getDistance() {
        return distance;
    }

    /**
     * @return duration
     */
    public final int getDuration() {
        return duration;
    }

    /**
     * @return travelMode
     */
    public final String getTravelMode() {
        return travelMode;
    }

    /**
     * @return HTML text
     */
    public final String getHtmlText() {
        return htmlText;
    }

    /**
     * @param path to set
     */
    public final void setmPath(List<Point> path) {
        this.path = path;
    }

    /**
     * @param distance to set
     */
    public final void setDistance(int distance) {
        this.distance = distance;
    }

    /**
     * @param duration to set
     */
    public final void setDuration(int duration) {
        this.duration = duration;
    }

    /**
     * @param travelMode to set
     */
    public final void setTravelMode(String travelMode) {
        this.travelMode = travelMode;
    }

    /**
     * @param htmlText to set
     */
    public final void setHtmlText(String htmlText) {
        this.htmlText = htmlText;
    }

    @Override
    public String toString() {
        StringBuilder strB = new StringBuilder();
        strB.append(getHtmlText().replaceAll("\\<.*?\\> ?", " "));
        return strB.toString();
    }

    /**
     * Gives results of coordinates in the path. This can be used for plotting
     * path on the map.
     *
     * @return
     */
    public String printPoints() {
        StringBuilder strB = new StringBuilder();
        for (Point point : path) {
            strB.append(point.toString());
            strB.append("|");
            strB.append(point.toString());
            strB.append("|");
        }
        return strB.toString();
    }

}
