package es.p32gocamuco.tfgdrone4.RecordingRoute;

import android.location.Location;
import android.support.annotation.FloatRange;
import android.support.annotation.Nullable;

import java.io.Serializable;

import static java.lang.Math.atan;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static java.lang.Math.toDegrees;

/**
 * RoutePoints are the points a drone will go through in a route.
 * A RoutePoint contains information about the drone's position, such as latitude, longitude and height over
 * the takeoff point.
 * A RoutePoint also contains information about the timing, having a set for how much should it take
 * to go from the previous RoutePoint to this one and how much time should it hover in this one.
 * It contains bearing, pitch and distance to point of interest information, in order to set
 * the camera.
 * It defines the action taken at this point as well.
 * @author Manuel Gomez Castro
 */

public class RoutePoint extends Target implements Serializable{
    @FloatRange(from=-90.0,to=0.0)
    private double pitch;

    private double bearing;
    private double focalDistance;
    private Actions action;
    private Target associatedTarget;

    //region Constructors

    /**
     * Empty constructor that will create a basic RoutePoint
     */
    public RoutePoint(){
        super();
        this.pitch = 0;
        this.bearing = 0;
        this.focalDistance = 0;
        this.action = Actions.NOTHING;
        this.associatedTarget = null;
    }

    /**
     * Basic constructor to build a RoutePoint from a Target
     * The RoutePoint will have the same location as the target and initialize the rest of its fields.
     * @param target Target in wich this RoutePoint is basesd.
     * @see Target
     */
    public RoutePoint(Target target){
        super(target.getLatLng(),target.getHeight(),target.getTravelTime());
        this.pitch = 0;
        this.bearing = 0;
        this.focalDistance = 0;
        this.action = Actions.NOTHING;
        this.associatedTarget = target;
    }
    //endregion

    //region Getters

    /**
     * Getter for the pitch of the camera.
     * The pitch will be a value between 0 and -90 degrees
     * @return Pitch angle in degrees, between 0 and -90.
     */
    public double getPitch() {
        return pitch;
    }

    /**
     * Getter for the bearing of the camera.
     * The bearing represents the orientation of the camera relative to true north.
     * 0 is North, 90 is East, -90 is West...
     * @return Bearing relative to true north in degrees.
     */
    public double getBearing() {
        return bearing;
    }

    /**
     * Getter for the focal distance of the camera.
     * This represents the distance between the camera and the point of interest.
     * @return Focal distance in meters.
     */
    public double getFocalDistance() {
        return focalDistance;
    }

    /**
     * Getter for the action taken at this RoutePoint
     * @return Action taken.
     * @see Actions
     */
    public Actions getAction() {
        return action;
    }

    /**
     * This method returns the Target associated to this RoutePoint.
     * The associated target is the point of interest of this RoutePoint and the element that has
     * prompted its generation.
     * @return Associated Target.
     */
    public Target getAssociatedTarget() {
        return associatedTarget;
    }

    //endregion

    //region Setters


    /**
     * Sets the associated target.
     * The target can be deleted by setting to null.
     * @param associatedTarget Associated target.
     */
    public void setAssociatedTarget(@Nullable Target associatedTarget) {
        this.associatedTarget = associatedTarget;
    }

    /**
     * Sets the pitch of the camera.
     * @param pitch Pitch of the camera in degrees, between 0 and -90
     */
    public void setPitch(double pitch) {
        this.pitch = pitch;
        if (getOnChangeListener() != null){
            getOnChangeListener().onChange(this);
        }
    }

    /**
     * Sets the bearing of the camera.
     * The bearing is defined relative to true north, defining positive values towards east.
     * @param bearing Bearing of the camera in degrees.
     */
    public void setBearing(double bearing) {
        this.bearing = bearing;
        if (getOnChangeListener() != null){
            getOnChangeListener().onChange(this);
        }
    }

    /**
     * Sets the focal distance of the camera.
     * We understand as focal distance the distance between this RoutePoint and the point of interest.
     * @param focalDistance Focal Distance in meters.
     */
    public void setFocalDistance(double focalDistance) {
        this.focalDistance = focalDistance;
        if (getOnChangeListener() != null){
            getOnChangeListener().onChange(this);
        }
    }

    /**
     * Sets the action of the aircraft in this routepoint.
     * @see Actions
     * @param action Action to be taken.
     */
    public void setAction(Actions action) {
        this.action = action;
        if (getOnChangeListener() != null){
            getOnChangeListener().onChange(this);
        }
    }

    //endregion

    /**
     * Calculates the difference in height between this RoutePoint and the specified target.
     * Positive values means that the RoutePoint is over the Target.
     * @param target Target of interest.
     * @return Height over the Target of interest in meters.
     * @see Target
     */
    public double calculateHeightOver(Target target){
        return this.getHeight() - target.getHeight();
    }

    /**
     * Calculates the distance between this RoutePoint and the Target of interest as if projected
     * against the surface of the earth.
     * @param target Target of interest.
     * @return Horizontal distance in meters.
     */
    public double calculateHorizontalDistanceTo(Target target){
        float[] result = new float[1];
        Location.distanceBetween(this.getLatitude(),this.getLongitude(),target.getLatitude(),target.getLongitude(),result);
        return result[0];
    }

    /**
     * Calculates the focal distance towards a Target.
     * The focal distance is defined as the hypotenuse of the triangle composed by the height difference
     * and the horizontal distance.
     * @param target Target of interest.
     * @return Focal distance in meters.
     */
    public double calculateFocalDistanceTo(Target target){
        double heightDiff = calculateHeightOver(target);
        double horizontalDistance = calculateHorizontalDistanceTo(target);
        return sqrt(pow(heightDiff,2)+pow(horizontalDistance,2));
    }

    /**
     * Calculates the bearing needed so the camera points towards the Target.
     * The bearing is calculated as the start bearing of the shortest route in the surface of the earth
     * between this RoutePoint and the Target.
     * If both points have the same coordinates, the bearing will be 0.
     * The bearing is defined relative to true north at the RoutePoint, over long distances or close to the poles,
     * this value can be significantly different from the bearing measured from the Target, and it can
     * lead to issues when plotting it in a map, as the straight line between two points in a map may
     * differ from the shortest distance in the spherical surface.
     * @param target Target of interest.
     * @return Bearing in degrees relative to true north at the RoutePoint.
     */
    public double calculateBearingTowards(Target target){
        float[] results = new float[2];
        if (this.getLatLng().equals(target.getLatLng())){
            return 0;
        } else {
            Location.distanceBetween(this.getLatitude(), this.getLongitude(), target.getLatitude(), target.getLongitude(), results);
            return results[1];
        }
    }

    /**
     * Calculates the pitch needed so the camera points towards the Target.
     * If the Target is over the RoutePoint, the pitch will be 0.
     * If the Target has the same coordinates as the RoutePoint and is under it, it will be -90.
     * In any other case, the pitch will be the angle from the square triangle composed by the
     * height over target and the horizontal distance.
     * @param target Target of interest.
     * @return Pitch in degrees in a range of 0 to -90
     */
    public double calculatePitchTowards(Target target){
        double height = calculateHeightOver(target);
        if (height <= 0){
            return 0;
        } else {
            double horizontalDistance = calculateHorizontalDistanceTo(target);
            if (horizontalDistance == 0){
                return -90;
            } else {
                double pitch = -toDegrees(atan(height/horizontalDistance));
                return pitch;
            }
        }
    }


    /**
     * This method modifies the pitch, bearing and focus distance of this RoutePoint so it points
     * towards the Target.
     * @param target Target of interest.
     */
    public void focus(Target target){
        this.focalDistance = calculateFocalDistanceTo(target);
        this.bearing = calculateBearingTowards(target);
        this.pitch = calculatePitchTowards(target);
        if (getOnChangeListener() != null){
            getOnChangeListener().onChange(this);
        }
    }

    /**
     * List of possible actions for a RoutePoint to take.
     */
    public enum Actions{
        START_RECORDING,
        STOP_RECORDING,
        TAKE_IMAGE,
        NOTHING
    }
}
