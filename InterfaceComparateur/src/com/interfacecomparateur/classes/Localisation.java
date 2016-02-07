package com.interfacecomparateur.classes;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

/**
 * Classe permettant de retrouver la position actuelle de l'utilisateur.
 * 
 * @author EquipeL3I1
 *
 */
public class Localisation {
	
	@SuppressWarnings("unused")
	private static double userLatitude;
	@SuppressWarnings("unused")
	private static double userLongitude;
	
	public static Location getLocation(Context context) {
	   LocationManager locationManager;
	   locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
	   Criteria criteria = new Criteria();
	   criteria.setAccuracy(Criteria.ACCURACY_COARSE);
	   String provider = locationManager.getBestProvider(criteria,false);
	   locationManager.requestSingleUpdate(provider, new LocationListener() {
				   
	      @Override
		  public void onLocationChanged(Location location) {
	    	 userLatitude = location.getLatitude();
	    	 userLongitude = location.getLongitude();
	      }

	      @Override
	      public void onProviderDisabled(String provider) {
		  }

		  @Override
		  public void onProviderEnabled(String provider) {
		  }

	      @Override
	      public void onStatusChanged(String provider, int status, Bundle extras) {
		  }
				   
	   }, null);
	   
	   return locationManager.getLastKnownLocation(provider);
	}

}
