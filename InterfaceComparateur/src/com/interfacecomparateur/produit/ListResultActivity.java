package com.interfacecomparateur.produit;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.interfacecomparateur.R;
import com.interfacecomparateur.classes.Localisation;
import com.interfacecomparateur.classes.Magasin;
import com.interfacecomparateur.classes.RequeteHTTP;
import com.interfacecomparateur.map.CarteActivity;

/**
 * <b>Activité d'affichage des prix enregistrés pour un produit dans les magasins à proximité de la position de l'utilisateur, à
 * proximité des adresses favorites de l'utilisateur, et dans les magasins favoris de l'utilisateur.</b>
 * 
 * @author Equipe L3I1
 *
 */
public class ListResultActivity extends ListActivity implements OnItemClickListener, OnClickListener {
	
	/**
	 * Identifiant (adresse e-mail) de l'utilisateur connecté.
	 */
	private String identifiant;
	/**
	 * Code-barres EAN-13 du produit consulté par l'utilisateur.
	 */
	private String ean;
	/**
	 * CustomAdapter permettant d'afficher les résultats (par catégorie)
	 * 
	 * @see CustomAdapter
	 */
	private CustomAdapter mAdapter;
	/**
	 * ArrayList des magasins se trouvant à proximité de la position de l'utilisateur et ayant un prix enregistré pour le produit.
	 */
	private ArrayList<Magasin> shopAround;
	/**
	 * ArrayList des magasins se trouvant à proximité d'une adresse favorite de l'utilisateur et ayant un prix enregistré pour le produit.
	 */
	private ArrayList<Magasin> shopAroundFavoriteAddress;
	/**
	 * ArrayList des magasins favoris ayant un prix enregistré pour le produit.
	 */
	private ArrayList<Magasin> favoriteShop;
	/**
	 * ArrayList des prix pour le produit dans les magasins à proximité de la position de l'utilisateur.
	 */
	private ArrayList<Double> prix_1;
	/**
	 * ArrayList des prix pour le produit dans les magasins à proximité d'une adresse favorite de l'utilisateur.
	 */
	private ArrayList<Double> prix_2;
	/**
	 * ArrayList des prix pour le produit dans les magasins favoris de l'utilisateur.
	 */
	private ArrayList<Double> prix_3;
	/**
	 * ArrayList contenant l'information qui permet de savoir si le produit est en promotion pour chaque magasin se 
	 * trouvant à proximité de la position de l'utilisateur et ayant un prix enregistré pour le produit.
	 */
	private ArrayList<String> promo_1;
	/**
	 * ArrayList contenant l'information qui permet de savoir si le produit est en promotion pour chaque magasin favori 
	 * de l'utilisateur ayant un prix enregistré pour le produit.
	 */
	private ArrayList<String> promo_2;
	/**
	 * ArrayList contenant l'information qui permet de savoir si le produit est en promotion pour chaque magasin se 
	 * trouvant à proximité de la position de l'utilisateur et ayant un prix enregistré pour le produit.
	 */
	private ArrayList<String> promo_3;
	/**
	 * Si vrai, les résultats sont triés par pri croissant, sinon les résultats dont triés par distance croissante (la distance est la distance calculée
	 * entre la position de l'utilisateur et l'emplacement du magasin considéré).
	 */
	private boolean triPrix=true;
	/**
	 * Vrai si la géolocalisation de la position de l'utilisateur a échouée, faux sinon.
	 */
	private boolean echecGeolocalisation=false;
	 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_results_vue);
        Intent intent = getIntent();
 	    identifiant = intent.getStringExtra("id");
 	    ean = intent.getStringExtra("ean");
 	    
        shopAround = new ArrayList<Magasin>();
        shopAroundFavoriteAddress = new ArrayList<Magasin>();
        favoriteShop = new ArrayList<Magasin>();
        prix_1 = new ArrayList<Double>();
        prix_2 = new ArrayList<Double>();
        prix_3 = new ArrayList<Double>();
        promo_1 = new ArrayList<String>();
        promo_2 = new ArrayList<String>();
        promo_3 = new ArrayList<String>();
        
        final Handler handler = new Handler() {
  		  
     	   @Override
     	   public void handleMessage(Message msg) {
     	      setListAdapter(mAdapter);
     	      ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBar);
     	      progressBar.setVisibility(View.GONE);
     	      ImageButton buttonSort = (ImageButton)findViewById(R.id.button_sort);
     	      buttonSort.setOnClickListener(ListResultActivity.this);
     	   }
     		   
     	};
        
        Thread thread = new Thread(new Runnable() {

     	   @Override
     	   public void run() {
     	      Looper.prepare();
     		  mAdapter = new CustomAdapter(ListResultActivity.this);
     		  
     		  mAdapter.addSectionHeaderItem("A proximité");
              retrieveResultsAround();
              organiseResults(1,triPrix);
             
              mAdapter.addSectionHeaderItem("Près de vos adresses favorites");
              retrieveResultsAroundFavoriteAddress();
              organiseResults(2,triPrix);
             
              mAdapter.addSectionHeaderItem("Vos magasins favoris");
              retrieveResultsFavoriteShop();
              sortByPrice(3);
     	      
     		  handler.sendMessage(handler.obtainMessage());
     	   }
     	   
        });
        thread.start();
        
        getListView().setOnItemClickListener(this);
        
        ImageButton buttonBack = (ImageButton)findViewById(R.id.button_back);
		buttonBack.setOnClickListener(new OnClickListener() {
				
		   public void onClick(View v) {
		      finish();
		   }
				
	    });
    }
    
    /**
	 * Méthode invoquée lorsque l'utilisateur clique sur l'un des items de la ListView.<br />
	 * Cette action lancera une nouvelle activité où sera affiché un marqueur indiquant 
	 * l'emplacement du magasin sur une carte, ainsi que certaines informations relatives à ce magasin dans une infobulle.
	 * 
	 * @author Equipe L3I1
	 * 
	 * @see OnItemClickListener
	 * @see CarteActivity
	 *
	 */
    public void onItemClick(AdapterView<?> arg0, View arg1,int arg2, long arg3) {
       Magasin magasin=null;
       double prix=-1;
       String isPromo=null;
	   if (arg2>0 && arg2<=shopAround.size() && shopAround.size()!=0) {
	      magasin = shopAround.get(arg2-1);
	      prix = prix_1.get(arg2-1);
	      isPromo = promo_1.get(arg2-1);
	   }
	   else if (arg2>shopAround.size()+1 && arg2<=shopAround.size()+shopAroundFavoriteAddress.size()+1 && shopAroundFavoriteAddress.size()!=0) {
		  magasin = shopAroundFavoriteAddress.get(arg2-shopAround.size()-2);
		  prix = prix_2.get(arg2-shopAround.size()-2);
	      isPromo = promo_2.get(arg2-shopAround.size()-2);
	   }
	   else if (arg2>shopAround.size()+shopAroundFavoriteAddress.size()+2 && favoriteShop.size()!=0) {
		  magasin = favoriteShop.get(arg2-shopAround.size()-shopAroundFavoriteAddress.size()-3);
		  prix = prix_3.get(arg2-shopAround.size()-shopAroundFavoriteAddress.size()-3);
	      isPromo = promo_3.get(arg2-shopAround.size()-shopAroundFavoriteAddress.size()-3);
	   }
	   if (magasin!=null) {
	      Intent intent = new Intent(ListResultActivity.this,CarteActivity.class);
	      intent.putExtra("result",true);
	      intent.putExtra("shop_result",magasin);
	      intent.putExtra("price",prix);
	      intent.putExtra("promo",isPromo);
	      startActivity(intent);
	   }
	}
    
    public void onClick(View v) {
       AlertDialog.Builder builderChoices = new AlertDialog.Builder(ListResultActivity.this);
	   CharSequence[] items =  {"Tri par prix","Tri par distance"};
       builderChoices.setItems(items, new DialogInterface.OnClickListener() {

	      @Override
		  public void onClick(DialogInterface dialog, int which) {
	         boolean active=false;
			 if (which==0) {
			    if (!triPrix) {
			       triPrix = !triPrix;
			       active=true;
			    }
			 }
			 else {
			    if (triPrix) {
			       triPrix = !triPrix;
			       active=true;
			    }
			 }
			 if (active) {
		        mAdapter = new CustomAdapter(ListResultActivity.this);
	            mAdapter.addSectionHeaderItem("A proximité");
	            organiseResults(1,triPrix);  
	            mAdapter.addSectionHeaderItem("Près de vos adresses favorites");
	            organiseResults(2,triPrix);
	            mAdapter.addSectionHeaderItem("Vos magasins favoris");
	            sortByPrice(3);
	            setListAdapter(mAdapter);
			 }
	      }
	         
	   });
	   builderChoices.show();
    }
    
    /**
     * Envoi d'une requête GET au serveur afin de récupérer les informations sur les magasins à proximité de la position de l'utilisateur qui
     * ont un prix enregistré pour le produit consulté
     */
    private void retrieveResultsAround() {
       Location userLocation = Localisation.getLocation(ListResultActivity.this);
       final TextView errorTextView = (TextView)findViewById(R.id.error_textview);
       if (userLocation!=null) {
    	  boolean networkAvailable = RequeteHTTP.isNetworkConnected(this);
 	      RequeteHTTP requete = new RequeteHTTP(getApplicationContext(),"/shop/near/product",new String[] {"ean","lat","lon"},new String[]{ean,""+userLocation.getLatitude(),""+userLocation.getLongitude()},identifiant);
 	      final JSONObject jsonDocument = requete.sendGetRequest();
 	      JSONArray results=null;
 	      String registered = null;
 	      if (!networkAvailable) {
 		     runOnUiThread(new Runnable() {
 			 		        	
 			    public void run() {
 			 	   Toast.makeText(getApplicationContext(),"Réseau non disponible",Toast.LENGTH_SHORT).show();
 			 	}
 			 			     
 		     });
 		  }
 	      else if (jsonDocument==null) {
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
		              magasinTrouve = new Magasin(resultJson.getString("shop_id"),resultJson.getString("shop_name"),
		        		                          resultJson.getString("shop_descriptive"),resultJson.getString("shop_type"),
		        		                          resultJson.getDouble("shop_latitude"),resultJson.getDouble("shop_longitude"),
		        		                          Math.round(resultJson.getDouble("distance")*100000),
		        		                          resultJson.getString("shop_house_num"),resultJson.getString("shop_road"),
		        		                          resultJson.getString("shop_town"),resultJson.getString("shop_postcode"));
		              shopAround.add(magasinTrouve);
		              prix_1.add(resultJson.getDouble("price"));
		              promo_1.add(resultJson.getBoolean("ispromotion") ? "(Promo)" : "");
		            }
		            catch(JSONException e) {
			           Log.d("Err",e.getMessage());
			        }
		         }
 	         }
 	      }
       }
       else {
    	  echecGeolocalisation = true;
          runOnUiThread(new Runnable() {
    		   
    	     public void run() {
    	        Toast.makeText(getApplicationContext(),"Echec de la géolocalisation",Toast.LENGTH_SHORT).show();
    		 }
    		   
    	  });
       }
    }
    
    /**
     * Envoi d'une requête GET au serveur afin de récupérer les informations sur les magasins à proximité d'une adresse favorite de l'utilisateur qui
     * ont un prix enregistré pour le produit consulté
     */
    private void retrieveResultsAroundFavoriteAddress() {
       final TextView errorTextView = (TextView)findViewById(R.id.error_textview);
       boolean networkAvailable = RequeteHTTP.isNetworkConnected(this);
  	   RequeteHTTP requete = new RequeteHTTP(getApplicationContext(),"/shop/near/productFavAddr",new String[] {"ean"},new String[]{ean},identifiant);
  	   final JSONObject jsonDocument = requete.sendGetRequest();
  	   JSONArray results=null;
  	   String registered = null;
  	   if (!networkAvailable) {
	      runOnUiThread(new Runnable() {
			 		        	
	         public void run() {
			    Toast.makeText(getApplicationContext(),"Réseau non disponible",Toast.LENGTH_SHORT).show();
			 }
			 			     
		  });
	   }
  	   else if (jsonDocument==null) {
  	      runOnUiThread(new Runnable() {
	        	
	         public void run() {
  	            Toast.makeText(getApplicationContext(),"Echec lors de la recupération des résultats de type 'Près de vos adresses favorites'",Toast.LENGTH_LONG).show();
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
 		           magasinTrouve = new Magasin(resultJson.getString("shop_id"),resultJson.getString("shop_name"),
 		        		                       resultJson.getString("shop_descriptive"),resultJson.getString("shop_type"),
 		        		                       resultJson.getDouble("shop_latitude"),resultJson.getDouble("shop_longitude"),
 		        		                       Math.round(resultJson.getDouble("distance")*100000),
 		        		                       resultJson.getString("shop_house_num"),resultJson.getString("shop_road"),
		        		                       resultJson.getString("shop_town"),resultJson.getString("shop_postcode"));
 		          shopAroundFavoriteAddress.add(magasinTrouve);
 		          prix_2.add(resultJson.getDouble("price"));
		          promo_2.add(resultJson.getBoolean("ispromotion") ? "(Promo)" : "");
 		         }
 		         catch(JSONException e) {
 			        Log.d("Err",e.getMessage());
 			     }
 		      }
  	      }
  	   }
    }
    
    /**
     * Envoi d'une requête GET au serveur afin de récupérer les informations sur les magasins favoris de l'utilisateur qui
     * ont un prix enregistré pour le produit consulté
     */
    private void retrieveResultsFavoriteShop() {
       final TextView errorTextView = (TextView)findViewById(R.id.error_textview);
       boolean networkAvailable = RequeteHTTP.isNetworkConnected(this);
  	   RequeteHTTP requete = new RequeteHTTP(getApplicationContext(),"/shop/near/productFavShop",new String[] {"ean"},new String[]{ean},identifiant);
  	   final JSONObject jsonDocument = requete.sendGetRequest();
  	   JSONArray results=null;
  	   String registered = null;
  	   if (!networkAvailable) {
	      runOnUiThread(new Runnable() {
			 		        	
	         public void run() {
			    Toast.makeText(getApplicationContext(),"Réseau non disponible",Toast.LENGTH_SHORT).show();
			 }
			 			     
		  });
	   }
  	   else if (jsonDocument==null) {
  	      runOnUiThread(new Runnable() {
	        	
	         public void run() {
  	            Toast.makeText(getApplicationContext(),"Echec lors de la recupération des résultats de type 'Vos magasins favoris'",Toast.LENGTH_LONG).show();
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
 		           magasinTrouve = new Magasin(resultJson.getString("shop_id"),resultJson.getString("shop_name"),
 		        		                       resultJson.getString("shop_descriptive"),resultJson.getString("shop_type"),
 		        		                       resultJson.getDouble("shop_latitude"),resultJson.getDouble("shop_longitude"),
 		        		                       resultJson.getString("shop_house_num"),resultJson.getString("shop_road"),
		        		                       resultJson.getString("shop_town"),resultJson.getString("shop_postcode"));
 		          favoriteShop.add(magasinTrouve);
 		          prix_3.add(resultJson.getDouble("price"));
		          promo_3.add(resultJson.getBoolean("ispromotion") ? "(Promo)" : "");
 		         }
 		         catch(JSONException e) {
 			        Log.d("Err",e.getMessage());
 			     }
 		      }
  	      }
  	   }	
    }
    
    /**
     * Méthode appelé pour classer les résultats selon un critère défini (prix/distance).
     * 
     * @param type  le type de résultat à classer (magasins à proximité / magasins à proximité des adresses favorites / magasins favoris)
     * @param triPrix  Si vrai, les résultats sont triés par pri croissant, sinon les résultats dont triés par distance croissante (la distance est la distance calculée
	 * entre la position de l'utilisateur et l'emplacement du magasin considéré).
     */
    private void organiseResults(int type, boolean triPrix) {
       if (triPrix) {
          sortByPrice(type);
       }
       else {
          sortByDistance(type);
       }
    }
    
    /**
     * Tri du type de résultat par distance croissante (la distance est la distance calculée entre la position de l'utilisateur et l'emplacement du magasin considéré).
     * 
     * @param type  le type de résultat à classer (magasins à proximité / magasins à proximité des adresses favorites / magasins favoris)
     */
    private void sortByDistance(int type) {
       double prix_inter;
       String enPromo_inter;
       Magasin magasin_inter;
       if (type==1) {
          for (int i=0;i<shopAround.size();i++) {
             for (int j=0;j<shopAround.size()-1-i;j++) {
        	    if (shopAround.get(j).getDistance()>shopAround.get(j+1).getDistance()) {
        	       magasin_inter = shopAround.get(j);
        	       shopAround.set(j,shopAround.get(j+1));
        	       shopAround.set(j+1,magasin_inter);
        	       prix_inter = prix_1.get(j);
        	       prix_1.set(j, prix_1.get(j+1));
        	       prix_1.set(j+1, prix_inter);
        	       enPromo_inter = promo_1.get(j);
        	       promo_1.set(j, promo_1.get(j+1));
        	       promo_1.set(j+1, enPromo_inter);
        	    }
        	 }
          }
          if (shopAround.size()==0) {
             if (echecGeolocalisation)
                mAdapter.addItem("Echec de la géolocalisation");
             else
		        mAdapter.addItem("Pas de magasin(s) vendant ce produit à proximité de votre position");
	      }
          else {
             for (int i=0;i<shopAround.size();i++) {
                mAdapter.addItem("Distance : "+shopAround.get(i).getDistance()+" m\n\n"+shopAround.get(i).toString()+"\n"+"Prix : "+prix_1.get(i)+"€ "+promo_1.get(i));
             }
          }
       }
       else if (type==2) {
          for (int i=0;i<shopAroundFavoriteAddress.size();i++) {
             for (int j=0;j<shopAroundFavoriteAddress.size()-1-i;j++) {
         	    if (shopAroundFavoriteAddress.get(j).getDistance()>shopAroundFavoriteAddress.get(j+1).getDistance()) {
         	       magasin_inter = shopAroundFavoriteAddress.get(j);
         	       shopAroundFavoriteAddress.set(j,shopAroundFavoriteAddress.get(j+1));
         	       shopAroundFavoriteAddress.set(j+1,magasin_inter);
         	       prix_inter = prix_2.get(j);
         	       prix_2.set(j, prix_2.get(j+1));
         	       prix_2.set(j+1, prix_inter);
         	       enPromo_inter = promo_2.get(j);
         	       promo_2.set(j, promo_2.get(j+1));
         	       promo_2.set(j+1, enPromo_inter);
         	    }
         	 }
          }
          if (shopAroundFavoriteAddress.size()==0) {
		     mAdapter.addItem("Pas de magasin(s) vendant ce produit à proximité de vos adresses favorites.\n\nAvez-vous enregistré vos adresses favorites ?");
	      }
          else {
             for (int i=0;i<shopAroundFavoriteAddress.size();i++) {
                mAdapter.addItem("Distance : "+shopAroundFavoriteAddress.get(i).getDistance()+" m\n\n"+shopAroundFavoriteAddress.get(i).toString()+"\n"+"Prix : "+prix_2.get(i)+"€ "+promo_2.get(i));
             }
          }
       }
    }
    
    /**
     * Tri du type de résultat par prix croissant.
     * 
     * @param type  le type de résultat à classer (magasins à proximité / magasins à proximité des adresses favorites / magasins favoris)
     */
    private void sortByPrice(int type) {
       double prix_inter;
       String enPromo_inter;
       Magasin magasin_inter;
       if (type==1) {
          for (int i=0;i<prix_1.size();i++) {
              for (int j=0;j<prix_1.size()-1-i;j++) {
         	    if (prix_1.get(j)>prix_1.get(j+1)) {
         	       prix_inter = prix_1.get(j);
          	       prix_1.set(j, prix_1.get(j+1));
          	       prix_1.set(j+1, prix_inter);
         	       magasin_inter = shopAround.get(j);
         	       shopAround.set(j,shopAround.get(j+1));
         	       shopAround.set(j+1,magasin_inter);
         	       enPromo_inter = promo_1.get(j);
         	       promo_1.set(j, promo_1.get(j+1));
         	       promo_1.set(j+1, enPromo_inter);
         	    }
         	 }
           }
           if (prix_1.size()==0) {
              if (echecGeolocalisation)
                 mAdapter.addItem("Echec de la gélocalisation");
              else
 		         mAdapter.addItem("Pas de magasin(s) vendant ce produit à proximité de votre position");
 	       }
           else {
              for (int i=0;i<prix_1.size();i++) {
                 mAdapter.addItem("Prix : "+prix_1.get(i)+"€ "+promo_1.get(i)+"\n\n"+shopAround.get(i).toString()+"\n"+"Distance : "+shopAround.get(i).getDistance()+" m");
              }
           }
        }
        else if (type==2) {
           for (int i=0;i<prix_2.size();i++) {
              for (int j=0;j<prix_2.size()-1-i;j++) {
          	    if (prix_2.get(j)>prix_2.get(j+1)) {
          	       prix_inter = prix_2.get(j);
           	       prix_2.set(j, prix_2.get(j+1));
           	       prix_2.set(j+1, prix_inter);
          	       magasin_inter = shopAroundFavoriteAddress.get(j);
          	       shopAroundFavoriteAddress.set(j,shopAroundFavoriteAddress.get(j+1));
          	       shopAroundFavoriteAddress.set(j+1,magasin_inter);
          	       enPromo_inter = promo_2.get(j);
          	       promo_2.set(j, promo_2.get(j+1));
          	       promo_2.set(j+1, enPromo_inter);
          	    }
          	 }
           }
           if (prix_2.size()==0) {
  		      mAdapter.addItem("Pas de magasin(s) vendant ce produit à proximité de vos adresses favorites.\n\nAvez-vous enregistré vos adresses favorites ?");
  	       }
           else {
              for (int i=0;i<prix_2.size();i++) {
                 mAdapter.addItem("Prix : "+prix_2.get(i)+"€ "+promo_2.get(i)+"\n\n"+shopAroundFavoriteAddress.get(i).toString()+"\n"+"Distance : "+shopAroundFavoriteAddress.get(i).getDistance()+" m");
              }
           }
        }
        else {
           for (int i=0;i<prix_3.size();i++) {
     	     for (int j=0;j<prix_3.size()-1-i;j++) {
           	    if (prix_3.get(j)>prix_3.get(j+1)) {
           	       prix_inter = prix_3.get(j);
            	   prix_3.set(j, prix_3.get(j+1));
            	   prix_3.set(j+1, prix_inter);
           	       magasin_inter = favoriteShop.get(j);
           	       favoriteShop.set(j,favoriteShop.get(j+1));
           	       favoriteShop.set(j+1,magasin_inter);
           	       enPromo_inter = promo_3.get(j);
           	       promo_3.set(j, promo_3.get(j+1));
           	       promo_3.set(j+1, enPromo_inter);
           	    }
           	 }
          }
          if (prix_3.size()==0) {
  		     mAdapter.addItem("Pas de magasin(s) vendant ce produit parmi vos magasins favoris.\n\nAvez-vous enregistré vos magasins favoris ?");
  	      }
          else {
     	     for (int i=0;i<prix_3.size();i++) {
                mAdapter.addItem("Prix : "+prix_3.get(i)+"€ "+promo_3.get(i)+"\n\n"+favoriteShop.get(i).toString());
             }
          }
       }
    }

}