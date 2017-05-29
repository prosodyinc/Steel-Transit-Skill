package co.prosody.portAuthority.util;


public class Result implements Comparable<Result>{
	String route;
	int estimate;

	public Result (String route, int prediction){
		this.route=route;
		this.estimate=prediction;
	}

	public int compareTo(Result r){
		if (route.compareTo(r.route)>0)  return 1;
		if (route.equalsIgnoreCase(r.route)){  
			if (estimate>r.estimate) return 1;
			if (estimate==r.estimate) return 0;
		}
		return -1;
	}

	public String getRoute(){
		return this.route;
	}

	public int getEstimate(){
		return this.estimate;
	}
}