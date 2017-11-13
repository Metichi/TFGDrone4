package es.p32gocamuco.tfgdrone4.RecordingRoute;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.ListIterator;

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
    private MapChangeListener listener;

    public RecordingRoute(RoutePoint home, Constrains constrains){
        this.home = home;
        this.constrains = constrains;
        this.recordingTechniques = new ArrayList<>();
        this.listener = null;

    }


    //region Setters

    public void setConstrains(Constrains constrains) {
        this.constrains = constrains;
    }

    public void setHome(RoutePoint home) {
        this.home = home;
    }

    public void setMapChangeListener(MapChangeListener listener) {
        this.listener = listener;
    }

    //endregion

    /**
     * Adds a new technique to the end of the technique list.
     * The RoutePoints in this technique will be placed in order at the end of the route.
     * Should this technique have Targets or RoutePoints already, and if the MapChangeListener has
     * been set, the methods {@link MapChangeListener#onNewRoutePoint(RoutePoint)} and {@link MapChangeListener#onNewTarget(Target)}
     * will be called for each of them.
     * @param technique Technique to add to the route.
     */
    public void addTechnique(RecordingTechnique technique){
        recordingTechniques.add(technique);
        if(listener != null) {
            ArrayList<Target> targets = technique.getTargets();
            if(!targets.isEmpty()){
                for(Target target : targets){
                    listener.onNewTarget(target);
                }
            }

            ArrayList<RoutePoint> routePoints = technique.getRoutePoints();
            if(!routePoints.isEmpty()){
                for(RoutePoint routePoint : routePoints){
                    listener.onNewRoutePoint(routePoint);
                }
            }
        }
        technique.setListener(this);
    }

    /**
     * This method calculates the minimum time it would take an aircraft to go between two routepoints
     * if it adheres to the constrains.
     * @param origin Start point.
     * @param destination End point.
     * @return Minimum travel time between the points in seconds.
     */
    public double minTravelTimeBetween(RoutePoint origin, RoutePoint destination){
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
     * Calculates the travel speed between two routepoints.
     * This method asumes the drone moves in a straight line between the two routepoints at a constant
     * speed.
     * The time of travel is considered as the traveltime of the destination.
     * The distance is obtained by calling {@link RoutePoint#getFocalDistance()}.
     * @param origin Routepoint of origin.
     * @param destination Routepoint of destination.
     * @return Speed of travel in meters per second.
     *
     */
    public static double getTravelSpeed(RoutePoint origin, RoutePoint destination){
        double distance = origin.calculateFocalDistanceTo(destination);
        double speed = distance/destination.getTravelTime();
        return speed;
    }

    /**
     * Returns a complete list of all the RoutePoints in the Route.
     * @return All RoutePoints contained in all techniques.
     */
    public ArrayList<RoutePoint> getAllRoutePoints(){
        ArrayList<RoutePoint> routePoints = new ArrayList<>();
        routePoints.add(this.home);
        for (RecordingTechnique technique : this.recordingTechniques){
            routePoints.addAll(technique.getRoutePoints());
        }
        return routePoints;
    }

    //region Action management

    /**
     * This method fixes the actions on the route.
     * All the routepoints are checked in order, and determines wether that routepoint is recording or not.
     * It is recording if a previoius routepoint has started a recording and no other Routepoint has stopped it,
     * the only actions aviable are "stop recording" and "do nothing".
     * If its not recording, then the aviable actions are "take a picture", "start recording" and "do nothing".
     */
    public void fixActions(){
        ArrayList<RoutePoint> routePoints = this.getAllRoutePoints();
        boolean isRecording = false;
        for (RoutePoint routePoint : routePoints){
            if (isRecording){
                switch (routePoint.getAction()){
                    case STOP_RECORDING:
                        isRecording = false;
                        break;
                    default:
                        routePoint.setAction(RoutePoint.Actions.NOTHING);
                }
            } else {
                switch (routePoint.getAction()){
                    case START_RECORDING:
                        isRecording = true;
                        break;
                    case STOP_RECORDING:
                        routePoint.setAction(RoutePoint.Actions.NOTHING);
                        break;
                }
            }
        }
        if (isRecording){
            RoutePoint lastRoutePoint = routePoints.get(routePoints.size()-1);
            lastRoutePoint.setAction(RoutePoint.Actions.STOP_RECORDING);
        }
    }

    /**
     * This method checks wether the provided routepoint is recording.
     * A point is recording if a previous routepoint started a recording and neither it or a routePoint
     * in between stopped it.
     * @param routePoint RoutePoint of Interest
     * @return Wether it is recording.
     */
    public boolean isRecording(RoutePoint routePoint){
        ArrayList<RoutePoint> routePoints = this.getAllRoutePoints();
        boolean isRecording = false;
        for (RoutePoint r : routePoints){
            if (isRecording){
                if (r.getAction() == RoutePoint.Actions.STOP_RECORDING){
                    isRecording = false;
                }
            } else {
                if(r.getAction() == RoutePoint.Actions.START_RECORDING){
                    isRecording = true;
                }
            }

            if(r == routePoint){
                break;
            }
        }
        return isRecording;
    }
    //endregion

    //region Interface implementation

    @Override
    public void onNewTarget(Target target, RecordingTechnique technique) {
        if (listener != null){
            listener.onNewTarget(target);
        }
    }

    @Override
    public void onTargetChanged(Target target, RecordingTechnique technique) {
        if (listener != null){
            listener.onTargetChanged(target);
        }
    }

    @Override
    public void onTargetDeleted(Target target, RecordingTechnique technique) {
        if (listener != null){
            listener.onTargetRemoved(target);
        }
    }

    @Override
    public void onNewRoutePoint(RoutePoint routePoint, RecordingTechnique technique) {
        RoutePoint previous = getAllRoutePoints().get(getAllRoutePoints().indexOf(routePoint)-1);
        double minTravelTime = this.minTravelTimeBetween(previous,routePoint);
        if (routePoint.getTravelTime()< minTravelTime){
            routePoint.setTravelTime(minTravelTime);
        }

        if (listener != null){
            listener.onNewRoutePoint(routePoint);
        }
    }

    @Override
    public void onRoutePointChanged(RoutePoint routePoint, RecordingTechnique technique) {


        if (listener != null){
            listener.onRoutePointChanged(routePoint);
        }
    }

    @Override
    public void onRoutePointDeleted(RoutePoint routePoint, RecordingTechnique technique) {
        if (listener != null){
            listener.onRoutePointRemoved(routePoint);
        }
    }

    @Override
    public void onTechniqueParametersChanged(RecordingTechnique recordingTechnique) {

    }
    //

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
     * The constrains limit the maximum speed of the aircraft, as well as the rate of change for its angular
     * parameters.
     * It also defines an allowed flight volume, the volume will be a cilinder centered on the home
     * position that spans from {@link Constrains#getMinHeight()} to {@link Constrains#getMaxHeight()}
     * and has a radius of {@link Constrains#getMaxDistance()}.
     */
    public class Constrains{
        private double maxSpeed;
        private double maxPitchSpeed;
        private double maxBearingSpeed;
        private double minHeight;
        private double maxHeight;
        private double maxDistance;

        /**
         * Basic constructor for the Constrains group.
         * @param maxSpeed Maximum speed allowed in a straight line in any direction in meters per second.
         * @param maxPitchSpeed Maximum rate of change of the pitch angle, in degrees per second.
         * @param maxBearingSpeed Maximum rate of change of the attitude, in degrees per second.
         * @param minHeight Minimum height the drone is allowed to fly at, in meters, relative to takeoff point.
         * @param maxHeight Maximum height the drone is allowed to fly at, in meters, relative to takeoff point.
         * @param maxDistance Maximum distance the drone is allowed to fly away from Home. In meters.
         */
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
