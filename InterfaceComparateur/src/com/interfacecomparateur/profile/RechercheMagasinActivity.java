package com.interfacecomparateur.profile;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.interfacecomparateur.R;
import com.interfacecomparateur.classes.Localisation;
import com.interfacecomparateur.classes.Magasin;
import com.interfacecomparateur.classes.RequeteHTTP;

/**
 * <b>Activité de recherche de magasins à proximité de la position de l'utilisateur.</b>
 * 
 * @author Equipe L3I1
 *
 */
public class RechercheMagasinActivity extends Activity implements OnItemClickListener {

	/**
	 * Intent de l'activité.
	 */
	private Intent intent;
	/**
	 * ListView où les informations sur les magasins sont affichées.
	 */
	private ListView listView;
	/**
	 * ArrayAdapter servant à l'affichage des informations sur les magasins dans la ListView.
	 */
	private ArrayAdapter<String> arrayAdapter;
	/**
	 * ArrayList des informations sur les magasins trouvés.
	 */
	private ArrayList<String> values;
	/**
	 * Identifiant (adresse e-mail) de l'utilisateur connecté.
	 */
	private String identifiant;
	/**
	 * JSONArray contenant les informations renvoyées par le serveur sur les magasins trouvés.
	 */
	private JSONArray results;
	/**
	 * Vrai si cette activité a été appélé depuis l'activité d'affichage des informations sur un produit scanné, faux sinon.
	 * 
	 * @see ProduitScanActivity
	 */
	private boolean fromScan;
	/**
	 * 
	 */
	private Magasin magasinTrouve = null;
	/**
	 * 
	 */
	private String recherche;
	/**
	 * Vrai si l'appareil a accès au réseau, faux sinon.
	 */
	private boolean connexionInternet;
	//private BaseDeDonnees bdd = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.ajout_magasin_favori_vue);
	   intent = getIntent();
	   identifiant = intent.getStringExtra("id");
	   fromScan = intent.getBooleanExtra("fromScan",false);
	   values = new ArrayList<String>();
	   
	   //bdd =new BaseDeDonnees(this);
	   connexionInternet = RequeteHTTP.isNetworkConnected(this);
	   
	   listView = (ListView)findViewById(R.id.listview_shops);
	   arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, values);
	   listView.setAdapter(arrayAdapter);
	   
	   final ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBar);
	   final EditText searchEditText = (EditText)findViewById(R.id.textfield_valeur);
	   final Button buttonSend = (Button)findViewById(R.id.button_send);
	   buttonSend.setOnClickListener(new OnClickListener() {
		  
		  public void onClick(View v) {
			 InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			 imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
	         final Handler handler = new Handler() {
				  
		        @Override
				public void handleMessage(Message msg) {
				   progressBar.setVisibility(View.GONE);
				   listView.setVisibility(View.VISIBLE);
				}
							   
		     };
		     progressBar.setVisibility(View.VISIBLE);
		     listView.setVisibility(View.GONE);
		     Thread thread = new Thread(new Runnable() {
			        
		        @Override
				public void run() {
				   Looper.prepare();
				   getShopByName(searchEditText.getText().toString());
				   handler.sendMessage(handler.obtainMessage());
				}
				         
		     });
			 thread.start();
		  }
	         
	   });
	   
	   searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
		   
		    @Override
		    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
		            buttonSend.performClick();
		            return true;
		        }
		        return false;
		    }
		    
	   });

	   ImageButton buttonBack = (ImageButton)findViewById(R.id.button_back);
	   buttonBack.setOnClickListener(new OnClickListener() {

	      public void onClick(View v) {
		     setResult(-1,intent);
			 finish();
		  }

	   });
	   
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
	   if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount()==0) {
	      setResult(-1,intent);
		  finish();
	   }
	   return super.onKeyDown(keyCode, event);
	}

	/**
	 * Méthode invoquée lorsque l'utilisateur clique sur l'un des items de la ListView.<br />
	 * Cette action aura pour effet de revenir à l'activité précédente. Le magasin choisi sera ajouté dans la liste des magasins favoris
	 * ou sera utilisé pour l'ajout d'un prix pour un produit dans ce magasin.
	 * 
	 * @author Equipe L3I1
	 * 
	 * @see OnItemClickListener
	 * @see ProduitScanActivity
	 * @see MagasinFavoriActivity
	 */
	public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
	   ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBar);	
	   final Handler handler = new Handler() {
			  
  	      @Override
  		  public void handleMessage(Message msg) {
  	   		 finish();
  		  }
  				   
  	   };
  	   listView.setVisibility(View.GONE);
  	   progressBar.setVisibility(View.VISIBLE);
  	   Thread thread = new Thread(new Runnable() {
          
  	      @Override
  	      public void run() {
  	         Looper.prepare();
  	         Magasin magasinChoisi=null;
  		     try {
  		        JSONObject jsonMagasin = results.getJSONObject(position);
  			    magasinChoisi = new Magasin(jsonMagasin.getString("id"),jsonMagasin.getString("name"),
  					                        jsonMagasin.getString("descriptive"),jsonMagasin.getString("type"),
  					                        jsonMagasin.getDouble("latitude"),jsonMagasin.getDouble("longitude"),
  					                        jsonMagasin.getString("numero"),jsonMagasin.getString("rue"),
  					                        jsonMagasin.getString("town"),jsonMagasin.getString("postcode"));
  		     }
  		     catch(JSONException e) {
  		        Log.d("Err",e.getMessage());
  		     }
  	         if (fromScan) {
  		        intent.putExtra("shop",magasinChoisi);
  			    setResult(4,intent);
  		     }
  		     else {
  		        addFavoriteShop(magasinChoisi);
  		     }
  		     handler.sendMessage(handler.obtainMessage());
  	       }
  	         
  		});
  		thread.start();
	}
	
	/**
	 * Envoi d'une requête GET au serveur afin de récupérer les informations sur les magasins à proximité de la position de l'utilisateur
	 * et dont le nom correspond à la chaîne entrée par l'utilisateur.
	 * 
	 * @param search  la chaîne de caractères entrée par l'utilisateur
	 */
	private void getShopByName(String search) {
       Location userLocation = Localisation.getLocation(getApplicationContext());
	   final TextView errorTextView = (TextView)findViewById(R.id.error_textview);
	   boolean networkAvailable = RequeteHTTP.isNetworkConnected(this);
	   if(connexionInternet) {
	      if (!search.equals("") && userLocation!=null) {
	         RequeteHTTP requete = new RequeteHTTP(getApplicationContext(),"/shop/near/name",new String[] {"lat","lon","name"},new String[]{""+userLocation.getLatitude(),""+userLocation.getLongitude(),search.replace(" ","_")},identifiant); 
	         final JSONObject jsonDocument = requete.sendGetRequest();
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
	                  Toast.makeText(getApplicationContext(),"Echec lors de la recherche de magasin(s)",Toast.LENGTH_LONG).show();
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
			       values.clear();
	               try {
	                  if (results.length()==0) {
	        	         values.add("Pas de magasin(s) correspondant(s) à proximité de votre position");
	        	         listView.setOnItemClickListener(null);
	                  }
	                  else {
			             for (int i=0;i<results.length();i++) {
			                String resultItem = formatResult(results.getJSONObject(i));
			                values.add(resultItem);
			             }
			             listView.setOnItemClickListener(RechercheMagasinActivity.this);
	                  }
	                  runOnUiThread(new Runnable() {
	     		         
	      		         @Override
	      		         public void run() {
	      		        	errorTextView.setVisibility(View.GONE);
	      		        	arrayAdapter.notifyDataSetChanged();
	      		         }
	      		         
	                  });
	               }
	               catch(JSONException e) {
	                  Log.d("Err",e.getMessage());
	               }
		        }
		        else if (registered.equals("failure") && fromScan) {
		           setResult(3,intent);
		        }
	         }
	      }
	      else if (userLocation==null) {
	         runOnUiThread(new Runnable() {
	    		   
	     	    public void run() {
	               Toast.makeText(getApplicationContext(),"Echec de la géolocalisation",Toast.LENGTH_SHORT).show();
	     	    }
	     	    
	         });
	      }
	   }
	   else { 
	      //bdd.open();
	      //magasinTrouve = bdd.getMagasinWithNom(recherche);
		  //bdd.close();
	   }
	}
	
	/**
	 * Envoi d'une requête POST au serveur afin d'ajouter le magasin correspondant à l'item sur lequel l'utilisateur a cliqué
	 * dans la liste des magasins favoris.
	 * 
	 * @param magasin  le magasin à ajouter dans la liste des magasins favoris de l'utilisateur
	 */
    private void addFavoriteShop(Magasin magasin) {
	   final TextView errorTextView = (TextView)findViewById(R.id.error_textview);
	   boolean networkAvailable = RequeteHTTP.isNetworkConnected(this);
	   RequeteHTTP requete = new RequeteHTTP(getApplicationContext(),"/favorite/shop/"+magasin.getId(),new String[]{},new String[]{},identifiant);
	   final JSONObject jsonDocument = requete.sendPostRequest();
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
	            Toast.makeText(getApplicationContext(),"Echec lors de l'ajout du magasin en tant que favori",Toast.LENGTH_LONG).show();
		     }
		     
	      });
	   }
	   else {
	      try {
		     registered = jsonDocument.getString("registered");
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
		  intent.putExtra("newShop",magasin);
		  if(registered.equals("success")) {
		     setResult(1,intent);
		  }
		  else if(registered.equals("failure")) {
		     setResult(0,intent);
		  }
	   }
    }
    
    /**
     * Formatage des informations sur un magasin pour l'affichage dans la ListView.
     * 
     * @param jsonResultat  Objet JSON où sont contenues les informations sur un magasin
     * @return  une chaîne de caractères où les informations sont formatées pour l'affichage
     * @throws JSONException  Erreur survenue lors du parsing de l'objet JSON
     */
    private String formatResult(JSONObject jsonResultat) throws JSONException {
       StringBuffer sb = new StringBuffer();
       sb.append(jsonResultat.getString("name")+"\n");
       if (!jsonResultat.getString("distance").equals("null") && !jsonResultat.getString("distance").equals(""))
          sb.append("Distance : "+ Math.round(Double.parseDouble(jsonResultat.getString("distance"))*100000)+" m\n");
       sb.append(formatAddress(jsonResultat));
       return sb.toString();
    }
    
    /**
     * Formatage des informations sur l'adresse d'un magasin pour l'affichage dans la ListView.
     * 
     * @param jsonMagasin  Objet JSON où sont contenues les informations sur un magasin
     * @return  une chaîne de caractères où les informations sur l'adresse du magasin sont formatées pour l'affichage
     * @throws JSONException  Erreur survenue lors du parsing de l'objet JSON
     */
    private String formatAddress(JSONObject jsonMagasin) throws JSONException {
       StringBuffer sb = new StringBuffer();
       if (!jsonMagasin.getString("numero").equals("null") && !jsonMagasin.getString("numero").equals(""))
           sb.append(jsonMagasin.getString("numero")+", ");
       if (!jsonMagasin.getString("rue").equals("null") && !jsonMagasin.getString("rue").equals(""))
           sb.append(jsonMagasin.getString("rue")+"\n");
       if (!jsonMagasin.getString("town").equals("null") && !jsonMagasin.getString("town").equals(""))
           sb.append(jsonMagasin.getString("town")+"\t");
       if (!jsonMagasin.getString("postcode").equals("null") && !jsonMagasin.getString("postcode").equals(""))
           sb.append(jsonMagasin.getString("postcode")+"\n");
       return sb.toString();
    }
    
}
