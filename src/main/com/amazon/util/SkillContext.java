package com.amazon.util;

import java.util.LinkedHashMap;

import co.prosody.portAuthority.storage.PaInputData;

/**
 * Contains session scoped settings.
 */
public class SkillContext {
    private boolean needsMoreHelp;
    private boolean allRoutes;
    private boolean needsLocation;
    private boolean needsBusStop;

    public static SkillContext create(Object o){
    	SkillContext context = null;
    	if (o instanceof LinkedHashMap){
    		LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>)o;
    		context = SkillContext.newInstance();
    		context.setNeedsMoreHelp((boolean)(map.get("needsMoreHelp")));
    		context.setAllRoutes((boolean)(map.get("allRoutes")));
    		context.setNeedsLocation((boolean)(map.get("needsLocation")));
    		context.setNeedsBusStop((boolean)(map.get("needsBusStop")));
    	} else if (o instanceof SkillContext){
    		context = (SkillContext)o;
    	} else {
    		throw new ClassCastException("Cannot create a SkillContext object with " + o.getClass().toString());
    	}
    	return context;
    }
    
    public static SkillContext newInstance(){
    	SkillContext context = new SkillContext();
    	context.setAllRoutes(false);
    	context.setNeedsBusStop(true);
    	context.setNeedsLocation(true);
    	context.setNeedsMoreHelp(true);
    	return context;
    }
    
    public boolean getNeedsMoreHelp() {
        return needsMoreHelp;
    }

    public void setNeedsMoreHelp(boolean needsMoreHelp) {
        this.needsMoreHelp = needsMoreHelp;
    }

	public boolean isAllRoutes() {
		return allRoutes;
	}

	public void setAllRoutes(boolean showAllRoutes) {
		this.allRoutes = showAllRoutes;
	}

	public boolean getNeedsLocation() {
		return needsLocation;
	}

	public void setNeedsLocation(boolean needsLocation) {
		this.needsLocation = needsLocation;
	}

	public boolean getNeedsBusStop() {
		return needsBusStop;
	}

	public void setNeedsBusStop(boolean needsBusStop) {
		this.needsBusStop = needsBusStop;
	}
}
