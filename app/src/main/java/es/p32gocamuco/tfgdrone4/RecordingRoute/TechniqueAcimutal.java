package es.p32gocamuco.tfgdrone4.RecordingRoute;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * This class represents an acimutal technique.
 * In the acimutal technique, the drone will be direclty overhead the target at a set height.
 * The bearing will either be constant, or be defined by the next point in the route.
 * @author Manuel Gomez Castro
 */

public class TechniqueAcimutal extends RecordingTechnique implements Serializable {
    private double heightOverTarget;
    private double bearing;
    private double hoverTime;
    private boolean constantBearing;

    public TechniqueAcimutal(double heightOverTarget, double bearing, double hoverTime, boolean constantBearing){
        super();
        this.heightOverTarget = heightOverTarget;
        this.bearing = bearing;
        this.hoverTime = hoverTime;
        this.constantBearing = constantBearing;
    }

    public void setHeightOverTarget(double heightOverTarget) {
        this.heightOverTarget = heightOverTarget;
        if (listener != null){
            listener.onTechniqueParametersChanged(this);
        }
    }

    public void setBearing(double bearing) {
        this.bearing = bearing;
        if (listener != null){
            listener.onTechniqueParametersChanged(this);
        }
    }

    public void setConstantBearing(boolean constantBearing) {
        this.constantBearing = constantBearing;
        if (listener != null){
            listener.onTechniqueParametersChanged(this);
        }
    }

    public void setHoverTime(double hoverTime) {
        this.hoverTime = hoverTime;
        if (listener != null){
            listener.onTechniqueParametersChanged(this);
        }
    }

    public double getHeightOverTarget() {
        return heightOverTarget;
    }

    public double getBearing() {
        return bearing;
    }

    public boolean isConstantBearing() {
        return constantBearing;
    }

    public double getHoverTime() {
        return hoverTime;
    }


    /**
     * Implementation of the Acimutal technique.
     * In an acimutal shot, the drone will be located directly overhead the target, at a specified
     * height over the target
     * @param target Target of interest.
     * @return
     */
    @Override
    public ArrayList<RoutePoint> calculateRoutePointsOf(Target target) {
        RoutePoint routePoint = new RoutePoint(target);
        routePoint.setHeight(target.getHeight()+getHeightOverTarget());
        routePoint.setActiveTime(getHoverTime());
        routePoint.focus(target);

        if((getRoutePoints().size() > 0)&& !isConstantBearing()){
            RoutePoint previous = getRoutePoints().get(getRoutePoints().size()-1);
            double bearing = previous.calculateBearingTowards(routePoint);
            previous.setBearing(bearing);
            routePoint.setBearing(bearing);
        } else {
            routePoint.setBearing(getBearing());
        }

        ArrayList<RoutePoint> routePoints = new ArrayList<>(1);
        routePoints.add(routePoint);
        return routePoints;
    }
}
