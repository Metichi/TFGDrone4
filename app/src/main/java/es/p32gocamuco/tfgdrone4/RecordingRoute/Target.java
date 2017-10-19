package es.p32gocamuco.tfgdrone4.RecordingRoute;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import java.io.Serializable;

/**
 * Targets are points of interest in a recording route and will be used along the RecordingTechniques
 * to generate the RoutePoints in our route.
 * A target is a 0 dimensional object that represents a position in the globe, its defined by its latitude,
 * longitude, and height over the takeoff point.
 * Each target will have a travel time, wich indicates the time it should take to go from the last
 * active target to this one.
 * Each target will also have an active time, that should be exactly the length as the sume of all the active
 * times of its associated RoutePoints and the travel time between them.
 *
 * @author Manuel Gómez Castro
 * @version %I%, %G%
 */

public class Target implements Serializable {
    private double latitude;
    private double longitude;
    private double height;
    private double activeTime;
    private double travelTime;
    private OnChangeListener onChangeListener;

    //region Constructors
    /**
     * Empty constructor that will create a basic target.
     */
    public Target(){
        this.latitude = 0;
        this.longitude = 0;
        this.height = 0;
        this.activeTime = 0;
        this.travelTime = 0;
        this.onChangeListener = null;
    }

    /**
     * Basic constructor that creates a target at a specified position, height, and with a travel time.
     * @param latLng Position of the target.
     * @param height Height over the takeoff point.
     * @param travelTime Time taken to travel since the previous target.
     * @see LatLng
     */
    public Target(LatLng latLng, double height, double travelTime){
        this.latitude = latLng.latitude;
        this.longitude = latLng.longitude;
        this.height = height;
        this.travelTime = travelTime;
        this.activeTime = 0;
        this.onChangeListener = null;
    }
    //endregion

    //region Getters

    /**
     * Getter for latitude.
     * @return Latitude of this target.
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Getter for longitude.
     * @return longitude of this target.
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Getter for coordinates
     * @return Latitude and Longitude as a LatLng object.
     * @see LatLng
     */
    public LatLng getLatLng(){
        return new LatLng(latitude,longitude);
    }

    /**
     * Getter for height.
     * The height of a target is measured relative to the takeoff position of a drone.
     * @return Height of this target in meters.
     */
    public double getHeight() {
        return height;
    }

    /**
     * Getter for the Active time.
     * The active time represents the time this target will be the object of attention in a route.
     * @return Active time for this target in seconds.
     */
    public double getActiveTime() {
        return activeTime;
    }

    /**
     * Getter for travel time.
     * The travel time represents the time it should take an aircraft to fly from the last routepoint
     * of the previous active target to the first routepoint of this target.
     * @return Travel time of this target in seconds.
     */
    public double getTravelTime() {
        return travelTime;
    }

    protected OnChangeListener getOnChangeListener() {
        return onChangeListener;
    }

    //endregion
    //region Setters

    /**
     * Sets latitude for this target.
     * @param latitude Latitude of this target.
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
        if (onChangeListener != null){
            onChangeListener.onChange(this);
        }
    }

    /**
     * Sets longitude for this target.
     * @param longitude Longitude for this target.
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
        if (onChangeListener != null){
            onChangeListener.onChange(this);
        }
    }

    /**
     * Sets coordinates for this target.
     * @param latLng Coordinates for this target.
     * @see LatLng
     */
    public void setLatLng(LatLng latLng){
        this.latitude = latLng.latitude;
        this.longitude = latLng.longitude;
        if (onChangeListener != null){
            onChangeListener.onChange(this);
        }
    }

    /**
     * Sets height for this target.
     * Height is defined relative to the takeoff position of the drone.
     * @param height Height of this target in meters.
     */
    public void setHeight(double height) {
        this.height = height;
        if (onChangeListener != null){
            onChangeListener.onChange(this);
        }
    }

    /**
     * Sets the active time for this target.
     * The active time represents the time this target is the point of interest in a route.
     * @param activeTime Active time in seconds.
     */
    public void setActiveTime(double activeTime) {
        this.activeTime = activeTime;
        if (onChangeListener != null){
            onChangeListener.onChange(this);
        }
    }

    /**
     * Sets the travel time for this target.
     * The travel time represents the time it takes to go from the last RoutePoint of the previous
     * target to the first RoutePoint of this target.
     * @param travelTime Travel time in seconds.
     */
    public void setTravelTime(double travelTime) {
        this.travelTime = travelTime;
        if (onChangeListener != null){
            onChangeListener.onChange(this);
        }
    }

    /**
     * Sets the OnChangeListener for this target.
     * @param onChangeListener Implementation of OnChangeListener.
     * @see OnChangeListener
     */
    public void setOnChangeListener(OnChangeListener onChangeListener) {
        this.onChangeListener = onChangeListener;
    }

    //endregion

    /**
     * This method displaces the object a distance in a specified direction.
     * @param distance Distance of displacement in meters.
     * @param heading Direction of displacement, being 0 true north and counting clockwise.
     */
    public void move(double distance, double heading){
        LatLng newPosition = SphericalUtil.computeOffset(this.getLatLng(),distance,heading);
        this.setLatLng(newPosition);
        if (onChangeListener != null){
            onChangeListener.onChange(this);
        }
    }

    /**
     * This interface defines a Listener for any changes made in the object after its creation.
     */
    public interface OnChangeListener{
        /**
         * This method will be called whenever a setter of this object is called.
         * @param target This object.
         */
        void onChange(Target target);
    }
}
