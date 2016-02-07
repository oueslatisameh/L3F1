package com.interfacecomparateur.map;

import java.util.ArrayList;

import org.osmdroid.bonuspack.overlays.FolderOverlay;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.widget.Toast;

import com.example.interfacecomparateur.R;
import com.interfacecomparateur.classes.Localisation;

public class Itineraire  {
	
	private Marker arg0;
	private MapView mapView;
	private Activity activity;
	private double userlongitude;
	private double userlatitude;
	private CarteActivity c = new CarteActivity();
	
	public Itineraire(Marker arg0,MapView mapView, Activity activity,double userlongitude,double userlatitude) {
		this.mapView = mapView;
		this.arg0 = arg0;
		this.activity = activity;
		this.userlongitude = userlongitude;
		this.userlatitude = userlatitude;
		
		Route();
	}
	
	protected void Route() {
		Location location = Localisation.getLocation(activity);
        userlatitude = location.getLatitude();
        userlongitude = location.getLongitude();
		
		new Thread(new Runnable(){
			
			   public void run(){
				   
				 GeoPoint p= new GeoPoint(userlatitude,userlongitude);  
				    ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();
				    
		   // Routage montage
		   RoadManager roadManager= new OSRMRoadManager();
		   
		   // RoadManager
		   waypoints.add(p);
		   GeoPoint EndPoint = new GeoPoint(arg0.getPosition());
		   waypoints.add(EndPoint);
		   Road road = roadManager.getRoad(waypoints);
		   if(road.mStatus!=Road.STATUS_OK)
			   Toast.makeText(activity, "Erreur chargement rout -statue="+road.mStatus,Toast.LENGTH_SHORT).show();
		   Polyline roadOverlay = RoadManager.buildRoadOverlay(road, activity);
		   mapView.getOverlays().add(roadOverlay);
		   
		   // Affichage de la route
		   FolderOverlay roadMarkers = new FolderOverlay(activity);
		   mapView.getOverlays().add(roadMarkers);
		   Drawable nodeIcon= activity.getResources().getDrawable(R.drawable.marker_node);
		   for(int j=0; j<road.mNodes.size();j++){
			   RoadNode node = road.mNodes.get(j);
			   Marker nodeMarker=new Marker(mapView);
			   nodeMarker.setPosition(node.mLocation);
			   nodeMarker.setIcon(nodeIcon);
			   
			   // Remplissage
			   nodeMarker.setTitle("Etape "+j);
			   nodeMarker.setSnippet(node.mInstructions);
			   nodeMarker.setSubDescription(Road.getLengthDurationText(node.mLength, node.mDuration));
			   Drawable iconContinue = activity.getResources().getDrawable(R.drawable.ic_continue);
			   nodeMarker.setImage(iconContinue);
			   
			   // Fin
			   roadMarkers.add(nodeMarker);
		   }
		   }
		}).start();
	}
}
