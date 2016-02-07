package com.interfacecomparateur.map;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.Marker.OnMarkerClickListener;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.interfacecomparateur.R;
import com.interfacecomparateur.classes.Adresse;
import com.interfacecomparateur.classes.Localisation;
import com.interfacecomparateur.classes.Magasin;
import com.interfacecomparateur.classes.RequeteHTTP;

/**
 * <b>Activité d'affichage d'une carte OpenStreetMap</b>
 * 
 * Cette carte servira à afficher différents lieux selon le contexte :
 * 	- une adresse favorite de l'utilisateur
 * 	- un magasin favori de l'utilisateur
 *  - l'ensemble des magasins se trouvant à proximité de la position de l'utilisateur et qui ont un prix pour le produit recherché par celui-ci
 *  - un magasin particulier parmi les résultats de la recherche de magasins à proximité de la position de l'utilisateur, à proximité des adresses
 *    favorites de l'utilisateur et parmi les magasins favoris de l'utilisateur qui ont un prix pour le produit recherché par celui-ci.
 * 
 * @author Equipe L3I1
 *
 */
public class CarteActivity extends Activity implements OnMarkerClickListener {
	
	/**
	 * ArrayList des magasins trouvés à proximité de la position de l'utilisateur et ayant un prix enregistré
	 * pour le produit concerné
	 */
	private ArrayList<Magasin> magasins;
	/**
	 * ArrayList des informations complémentaires issues des résultats de la recherche
	 */
	private ArrayList<String> listInfos;
	/**
	 * Identifiant saisi par l'utilisateur
	 */
	private String identifiant;
	/**
	 * Code-barres (EAN13) du produit recherché
	 */
	private String eanProduct;
	/**
	 * Latitude de la position de l'utilisateur trouvée par géolocalisation
	 */
	private double userLatitude;
	/**
	 * Longitude de la position de l'utilisateur trouvée par géolocalisation
	 */
	private double userLongitude;
	
	private Activity activity = this;
	private Carte map;
	
	private OnMarkerClickListener click = (OnMarkerClickListener) this;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	   super.onCreate(savedInstanceState);
	   setContentView(R.layout.map_vue);
	   Intent intent = getIntent();
	   identifiant = intent.getStringExtra("id");
	   eanProduct = intent.getStringExtra("ean");
	   
	   RelativeLayout cadre = (RelativeLayout)findViewById(R.id.relative_layout_carte);
	   final Carte map = new Carte(this,cadre);
	   final Drawable markerDrawable = this.getResources().getDrawable(R.drawable.ic_map_marker);
	   
	   if (intent.hasExtra("address")) {
		  Adresse adresse = (Adresse)intent.getSerializableExtra("address");
		  Marker startMarker = new Marker(map);
		  GeoPoint addressPoint = new GeoPoint(adresse.getLatitude(),adresse.getLongitude());
		  startMarker.setPosition(addressPoint);
		  startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
		  startMarker.setIcon(markerDrawable);
		  startMarker.setTitle(adresse.toString());
		  map.getOverlays().add(startMarker);
		  map.setZoom(15);
		  map.setPosition(addressPoint);
		  map.invalidate();
	   }
	   else if (intent.hasExtra("shop")) {
	      Magasin magasin = (Magasin)intent.getSerializableExtra("shop");
		  Marker startMarker = new Marker(map);
		  GeoPoint shopPoint = new GeoPoint(magasin.getLatitude(),magasin.getLongitude());
		  startMarker.setPosition(shopPoint);
		  startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
		  startMarker.setIcon(markerDrawable);
		  startMarker.setTitle(magasin.getNom());
		  startMarker.setSubDescription(magasin.getSubDescription());
		  map.getOverlays().add(startMarker);
		  map.setZoom(15);
		  map.setPosition(shopPoint);
		  map.invalidate();
	   }
	   else if (intent.hasExtra("result")) {
		  Magasin magasin = (Magasin)intent.getSerializableExtra("shop_result");
		  double price = intent.getDoubleExtra("price",-1);
		  String isPromo = intent.getStringExtra("promo");
		  Marker startMarker = new Marker(map);
		  GeoPoint shopPoint = new GeoPoint(magasin.getLatitude(),magasin.getLongitude());
		  startMarker.setPosition(shopPoint);
		  startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
		  startMarker.setIcon(markerDrawable);
		  startMarker.setTitle(magasin.getNom());
		  startMarker.setSubDescription(magasin.getSubDescription());
		  startMarker.setSnippet("Distance : "+magasin.getDistance()+" m\n"+"Prix : "+price+"€ "+isPromo);
		  map.getOverlays().add(startMarker);
		  map.setZoom(15);
		  map.setPosition(shopPoint);
		  map.invalidate();
	   }
	   else {
		  final ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBar);
	      final Handler handler = new Handler() {
				  
	         @Override
			 public void handleMessage(Message msg) {
	            Marker shopMarker;
	            
		        for(int i=0;i<magasins.size();i++){
		           shopMarker = new Marker(map);
				   shopMarker.setPosition(magasins.get(i).getPosition());
				   shopMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
				   shopMarker.setIcon(markerDrawable);
				   shopMarker.setTitle(magasins.get(i).getNom());
				   shopMarker.setSubDescription(magasins.get(i).getSubDescription());
				   shopMarker.setSnippet(listInfos.get(i));
				   shopMarker.setOnMarkerClickListener(click);
				   map.getOverlays().add(shopMarker);

		        }
		        map.setZoom(14);
			    map.invalidate();
			 }
				   
		  };
		  Location location = Localisation.getLocation(this);
		  boolean networkAvailable = RequeteHTTP.isNetworkConnected(this);
		  if (location!=null && networkAvailable) {
		     map.activerLocalisation();
	         userLatitude = location.getLatitude();
	         userLongitude = location.getLongitude();
             magasins = new ArrayList<Magasin>();
             listInfos = new ArrayList<String>();
		     Thread thread = new Thread(new Runnable() {

	            @Override
	     	    public void run() {
	        	   Looper.prepare();
		           retrieveResultsAround();
		           handler.sendMessage(handler.obtainMessage());
	            }
	         
		     });
		     thread.start();
	      }
	      else if (location==null){
	         Toast.makeText(getApplicationContext(),"Echec de la géolocalisation",Toast.LENGTH_SHORT).show();
	      }
	      else {
	    	 Toast.makeText(getApplicationContext(),"Réseau non disponible",Toast.LENGTH_SHORT).show();
	      }
	   }
	         
	   ImageButton buttonBack = (ImageButton)findViewById(R.id.button_back);
	   buttonBack.setOnClickListener(new OnClickListener() {
				
	      public void onClick(View v) {
		     finish();
		  }
				
	   });
	}
	
	private void retrieveResultsAround() {
	   final TextView errorTextView = (TextView)findViewById(R.id.error_textview);
	   RequeteHTTP requete = new RequeteHTTP(getApplicationContext(),"/shop/near/product",new String[] {"ean","lat","lon"},new String[]{eanProduct,""+userLatitude,""+userLongitude},identifiant);
	   final JSONObject jsonDocument = requete.sendGetRequest();
	   JSONArray results=null;
	   String registered = null;
	   if (jsonDocument==null) {
		  runOnUiThread(new Runnable() {
			  
			  public void run() {
			     Toast.makeText(getApplicationContext(),"Echec lors de la recupération des résultats de type 'A proximité'",Toast.LENGTH_LONG).show();  
			  }
			  
		  });  
	   }
	   else {
	      try {
	 	     registered = jsonDocument.getString("registered");
	 	     if (jsonDocument.has("results")) {
	 	        results=jsonDocument.getJSONArray("results");
	 	     }
	 	     if (jsonDocument.has("err")) {
	 	    	runOnUiThread(new Runnable() {
		        	
	 		       public void run() {
	 			      try {
	 				     JSONArray errors = jsonDocument.getJSONArray("err");
	 				     errorTextView.setVisibility(View.VISIBLE);
	 					 StringBuffer sb = new StringBuffer();
	 					 for (int i=0;i<errors.length();i++)
	 				        sb.append(errors.get(i)+"\n");
	 					 errorTextView.setText(sb.toString());
	 			      }
	 				  catch(JSONException e) {
	 				     Log.d("Err",e.getMessage());
	 				  }
	 			   }
	 								           
	 			});
	 	     }
	      }
	      catch(JSONException e) {
	 	     Log.d("Err",e.getMessage());
	      }
	 	  if(registered.equals("success")) {
		     for (int i=0;i<results.length();i++) {
			    Magasin magasinTrouve=null;
			    try {
			       JSONObject resultJson = results.getJSONObject(i);
			       Log.d("Ouf1","Ouf1 : "+resultJson.toString());
			       magasinTrouve = new Magasin(resultJson.getString("shop_id"),resultJson.getString("shop_name"),
			        		                   resultJson.getString("shop_descriptive"),resultJson.getString("shop_type"),
			        		                   resultJson.getDouble("shop_latitude"),resultJson.getDouble("shop_longitude"),
			        		                   resultJson.getString("shop_house_num"),resultJson.getString("shop_road"),
			        		                   resultJson.getString("shop_town"),resultJson.getString("shop_postcode"));
			       magasins.add(magasinTrouve);
			       String distance = Math.round(resultJson.getDouble("distance")*100000)+" m";
			       double price = resultJson.getDouble("price");
			       String isPromo = (resultJson.getBoolean("ispromotion")) ? "(Promo)" : "";
			       listInfos.add("Distance : "+distance+"\n"+"Prix : "+price+"€ "+isPromo);
			    }
			    catch(JSONException e) {
				   Log.d("Err",e.getMessage());
				}
			 }
	 	  }
	   }
	}

	@Override
	public boolean onMarkerClick(Marker arg0, MapView arg1) {
			Itineraire it = new Itineraire(arg0,arg1,activity,userLatitude,userLongitude);
			
		
		return false;
	}
	public Carte getcarte(){
		return map;
	}
	   
}