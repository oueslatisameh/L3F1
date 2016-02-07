package com.interfacecomparateur.map;

import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.ViewGroup;

public class Carte extends MapView {

	private MapController controles;

	private String typeCarte = "Carte par défaut.";

	private MyLocationNewOverlay repereGPS;

	public final static OnlineTileSourceBase PAR_DEFAUT = TileSourceFactory.DEFAULT_TILE_SOURCE;

	public final static OnlineTileSourceBase MAPQUEST = TileSourceFactory.MAPQUESTOSM;

	public final static OnlineTileSourceBase SATELLITE = TileSourceFactory.MAPQUESTAERIAL;

	public final static OnlineTileSourceBase TRANSPORTS_PUBLIC = TileSourceFactory.PUBLIC_TRANSPORT;

	public final static OnlineTileSourceBase ROUTES = TileSourceFactory.ROADS_OVERLAY_NL;

	public Carte(Context contexte) {

		super(contexte, 256);

	}

	public Carte(Context arg0, AttributeSet arg1) {
		super(arg0, arg1);
	}

	public Carte(Context arg0, int arg1) {
		super(arg0, arg1);
	}

	public Carte(Context arg0, int arg1, ResourceProxy arg2) {
		super(arg0, arg1, arg2);
	}

	public Carte(Context arg0, int arg1, ResourceProxy arg2,
			MapTileProviderBase arg3) {
		super(arg0, arg1, arg2, arg3);
	}

	public Carte(Context arg0, int arg1, ResourceProxy arg2,
			MapTileProviderBase arg3, Handler arg4) {
		super(arg0, arg1, arg2, arg3, arg4);
	}

	public Carte(Context arg0, int arg1, ResourceProxy arg2,
			MapTileProviderBase arg3, Handler arg4, AttributeSet arg5) {
		super(arg0, arg1, arg2, arg3, arg4, arg5);
	}

	public Carte(Context contexte, ViewGroup cadre, boolean boutonsZoom, boolean zoomMultiTouch, int niveauZoom, double longitude, double latitude) {
		this(contexte);

		this.setBuiltInZoomControls(boutonsZoom);

		this.setMultiTouchControls(zoomMultiTouch);

		this.controles = (MapController) this.getController();

		this.controles.setZoom(niveauZoom);

		this.controles.setCenter(new GeoPoint(longitude, latitude));

		this.setLayoutParams(cadre.getLayoutParams());

		cadre.addView(this);

		repereGPS = new MyLocationNewOverlay(contexte, this);
		repereGPS.disableMyLocation(); // not on by default
		repereGPS.disableFollowLocation();
		repereGPS.setDrawAccuracyEnabled(true);
		repereGPS.runOnFirstFix(new Runnable() {
			public void run() {
				Carte.this.getController().animateTo(repereGPS
						.getMyLocation());
			}
		});

	}

	public Carte (Context contexte, ViewGroup cadre, boolean boutonsZoom, boolean zoomMultiTouch) {

		this(contexte, cadre, boutonsZoom, zoomMultiTouch, 17, 48.854338, 2.346707);

	}

	public Carte (Context contexte, ViewGroup cadre) {

		this(contexte, cadre, false, true);

	}

	public void setZoom (int niveauZoom) {

		this.controles.setZoom(niveauZoom);

	}

	public int getZoom () {

		return this.getZoomLevel();

	}

	public void setPosition(GeoPoint point) {

		this.controles.setCenter(point);

	}

	public void setPosition(double longitude, double latitude) {

		this.setPosition(new GeoPoint(longitude, latitude));

	}

	public GeoPoint getPosition () {

		return (GeoPoint) this.getMapCenter();

	}

	public void setLongitude (double longitude) {

		this.setPosition(longitude, this.getLatitude());

	}

	public void setLatitude (double latitude) {

		this.setPosition(this.getLongitude(), latitude);
	}

	public double getLongitude () {

		return this.getPosition().getLongitude();

	}

	public double getLatitude () {

		return this.getPosition().getLatitude();

	}

	public void setTypeCarte(OnlineTileSourceBase type) {

		this.setTileSource(type);

		if (type == this.PAR_DEFAUT) {
			this.typeCarte = "Carte par defaut.";
		}
		else if (type == this.MAPQUEST) {
			this.typeCarte = "Carte MAPQUEST.";
		}
		else if (type == this.SATELLITE) {
			this.typeCarte = "Carte satellite.";
		}
		else if (type == this.TRANSPORTS_PUBLIC) {
			this.typeCarte = "Carte des transports publics.";
		}
		else if (type == this.ROUTES) {
			this.typeCarte = "Carte des routes.";
		}
		else {
			this.typeCarte = "Autre carte.";
		}

	}

	public String getTypeCarte () {

		return typeCarte;

	}

	public void autoriserInternet(boolean autorisation) {

		this.setUseDataConnection(autorisation);

	}
	


	public void activerLocalisation () {

		this.getOverlays().add(repereGPS);
		repereGPS.enableMyLocation();
		repereGPS.enableFollowLocation();

	}

	public void desactiverLocalisation () {

		this.getOverlays().remove(repereGPS);
		repereGPS.disableMyLocation();
		repereGPS.disableFollowLocation();

	}


}
