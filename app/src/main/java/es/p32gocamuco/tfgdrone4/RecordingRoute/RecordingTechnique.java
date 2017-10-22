package es.p32gocamuco.tfgdrone4.RecordingRoute;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Generic class to define any given Recording Technique.
 * A recording technique is defined as a collection of Targets, Routepoints, and the methods to link
 * them to each other and to generate the RoutePoints.
 * Recording techniques will define specific flight paths and positions for the drone unique to each
 * of the techniques.
 * Each technique will implement the Target.OnChangeListener in order to respond to changes that might
 * affect the route.
 * @author Manuel Gomez Castro
 * @see Target
 * @see RoutePoint
 */

public abstract class RecordingTechnique implements Serializable, Target.OnChangeListener{
    protected ArrayList<Target> targets;
    protected ArrayList<RoutePoint> routePoints;
    protected OnTechniqueChangeListener listener;
    private HashMap<Target,ArrayList<RoutePoint>> targetRoutePointsHashMap;

    /**
     * Basic constructor for a Recording Technique.
     * The basic constructor will take no arguments and will simply initialize the fields for storing
     * the targets, routePoints and listener.
     * Constructor for specific Recording Techniques should call upon this constructor and also initialize
     * its specific fields for route generation.
     */
    public RecordingTechnique(){
        this.targets = new ArrayList<>(0);
        this.routePoints = new ArrayList<>(0);
        this.listener = null;
    }

    /**
     * This method will generate a list of RoutePoints that correspond to the given target.
     * Each Technique should implement this method to decide exactly how the RoutePoints are arranged.
     * The points calculated by this method should not be included in the route yet, but be simply
     * returned to the caller.
     * @param target Target of interest.
     * @return Associated RoutePoints.
     */
    abstract public ArrayList<RoutePoint> calculateRoutePointsOf(Target target);

    //region Add
    /**
     * This method adds a target to the technique.
     * Once the target is added, its correspondent RoutePoints are also calculated and added to the
     * Route using {@link RecordingTechnique#addRoutePoint(RoutePoint)}.
     * The target is added to the end of the target list. Its active time will be set to match that
     * of its RoutePoints.
     * The method {@link OnTechniqueChangeListener#onNewTarget(Target)} will be called
     * if a listener has been set.
     * This technique will be set as the Targets {@link es.p32gocamuco.tfgdrone4.RecordingRoute.Target.OnChangeListener}
     *
     * @param target Target to add.
     * @see RecordingTechnique#getTargets()
     */
    public void addTarget(Target target){
        this.targets.add(target);
        ArrayList<RoutePoint> routePoints = calculateRoutePointsOf(target);
        this.targetRoutePointsHashMap.put(target,routePoints);

        for (RoutePoint routePoint : routePoints){
            addRoutePoint(routePoint);
            routePoint.setAssociatedTarget(target);
        }

        double activeTime = calculateActiveTimeOf(target);
        target.setActiveTime(activeTime);

        target.setOnChangeListener(this);

        if(this.listener != null){
            this.listener.onNewTarget(target);
        }
    }

    /**
     * This method adds a RoutePoint to the technique.
     * The Routepoint will be added to the end of the RoutePoint list.
     * The method {@link OnTechniqueChangeListener#onNewRoutePoint(RoutePoint)} will be called if a
     * listener is set.
     * This technique will be set as the RoutePoint's {@link es.p32gocamuco.tfgdrone4.RecordingRoute.Target.OnChangeListener}
     * @param routePoint RoutePoint to add.
     */
    public void addRoutePoint(RoutePoint routePoint){
        this.routePoints.add(routePoint);
        routePoint.setOnChangeListener(this);
        if(this.listener != null){
            this.listener.onNewRoutePoint(routePoint);
        }
    }

    /**
     * Adds a RoutePoint at a specified point of the technique.
     * This method is identical to {@link RecordingTechnique#addRoutePoint(RoutePoint)} but it places
     * the Routepoint in any entry of the list.
     * @param routePoint RoutePoint to add.
     * @param index Index.
     */
    public void addRoutePoint(RoutePoint routePoint, int index){
        this.routePoints.add(index, routePoint);
        routePoint.setOnChangeListener(this);
        if(this.listener != null){
            this.listener.onNewRoutePoint(routePoint);
        }
    }
    //endregion

    //region Remove

    /**
     * Removes a target from the technique.
     * Calling this method will also remove its associated RoutePoints.
     * Calling this method will trigger {@link OnTechniqueChangeListener#onTargetDeleted(Target)} if
     * the listener is defined.
     * @param target Target to delete.
     */
    public void removeTarget(Target target){
        ArrayList<RoutePoint> routePoints = getRoutePoints(target);
        this.targets.remove(target);
        this.targetRoutePointsHashMap.remove(target);
        for (RoutePoint routePoint: routePoints){
            removeRoutePoint(routePoint);
        }
        if (this.listener != null){
            this.listener.onTargetDeleted(target);
        }
    }

    /**
     * Removes a RoutePoint from the technique.
     * Calling this method will remove the RoutePoint from both the RoutePoint general list and its
     * association to a target.
     * Calling this method will trigger {@link OnTechniqueChangeListener#onRoutePointDeleted(RoutePoint)} if
     * the listener is defined.
     * @param routePoint RoutePoint to delete.
     */
    public void removeRoutePoint(RoutePoint routePoint){
        this.routePoints.remove(routePoint);
        ArrayList<RoutePoint> routePoints = getRoutePoints(routePoint.getAssociatedTarget());
        routePoints.remove(routePoint);
        routePoint.setAssociatedTarget(null);

        if(this.listener != null){
            this.listener.onRoutePointDeleted(routePoint);
        }
    }
    //endregion

    //region Change

    /**
     * Implementation of the {@link es.p32gocamuco.tfgdrone4.RecordingRoute.Target.OnChangeListener}
     * interface.
     * This method will be called when one of the technique's targets or routePoints is edited in
     * any way.
     * Depending on wich kind of object it is, the apropiate method will be called.
     * @param target This object.
     */
    public void onChange(Target target){
        if (target instanceof RoutePoint){
            onRoutePointChanged((RoutePoint) target);
            if (this.listener != null){
                this.listener.onRoutePointChanged((RoutePoint) target);
            }
        } else {
            onTargetChanged(target);
            if (this.listener != null){
                this.listener.onTargetChanged(target);
            }
        }
    }

    /**
     * Action to be taken if a target of the Technique is edited.
     * If a target is edited, all its associated RoutePoints will be deleted, new ones will be calculated,
     * and they will be put in place.
     * Its recommended that each Technique overrides this method with its own behaviour.
     * @param target Edited target.
     */
    public void onTargetChanged(Target target){
        ArrayList<RoutePoint> oldRoutePoints, newRoutePoints;
        oldRoutePoints = getRoutePoints(target);
        newRoutePoints = calculateRoutePointsOf(target);

        int index = getRoutePoints().indexOf(oldRoutePoints.get(0));
        for (RoutePoint routePoint : oldRoutePoints){
            removeRoutePoint(routePoint);
        }
        oldRoutePoints.addAll(newRoutePoints);

        for (RoutePoint routePoint : newRoutePoints){
            addRoutePoint(routePoint,index);
            index += 1;
        }

    }

    /**
     * Action taken if a RoutePoint of the technique is edited.
     * If a routePoint of the technique is edited, by default, the only action taken will be to
     * recalculate the related Target's active time.
     * Its recommended that each technique overrides this method with its own behaviour.
     * @param routePoint RoutePoint edited.
     */
    public void onRoutePointChanged(RoutePoint routePoint){
        Target associatedTarget = routePoint.getAssociatedTarget();
        double activeTime = calculateActiveTimeOf(associatedTarget);
        associatedTarget.setActiveTime(activeTime);
    }
    //endregion
    //region Getters
    /**
     *
     * @return Complete list of Targets of this technique.
     */
    public ArrayList<Target> getTargets() {
        return targets;
    }

    /**
     * @return Complete list of RoutePoints of this technique.
     */
    public ArrayList<RoutePoint> getRoutePoints() {
        return routePoints;
    }

    /**
     * Returns the RoutePoints associated to a Target.
     * @param target Target of interest.
     * @return RoutePoints associated.
     */
    public ArrayList<RoutePoint> getRoutePoints(Target target){
        return this.targetRoutePointsHashMap.get(target);
    }
    //endregion

    /**
     * Calculates the active time of a target.
     * The active time of a target is the sum of all the activeTimes and travelTimes of its associated
     * RoutePoints except for the travelTime of the first RoutePoint. Wich should be identical to the
     * target's travelTime.
     * @param target Target of interest.
     * @return Its calculated active time.
     */
    private double calculateActiveTimeOf(Target target){
        ArrayList<RoutePoint> routePoints = getRoutePoints(target);

        double activeTime = 0;
        for (RoutePoint r : routePoints){
            activeTime += r.getActiveTime();
            if (routePoints.indexOf(r) != 0){
                activeTime += r.getTravelTime();
            }
        }
        return activeTime;
    }
    /**
     * This interface defines a listener for any changes on the Technique.
     * The technique will change after its creation to add new Targets, generate new RoutePoints, or
     * modify them in any way.
     * This listener will provide methods useful for keeping a map representing these points up to
     * date.
     */
    public interface OnTechniqueChangeListener{
        void onNewTarget(Target target);
        void onTargetChanged(Target target);
        void onTargetDeleted(Target target);
        void onNewRoutePoint(RoutePoint routePoint);
        void onRoutePointChanged(RoutePoint routePoint);
        void onRoutePointDeleted(RoutePoint routePoint);
    }
}
