package es.p32gocamuco.tfgdrone4.RecordingRoute;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * This class represents a complete recording route.
 * A recording route is a collection of techniques in order to represent a flight path.
 * The route should manage global aspects that are not managed by each technique, like the actions
 * taken by the routepoints, or the constrains applied to the route.
 * The class becomes the listener for each of the techniques, but also provides a listening interface
 * for any program external to the Route to react to changes in the route.
 * @author Manuel Gomez Castro
 */

public class RecordingRoute implements Serializable, RecordingTechnique.OnTechniqueChangeListener {
    private RoutePoint home;
    private ArrayList<RecordingTechnique> recordingTechniques;
    private Constrains constrains;


    /**
     * This method calculates the minimum time it would take an aircraft to go between two routepoints
     * if it adheres to the constrains.
     * @param origin Start point.
     * @param destination End point.
     * @param constrains Constrains to follow.
     * @return Minimum travel time between the points in seconds.
     */
    public double minTravelTimeBetween(RoutePoint origin, RoutePoint destination, Constrains constrains){
        double minTime = 0;
        //Min time because of travel distance
        double distance = origin.calculateFocalDistanceTo(destination);
        double distanceTime = distance/constrains.getMaxSpeed();
        minTime = Math.max(minTime,distanceTime);

        //Min time because of bearing
        double bearingChange = origin.calculateRotationDistanceTo(destination);
        double bearingTime = bearingChange/constrains.getMaxBearingSpeed();
        minTime = Math.max(minTime,bearingTime);

        //Min time because of the pitch
        double pitchChange = Math.abs(origin.getPitch()-destination.getPitch());
        double pitchTime = pitchChange/constrains.getMaxPitchSpeed();
        minTime = Math.max(minTime,pitchTime);

        return minTime;
    }


    /**
     * This interface is built to manage the representation of the coordinated elements in a map.
     * Each of these methods will be called if a listener is set, and should manage the map changes.
     */
    public interface MapChangeListener{
        void onNewTarget(Target target);
        void onTargetChanged(Target target);
        void onTargetRemoved(Target target);

        void onNewRoutePoint(RoutePoint routePoint);
        void onRoutePointChanged(RoutePoint routePoint);
        void onRoutePointRemoved(RoutePoint routePoint);
    }

    /**
     * This class is used to hold the constrains on a Route.
     * The constrains limit the maximum speed of the aircraft
     */
    public class Constrains{
        private double maxSpeed;
        private double maxPitchSpeed;
        private double maxBearingSpeed;
        private double minHeight;
        private double maxHeight;
        private double maxDistance;

        public Constrains(double maxSpeed, double maxPitchSpeed, double maxBearingSpeed, double minHeight, double maxHeight, double maxDistance){
            this.maxSpeed = maxSpeed;
            this.maxPitchSpeed = maxPitchSpeed;
            this.maxBearingSpeed = maxBearingSpeed;
            this.minHeight = minHeight;
            this.maxHeight = maxHeight;
            this.maxDistance = maxDistance;
        }

        /**
         * This method returns the maximum flight speed of the aircraft.
         * @return Flight speed of the drone in meters per second.
         */
        public double getMaxSpeed() {
            return maxSpeed;
        }

        /**
         * This method returns the Pitch change rate.
         * The pitch refers to the inclination of the camera.
         * @return Pitch change rate in degrees per second.
         */
        public double getMaxPitchSpeed() {
            return maxPitchSpeed;
        }

        /**
         * This method returns the bearing change rate.
         * The bearing refers to the orientation, relative to true north, of the camera.
         * @return bearing change rate in degrees per second.
         */
        public double getMaxBearingSpeed() {
            return maxBearingSpeed;
        }

        /**
         * This method returns the minimum flight height.
         * The flight height is measured relative to the takeoff point.
         * @return Minimum flight height in meters.
         */
        public double getMinHeight() {
            return minHeight;
        }

        /**
         * Returns the maximum flight height.
         * The flight height is measured relative to the takeoff point.
         * @return Maximum flight in meters.
         */
        public double getMaxHeight() {
            return maxHeight;
        }

        /**
         * This method returns the maximum distance allowed for the aircraft.
         * The distance is defined as the radius of a cilinder centered on the home point.
         * @return Maximum distance in meters.
         */
        public double getMaxDistance() {
            return maxDistance;
        }
    }
}
