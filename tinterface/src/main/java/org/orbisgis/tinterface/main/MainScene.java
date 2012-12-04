package org.orbisgis.tinterface.main;

import java.awt.Window;

import org.mt4j.MTApplication;
import org.mt4j.components.MTComponent;
import org.mt4j.input.gestureAction.TapAndHoldVisualizer;
import org.mt4j.input.inputProcessors.IGestureEventListener;
import org.mt4j.input.inputProcessors.MTGestureEvent;
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragEvent;
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragProcessor;
import org.mt4j.input.inputProcessors.componentProcessors.scaleProcessor.ScaleProcessor;
import org.mt4j.input.inputProcessors.componentProcessors.tapAndHoldProcessor.TapAndHoldEvent;
import org.mt4j.input.inputProcessors.componentProcessors.tapAndHoldProcessor.TapAndHoldProcessor;
import org.mt4j.input.inputProcessors.globalProcessors.CursorTracer;
import org.mt4j.sceneManagement.AbstractScene;
import org.mt4j.util.math.Vector3D;

/**
 * This class create the main scene from the application. The map, the layer
 * list and the temporal line are added as son from this scene
 * 
 * @author patrick
 * 
 */
public class MainScene extends AbstractScene {

	/**
	 * The MTApplication
	 */
	private MTApplication mtApplication;
	
	/**
	 * The map
	 */
	private Map map;

	/**
	 * The layer list
	 */
	private LayerList layerList;
	
	private int compteur;
	private Vector3D vect;

	/**
	 * The temporal line
	 */
	// private TemporalLine temporalLine;

	/**
	 * Constructor of MainScene
	 * 
	 * @param mtApplication
	 *            the application
	 * @param name
	 *            the name of the scene
	 * @throws Exception
	 */
	public MainScene(MTApplication mtApplication, String name) {
		super(mtApplication, name);
		
		this.mtApplication = mtApplication;

		compteur =0;
		vect = new Vector3D(0, 0);
		// Add a circle around every point that is touched
		this.registerGlobalInputProcessor(new CursorTracer(mtApplication, this));

		// Instantiate a new map with the default configuration (specify in a
		// configuration file) and add it to the scene
		try {
			setMap(new Map(mtApplication, this));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Instantiate the list of Layers
		setLayerList(new LayerList(this, mtApplication));

		// Instantiate a new temporal line and add it to the scene
		// temporalLine = new TemporalLine();
		// this.getCanvas().addChild(temporalLine);

		// Add the drag gesture on the map (the class MapDrag will be used)
		map.registerInputProcessor(new DragProcessor(mtApplication));
		map.addGestureListener(DragProcessor.class, new MapDrag());

		// Add the scale gesture on the map (the class MapScale will be used)
		map.registerInputProcessor(new ScaleProcessor(mtApplication));
		map.addGestureListener(ScaleProcessor.class, new MapScale());

		// Add the tap and hold gesture on the map (with 1s hold)(the class
		// MapTapAndHold will be used)
		TapAndHoldProcessor tap = new TapAndHoldProcessor(mtApplication);
		tap.setHoldTime(1000);
		map.registerInputProcessor(tap);
		map.addGestureListener(TapAndHoldProcessor.class, new MapTapAndHold());
		
		//Creation of a visualizer for the tap and hold gesture
		TapAndHoldVisualizer visualizer = new TapAndHoldVisualizer(mtApplication, this.getCanvas());
		map.addGestureListener(TapAndHoldProcessor.class, visualizer);

	}

	public Map getMap() {
		return map;
	}

	public void setMap(Map map) {
		this.map = map;
	}
	
	public LayerList getLayerList() {
		return layerList;
	}

	public void setLayerList(LayerList layerList) {
		this.layerList = layerList;
	}

	/**
	 * Nested class used when a drag gesture on the map is detected
	 * 
	 * @author patrick
	 * 
	 */
	private class MapDrag implements IGestureEventListener {

		/**
		 * Method called when a drag gesture is detected
		 */
		public boolean processGestureEvent(MTGestureEvent gesture) {
			compteur++;				
			// Get the translation vector
			Vector3D tVect = ((DragEvent) gesture).getTranslationVect();
			vect = vect.addLocal(tVect);
			if (compteur>2){
				// Move the map
				map.move(vect.x, vect.y);
				System.out.println("move");
				compteur=0;
				vect.setX(0);
				vect.setY(0);
			}
			
			//Move all the children of the map (the tooltips)
			MTComponent[] children = map.getChildren();
			int i;
			for (i=0; i<children.length; i++){
				children[i].translateGlobal(tVect);
			}

			return false;
		}
	}

	/**
	 * Nested class used when a scale gesture on the map is detected
	 * 
	 * @author patrick
	 * 
	 */
	private class MapScale implements IGestureEventListener {

		/**
		 * Method called when a scale gesture is detected
		 */
		public boolean processGestureEvent(MTGestureEvent gesture) {
			// Scale the map
			System.out.println("scale");
			return false;
		}
	}

	/**
	 * Nested class used when a tap and hold gesture on the map is detected
	 * 
	 * @author patrick
	 * 
	 */
	private class MapTapAndHold implements IGestureEventListener {

		/**
		 * Method called when a tap and hold gesture is detected
		 */
		public boolean processGestureEvent(MTGestureEvent gest) {
			TapAndHoldEvent gesture = (TapAndHoldEvent) gest;
			//Check if the gesture is finished and if it was completed
			if (gesture.getId() == MTGestureEvent.GESTURE_ENDED && gesture.isHoldComplete()) {
				//Get the vector corresponding to the position of the start of the tap and hold gesture
				Vector3D vector = new Vector3D(gesture.getCursor().getStartPosX(), gesture.getCursor().getStartPosY());

				//Get the informations about this position
				String infos = map.getInfos(vector);
				Tooltip tooltip = new Tooltip(mtApplication, vector, infos);
				map.addChild(tooltip);
			}
			return false;
		}
	}
}