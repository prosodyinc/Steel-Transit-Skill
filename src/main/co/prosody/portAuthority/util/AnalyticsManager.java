package co.prosody.portAuthority.util;

import com.brsanthu.googleanalytics.EventHit;
import com.brsanthu.googleanalytics.ExceptionHit;
import com.brsanthu.googleanalytics.GoogleAnalytics;

public class AnalyticsManager {
	public static final String UA_ID="UA-88894500-1";
	public static final String CATEGORY_SESSION="Session";
	public static final String ACTION_SESSION_START="start";
	public static final String ACTION_SESSION_END="end";
	public static final String CATEGORY_LAUNCH="Launch";
	public static final String CATEGORY_INTENT="Intent";
	public static final String CATEGORY_RESPONSE="Response";
	
	private GoogleAnalytics ga;
	private String userId;

	public AnalyticsManager() {
		this.ga = new GoogleAnalytics(UA_ID);
	}

	private GoogleAnalytics getGa() {
		return ga;
	}

	public void setGa(GoogleAnalytics ga) {
		this.ga = ga;
	}

	private String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	public void postSessionEvent(String eventName){
		EventHit event = new EventHit(CATEGORY_SESSION, eventName);
		event.userId(getUserId());
		event.sessionControl(eventName);
		getGa().post(event);
	}
	
	public void postEvent(String category, String action){
		EventHit event = new EventHit(category, action);
		event.userId(getUserId());
		getGa().post(event);
	}
	
	public void postEvent(String category, String action, String label, Integer value){
		EventHit event = new EventHit(category, action, label, value);
		event.userId(getUserId());
		getGa().post(event);
	}
	
	public void postException(String message, Boolean isFatal){
		ExceptionHit exception = new ExceptionHit(message, isFatal);
		exception.userId(getUserId());
		getGa().post(exception);
	}
	
}