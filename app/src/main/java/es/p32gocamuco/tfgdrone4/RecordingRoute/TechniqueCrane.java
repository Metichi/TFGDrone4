package es.p32gocamuco.tfgdrone4.RecordingRoute;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * This class defines a crane shot.
 * In a crane shot, the drone will be focusing the Target  at an angle.
 * The drones position will be defined by spherical coordinates centered on the target; distance, attitude,
 * and shot angle.
 * The distance will define the focal distance of the drone. The attitude will represent wich side
 * of the target will be focused and angle will represent the height of the drone over the target.
 * @author Manuel Gomez Castro
 */

public class TechniqueCrane extends RecordingTechnique implements Serializable {
    private double distance;
    private double attitude;
    private double angle;
    private double hoverTime;

    /**
     * Basic constructor for the Crane Technique.
     * @param distance Distance of the RoutePoints to the target in meters, this will be equal to the RoutePoint's {@link RoutePoint#getFocalDistance()}
     * @param attitude Direction of the shooting relative to true north in degrees; an attitude of 0 will place the drone north of the Target, 90 will be east, etc...
     * @param angle Angle of elevation over the target in degrees; an angle of 0 will mean the RoutePoint is level with the target and 90 will mean its directly overhead.
     * @param hoverTime Ammount of time, in seconds, the drone will stay in this RoutePoint before continuing.
     */
    public TechniqueCrane(double distance, double attitude, double angle, double hoverTime){
        super();
        this.distance = distance;
        this.attitude = attitude;
        this.angle = angle;
        this.hoverTime = hoverTime;
    }

    public double getHoverTime() {
        return hoverTime;
    }

    public double getDistance() {
        return distance;
    }

    public double getAngle() {
        return angle;
    }

    public double getAttitude() {
        return attitude;
    }

    public void setDistance(double distance) {
        this.distance = distance;
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

    public void setAngle(double angle) {
        this.angle = angle;
        if (listener != null){
            listener.onTechniqueParametersChanged(this);
        }
    }

    public void setAttitude(double attitude) {
        this.attitude = attitude;
        if (listener != null){
            listener.onTechniqueParametersChanged(this);
        }
    }

    @Override
    public ArrayList<RoutePoint> calculateRoutePointsOf(Target target) {
        RoutePoint routePoint = new RoutePoint(target);
        double horizontalDistance = getDistance()*Math.cos(Math.toRadians(getAngle()));
        double verticalDistance = getDistance()*Math.sin(Math.toRadians(getAngle()));

        routePoint.setHeight(target.getHeight()+verticalDistance);
        routePoint.move(horizontalDistance,getAttitude());
        routePoint.focus(target);
        routePoint.setActiveTime(getHoverTime());

        ArrayList<RoutePoint> routePoints = new ArrayList<>(1);
        routePoints.add(routePoint);
        return routePoints;
    }
}
