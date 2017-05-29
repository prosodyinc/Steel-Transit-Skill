package com.amazon.util;

/**
 * Contains session scoped settings.
 */
public class SkillContext {
    private boolean needsMoreHelp = true;
    private boolean allRoutes = false;
    private boolean needsLocation=true;
    private boolean needsBusStop=true;

    public boolean needsMoreHelp() {
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

	public boolean needsLocation() {
		return needsLocation;
	}

	public void setNeedsLocation(boolean needsLocation) {
		this.needsLocation = needsLocation;
	}

	public boolean needsBusStop() {
		return needsBusStop;
	}

	public void setNeedsBusStop(boolean needsBusStop) {
		this.needsBusStop = needsBusStop;
	}
}
